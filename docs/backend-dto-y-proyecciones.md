# Reglas de Implementacion del Proyecto

## Alcance
- Estas reglas aplican a backend, frontend y flujo de trabajo dentro de este repositorio.
- Para cambios futuros, deben tomarse como criterio por defecto salvo que haya una decision explicita en contrario.

## Backend: DTOs y proyecciones
- Las lecturas (`GET`, busquedas, listados, tableros, defaults de formularios, reportes) no deben cargar entidades completas si alcanza con una proyeccion.
- Los controladores deben devolver DTOs, nunca entidades JPA.
- Cada endpoint debe devolver solo los campos que realmente consume la pantalla o accion que lo llama.

## Backend: eficiencia de acceso a datos
- Se debe minimizar la cantidad de llamadas a la base de datos en cada caso de uso.
- Antes de agregar una query nueva, revisar si el caso puede resolverse con una sola lectura dedicada.
- Evitar lecturas completas previas si alcanza con:
  - una proyeccion minima, o
  - `getReferenceById(...)` despues de obtener solo el `id`.
- Si una pantalla necesita resumen, no reutilizar DTOs o queries de detalle completo.

## Como aplicarlo
- Para listados o tableros:
  usar interfaces `*View` en `model/repository/projection`.
- Para detalle editable:
  crear una proyeccion especifica del caso de uso y mapearla a un DTO.
- Para updates:
  persistir con entidad, pero con la menor cantidad posible de lecturas previas.
- Si una vista necesita datos extra:
  crear un DTO/proyeccion nueva en lugar de "aprovechar" una entidad o un DTO mas grande.

## Frontend: grillas
- Toda grilla nueva debe permitir ordenar por titulo de columna.
- Toda grilla nueva debe permitir modificar el ancho de sus columnas.
- Si una grilla no cumple estas dos reglas, debe documentarse la razon funcional o tecnica.

## Commits
- Los mensajes de commit deben escribirse en espanol.
- Deben describir el cambio principal con claridad y sin mezclar temas no relacionados.

## Checklist antes de cerrar una feature
- El endpoint devuelve DTO y no entidad.
- La query de lectura usa proyeccion dedicada.
- El payload no incluye campos que la UI no usa.
- La solucion minimiza la cantidad de llamadas a la base.
- Las grillas nuevas permiten orden por columna.
- Las grillas nuevas permiten resize de columnas.
- El mensaje de commit esta escrito en espanol.

## Ejemplos de criterio
- Correcto:
  `status-board` devuelve solo lo que la columna necesita.
- Correcto:
  el modal de reporte pide un DTO especifico del reporte.
- Correcto:
  una grilla de reparaciones permite ordenar por encabezado y ajustar anchos.
- Incorrecto:
  cargar `Repair`, `Client` y `Device` completos solo para armar un resumen de 8 campos.
- Incorrecto:
  devolver al frontend campos que no se usan "por las dudas".
