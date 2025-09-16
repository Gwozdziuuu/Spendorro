# Keycloak Configuration

This directory contains configuration and tools for managing Keycloak in the Spendorro project.

## Files

- `export-realm.sh` - Script for automatic realm configuration export
- `spendorro-realm-export.json` - Exported spendorro realm configuration

## Exporting Realm Configuration

### Automatic Export

Use the `export-realm.sh` script to automatically export the current realm configuration:

```bash
./export-realm.sh
```

### What the script does:

1. **Checks** if the Keycloak container is running
2. **Configures** kcadm CLI with administrator credentials
3. **Exports** the `spendorro` realm to a JSON file
4. **Copies** the export file from the container to the local directory
5. **Displays** information about the exported file

### Requirements

- Running Keycloak container (`docker-compose up keycloak`)
- Access to administrator account (admin/admin)

## Configuration Import

The realm configuration is automatically imported during Keycloak startup thanks to:

- The `--import-realm` parameter in the startup command
- Mapping the export file to `/opt/keycloak/data/import/`

## Realm Structure

The exported realm contains:
- Realm configuration (security settings, sessions, etc.)
- OAuth2/OIDC clients
- Users and groups
- Roles and permissions
- Attribute mappings
- Themes and customizations

## Troubleshooting

### Container is not running
```bash
docker-compose up keycloak
```

### Authorization issues
Check if the administrator credentials in docker-compose.yml are correct:
- KEYCLOAK_ADMIN: admin
- KEYCLOAK_ADMIN_PASSWORD: admin