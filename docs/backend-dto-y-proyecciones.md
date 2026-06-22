# Regla Backend: DTOs y Proyecciones

## Regla principal
- Las lecturas (`GET`, búsquedas, listados, tableros, defaults de formularios, reportes) no deben cargar entidades completas si alcanza con una proyección.
- Los controladores deben devolver DTOs, nunca entidades JPA.
- Cada endpoint debe devolver solo los campos que realmente consume la pantalla o acción que lo llama.

## Cómo aplicarlo
- Para listados o tableros:
  usar interfaces `*View` en `model/repository/projection`.
- Para detalle editable:
  crear una proyección específica del caso de uso y mapearla a un DTO.
- Para updates:
  se puede persistir con entidad, pero evitar lecturas completas previas si alcanza con:
  - una proyección mínima, o
  - `getReferenceById(...)` después de obtener solo el `id`.

## Checklist antes de cerrar una feature
- El endpoint devuelve DTO y no entidad.
- La query de lectura usa proyección dedicada.
- El payload no incluye campos que la UI no usa.
- Si la pantalla muestra resumen, no reutilizar el DTO de detalle completo.
- Si una vista necesita datos extra, crear un DTO/proyección nueva en lugar de “aprovechar” una entidad o un DTO más grande.

## Ejemplo de criterio
- Correcto:
  `status-board` devuelve solo lo que la columna necesita.
- Correcto:
  el modal de reporte pide un DTO específico del reporte.
- Incorrecto:
  cargar `Repair`, `Client` y `Device` completos solo para armar un resumen de 8 campos.
