# Práctica Calificada 02 — NASA

Aplicación Android existente en Kotlin y Jetpack Compose. minSdk 34; compile/target SDK 37.

## Ejecutar

1. Abrir este proyecto en Android Studio con SDK 37 y el JDK indicado por Gradle (25).
2. En `local.properties` conservar `sdk.dir` y agregar `NASA_API_KEY=tu_clave_personal`.
3. Obtener una clave propia en https://api.nasa.gov/. No usar la clave de demostración.
4. Sincronizar Gradle y ejecutar `./gradlew :app:assembleDebug`.

`local.properties` está excluido de Git. La clave se inyecta en `BuildConfig.NASA_API_KEY`; nunca incluir claves ni APK en commits. Sin clave se puede compilar, pero las consultas remotas mostrarán un mensaje de configuración.

## Equipo

- Integrante 1: base compartida, navegación principal, Retrofit, Room, Gradle y módulo `apod/`.
- Integrante 2: módulo `asteroides/` en sus propios archivos.
- Contrato propuesto y puntos de integración: [docs/INTEGRACION.md](docs/INTEGRACION.md).

Trabajar mediante `setup/base` y `feature/imagenes`, con PR hacia `main`. No hacer commits directos sobre `main`.
