# Versionado por PR

La version funcional de la app se arma con este formato:

`<baseVersion>.<numeroDePR>`

Ejemplo:

- `1.0.0.60`

## Como cambiar la base

Edita el archivo raiz `app-version.json` y cambia `baseVersion`.

Ejemplo:

```json
{
  "baseVersion": "1.2.0"
}
```

Con eso, el siguiente PR quedaria como `1.2.0.<numeroDePR>`.

## Como se genera

- El script `frontend/scripts/write-version.mjs` toma `baseVersion` desde `app-version.json`.
- Si el build corre dentro de un PR de GitHub, usa el numero real del PR como cuarto segmento.
- Si corre localmente, usa `0`, por ejemplo `1.0.0.0`.

El resultado se escribe en:

- `frontend/src/assets/version.json`

## Workflow

El workflow `.github/workflows/pr-version.yml`:

- se ejecuta en cada PR
- genera la version
- deja el valor en el resumen del job
- sube `version.json` como artifact

## Recomendacion

Te conviene que vos manejes solo `major.minor.patch` en `app-version.json` y que el cuarto numero quede automatizado por PR. Asi no se pisan cambios ni hace falta editar versiones manualmente para cada rama.
