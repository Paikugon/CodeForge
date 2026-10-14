#!/bin/bash
set -e

export DB_HOST=$(ip route show default | awk '{print $3}')
export DB_PORT=5432
export DB_NAME=codeforge
export DB_USERNAME=codeforge_user
export DB_PASSWORD=123456
export JUDGE_STORAGE_PATH="$HOME/projects/OnlineJudgeDeploy/data/backend/test_case"

export JUDGE_SERVER_TOKEN="$(
  grep '^JUDGE_SERVER_TOKEN=' \
    ~/projects/OnlineJudgeDeploy/.env | cut -d= -f2-
)"
export JUDGE_TOKEN_HASH="$(
  printf '%s' "$JUDGE_SERVER_TOKEN" | sha256sum | cut -d' ' -f1
)"

echo "PostgreSQL Host: $DB_HOST"
echo "Judge Server Token: $JUDGE_SERVER_TOKEN"

./mvnw spring-boot:run