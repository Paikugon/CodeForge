# CodeForge

[Tiếng Việt](README.md) | English

CodeForge is an Online Judge capstone project for practicing programming and judging submissions automatically. It has its own backend and uses QingDaoU’s Judge Server/Judger to compile code, run it in a sandbox, and check the results. A frontend is planned for the next stage.

For now, this README covers running the Judge Server locally. Backend, database, and frontend setup instructions will follow.

## 1. Prerequisites

- Linux, or Windows with WSL2 Ubuntu.
- Docker Engine with Docker Compose v2 or later, or Docker Desktop with WSL2 integration enabled.
- Python 3 and OpenSSL for connection checks and token generation.
- Git if deployment configuration will be managed in a separate repository.
- An available port at `127.0.0.1:8080`.

Check your installation in Ubuntu/WSL2:

```bash
docker version
docker compose version
```

Run the following commands in Ubuntu/WSL2. On Windows, keep the project in the Linux filesystem, such as `~/projects`, to simplify file permission management.

## 2. Prepare the Directory

Docker configuration lives in a separate directory called `OnlineJudgeDeploy`. This guide starts only `oj-judge`, which CodeForge calls when judging submissions.

```bash
mkdir -p ~/projects/OnlineJudgeDeploy
cd ~/projects/OnlineJudgeDeploy
mkdir -p data/backend/test_case data/judge_server/log data/judge_server/run
```

If you already have an `OnlineJudgeDeploy` repository, use it and update the `oj-judge` service to match the configuration below. Keep any other services you still need.

After adding the configuration files, the directory should look like this:

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

## 3. Generate a Shared Token

Generate a random token:

```bash
openssl rand -hex 32
```

Create `.env` and insert the generated value:

```dotenv
JUDGE_SERVER_TOKEN=replace_with_your_generated_token
```

The backend and Judge Server share this token to authenticate judging requests.

Add the following to the deployment repository's `.gitignore`:

```gitignore
.env
data/
```

You may commit an `.env.example` containing a placeholder, but do not commit the actual token.

## 4. Configure Docker Compose

Create `docker-compose.yml` as shown below. The project currently uses Judge Server image version `1.6.1`.

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

Key settings:

| Setting | Purpose |
| --- | --- |
| `127.0.0.1:8080:8080` | Allows access from the development host while binding the published port to loopback. |
| `/test_case:ro` | Allows the Judge Server to read host test cases without letting the container write to this directory. |
| `/judger` | Stores working data for compilation and execution. |
| `/log` | Stores Judge Server logs. |
| `TOKEN` | Shared token for authenticating judging requests. |

`BACKEND_URL` is used to send heartbeats to the QingDaoU backend, so it is omitted from this standalone setup. If you run the full QingDaoU stack, keep its heartbeat URL. CodeForge does not use that endpoint.

## 5. Start the Judge Server

From the directory containing `docker-compose.yml`:

```bash
docker compose config --quiet
docker compose pull oj-judge
docker compose up -d --no-deps oj-judge
docker compose ps oj-judge
docker compose logs --tail=100 oj-judge
```

`--no-deps` starts only the Judge Server, which is useful when the existing Compose file also contains QingDaoU's backend, PostgreSQL, or Redis services.

Check connectivity and authentication from the host/WSL2. When prompted, enter the original token from `.env`:

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

A successful response includes `"err": null`, `"action": "pong"` inside `data`, and information about Judger. The script sends a **POST** request to `/ping` with the token’s SHA-256 digest in the `X-Judge-Server-Token` header. When checking manually, inspect `err` too: authentication errors can still return HTTP 200.

A successful ping confirms the connection and token. Once the backend is integrated, submit both a correct and an incorrect program to check the full judging flow.

> If the backend runs on the host/WSL2, use `http://127.0.0.1:8080`. If it runs in a container on the same Compose network, use `http://oj-judge:8080`.

## 6. Test Cases and File Permissions

CodeForge uses a `codeforge_<problemId>` directory for each problem. For example:

```text
data/backend/test_case/codeforge_1/
├── info
├── 1.in
└── 1.out
```

Each directory needs an `info` file in the Judge Server’s format as well as its input and output files. The backend will generate and update these files; this will be covered in the backend guide.

Inspect the directories on the host and inside the container:

```bash
ls -ld data/backend/test_case
docker compose exec oj-judge sh -c 'ls -ld /test_case; ls -la /test_case'
```

The backend needs to write test cases on the host, and the Judge Server needs to read them inside the container. For local development, you can use these permissions:

```bash
chmod 755 data/backend/test_case
find data/backend/test_case -mindepth 1 -type d -exec chmod 755 {} +
find data/backend/test_case -type f -exec chmod 644 {} +
```

These permissions let other users on the machine read test cases. For private test cases, restrict access with a group or ACL. Avoid `chmod -R 777`.

New files created by the backend also need suitable permissions. If the backend cannot write test cases, check the directory’s owner and group first.

## 7. Common Commands

Follow logs:

```bash
docker compose logs -f oj-judge
```

Restart:

```bash
docker compose restart oj-judge
```

After changing `.env`, environment variables, volumes, or service configuration, recreate the container to apply the changes:

```bash
docker compose up -d --no-deps --force-recreate oj-judge
```

Stop only the Judge Server:

```bash
docker compose stop oj-judge
```

## 8. Troubleshooting

### Cannot Connect to Port 8080

- Verify that Docker is running and the container is active.
- Inspect logs with `docker compose logs --tail=100 oj-judge`.
- Check whether another application is using port 8080.
- If the backend runs in a container, check its target address and Docker network rather than using that container's loopback address.

### Judge Server Authentication Errors

- Verify that the client/backend token matches `JUDGE_SERVER_TOKEN` in `.env`.
- Recreate the container after changing `.env`, as shown in section 7.
- The `X-Judge-Server-Token` header must contain the token’s SHA-256 digest, as shown in section 5.

### `JudgeClientError: Test case not found`

Check the following in order:

1. The test case directory ID in the request matches `codeforge_<problemId>`.
2. The directory and its `info` file exist under `/test_case` inside the container.
3. The bind mount points to the correct host directory.
4. Directory traversal permissions, file read permissions, and the contents of `info` are valid.

For problem 1:

```bash
docker compose exec oj-judge sh -c 'ls -ld /test_case /test_case/codeforge_1; ls -la /test_case/codeforge_1'
```

## 9. Sources and Acknowledgments

CodeForge integrates open-source components from **QingDaoU OnlineJudge**:

- [QingDaoU/OnlineJudge](https://github.com/QingDaoU/OnlineJudge): the original OnlineJudge project and the judging ecosystem used by CodeForge.
- [QingDaoU/JudgeServer](https://github.com/QingDaoU/JudgeServer): the HTTP service wrapping the judging engine.
- [QingDaoU/Judger](https://github.com/QingDaoU/Judger): the execution engine and sandbox.
- [QingDaoU/OnlineJudgeDeploy](https://github.com/QingDaoU/OnlineJudgeDeploy): Docker deployment configuration on which the project's separate deployment repository is based.

Thanks to QingDaoU for developing and sharing these open-source judging components. CodeForge builds its own application backend, APIs, and integration with the judge; the frontend will follow. The Docker setup above is adapted from QingDaoU’s deployment configuration for this project.

When using or redistributing upstream components, consult the licenses and copyright notices in their respective repositories.
