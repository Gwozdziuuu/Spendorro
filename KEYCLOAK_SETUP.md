# Keycloak Setup Instructions

Aplikacja wymaga skonfigurowanego Keycloak do autoryzacji. Wykonaj poniższe kroki:

## 1. Uruchom Keycloak (jeśli nie działa)

```bash
docker run -p 9080:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:latest start-dev
```

## 2. Dostęp do Admin Console

Otwórz: http://localhost:9080/admin/
Login: `admin` / Password: `admin`

## 3. Utwórz Realm

1. Kliknij dropdown "Master" (lewy górny róg)
2. Kliknij "Create Realm"
3. Name: `spendorro`
4. Save

## 4. Utwórz Client

1. W realm `spendorro` → Clients → Create client
2. Client ID: `spendorro-be`
3. Client type: `OpenID Connect`
4. Next → Save

## 5. Konfiguruj Client

1. W client `spendorro-be` → Settings:
   - Client authentication: `On`
   - Authentication flow: Standard flow, Direct access grants
   - Valid redirect URIs: `http://localhost:8213/*`
   - Web origins: `http://localhost:8213`
   - Save

2. Credentials tab:
   - Skopiuj Client Secret
   - Zaktualizuj w application.properties:
     ```
     KEYCLOAK_CLIENT_SECRET=TWOJ_SECRET_TUTAJ
     ```

## 6. Utwórz Test User

1. Users → Create new user
2. Username: `testuser`
3. Save
4. Credentials tab → Set password → Temporary: Off

## 7. Restart Aplikację

Po konfiguracji Keycloak zrestartuj aplikację Quarkus.

## Rozwiązywanie problemów

- **404 na Keycloak URLs**: Sprawdź czy Keycloak działa na porcie 9080
- **401 Unauthorized**: Sprawdź client secret w application.properties  
- **Infinite redirect**: Sprawdź Valid Redirect URIs w client config