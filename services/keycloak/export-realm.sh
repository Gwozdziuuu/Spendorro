#!/bin/bash

# Keycloak Realm Export Script
# Automatically exports the spendorro realm configuration from a running Keycloak instance

set -e

REALM_NAME="spendorro"
CONTAINER_NAME="services-keycloak-1"
OUTPUT_FILE="spendorro-realm-export.json"

echo "🔐 Eksportowanie konfiguracji realm '$REALM_NAME'..."

# Check if container is running
if ! docker ps | grep -q "$CONTAINER_NAME"; then
    echo "❌ Kontener Keycloak '$CONTAINER_NAME' nie jest uruchomiony!"
    echo "Uruchom najpierw: docker-compose up keycloak"
    exit 1
fi

echo "📋 Konfigurowanie kcadm CLI..."
docker exec "$CONTAINER_NAME" /opt/keycloak/bin/kcadm.sh config credentials \
    --server http://localhost:8080 \
    --realm master \
    --user admin \
    --password admin

echo "📤 Eksportowanie realm '$REALM_NAME'..."
docker exec "$CONTAINER_NAME" /opt/keycloak/bin/kc.sh export \
    --realm "$REALM_NAME" \
    --file "/tmp/$OUTPUT_FILE" \
    --optimized

echo "💾 Kopiowanie pliku eksportu do hosta..."
docker cp "$CONTAINER_NAME:/tmp/$OUTPUT_FILE" "./$OUTPUT_FILE"

echo "✅ Eksport zakończony pomyślnie!"
echo "📁 Plik konfiguracji: $(pwd)/$OUTPUT_FILE"
echo "📊 Rozmiar pliku: $(du -h "$OUTPUT_FILE" | cut -f1)"

# Show basic info about exported realm
echo ""
echo "ℹ️  Informacje o wyeksportowanym realm:"
echo "   Nazwa realm: $REALM_NAME"
echo "   Data eksportu: $(date)"
echo "   Plik: $OUTPUT_FILE"