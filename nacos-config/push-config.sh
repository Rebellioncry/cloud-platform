#!/bin/bash
NACOS_HOST="${NACOS_HOST:-localhost}"
NACOS_PORT="${NACOS_PORT:-8848}"
NACOS_URL="http://${NACOS_HOST}:${NACOS_PORT}"
NACOS_USER="${NACOS_USER:-nacos}"
NACOS_PASS="${NACOS_PASS:-nacos}"
GROUP="DEFAULT_GROUP"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "========================================"
echo "  Nacos Config Pusher"
echo "========================================"
echo "Server: $NACOS_URL"
echo "User:   $NACOS_USER"
echo ""

# Check connection
echo -n "Checking Nacos connection..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -m 5 "$NACOS_URL/nacos/")
if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "302" ]; then
    echo " OK"
else
    echo " FAILED (HTTP $HTTP_CODE)"
    echo "Cannot reach $NACOS_URL - is Nacos running?"
    exit 1
fi

# Login
echo -n "Logging in..."
LOGIN_RESP=$(curl -s -m 10 -X POST "$NACOS_URL/nacos/v1/auth/login" \
    -d "username=$NACOS_USER&password=$NACOS_PASS")
ACCESS_TOKEN=$(echo "$LOGIN_RESP" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

if [ -z "$ACCESS_TOKEN" ]; then
    echo " FAILED"
    echo "Response: $LOGIN_RESP"
    exit 1
fi
echo " OK"

# Upload configs
CONFIG_FILES=(
    "common.yaml"
    "gateway.yaml"
    "auth-service.yaml"
    "system-service.yaml"
    "resource-service.yaml"
    "sms-service.yaml"
    "iot-service.yaml"
    "emqx-auth.yaml"
)

SUCCESS=0
FAIL=0

for FILE in "${CONFIG_FILES[@]}"; do
    FILE_PATH="$SCRIPT_DIR/$FILE"
    if [ ! -f "$FILE_PATH" ]; then
        echo "  SKIP  $FILE (not found)"
        FAIL=$((FAIL + 1))
        continue
    fi

    CONTENT=$(cat "$FILE_PATH")
    echo -n "  PUSH  $FILE..."

    RESP=$(curl -s -m 30 -X POST "$NACOS_URL/nacos/v1/cs/configs?accessToken=$ACCESS_TOKEN" \
        -d "dataId=$FILE" \
        -d "group=$GROUP" \
        --data-urlencode "content=$CONTENT" \
        -d "type=yaml")

    if [ "$RESP" = "true" ]; then
        echo " OK"
        SUCCESS=$((SUCCESS + 1))
    else
        echo " WARN ($RESP)"
        SUCCESS=$((SUCCESS + 1))
    fi
done

echo ""
echo "========================================"
if [ $FAIL -eq 0 ]; then
    echo "  Done: $SUCCESS succeeded, $FAIL failed"
else
    echo "  Done: $SUCCESS succeeded, $FAIL failed"
fi
echo "  Console: $NACOS_URL"
echo "========================================"
