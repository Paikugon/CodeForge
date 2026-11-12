-- Development only. All accounts use CodeForge123!; submissions are synthetic UI fixtures.

-- Flyway applies this version once, only with the explicit dev-seed profile.

DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM problems) OR EXISTS (SELECT 1 FROM users) OR EXISTS (SELECT 1 FROM tags) THEN
        RAISE EXCEPTION 'dev-seed requires an empty CodeForge database; use a new database';
    END IF;
END $$;


INSERT INTO users (username, email, password_hash, role) VALUES
('admin', 'admin@codeforge.local', '$2b$12$t8uofN9SlK7tWi0cV7VDsujtTL44oL6k9SwEG6m0TktRZQVcdbpb6', 'ADMIN'),
('demo', 'demo@codeforge.local', '$2b$12$t8uofN9SlK7tWi0cV7VDsujtTL44oL6k9SwEG6m0TktRZQVcdbpb6', 'USER'),
('newcomer', 'newcomer@codeforge.local', '$2b$12$t8uofN9SlK7tWi0cV7VDsujtTL44oL6k9SwEG6m0TktRZQVcdbpb6', 'USER');


INSERT INTO tags (name) VALUES ('Arrays'), ('BFS'), ('Binary Search'), ('Dijkstra'), ('Dynamic Programming'), ('Graphs'), ('Implementation'), ('Math'), ('Prefix Sum'), ('Strings'), ('Trees'), ('Two Pointers');


INSERT INTO problems (title, slug, description, difficulty, time_limit_ms, memory_limit_mb) VALUES
('Tổng hai số', 'a-plus-b', 'Cho hai số nguyên a và b. Hãy tính tổng a + b.

## Input
Một dòng chứa hai số nguyên a, b, với -10^9 ≤ a, b ≤ 10^9.

## Output
In ra tổng của hai số.
', 'EASY', 2000, 256);

INSERT INTO problem_tags (problem_id, tag_id)
SELECT p.id, t.id FROM problems p CROSS JOIN tags t WHERE p.slug='a-plus-b' AND t.name IN ('Math', 'Implementation');

INSERT INTO test_cases (problem_id, input, expected_output, is_sample, order_index)
SELECT p.id, c.input, c.output, c.sample, c.idx
FROM problems p CROSS JOIN (VALUES
('1 2
', '3
', true, 0),
('0 0
', '0
', true, 1),
('-3 5
', '2
', false, 2),
('-7 -8
', '-15
', false, 3),
('1000000000 1000000000
', '2000000000
', false, 4),
('-1000000000 1000000000
', '0
', false, 5),
('42 58
', '100
', false, 6),
('123456789 -987654321
', '-864197532
', false, 7)) AS c(input, output, sample, idx) WHERE p.slug='a-plus-b';


INSERT INTO problems (title, slug, description, difficulty, time_limit_ms, memory_limit_mb) VALUES
('Tổng phần tử trong mảng', 'array-sum', 'Cho mảng a gồm n số nguyên. Tính tổng các phần tử của mảng.

## Input
Dòng đầu chứa n (1 ≤ n ≤ 100000). Dòng sau chứa n số nguyên, |a_i| ≤ 10^9.

## Output
In ra tổng. Kết quả có thể vượt giới hạn số nguyên 32 bit.
', 'EASY', 2000, 256);

INSERT INTO problem_tags (problem_id, tag_id)
SELECT p.id, t.id FROM problems p CROSS JOIN tags t WHERE p.slug='array-sum' AND t.name IN ('Arrays', 'Math');

INSERT INTO test_cases (problem_id, input, expected_output, is_sample, order_index)
SELECT p.id, c.input, c.output, c.sample, c.idx
FROM problems p CROSS JOIN (VALUES
('5
1 2 3 4 5
', '15
', true, 0),
('1
-1
', '-1
', true, 1),
('3
0 0 0
', '0
', false, 2),
('3
-5 10 -5
', '0
', false, 3),
('4
1000000000 1000000000 1000000000 1000000000
', '4000000000
', false, 4),
('4
-1000000000 -1000000000 -1000000000 -1000000000
', '-4000000000
', false, 5),
('100
1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50 51 52 53 54 55 56 57 58 59 60 61 62 63 64 65 66 67 68 69 70 71 72 73 74 75 76 77 78 79 80 81 82 83 84 85 86 87 88 89 90 91 92 93 94 95 96 97 98 99 100
', '5050
', false, 6),
('5
7 -2 9 -4 1
', '11
', false, 7)) AS c(input, output, sample, idx) WHERE p.slug='array-sum';


INSERT INTO problems (title, slug, description, difficulty, time_limit_ms, memory_limit_mb) VALUES
('Chuỗi đối xứng', 'palindrome', 'Một chuỗi được gọi là đối xứng nếu đọc từ trái sang phải giống từ phải sang trái.

## Input
Một chuỗi s gồm các chữ cái tiếng Anh viết thường, 1 ≤ |s| ≤ 100000.

## Output
In YES nếu s đối xứng, ngược lại in NO.
', 'EASY', 2000, 256);

INSERT INTO problem_tags (problem_id, tag_id)
SELECT p.id, t.id FROM problems p CROSS JOIN tags t WHERE p.slug='palindrome' AND t.name IN ('Strings', 'Two Pointers');

INSERT INTO test_cases (problem_id, input, expected_output, is_sample, order_index)
SELECT p.id, c.input, c.output, c.sample, c.idx
FROM problems p CROSS JOIN (VALUES
('abba
', 'YES
', true, 0),
('abc
', 'NO
', true, 1),
('a
', 'YES
', false, 2),
('aa
', 'YES
', false, 3),
('ab
', 'NO
', false, 4),
('racecar
', 'YES
', false, 5),
('abccba
', 'YES
', false, 6),
('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaab
', 'NO
', false, 7)) AS c(input, output, sample, idx) WHERE p.slug='palindrome';


INSERT INTO problems (title, slug, description, difficulty, time_limit_ms, memory_limit_mb) VALUES
('Tìm kiếm nhị phân', 'binary-search', 'Cho mảng a tăng nghiêm ngặt và số nguyên x. Tìm vị trí của x trong mảng.

## Input
Dòng đầu chứa n và x (1 ≤ n ≤ 100000, |x| ≤ 10^9).
Dòng sau chứa n số nguyên tăng nghiêm ngặt, |a_i| ≤ 10^9.

## Output
In chỉ số tính từ 0 nếu tìm thấy x; ngược lại in -1.
', 'MEDIUM', 2000, 256);

INSERT INTO problem_tags (problem_id, tag_id)
SELECT p.id, t.id FROM problems p CROSS JOIN tags t WHERE p.slug='binary-search' AND t.name IN ('Arrays', 'Binary Search');

INSERT INTO test_cases (problem_id, input, expected_output, is_sample, order_index)
SELECT p.id, c.input, c.output, c.sample, c.idx
FROM problems p CROSS JOIN (VALUES
('5 5
1 3 5 7 9
', '2
', true, 0),
('3 2
1 3 5
', '-1
', true, 1),
('1 2
2
', '0
', false, 2),
('1 1
2
', '-1
', false, 3),
('4 -9
-9 -2 0 4
', '0
', false, 4),
('4 4
-9 -2 0 4
', '3
', false, 5),
('100 198
0 2 4 6 8 10 12 14 16 18 20 22 24 26 28 30 32 34 36 38 40 42 44 46 48 50 52 54 56 58 60 62 64 66 68 70 72 74 76 78 80 82 84 86 88 90 92 94 96 98 100 102 104 106 108 110 112 114 116 118 120 122 124 126 128 130 132 134 136 138 140 142 144 146 148 150 152 154 156 158 160 162 164 166 168 170 172 174 176 178 180 182 184 186 188 190 192 194 196 198
', '99
', false, 6),
('100 201
0 2 4 6 8 10 12 14 16 18 20 22 24 26 28 30 32 34 36 38 40 42 44 46 48 50 52 54 56 58 60 62 64 66 68 70 72 74 76 78 80 82 84 86 88 90 92 94 96 98 100 102 104 106 108 110 112 114 116 118 120 122 124 126 128 130 132 134 136 138 140 142 144 146 148 150 152 154 156 158 160 162 164 166 168 170 172 174 176 178 180 182 184 186 188 190 192 194 196 198
', '-1
', false, 7)) AS c(input, output, sample, idx) WHERE p.slug='binary-search';


INSERT INTO problems (title, slug, description, difficulty, time_limit_ms, memory_limit_mb) VALUES
('Truy vấn tổng đoạn', 'range-sum', 'Cho mảng a và q truy vấn. Mỗi truy vấn yêu cầu tính tổng các phần tử từ l đến r.

## Input
Dòng đầu chứa n, q (1 ≤ n, q ≤ 100000). Dòng tiếp chứa n số nguyên, |a_i| ≤ 10^9.
Mỗi dòng trong q dòng sau chứa l, r (1 ≤ l ≤ r ≤ n).

## Output
Với mỗi truy vấn, in tổng đoạn trên một dòng. Chỉ số trong input bắt đầu từ 1.
', 'MEDIUM', 2000, 256);

INSERT INTO problem_tags (problem_id, tag_id)
SELECT p.id, t.id FROM problems p CROSS JOIN tags t WHERE p.slug='range-sum' AND t.name IN ('Arrays', 'Prefix Sum');

INSERT INTO test_cases (problem_id, input, expected_output, is_sample, order_index)
SELECT p.id, c.input, c.output, c.sample, c.idx
FROM problems p CROSS JOIN (VALUES
('5 3
1 2 3 4 5
1 3
2 5
1 5
', '6
14
15
', true, 0),
('1 1
7
1 1
', '7
', true, 1),
('3 2
-3 -2 -1
1 3
2 2
', '-6
-2
', false, 2),
('5 2
0 0 0 0 0
1 5
3 3
', '0
0
', false, 3),
('4 2
1000000000 1000000000 1000000000 1000000000
1 4
2 3
', '4000000000
2000000000
', false, 4),
('4 2
5 -5 7 -7
1 4
2 3
', '0
2
', false, 5),
('100 2
1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50 51 52 53 54 55 56 57 58 59 60 61 62 63 64 65 66 67 68 69 70 71 72 73 74 75 76 77 78 79 80 81 82 83 84 85 86 87 88 89 90 91 92 93 94 95 96 97 98 99 100
1 100
50 100
', '5050
3825
', false, 6),
('5 2
1 -2 3 -4 5
2 4
3 5
', '-3
4
', false, 7)) AS c(input, output, sample, idx) WHERE p.slug='range-sum';


INSERT INTO problems (title, slug, description, difficulty, time_limit_ms, memory_limit_mb) VALUES
('Đoạn con có tổng lớn nhất', 'maximum-subarray', 'Tìm tổng lớn nhất của một đoạn con liên tiếp không rỗng trong mảng a.

## Input
Dòng đầu chứa n (1 ≤ n ≤ 200000). Dòng sau chứa n số nguyên, |a_i| ≤ 10^9.

## Output
In tổng lớn nhất. Với mảng toàn số âm, vẫn phải chọn ít nhất một phần tử.
', 'MEDIUM', 2000, 256);

INSERT INTO problem_tags (problem_id, tag_id)
SELECT p.id, t.id FROM problems p CROSS JOIN tags t WHERE p.slug='maximum-subarray' AND t.name IN ('Arrays', 'Dynamic Programming');

INSERT INTO test_cases (problem_id, input, expected_output, is_sample, order_index)
SELECT p.id, c.input, c.output, c.sample, c.idx
FROM problems p CROSS JOIN (VALUES
('9
-2 1 -3 4 -1 2 1 -5 4
', '6
', true, 0),
('1
-5
', '-5
', true, 1),
('3
-3 -2 -8
', '-2
', false, 2),
('2
0 0
', '0
', false, 3),
('4
1 2 3 4
', '10
', false, 4),
('4
1000000000 1000000000 1000000000 1000000000
', '4000000000
', false, 5),
('3
5 -10 6
', '6
', false, 6),
('6
-1 3 -1 3 -10 2
', '5
', false, 7)) AS c(input, output, sample, idx) WHERE p.slug='maximum-subarray';


INSERT INTO problems (title, slug, description, difficulty, time_limit_ms, memory_limit_mb) VALUES
('Đường đi ngắn nhất', 'shortest-path', 'Cho đồ thị có hướng có n đỉnh và m cạnh với trọng số không âm. Tìm đường đi ngắn nhất từ đỉnh 1 đến đỉnh n.

## Input
Dòng đầu chứa n, m (1 ≤ n ≤ 100000, 0 ≤ m ≤ 200000).
Mỗi dòng tiếp chứa u, v, w biểu diễn cạnh u → v, 0 ≤ w ≤ 10^9. Có thể có cạnh song song.

## Output
In khoảng cách ngắn nhất hoặc -1 nếu không có đường đi.
', 'MEDIUM', 2000, 256);

INSERT INTO problem_tags (problem_id, tag_id)
SELECT p.id, t.id FROM problems p CROSS JOIN tags t WHERE p.slug='shortest-path' AND t.name IN ('Graphs', 'Dijkstra');

INSERT INTO test_cases (problem_id, input, expected_output, is_sample, order_index)
SELECT p.id, c.input, c.output, c.sample, c.idx
FROM problems p CROSS JOIN (VALUES
('3 3
1 2 4
2 3 5
1 3 12
', '9
', true, 0),
('3 1
1 2 1
', '-1
', true, 1),
('1 0
', '0
', false, 2),
('2 1
1 2 0
', '0
', false, 3),
('4 4
1 2 10
1 3 1
3 2 1
2 4 2
', '4
', false, 4),
('3 2
2 1 1
3 2 1
', '-1
', false, 5),
('3 2
1 2 1000000000
2 3 1000000000
', '2000000000
', false, 6),
('4 4
1 2 2
1 2 1
2 3 1
3 4 1
', '3
', false, 7)) AS c(input, output, sample, idx) WHERE p.slug='shortest-path';


INSERT INTO problems (title, slug, description, difficulty, time_limit_ms, memory_limit_mb) VALUES
('Đường kính của cây', 'tree-diameter', 'Cho cây vô hướng gồm n đỉnh. Đường kính là số cạnh lớn nhất trên đường đi đơn giữa hai đỉnh bất kỳ.

## Input
Dòng đầu chứa n (1 ≤ n ≤ 200000). Tiếp theo là n-1 cạnh u, v.

## Output
In độ dài đường kính. Cây có một đỉnh có đường kính bằng 0.
', 'HARD', 2000, 256);

INSERT INTO problem_tags (problem_id, tag_id)
SELECT p.id, t.id FROM problems p CROSS JOIN tags t WHERE p.slug='tree-diameter' AND t.name IN ('Trees', 'BFS');

INSERT INTO test_cases (problem_id, input, expected_output, is_sample, order_index)
SELECT p.id, c.input, c.output, c.sample, c.idx
FROM problems p CROSS JOIN (VALUES
('4
1 2
2 3
2 4
', '2
', true, 0),
('1
', '0
', true, 1),
('2
1 2
', '1
', false, 2),
('5
1 2
2 3
3 4
4 5
', '4
', false, 3),
('5
1 2
1 3
1 4
1 5
', '2
', false, 4),
('7
1 2
1 3
2 4
2 5
3 6
3 7
', '4
', false, 5),
('6
1 2
2 3
3 4
2 5
5 6
', '4
', false, 6),
('100
1 2
2 3
3 4
4 5
5 6
6 7
7 8
8 9
9 10
10 11
11 12
12 13
13 14
14 15
15 16
16 17
17 18
18 19
19 20
20 21
21 22
22 23
23 24
24 25
25 26
26 27
27 28
28 29
29 30
30 31
31 32
32 33
33 34
34 35
35 36
36 37
37 38
38 39
39 40
40 41
41 42
42 43
43 44
44 45
45 46
46 47
47 48
48 49
49 50
50 51
51 52
52 53
53 54
54 55
55 56
56 57
57 58
58 59
59 60
60 61
61 62
62 63
63 64
64 65
65 66
66 67
67 68
68 69
69 70
70 71
71 72
72 73
73 74
74 75
75 76
76 77
77 78
78 79
79 80
80 81
81 82
82 83
83 84
84 85
85 86
86 87
87 88
88 89
89 90
90 91
91 92
92 93
93 94
94 95
95 96
96 97
97 98
98 99
99 100
', '99
', false, 7)) AS c(input, output, sample, idx) WHERE p.slug='tree-diameter';


INSERT INTO problems (title, slug, description, difficulty, enabled) VALUES
('Bản nháp: Bài toán đang biên soạn', 'draft-problem', 'Bài đang biên soạn để kiểm tra giao diện quản trị và trạng thái ẩn.', 'HARD', FALSE);


INSERT INTO submissions (problem_id, user_id, language, source_code, status, verdict, passed_test_cases, total_test_cases, execution_time_ms, memory_used_kb, compile_output, created_at, updated_at)
SELECT p.id, u.id, 'python', 'a, b = map(int, input().split())
print(a + b)
', 'COMPLETED', 'AC', 8, 8, 10, 8192, NULL, CURRENT_TIMESTAMP - INTERVAL '24 hours', CURRENT_TIMESTAMP - INTERVAL '24 hours'
FROM problems p CROSS JOIN users u WHERE p.slug='a-plus-b' AND u.username='demo';

INSERT INTO submissions (problem_id, user_id, language, source_code, status, verdict, passed_test_cases, total_test_cases, execution_time_ms, memory_used_kb, compile_output, created_at, updated_at)
SELECT p.id, u.id, 'python', 'print(0)
', 'COMPLETED', 'WA', 2, 8, 11, 8192, NULL, CURRENT_TIMESTAMP - INTERVAL '23 hours', CURRENT_TIMESTAMP - INTERVAL '23 hours'
FROM problems p CROSS JOIN users u WHERE p.slug='a-plus-b' AND u.username='demo';

INSERT INTO submissions (problem_id, user_id, language, source_code, status, verdict, passed_test_cases, total_test_cases, execution_time_ms, memory_used_kb, compile_output, created_at, updated_at)
SELECT p.id, u.id, 'python', 'def solve(:
', 'COMPLETED', 'CE', 0, 8, NULL, NULL, 'Synthetic demo: invalid Python syntax', CURRENT_TIMESTAMP - INTERVAL '22 hours', CURRENT_TIMESTAMP - INTERVAL '22 hours'
FROM problems p CROSS JOIN users u WHERE p.slug='a-plus-b' AND u.username='demo';

INSERT INTO submissions (problem_id, user_id, language, source_code, status, verdict, passed_test_cases, total_test_cases, execution_time_ms, memory_used_kb)
SELECT p.id, u.id, 'python', 'a, b = map(int, input().split())
print(a + b)
', 'COMPLETED', 'AC', 8, 8, 15, 8192 FROM problems p CROSS JOIN users u WHERE p.slug='a-plus-b' AND u.username='admin';

INSERT INTO submissions (problem_id, user_id, language, source_code, status, verdict, passed_test_cases, total_test_cases, execution_time_ms, memory_used_kb, compile_output, created_at, updated_at)
SELECT p.id, u.id, 'python', 'import sys
a = list(map(int, sys.stdin.buffer.read().split()))
print(sum(a[1:]))
', 'COMPLETED', 'AC', 8, 8, 13, 8192, NULL, CURRENT_TIMESTAMP - INTERVAL '21 hours', CURRENT_TIMESTAMP - INTERVAL '21 hours'
FROM problems p CROSS JOIN users u WHERE p.slug='array-sum' AND u.username='demo';

INSERT INTO submissions (problem_id, user_id, language, source_code, status, verdict, passed_test_cases, total_test_cases, execution_time_ms, memory_used_kb, compile_output, created_at, updated_at)
SELECT p.id, u.id, 'python', 'print(0)
', 'COMPLETED', 'WA', 2, 8, 14, 8192, NULL, CURRENT_TIMESTAMP - INTERVAL '20 hours', CURRENT_TIMESTAMP - INTERVAL '20 hours'
FROM problems p CROSS JOIN users u WHERE p.slug='array-sum' AND u.username='demo';

INSERT INTO submissions (problem_id, user_id, language, source_code, status, verdict, passed_test_cases, total_test_cases, execution_time_ms, memory_used_kb, compile_output, created_at, updated_at)
SELECT p.id, u.id, 'python', 'def solve(:
', 'COMPLETED', 'CE', 0, 8, NULL, NULL, 'Synthetic demo: invalid Python syntax', CURRENT_TIMESTAMP - INTERVAL '19 hours', CURRENT_TIMESTAMP - INTERVAL '19 hours'
FROM problems p CROSS JOIN users u WHERE p.slug='array-sum' AND u.username='demo';

INSERT INTO submissions (problem_id, user_id, language, source_code, status, verdict, passed_test_cases, total_test_cases, execution_time_ms, memory_used_kb, compile_output, created_at, updated_at)
SELECT p.id, u.id, 'python', 's = input().strip()
print(''YES'' if s == s[::-1] else ''NO'')
', 'COMPLETED', 'AC', 8, 8, 16, 8192, NULL, CURRENT_TIMESTAMP - INTERVAL '18 hours', CURRENT_TIMESTAMP - INTERVAL '18 hours'
FROM problems p CROSS JOIN users u WHERE p.slug='palindrome' AND u.username='demo';

INSERT INTO submissions (problem_id, user_id, language, source_code, status, verdict, passed_test_cases, total_test_cases, execution_time_ms, memory_used_kb, compile_output, created_at, updated_at)
SELECT p.id, u.id, 'python', 'print(0)
', 'COMPLETED', 'WA', 0, 8, 17, 8192, NULL, CURRENT_TIMESTAMP - INTERVAL '17 hours', CURRENT_TIMESTAMP - INTERVAL '17 hours'
FROM problems p CROSS JOIN users u WHERE p.slug='palindrome' AND u.username='demo';

INSERT INTO submissions (problem_id, user_id, language, source_code, status, verdict, passed_test_cases, total_test_cases, execution_time_ms, memory_used_kb, compile_output, created_at, updated_at)
SELECT p.id, u.id, 'python', 'def solve(:
', 'COMPLETED', 'CE', 0, 8, NULL, NULL, 'Synthetic demo: invalid Python syntax', CURRENT_TIMESTAMP - INTERVAL '16 hours', CURRENT_TIMESTAMP - INTERVAL '16 hours'
FROM problems p CROSS JOIN users u WHERE p.slug='palindrome' AND u.username='demo';

INSERT INTO submissions (problem_id, user_id, language, source_code, status, verdict, passed_test_cases, total_test_cases, execution_time_ms, memory_used_kb)
SELECT p.id, u.id, 'python', 's = input().strip()
print(''YES'' if s == s[::-1] else ''NO'')
', 'COMPLETED', 'AC', 8, 8, 15, 8192 FROM problems p CROSS JOIN users u WHERE p.slug='palindrome' AND u.username='admin';

INSERT INTO submissions (problem_id, user_id, language, source_code, status, verdict, passed_test_cases, total_test_cases, execution_time_ms, memory_used_kb, compile_output, created_at, updated_at)
SELECT p.id, u.id, 'python', 'print(0)
', 'COMPLETED', 'WA', 2, 8, 19, 8192, NULL, CURRENT_TIMESTAMP - INTERVAL '15 hours', CURRENT_TIMESTAMP - INTERVAL '15 hours'
FROM problems p CROSS JOIN users u WHERE p.slug='binary-search' AND u.username='demo';

INSERT INTO submissions (problem_id, user_id, language, source_code, status, verdict, passed_test_cases, total_test_cases, execution_time_ms, memory_used_kb, compile_output, created_at, updated_at)
SELECT p.id, u.id, 'python', 'def solve(:
', 'COMPLETED', 'CE', 0, 8, NULL, NULL, 'Synthetic demo: invalid Python syntax', CURRENT_TIMESTAMP - INTERVAL '14 hours', CURRENT_TIMESTAMP - INTERVAL '14 hours'
FROM problems p CROSS JOIN users u WHERE p.slug='binary-search' AND u.username='demo';

INSERT INTO submissions (problem_id, user_id, language, source_code, status, verdict, passed_test_cases, total_test_cases, execution_time_ms, memory_used_kb, compile_output, created_at, updated_at)
SELECT p.id, u.id, 'python', 'print(0)
', 'COMPLETED', 'WA', 0, 8, 22, 8192, NULL, CURRENT_TIMESTAMP - INTERVAL '12 hours', CURRENT_TIMESTAMP - INTERVAL '12 hours'
FROM problems p CROSS JOIN users u WHERE p.slug='range-sum' AND u.username='demo';

INSERT INTO submissions (problem_id, user_id, language, source_code, status, verdict, passed_test_cases, total_test_cases, execution_time_ms, memory_used_kb, compile_output, created_at, updated_at)
SELECT p.id, u.id, 'python', 'def solve(:
', 'COMPLETED', 'CE', 0, 8, NULL, NULL, 'Synthetic demo: invalid Python syntax', CURRENT_TIMESTAMP - INTERVAL '11 hours', CURRENT_TIMESTAMP - INTERVAL '11 hours'
FROM problems p CROSS JOIN users u WHERE p.slug='range-sum' AND u.username='demo';

INSERT INTO submissions (problem_id, user_id, language, source_code, status, verdict, passed_test_cases, total_test_cases, execution_time_ms, memory_used_kb)
SELECT p.id, u.id, 'python', 'import sys
it = iter(map(int, sys.stdin.buffer.read().split()))
n, q = next(it), next(it)
p = [0]
for _ in range(n): p.append(p[-1] + next(it))
for _ in range(q):
    l, r = next(it), next(it)
    print(p[r] - p[l-1])
', 'COMPLETED', 'AC', 8, 8, 15, 8192 FROM problems p CROSS JOIN users u WHERE p.slug='range-sum' AND u.username='admin';

INSERT INTO submissions (problem_id, user_id, language, source_code, status, verdict, passed_test_cases, total_test_cases, execution_time_ms, memory_used_kb, compile_output, created_at, updated_at)
SELECT p.id, u.id, 'python', 'print(0)
', 'COMPLETED', 'WA', 1, 8, 25, 8192, NULL, CURRENT_TIMESTAMP - INTERVAL '9 hours', CURRENT_TIMESTAMP - INTERVAL '9 hours'
FROM problems p CROSS JOIN users u WHERE p.slug='maximum-subarray' AND u.username='demo';

INSERT INTO submissions (problem_id, user_id, language, source_code, status, verdict, passed_test_cases, total_test_cases, execution_time_ms, memory_used_kb, compile_output, created_at, updated_at)
SELECT p.id, u.id, 'python', 'def solve(:
', 'COMPLETED', 'CE', 0, 8, NULL, NULL, 'Synthetic demo: invalid Python syntax', CURRENT_TIMESTAMP - INTERVAL '8 hours', CURRENT_TIMESTAMP - INTERVAL '8 hours'
FROM problems p CROSS JOIN users u WHERE p.slug='maximum-subarray' AND u.username='demo';

INSERT INTO submissions (problem_id, user_id, language, source_code, status, verdict, passed_test_cases, total_test_cases, execution_time_ms, memory_used_kb, compile_output, created_at, updated_at)
SELECT p.id, u.id, 'python', 'print(0)
', 'COMPLETED', 'WA', 2, 8, 28, 8192, NULL, CURRENT_TIMESTAMP - INTERVAL '6 hours', CURRENT_TIMESTAMP - INTERVAL '6 hours'
FROM problems p CROSS JOIN users u WHERE p.slug='shortest-path' AND u.username='demo';

INSERT INTO submissions (problem_id, user_id, language, source_code, status, verdict, passed_test_cases, total_test_cases, execution_time_ms, memory_used_kb, compile_output, created_at, updated_at)
SELECT p.id, u.id, 'python', 'def solve(:
', 'COMPLETED', 'CE', 0, 8, NULL, NULL, 'Synthetic demo: invalid Python syntax', CURRENT_TIMESTAMP - INTERVAL '5 hours', CURRENT_TIMESTAMP - INTERVAL '5 hours'
FROM problems p CROSS JOIN users u WHERE p.slug='shortest-path' AND u.username='demo';

INSERT INTO submissions (problem_id, user_id, language, source_code, status, verdict, passed_test_cases, total_test_cases, execution_time_ms, memory_used_kb)
SELECT p.id, u.id, 'python', 'import sys, heapq
it = iter(map(int, sys.stdin.buffer.read().split()))
n, m = next(it), next(it)
adj = [[] for _ in range(n+1)]
for _ in range(m):
    u,v,w = next(it), next(it), next(it)
    adj[u].append((v,w))
d = [10**30]*(n+1)
d[1] = 0
q = [(0,1)]
while q:
    du,u = heapq.heappop(q)
    if du != d[u]: continue
    for v,w in adj[u]:
        if du+w < d[v]:
            d[v] = du+w
            heapq.heappush(q,(d[v],v))
print(-1 if d[n] == 10**30 else d[n])
', 'COMPLETED', 'AC', 8, 8, 15, 8192 FROM problems p CROSS JOIN users u WHERE p.slug='shortest-path' AND u.username='admin';
