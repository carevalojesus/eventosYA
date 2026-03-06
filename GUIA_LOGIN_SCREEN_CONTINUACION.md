# Continuacion de la Guia: Login + Material 3 + UX (eventosYA)

Este documento continua la guia de `GUIA_LOGIN_SCREEN.md` con todo lo implementado hasta ahora en el proyecto, para uso de alumnos.

## 1. Objetivo de esta continuacion

Pasar de una pantalla de login base a una experiencia mas completa en Material 3:

- UX de autenticacion mas robusta.
- Tema configurable por el usuario.
- Navegacion adaptativa para distintos tamanos de pantalla.
- Mejora de accesibilidad, motion y consistencia visual.
- Estandarizacion de textos mediante recursos.

## 2. Resumen de lo implementado

### 2.1 Login y registro

- Login con email/password + Google Sign-In.
- Registro con email/password + Google Sign-In.
- Validacion inline en login (`emailError`, `passwordError`).
- Flujo de recuperacion de contrasena (envio de correo desde Firebase Auth).
- Mensajes de error e informacion via `Snackbar`.

Archivos clave:

- `app/src/main/java/com/carevalojesus/eventosya/ui/screens/login/LoginScreen.kt`
- `app/src/main/java/com/carevalojesus/eventosya/ui/screens/login/LoginViewModel.kt`
- `app/src/main/java/com/carevalojesus/eventosya/ui/screens/register/RegisterScreen.kt`
- `app/src/main/java/com/carevalojesus/eventosya/ui/screens/register/RegisterViewModel.kt`

### 2.2 Tema Material 3 y preferencias de usuario

- Esquema de colores Light y Dark.
- Variante de alto contraste (`ContrastLevel`).
- Soporte de Dynamic Color (Android 12+).
- Persistencia de preferencias de tema con DataStore:
  - `dynamicColorEnabled`
  - `highContrastEnabled`

Archivos clave:

- `app/src/main/java/com/carevalojesus/eventosya/ui/theme/Theme.kt`
- `app/src/main/java/com/carevalojesus/eventosya/data/preferences/ThemePreferences.kt`
- `app/src/main/java/com/carevalojesus/eventosya/MainActivity.kt`

### 2.3 Modulo Admin (nuevas pantallas)

- Dashboard admin con navegacion adaptativa:
  - `NavigationBar` en pantallas compactas.
  - `NavigationRail` desde ~600dp.
- Pantalla de eventos con:
  - listado,
  - detalle en dos paneles para pantallas amplias (list-detail),
  - creacion y eliminacion.
- Pantalla de tickets/ordenes con estados vacio/cargando/lista.
- Perfil admin con toggles de tema (Dynamic Color / Alto contraste) y lectura de rol real desde Firestore.

Archivos clave:

- `app/src/main/java/com/carevalojesus/eventosya/ui/screens/admin/AdminNavigation.kt`
- `app/src/main/java/com/carevalojesus/eventosya/ui/screens/admin/AdminHomeScreen.kt`
- `app/src/main/java/com/carevalojesus/eventosya/ui/screens/admin/AdminHomeViewModel.kt`
- `app/src/main/java/com/carevalojesus/eventosya/ui/screens/admin/CreateEventScreen.kt`
- `app/src/main/java/com/carevalojesus/eventosya/ui/screens/admin/CreateEventViewModel.kt`
- `app/src/main/java/com/carevalojesus/eventosya/ui/screens/admin/AdminTicketsScreen.kt`
- `app/src/main/java/com/carevalojesus/eventosya/ui/screens/admin/AdminTicketsViewModel.kt`
- `app/src/main/java/com/carevalojesus/eventosya/ui/screens/admin/AdminProfileScreen.kt`

### 2.4 Motion y estados

- Transicion entre pantallas principales con `AnimatedContent`.
- Transiciones en pantallas admin para cambios de estado (`loading`, `empty`, `content`).
- `Crossfade` para detalle de evento seleccionado en layout de dos paneles.

### 2.5 Accesibilidad y coherencia UX

- Encabezados semanticos en login/registro.
- Evitamos affordances enganosos: estados ahora usan componentes visuales no clickeables cuando corresponde.
- Mejor jerarquia tonal en superficies (TopAppBar, cards, FAB tonal).

### 2.6 Internacionalizacion (i18n)

- Textos principales migrados a `strings.xml`.
- Menos hardcodeo de textos en composables.

Archivo clave:

- `app/src/main/res/values/strings.xml`

## 3. Dependencias agregadas

En `app/build.gradle.kts` se incorporaron, entre otras:

- `androidx.datastore:datastore-preferences`
- `androidx.compose.material:material-icons-core`
- librerias Material 3 adaptive

## 4. Estructura de datos incorporada

Se anadieron modelos para separar mejor responsabilidades:

- `app/src/main/java/com/carevalojesus/eventosya/data/model/User.kt`
- `app/src/main/java/com/carevalojesus/eventosya/data/model/Event.kt`

## 5. Verificacion tecnica ejecutada

Se validaron builds y tests:

- `./gradlew :app:assembleDebug` -> OK
- `./gradlew :app:testDebugUnitTest` -> OK

## 6. Pendiente conocido

- Navegacion a editar evento desde `MainActivity` sigue como `TODO`.

## 7. Recomendaciones didacticas para alumnos

1. Leer primero `GUIA_LOGIN_SCREEN.md` y luego este documento.
2. Revisar `MainActivity.kt` para entender estado global + tema + transiciones.
3. Revisar `LoginViewModel.kt` para validar formularios y flujo de recuperacion.
4. Revisar `AdminNavigation.kt` y `AdminHomeScreen.kt` para patrones adaptativos.
5. Practicar extrayendo strings hardcodeados restantes a `strings.xml`.

## 8. Siguiente extension sugerida para clase

- Implementar pantalla de edicion de eventos.
- Agregar tests UI de Compose para login/registro/admin.
- Aplicar manejo de errores de red mas granular por casos de Firebase.
