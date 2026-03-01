#!/usr/bin/env bash
# deploy-ecs.sh — Build & deploy orbit-backend (Leo's new Mission backend) to ECS
#
# Usage:
#   ./deploy-ecs.sh [dev|prod]
#
# Prerequisites:
#   - Docker installed locally
#   - SSH access to ali_ecs_centos7 (see ~/.ssh/config)
#   - /opt/orbit/.env.dev or .env.prod filled in on ECS (DB_PASSWORD, JWT_SECRET)
#
# Environment variables (override defaults):
#   HOST_PORT_DEV   — host port for dev container  (default: 8081)
#   HOST_PORT_PROD  — host port for prod container (default: 8080)
#
set -euo pipefail

ENV="${1:-dev}"
ECS_HOST="ali_ecs_centos7"
IMAGE_TAG="orbit-backend-mission:${ENV}"
CONTAINER_NAME="orbit-backend-mission-${ENV}"
ENV_FILE="/opt/orbit/.env.${ENV}"

if [[ "$ENV" == "prod" ]]; then
  HOST_PORT="${HOST_PORT_PROD:-8080}"
else
  HOST_PORT="${HOST_PORT_DEV:-8081}"
fi

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "==> [orbit-backend] Deploying ENV=${ENV} → ECS port ${HOST_PORT}"

# ---- 1. Build JAR ----
echo "==> Building JAR (skipping tests)..."
cd "${SCRIPT_DIR}"
./mvnw package -DskipTests -q
JAR=$(ls target/*.jar | head -1)
echo "    JAR: ${JAR}"

# ---- 2. Build Docker image ----
echo "==> Building Docker image: ${IMAGE_TAG}..."
docker build -t "${IMAGE_TAG}" .
echo "    Image built."

# ---- 3. Transfer image to ECS ----
echo "==> Transferring image to ECS (docker save | ssh docker load)..."
docker save "${IMAGE_TAG}" | ssh "${ECS_HOST}" "docker load"
echo "    Image loaded on ECS."

# ---- 4. Deploy on ECS ----
echo "==> Starting container on ECS..."
ssh "${ECS_HOST}" bash <<REMOTE
set -euo pipefail

if [ ! -f "${ENV_FILE}" ]; then
  echo "ERROR: ${ENV_FILE} not found on ECS. Fill in DB_PASSWORD and JWT_SECRET first."
  exit 1
fi

# Check for unfilled placeholders
if grep -q 'REPLACE_WITH' "${ENV_FILE}"; then
  echo "ERROR: ${ENV_FILE} still has REPLACE_WITH placeholders. Fill them in first."
  exit 1
fi

# Stop & remove old container
docker rm -f "${CONTAINER_NAME}" 2>/dev/null && echo "    Removed old container." || true

# Start new container
docker run -d \
  --name "${CONTAINER_NAME}" \
  --restart unless-stopped \
  --env-file "${ENV_FILE}" \
  -p "127.0.0.1:${HOST_PORT}:8080" \
  "${IMAGE_TAG}"

echo "    Container started: ${CONTAINER_NAME} → 127.0.0.1:${HOST_PORT}"

# Wait for health
echo "    Waiting for health check (up to 60s)..."
for i in \$(seq 1 12); do
  sleep 5
  STATUS=\$(docker inspect --format='{{.State.Health.Status}}' "${CONTAINER_NAME}" 2>/dev/null || echo "none")
  echo "    [${i}/12] health: \${STATUS}"
  if [[ "\${STATUS}" == "healthy" ]]; then
    echo "    ✅ Container is healthy."
    break
  fi
done
REMOTE

echo "==> ✅ orbit-backend [${ENV}] deployed. Container: ${CONTAINER_NAME}, ECS port: ${HOST_PORT}"
echo ""
echo "    Next steps:"
echo "    - Verify: ssh ${ECS_HOST} 'curl -s http://localhost:${HOST_PORT}/actuator/health'"
echo "    - Logs:   ssh ${ECS_HOST} 'docker logs -f ${CONTAINER_NAME}'"
