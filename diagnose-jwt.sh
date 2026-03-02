#!/bin/bash
# Diagnose JWT token issues

set -euo pipefail

ECS_HOST="ali_ecs_centos7"
CONTAINER_NAME="${1:-orbit-mission}"

echo "==> Diagnosing JWT issues for container: ${CONTAINER_NAME}"
echo ""

# Get the JWT secret from the container
echo "1. Checking JWT_SECRET configuration..."
ssh "${ECS_HOST}" "docker exec ${CONTAINER_NAME} printenv | grep JWT_SECRET" || echo "   ⚠️  Could not retrieve JWT_SECRET"
echo ""

# Check recent logs for JWT errors
echo "2. Recent JWT validation errors:"
ssh "${ECS_HOST}" "docker logs --tail 50 ${CONTAINER_NAME} 2>&1 | grep -i 'jwt\|token'" || echo "   No JWT errors found"
echo ""

# Test login and token generation
echo "3. Testing login endpoint..."
LOGIN_RESPONSE=$(ssh "${ECS_HOST}" "curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{\"username\":\"admin\",\"password\":\"admin123\"}'")

echo "$LOGIN_RESPONSE" | jq . 2>/dev/null || echo "$LOGIN_RESPONSE"

TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data.accessToken' 2>/dev/null || echo "")

if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
  echo "   ❌ Failed to get token from login"
  exit 1
fi

echo ""
echo "4. Token received (first 50 chars): ${TOKEN:0:50}..."
echo ""

# Decode token header and payload (without verification)
echo "5. Token structure:"
HEADER=$(echo "$TOKEN" | cut -d. -f1)
PAYLOAD=$(echo "$TOKEN" | cut -d. -f2)

echo "   Header: $(echo "$HEADER" | base64 -d 2>/dev/null || echo "$HEADER=" | base64 -d 2>/dev/null)"
echo "   Payload: $(echo "$PAYLOAD" | base64 -d 2>/dev/null || echo "$PAYLOAD=" | base64 -d 2>/dev/null)"
echo ""

# Test the token immediately
echo "6. Testing token with /me endpoint..."
ME_RESPONSE=$(ssh "${ECS_HOST}" "curl -s -X GET http://localhost:8080/api/v1/auth/me \
  -H 'Authorization: Bearer $TOKEN'")

echo "$ME_RESPONSE" | jq . 2>/dev/null || echo "$ME_RESPONSE"

if echo "$ME_RESPONSE" | jq -e '.success == true' > /dev/null 2>&1; then
  echo "   ✅ Token validation successful"
else
  echo "   ❌ Token validation failed"
  echo ""
  echo "7. Checking container logs for this request:"
  ssh "${ECS_HOST}" "docker logs --tail 20 ${CONTAINER_NAME} 2>&1"
fi
