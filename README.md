# Práctica Calificada 02 — NASA

Aplicación Android existente en Kotlin y Jetpack Compose. minSdk 34; compile/target SDK 37.

## Ejecutar

1. Abrir este proyecto en Android Studio con SDK 37 y el JDK indicado por Gradle (25).
2. En `local.properties` conservar `sdk.dir` y agregar `NASA_API_KEY=tu_clave_personal`.
3. Obtener una clave propia en https://api.nasa.gov/. No usar la clave de demostración.
4. Sincronizar Gradle y ejecutar `./gradlew :app:assembleDebug`.

`local.properties` está excluido de Git. La clave se inyecta en `BuildConfig.NASA_API_KEY`; nunca incluir claves ni APK en commits. Sin clave se puede compilar, pero las consultas remotas mostrarán un mensaje de configuración.

## Equipo

- Alumno 1 (`ANIM-OS`): base compartida, navegación principal, Retrofit, Room, Gradle y módulo `apod/`.
- Alumno 2 (`brayan-ht`): módulo `asteroides/` en sus propios archivos.
- Contrato propuesto y puntos de integración: [docs/INTEGRACION.md](docs/INTEGRACION.md).

Trabajar mediante `setup/base` y `feature/imagenes`, con PR hacia `main`. No hacer commits directos sobre `main`.


## Imágenes APOD

- Al abrir Imágenes se consulta `GET planetary/apod` desde hace seis días hasta hoy, según la fecha local del dispositivo.
- El Repository convierte DTO → Entity y guarda en Room. Lista, detalle y favoritos observan exclusivamente la base local mediante Flows y un ViewModel con StateFlow.
- La lista se ordena por fecha descendente. Los videos muestran «Contenido en video» y nunca se envían a Coil.
- Tocar una tarjeta abre el detalle con explicación completa, autor opcional e imagen HD (`hdurl` o `url`).
- La estrella permite agregar/quitar favoritos en lista y detalle. «Favoritas» abre «Mis imágenes favoritas», incluyendo favoritos anteriores a los siete días.
- Actualizar la API conserva las marcas locales. Sin conexión, los datos ya guardados y los favoritos siguen disponibles.
- Hay estados de carga, contenido vacío, errores de red y reemplazos para campos ausentes e imágenes no disponibles. Registros sin fecha válida se omiten.

### Estructura

- `apod/data/remote/ApodApi.kt`: endpoint Retrofit.
- `apod/data/remote/ApodDto.kt`: DTO nullable y conversión explícita a Entity.
- `apod/data/ApodRepository.kt`: sincronización y lectura desde Room.
- `apod/data/local/`: Entity y DAO.
- `apod/ui/ApodViewModel.kt`: StateFlow, actualización y favoritos.
- `apod/ui/ApodScreens.kt`: lista, detalle y favoritos en Compose.
- `MainActivity.kt`: inyección manual y conexión con las rutas compartidas.

### Comprobar

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

Las pruebas unitarias de APOD cubren fechas, campos ausentes, respuestas incompletas, errores de red, cancelación y conservación de favoritos con un DAO de prueba. Para revisar visualmente: ejecutar en un dispositivo API 34+, abrir Imágenes, marcar una estrella, abrir el detalle y Favoritas; comprobar que se puede quitar allí y que el estado cambia al volver. Repetir sin conexión para comprobar la lectura local.

La base y APOD se entregan en PR separados (`setup/base` y `feature/imagenes`) hacia `main`. Integrar primero la base. El otro integrante debe comentar y aprobar la revisión; no fusionar sin esa revisión. Los nombres completos del equipo deben completarse antes de la entrega académica.
