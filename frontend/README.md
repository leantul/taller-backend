# Frontend (Angular)

Base del frontend para la release `2.0.0`.

## Comandos locales

```bash
npm install
npm run start
```

`npm run start` levanta Angular con `proxy.conf.cjs`. En `localhost` el frontend usa rutas relativas (`/auth`, `/client`, `/repair`, etc.) y el proxy las reenvía al backend remoto configurado en ese archivo. Esto evita CORS durante desarrollo local sin levantar backend ni base local.

Si necesitás probar sin proxy, usá:

```bash
npm run start:direct
```

## Configuración de backend (importante)

El frontend usa `assets/env.js` para definir a qué backend conectarse:

- `API_URL`
- `AUTH_URL`

Valores por defecto (desarrollo):

- `http://localhost:8080`
- `http://localhost:8080/auth`

En Docker/Nginx se inyectan por variables de entorno del contenedor.

## Primer acceso

El usuario inicial se define desde las variables del backend (`BOOTSTRAP_ADMIN_USERNAME`, `BOOTSTRAP_ADMIN_PASSWORD`, `BOOTSTRAP_ADMIN_FULLNAME`). Cambiar la clave luego del ingreso inicial en `/cambiar-password`.

## Módulos ya creados

- Dashboard
- Clientes
- Dispositivos
- Reparaciones
- Notificaciones
