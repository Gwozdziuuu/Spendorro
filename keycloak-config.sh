#!/bin/bash

# Keycloak Configuration Script
# Run this to configure Valid Redirect URIs and Web Origins for localhost

KEYCLOAK_URL="http://localhost:9080"
REALM="spendorro"
CLIENT_ID="spendorro-be"
LOCALHOST_URL="http://localhost:8213"

echo "Configuring Keycloak client for localhost development..."

# Get admin token (requires admin credentials)
echo "Please enter Keycloak admin credentials:"
read -p "Username: " ADMIN_USER
read -s -p "Password: " ADMIN_PASS
echo

TOKEN=$(curl -s -X POST "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=$ADMIN_USER" \
  -d "password=$ADMIN_PASS" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" | \
  jq -r '.access_token')

if [ "$TOKEN" = "null" ] || [ -z "$TOKEN" ]; then
  echo "Failed to get admin token. Check credentials."
  exit 1
fi

echo "Got admin token, configuring client..."

# Get client internal ID
CLIENT_UUID=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/$REALM/clients" \
  -H "Authorization: Bearer $TOKEN" | \
  jq -r ".[] | select(.clientId==\"$CLIENT_ID\") | .id")

if [ "$CLIENT_UUID" = "null" ] || [ -z "$CLIENT_UUID" ]; then
  echo "Client $CLIENT_ID not found in realm $REALM"
  exit 1
fi

# Update client configuration
curl -s -X PUT "$KEYCLOAK_URL/admin/realms/$REALM/clients/$CLIENT_UUID" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"redirectUris\": [\"$LOCALHOST_URL/*\"],
    \"webOrigins\": [\"$LOCALHOST_URL\"],
    \"adminUrl\": \"$LOCALHOST_URL\"
  }"

echo "✅ Keycloak client configured for localhost development"
echo "Valid Redirect URIs: $LOCALHOST_URL/*"  
echo "Web Origins: $LOCALHOST_URL"