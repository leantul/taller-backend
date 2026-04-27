# Taller de reparación - Requisitos cerrados y arquitectura propuesta (MVP)

## 1) Decisiones funcionales confirmadas

- Uso: una sola sucursal, un único usuario administrador.
- Seguridad: login obligatorio con JWT + refresh token.
- Idioma: español.
- Módulos: clientes, dispositivos, reparaciones, dashboard y notificaciones.
- Flujo de estados de reparación:
  1. Por recibir
  2. Recibido
  3. Presupuestado y esperando respuesta
  4. Haciendo
  5. Esperando para retirar
  6. Retirado
- Archivos adjuntos en reparaciones.
- Compartir comprobante por WhatsApp (mediante enlace prearmado).
- Clientes:
  - Obligatorio: nombre, apellido, DNI, mail, celular
  - Opcional: fecha de nacimiento, dirección
  - Múltiples teléfonos y múltiples emails
  - Solo personas físicas
  - Observaciones internas
  - Notificación interna por los próximos 5 cumpleaños
- Dispositivos:
  - Categorías cerradas: desktop, notebook, tablet, celular, otros
  - Campos: marca, modelo, serie/IMEI, contraseña, accesorios recibidos, estado estético
  - Un dispositivo pertenece a un solo cliente
  - Historial completo por dispositivo
- Reparaciones:
  - Precio final = mano de obra + repuestos + extras
  - Presupuesto con aprobación/rechazo
  - Alertas de no retirado a los 3 días de avisado
  - Múltiples repuestos por reparación (cantidad, proveedor)
  - Guardar costo y precio de venta de cada repuesto
  - Pagos parciales y señas
  - Moneda principal: ARS (dejar preparado para USD)
- Dashboard:
  - Recaudación, costos, ganancia del mes calendario
  - Consulta por rango personalizable en módulo financiero
  - Top 5 clientes que hace más no vienen (por última reparación finalizada)
- Notificaciones:
  - Recordatorio comercial 30 días antes del año de la reparación
  - Cumpleaños: notificación interna diaria
  - Centro de notificaciones leído/no leído
- UX:
  - Búsqueda global (cliente, serie/IMEI, orden, teléfono, equipo)
  - Todas las tablas con paginación y ordenamiento
  - Responsive y accesible
  - Modo offline parcial
- Auditoría: bitácora de cambios.
- Eliminación física (hard delete).
- Backups: sí.
- Integraciones externas: no por ahora.
- Reportes PDF/Excel: no por ahora.

## 2) Stack propuesto

### Backend
- Java 21
- Spring Boot 3.x
- Spring Security + JWT + refresh tokens
- Spring Data JPA + PostgreSQL
- Flyway para migraciones
- OpenAPI/Swagger

### Frontend
- Angular (última versión estable disponible al iniciar implementación)
- PrimeNG
- PWA para offline parcial

### Infraestructura sugerida para empezar gratis
- Frontend: Cloudflare Pages o Vercel (plan gratuito)
- Backend: Render Web Service (free) o Railway (con créditos de prueba)
- Base de datos PostgreSQL: Neon (free)

## 3) Arquitectura recomendada

Aunque se pidió microservicios, para un único usuario y primera versión conviene **monolito modular**:

- Menor complejidad operativa
- Menor costo
- Despliegue más simple
- Escalable luego a microservicios si crece

Módulos internos:
- auth
- clients
- devices
- repairs
- inventory (repuestos)
- billing/payments
- dashboard
- notifications
- audit

## 4) Entidades mínimas del dominio

- `users`
- `clients`
- `client_contacts` (múltiples emails/teléfonos)
- `devices`
- `repairs`
- `repair_status_history`
- `repair_items` (mano de obra/repuesto/extra)
- `spare_parts`
- `repair_spare_parts`
- `payments`
- `notifications`
- `audit_log`
- `attachments`

## 5) Entregables técnicos del MVP

1. API REST completa (CRUD + búsquedas + paginación/ordenamiento)
2. Login/refresh/logout seguro
3. Dashboard mensual + consultas por rango
4. Motor de alertas (cumpleaños, aniversario comercial, no retirados)
5. Historial por dispositivo y por reparación
6. Documentación OpenAPI
7. Deploy inicial listo en entorno gratuito

## 6) Pendiente menor por definir antes de implementar

- ¿Querés URL pública de demo desde el primer sprint o primero versión local/documentada?
