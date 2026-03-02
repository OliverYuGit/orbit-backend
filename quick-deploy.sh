#!/usr/bin/env bash
# quick-deploy.sh — Build in Docker and deploy to ECS
set -euo pipefail

ECS_HOST="ali_ecs_centos7"
IMAGE_TAG="orbit-backend-mission:latest"
CONTAINER_NAME="orbit-mission"
ENV_FILE="/opt/orbit/.env.prod"
HOST_PORT="8080"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "==> [orbit-backend] Quick deploy to ECS"

# ---- 1. Build Docker image locally ----
echo "==> Building Docker image: ${IMAGE_TAG}..."
cd "${SCRIPT_DIR}"
docker build -t "${IMAGE_TAG}" .
echo "    Image built."

# ---- 2. Transfer image to ECS ----
echo "==> Transferring image to ECS..."
docker save "${IMAGE_TAG}" | ssh "${ECS_HOST}" "docker load"
echo "    Image loaded on ECS."

# ---- 3. Deploy on ECS ----
echo "==> Deploying container on ECS..."
ssh "${ECS_HOST}" bash <<'REMOTE'
set -euo pipefail

CONTAINER_NAME="orbit-mission"
IMAGE_TAG="orbit-backend-mission:latest"
ENV_FILE="/opt/orbit/.env.prod"
HOST_PORT="8080"

# Stop & remove old container
docker rm -f "${CONTAINER_NAME}" 2>/dev/null && echo "    Removed old container." || true

# Start new container
docker run -d \
  --name "${CONTAINER_NAME}" \
  --restart unless-stopped \
  --env-file "${ENV_FILE}" \
  -p "127.0.0.1:${HOST_PORT}:8080" \
  "${IMAGE_TAG}"

echo "    Container started: ${CONTAINER_NAME}"

# Wait for health
echo "    Waiting for health check..."
for i in $(seq 1 12); do
  sleep 5
  STATUS=$(docker inspect --format='{{.State.Health.Status}}' "${CONTAINER_NAME}" 2>/dev/null || echo "none")
  echo "    [${i}/12] health: ${STATUS}"
  if [[ "${STATUS}" == "healthy" ]]; then
    echo "    ✅ Container is healthy."
    break
  fi
done

# Show recent logs
echo ""
echo "==> Recent logs:"
docker logs --tail 30 "${CONTAINER_NAME}"
REMOTE

echo ""
echo "==> ✅ Deployment complete!"
echo "    Container: ${CONTAINER_NAME}"
echo "    Check logs: ssh ${ECS_HOST} 'docker logs -f ${CONTAINER_NAME}'"
