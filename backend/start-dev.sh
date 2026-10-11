#!/bin/bash
set -e

export DB_HOST=$(ip route show default | awk '{print $3}')
export DB_PORT=5432
export DB_NAME=codeforge
export DB_USERNAME=codeforge_user
export DB_PASSWORD=123456

echo "PostgreSQL Host: $DB_HOST"

./mvnw spring-boot:run