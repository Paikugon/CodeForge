# CodeForge

Tiếng Việt | [English](README.en.md)

CodeForge là đồ án xây dựng hệ thống Online Judge để luyện tập lập trình và chấm bài tự động. Dự án có backend riêng và sử dụng Judge Server/Judger của QingDaoU để biên dịch, chạy mã nguồn trong sandbox và kiểm tra kết quả. Frontend sẽ được phát triển ở giai đoạn tiếp theo.

Hiện tại, README hướng dẫn chạy Judge Server trên máy cá nhân. Hướng dẫn cài đặt backend, cơ sở dữ liệu và frontend sẽ được bổ sung sau.

## 1. Yêu cầu

- Linux hoặc Windows có WSL2 Ubuntu.
- Docker Engine với Docker Compose v2 trở lên, hoặc Docker Desktop đã bật tích hợp WSL2.
- Python 3 và OpenSSL để kiểm tra kết nối và tạo token.
- Git nếu quản lý cấu hình triển khai trong repository riêng.
- Cổng `127.0.0.1:8080` còn trống.

Kiểm tra cài đặt trong Ubuntu/WSL2:

```bash
docker version
docker compose version
```

Các lệnh bên dưới chạy trong Ubuntu/WSL2. Nếu dùng Windows, nên đặt dự án trong hệ thống file của Linux, như `~/projects`, để dễ quản lý quyền truy cập.

## 2. Chuẩn bị thư mục

Cấu hình Docker nằm trong thư mục riêng `OnlineJudgeDeploy`. Hướng dẫn này chỉ chạy `oj-judge`, dịch vụ mà CodeForge gọi đến khi chấm bài.

```bash
mkdir -p ~/projects/OnlineJudgeDeploy
cd ~/projects/OnlineJudgeDeploy
mkdir -p data/backend/test_case data/judge_server/log data/judge_server/run
```

Nếu đã có repository `OnlineJudgeDeploy`, dùng thư mục đó và cập nhật service `oj-judge` theo cấu hình bên dưới. Giữ lại những service khác nếu vẫn cần dùng.

Sau khi thêm các file cấu hình, thư mục sẽ có dạng:

```text
OnlineJudgeDeploy/
├── .env
├── .gitignore
├── docker-compose.yml
└── data/
    ├── backend/test_case/
    └── judge_server/
        ├── log/
        └── run/
```

## 3. Tạo token dùng chung

Tạo một token ngẫu nhiên:

```bash
openssl rand -hex 32
```

Tạo file `.env` và điền token vừa tạo:

```dotenv
JUDGE_SERVER_TOKEN=replace_with_your_generated_token
```

Backend và Judge Server dùng chung token này để xác thực các yêu cầu chấm bài.

Thêm nội dung sau vào `.gitignore` của repository triển khai:

```gitignore
.env
data/
```

Có thể commit file `.env.example` chứa giá trị mẫu, nhưng không commit token thật.

## 4. Cấu hình Docker Compose

Tạo `docker-compose.yml` như bên dưới. Dự án hiện dùng image Judge Server phiên bản `1.6.1`.

```yaml
services:
  oj-judge:
    image: registry.cn-hongkong.aliyuncs.com/oj-image/judge:1.6.1
    ports:
      - "127.0.0.1:8080:8080"
    restart: always
    read_only: true
    cap_drop:
      - SETPCAP
      - MKNOD
      - NET_BIND_SERVICE
      - SYS_CHROOT
      - SETFCAP
      - FSETID
    tmpfs:
      - /tmp
    volumes:
      - ./data/backend/test_case:/test_case:ro
      - ./data/judge_server/log:/log
      - ./data/judge_server/run:/judger
    environment:
      SERVICE_URL: http://oj-judge:8080
      TOKEN: ${JUDGE_SERVER_TOKEN:?Set JUDGE_SERVER_TOKEN in .env}
```

Các cấu hình chính:

| Cấu hình | Mục đích |
| --- | --- |
| `127.0.0.1:8080:8080` | Cho phép truy cập từ máy phát triển, với cổng được công bố trên địa chỉ loopback. |
| `/test_case:ro` | Cho phép Judge Server đọc test case từ host; container không được ghi vào thư mục này. |
| `/judger` | Lưu dữ liệu làm việc khi biên dịch và thực thi mã nguồn. |
| `/log` | Lưu log của Judge Server. |
| `TOKEN` | Token dùng chung để xác thực yêu cầu chấm bài. |

`BACKEND_URL` dùng để gửi heartbeat về backend QingDaoU nên được bỏ khỏi cấu hình chạy riêng này. Nếu dùng toàn bộ stack QingDaoU, giữ URL heartbeat của stack đó. CodeForge không dùng endpoint này.

## 5. Khởi động Judge Server

Chạy trong thư mục chứa `docker-compose.yml`:

```bash
docker compose config --quiet
docker compose pull oj-judge
docker compose up -d --no-deps oj-judge
docker compose ps oj-judge
docker compose logs --tail=100 oj-judge
```

`--no-deps` giúp chỉ khởi chạy Judge Server, hữu ích khi file Compose còn có backend, PostgreSQL hoặc Redis của QingDaoU.

Kiểm tra kết nối và xác thực từ host/WSL2. Khi được hỏi, nhập token gốc trong `.env`:

```bash
python3 - <<'PYTHON'
import getpass
import hashlib
import json
import urllib.request

token = getpass.getpass("JUDGE_SERVER_TOKEN (from .env): ")
request = urllib.request.Request(
    "http://127.0.0.1:8080/ping",
    data=b"{}",
    headers={
        "Content-Type": "application/json",
        "X-Judge-Server-Token": hashlib.sha256(token.encode("utf-8")).hexdigest(),
    },
    method="POST",
)
with urllib.request.urlopen(request, timeout=10) as response:
    result = json.load(response)
print(json.dumps(result, indent=2))
if result.get("err") is not None or result.get("data", {}).get("action") != "pong":
    raise SystemExit("Judge Server ping failed")
PYTHON
```

Nếu thành công, kết quả sẽ có `"err": null`, `"action": "pong"` trong `data` và thông tin về Judger. Script gửi yêu cầu **POST** đến `/ping`, với SHA-256 của token trong header `X-Judge-Server-Token`. Khi kiểm tra thủ công, nhớ xem cả trường `err`: lỗi xác thực vẫn có thể trả về HTTP 200.

Ping thành công xác nhận kết nối và token đã đúng. Sau khi tích hợp backend, thử nộp cả chương trình đúng và chương trình sai để kiểm tra toàn bộ luồng chấm.

> Nếu backend chạy trên host/WSL2, dùng `http://127.0.0.1:8080`. Nếu backend chạy trong container cùng mạng Compose, dùng `http://oj-judge:8080`.

## 6. Test case và quyền truy cập

CodeForge dùng thư mục `codeforge_<problemId>` cho mỗi bài. Ví dụ:

```text
data/backend/test_case/codeforge_1/
├── info
├── 1.in
└── 1.out
```

Mỗi thư mục cần có file `info` đúng định dạng của Judge Server, cùng các file input và output. Backend sẽ tạo và cập nhật những file này; chi tiết sẽ được bổ sung trong hướng dẫn backend.

Kiểm tra thư mục trên host và trong container:

```bash
ls -ld data/backend/test_case
docker compose exec oj-judge sh -c 'ls -ld /test_case; ls -la /test_case'
```

Backend cần ghi test case trên host, còn Judge Server cần đọc chúng trong container. Với môi trường phát triển cục bộ, có thể đặt quyền như sau:

```bash
chmod 755 data/backend/test_case
find data/backend/test_case -mindepth 1 -type d -exec chmod 755 {} +
find data/backend/test_case -type f -exec chmod 644 {} +
```

Các quyền trên cho phép người dùng khác trên máy đọc test case. Nếu cần giữ riêng tư, giới hạn quyền truy cập bằng group hoặc ACL. Tránh dùng `chmod -R 777`.

Các file mới do backend tạo cũng cần có quyền phù hợp. Nếu backend không ghi được test case, kiểm tra owner và group của thư mục trước.

## 7. Các lệnh thường dùng

Theo dõi log:

```bash
docker compose logs -f oj-judge
```

Khởi động lại:

```bash
docker compose restart oj-judge
```

Sau khi thay đổi `.env`, biến môi trường, volume hoặc cấu hình service, tạo lại container để áp dụng thay đổi:

```bash
docker compose up -d --no-deps --force-recreate oj-judge
```

Dừng riêng Judge Server:

```bash
docker compose stop oj-judge
```

## 8. Xử lý lỗi thường gặp

### Không kết nối được cổng 8080

- Kiểm tra Docker và container đang chạy.
- Xem log bằng `docker compose logs --tail=100 oj-judge`.
- Kiểm tra có ứng dụng khác đang chiếm cổng 8080 hay không.
- Nếu backend chạy trong container, kiểm tra địa chỉ đích và mạng Docker, thay vì dùng địa chỉ loopback của chính container đó.

### Lỗi xác thực với Judge Server

- Kiểm tra token của client/backend khớp với `JUDGE_SERVER_TOKEN` trong `.env`.
- Tạo lại container sau khi thay đổi `.env` (xem mục 7).
- Header `X-Judge-Server-Token` phải chứa SHA-256 của token (xem mục 5).

### `JudgeClientError: Test case not found`

Kiểm tra lần lượt:

1. ID thư mục test case trong yêu cầu có khớp `codeforge_<problemId>` không.
2. Thư mục và file `info` có tồn tại dưới `/test_case` trong container không.
3. Bind mount có trỏ đúng thư mục trên host không.
4. Quyền đi qua thư mục, quyền đọc file và nội dung `info` có hợp lệ không.

Ví dụ với bài số 1:

```bash
docker compose exec oj-judge sh -c 'ls -ld /test_case /test_case/codeforge_1; ls -la /test_case/codeforge_1'
```

## 9. Nguồn và lời cảm ơn

CodeForge tích hợp các thành phần mã nguồn mở của **QingDaoU OnlineJudge**:

- [QingDaoU/OnlineJudge](https://github.com/QingDaoU/OnlineJudge): dự án OnlineJudge gốc và hệ sinh thái chấm bài mà CodeForge sử dụng.
- [QingDaoU/JudgeServer](https://github.com/QingDaoU/JudgeServer): dịch vụ HTTP bao bọc bộ chấm.
- [QingDaoU/Judger](https://github.com/QingDaoU/Judger): bộ thực thi và sandbox.
- [QingDaoU/OnlineJudgeDeploy](https://github.com/QingDaoU/OnlineJudgeDeploy): cấu hình Docker làm cơ sở cho repository triển khai riêng của dự án.

Cảm ơn QingDaoU đã phát triển và chia sẻ các thành phần chấm bài mã nguồn mở. CodeForge xây dựng backend nghiệp vụ, API và phần tích hợp với bộ chấm; frontend sẽ được bổ sung sau. Cấu hình Docker ở trên được điều chỉnh từ cấu hình triển khai của QingDaoU để phục vụ dự án.

Khi sử dụng hoặc phân phối lại các thành phần upstream, tham khảo giấy phép và thông báo bản quyền trong từng repository tương ứng.
