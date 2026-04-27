# Deploy / ejecución productiva inicial

## Opción recomendada para empezar hoy (local)

1. Levantar todo con Docker Compose:

```bash
docker compose up --build
```

2. Accesos:
- Frontend: `http://localhost:4200`
- Backend: `http://localhost:8080`

3. Primer login (creado automáticamente al primer arranque):
- Usuario: `admin`
- Clave: `Admin1234!`

> Cambiá la clave inmediatamente desde la pantalla **Cambiar contraseña**.

## Variables importantes

- `JWT_SECRET`: clave del token JWT.
- `BOOTSTRAP_ADMIN_USERNAME`
- `BOOTSTRAP_ADMIN_PASSWORD`
- `BOOTSTRAP_ADMIN_FULLNAME`
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`

## Deploy en nube (siguiente paso)

- Backend: Render/Railway/Fly.io
- Frontend: Cloudflare Pages/Vercel
- DB: Neon o Supabase Postgres

Siempre usar HTTPS + una `JWT_SECRET` fuerte + contraseña inicial distinta.
