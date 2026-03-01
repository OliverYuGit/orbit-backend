#!/bin/bash

# Test authentication flow
API_URL="${API_URL:-http://localhost:8080/api/v1}"

echo "=== Testing Login ==="
LOGIN_RESPONSE=$(curl -s -X POST "$API_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}')

echo "$LOGIN_RESPONSE" | jq .

TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data.accessToken')

if [ "$TOKEN" = "null" ] || [ -z "$TOKEN" ]; then
  echo "❌ Login failed - no token received"
  exit 1
fi

echo ""
echo "=== Testing /me endpoint with token ==="
ME_RESPONSE=$(curl -s -X GET "$API_URL/auth/me" \
  -H "Authorization: Bearer $TOKEN")

echo "$ME_RESPONSE" | jq .

if echo "$ME_RESPONSE" | jq -e '.success == true' > /dev/null; then
  echo "✅ Authentication working correctly"
else
  echo "❌ Authentication failed - got 403 or error"
  exit 1
fi
