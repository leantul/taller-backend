# Guía de deploy (GitHub -> Web, sin levantar nada local)

Esta guía deja el sistema publicado directamente desde tu repositorio de GitHub con:

- **Backend:** Render (Spring Boot)
- **Base de datos:** Neon Postgres
- **Frontend:** Vercel (Angular estático en Nginx)

> También funciona con Railway/Fly.io para backend y Cloudflare Pages para frontend, pero este flujo es el más simple para empezar.

---

## 0) Estado actual del repo

El proyecto ya incluye:

- Seguridad JWT (`/auth/login`, `/auth/change-password`) y backend stateless.
- Usuario admin bootstrap por variables (`BOOTSTRAP_ADMIN_*`).
- Migraciones Flyway para esquema y usuarios.
- Dockerfile backend + Dockerfile frontend.
- `docker-compose.yml` para entorno local.
- Frontend configurable por variables en runtime (`API_URL`, `AUTH_URL`) para deploy web.
- CORS configurable en backend por `APP_CORS_ALLOWED_ORIGINS`.

---

## 1) Subir cambios a GitHub

Desde tu máquina:

```bash
git add .
git commit -m "chore: preparar deploy cloud con config runtime frontend y CORS"
git push origin work
```

Si desplegás desde otra rama (ej. `main`), hace merge primero.

---

## 2) Crear base de datos en Neon

1. Entrá a Neon y creá un proyecto.
2. Creá una database (por ejemplo `tallerdb`).
3. Copiá los datos de conexión:
   - host
   - puerto (`5432`)
   - database
   - usuario
   - password
4. Armá esta URL JDBC para Spring:

```text
jdbc:postgresql://<HOST>:5432/<DATABASE>?sslmode=require
```

Guardá estos 3 valores porque los usarás en Render:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

---

## 3) Deploy del backend en Render

1. En Render: **New +** -> **Web Service**.
2. Conectá el repo de GitHub `taller-backend`.
3. Configuración:
   - **Runtime:** Docker
   - **Branch:** la que querés publicar (ej. `main` o `work`)
   - **Region:** la más cercana a tus usuarios
4. Variables de entorno (Environment):

```text
DB_URL=jdbc:postgresql://<HOST>:5432/<DATABASE>?sslmode=require
DB_USERNAME=<USUARIO_NEON>
DB_PASSWORD=<PASSWORD_NEON>
JWT_SECRET=<una_clave_larga_y_unica>
JWT_EXPIRATION_MINUTES=720
BOOTSTRAP_ADMIN_USERNAME=admin
BOOTSTRAP_ADMIN_PASSWORD=<clave_fuerte_inicial>
BOOTSTRAP_ADMIN_FULLNAME=Administrador Taller
APP_CORS_ALLOWED_ORIGINS=https://<tu-frontend>.vercel.app
```

5. Deploy.
6. Cuando termine, probá salud:
   - `https://<tu-backend>.onrender.com/actuator/health`

Si responde UP (o status OK), backend listo.

---

## 4) Deploy del frontend en Vercel

1. En Vercel: **Add New -> Project**.
2. Importá el mismo repo.
3. **Root directory:** `frontend`
4. Usá build por Docker del repo (el Dockerfile de `frontend`).
5. Variables de entorno del frontend:

```text
API_URL=https://<tu-backend>.onrender.com
AUTH_URL=https://<tu-backend>.onrender.com/auth
```

6. Deploy.

El contenedor frontend genera `assets/env.js` al iniciar con esas variables.

---

## 5) Cerrar el circuito CORS

Cuando tengas la URL final del frontend (Vercel), asegurate de que en Render:

```text
APP_CORS_ALLOWED_ORIGINS=https://<tu-frontend>.vercel.app
```

Si usás más de un dominio (preview + producción), separados por coma:

```text
APP_CORS_ALLOWED_ORIGINS=https://<prod>.vercel.app,https://<preview>.vercel.app
```

Re-deploy del backend después de cambiar variables.

---

## 6) Primer login y seguridad

1. Entrá al frontend publicado.
2. Login con:
   - usuario: valor de `BOOTSTRAP_ADMIN_USERNAME`
   - clave: valor de `BOOTSTRAP_ADMIN_PASSWORD`
3. Cambiá la contraseña desde **Cambiar contraseña** apenas ingreses.
4. Recomendado: luego desactivar bootstrap cambiando esas variables a valores nuevos/seguros.

---

## 7) ¿Cómo conectar si tu DB está “en otro lado”?

Es igual para cualquier proveedor PostgreSQL (Supabase, RDS, Railway, etc.):

1. Conseguí host, puerto, db, user, password.
2. Armá `DB_URL` con formato JDBC:

```text
jdbc:postgresql://HOST:PUERTO/DB?sslmode=require
```

3. Cargá `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` en Render.
4. Confirmá que el proveedor permite conexiones externas desde internet.
5. Re-deploy del backend.

Flyway va a crear/actualizar tablas automáticamente al iniciar.

---

## 8) Checklist rápido de producción

- `JWT_SECRET` largo y único.
- Password inicial admin fuerte.
- CORS apuntando solo a tu frontend real.
- Backend y frontend en HTTPS.
- No exponer credenciales en el repo.
- Configurar backups en el proveedor de DB.

---

## 9) Troubleshooting express

- **401 en endpoints protegidos:** revisar login y token en requests.
- **CORS error en navegador:** revisar `APP_CORS_ALLOWED_ORIGINS`.
- **Backend no conecta DB:** revisar `DB_URL`, SSL y credenciales.
- **Frontend pega a localhost:** revisar variables `API_URL` y `AUTH_URL` en Vercel.

