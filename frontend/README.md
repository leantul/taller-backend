# Frontend (Angular)

Base del frontend para la release `2.0.0`.

## Comandos locales

```bash
npm install
npm run start
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

- Usuario: `admin`
- Clave: `Admin1234!`
- Cambiar clave luego del ingreso inicial en `/cambiar-password`.

## Módulos ya creados

- Dashboard
- Clientes
- Dispositivos
- Reparaciones
- Notificaciones
