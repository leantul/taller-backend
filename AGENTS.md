# Instrucciones para agentes del repositorio

Estas reglas deben tomarse como criterio por defecto en todo cambio dentro de este proyecto.

## Backend
- En lecturas (`GET`, busquedas, listados, tableros, defaults de formularios, reportes), usar DTOs y proyecciones dedicadas.
- No devolver entidades JPA desde controladores.
- Cada endpoint debe devolver solo los campos que la pantalla o accion consume realmente.
- Minimizar la cantidad de llamadas a la base de datos en cada caso de uso.
- Evitar lecturas completas previas si alcanza con una proyeccion minima o con `getReferenceById(...)` despues de obtener solo el `id`.
- No reutilizar DTOs de detalle completo para vistas de resumen.

## Frontend
- Toda grilla nueva debe permitir ordenar por titulo de columna.
- Toda grilla nueva debe permitir modificar el ancho de sus columnas.
- Si por una razon funcional o tecnica una grilla no puede cumplir eso, debe quedar explicitado en la entrega.

## Commits
- Los mensajes de commit deben escribirse en espanol.
- Cada commit debe describir con claridad el cambio principal y evitar mezclar temas no relacionados.

## Criterio general
- Respetar los patrones ya existentes del proyecto antes de introducir una nueva abstraccion.
- Priorizar payloads chicos, queries dedicadas y cambios acotados al caso de uso.
