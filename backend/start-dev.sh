#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

# Load environment variables
if [[ -f .env ]]; then
    set -a
    source .env
    set +a
else
    echo "Error: .env file not found"
    exit 1
fi

# PostgreSQL running on Windows host via WSL2
export DB_HOST="$(ip route show default | awk '{print $3; exit}')"

# Load Judge Server Token
JUDGE_ENV="$HOME/projects/OnlineJudgeDeploy/.env"

if [[ ! -f "$JUDGE_ENV" ]]; then
    echo "Error: Judge Server .env not found"
    exit 1
fi

export JUDGE_SERVER_TOKEN="$(
    grep '^JUDGE_SERVER_TOKEN=' "$JUDGE_ENV" | cut -d= -f2-
)"

if [[ -z "$JUDGE_SERVER_TOKEN" ]]; then
    echo "Error: JUDGE_SERVER_TOKEN is empty"
    exit 1
fi

export JUDGE_TOKEN_HASH="$(
    printf '%s' "$JUDGE_SERVER_TOKEN" |
        sha256sum | cut -d' ' -f1
)"

# Validate JWT secret
: "${JWT_SECRET:?JWT_SECRET is required}"

echo "PostgreSQL Host: $DB_HOST"
echo "Judge Server Token: [configured]"
echo "JWT Secret: [configured]"

exec ./mvnw spring-boot:run