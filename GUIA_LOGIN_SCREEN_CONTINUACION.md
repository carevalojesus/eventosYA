# Guia paso a paso (Continuacion): UX Material 3 + Admin + Persistencia de Tema

Esta guia continua `GUIA_LOGIN_SCREEN.md` y explica, paso a paso, todo lo nuevo implementado hasta ahora.

## Tabla de contenido

1. [Prerequisitos para esta continuacion](#1-prerequisitos-para-esta-continuacion)
2. [Estructura final del proyecto (modulos nuevos)](#2-estructura-final-del-proyecto-modulos-nuevos)
3. [Paso 1: Mejorar UX de Login (validacion inline + recuperar contrasena)](#paso-1-mejorar-ux-de-login-validacion-inline--recuperar-contrasena)
4. [Paso 2: Persistir preferencias de tema con DataStore](#paso-2-persistir-preferencias-de-tema-con-datastore)
5. [Paso 3: Integrar preferencias en MainActivity + Theme](#paso-3-integrar-preferencias-en-mainactivity--theme)
6. [Paso 4: Navegacion adaptativa Admin (mobile y tablet)](#paso-4-navegacion-adaptativa-admin-mobile-y-tablet)
7. [Paso 5: Pantalla de eventos admin con list-detail y estados](#paso-5-pantalla-de-eventos-admin-con-list-detail-y-estados)
8. [Paso 6: Perfil admin (rol real + toggles de tema)](#paso-6-perfil-admin-rol-real--toggles-de-tema)
9. [Paso 7: Pantalla de tickets con estados y motion](#paso-7-pantalla-de-tickets-con-estados-y-motion)
10. [Paso 8: Internacionalizacion de textos](#paso-8-internacionalizacion-de-textos)
11. [Conceptos clave explicados](#conceptos-clave-explicados)
12. [Errores comunes y soluciones](#errores-comunes-y-soluciones)
13. [Checklist final para alumnos](#checklist-final-para-alumnos)

---

## 1. Prerequisitos para esta continuacion

Antes de seguir, debes tener listo lo de la primera guia (`GUIA_LOGIN_SCREEN.md`):

- Firebase Auth funcionando.
- Login/registro en Compose.
- Tema Material 3 base (`Color.kt`, `Theme.kt`, `Type.kt`).
- Proyecto compilando correctamente.

Adicional para esta parte:

- Conocer `State` y `ViewModel` en Compose.
- Entender `Flow` basico (para DataStore).
- Tener Firestore habilitado para leer rol de usuario.

---

## 2. Estructura final del proyecto (modulos nuevos)

La continuacion agrega estas piezas importantes:

```
app/src/main/java/com/carevalojesus/eventosya/
├── data/
│   ├── model/
│   │   ├── User.kt
│   │   └── Event.kt
│   └── preferences/
│       └── ThemePreferences.kt
├── ui/
│   └── screens/
│       ├── login/
│       │   ├── LoginScreen.kt        (validacion inline + forgot password)
│       │   └── LoginViewModel.kt
│       ├── register/
│       │   ├── RegisterScreen.kt     (textos por resources)
│       │   └── RegisterViewModel.kt
│       └── admin/
│           ├── AdminNavigation.kt     (adaptativo: bottom bar / rail)
│           ├── AdminHomeScreen.kt     (list-detail + estados)
│           ├── AdminHomeViewModel.kt
│           ├── AdminTicketsScreen.kt
│           ├── AdminTicketsViewModel.kt
│           ├── AdminProfileScreen.kt  (rol real + toggles tema)
│           ├── CreateEventScreen.kt
│           └── CreateEventViewModel.kt
└── MainActivity.kt                    (tema persistente + transiciones)

app/src/main/res/values/
└── strings.xml                        (i18n centralizada)
```

---

## Paso 1: Mejorar UX de Login (validacion inline + recuperar contrasena)

### 1.1 Extender el estado de UI

En `LoginUiState` se agregan errores por campo e informacion:

```kotlin
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val isLoginSuccess: Boolean = false,
    val userRole: String? = null
)
```

Por que: ahora la UI puede mostrar feedback exacto en cada campo y no solo un snackbar generico.

### 1.2 Validar antes de hacer sign-in

`loginWithEmail()` valida:

- correo vacio,
- formato de correo,
- password vacia.

Si falla, no llama Firebase y marca `emailError/passwordError`.

### 1.3 Recuperar contrasena

Se agrega `sendPasswordReset()`:

```kotlin
auth.sendPasswordResetEmail(email).await()
```

Y en `LoginScreen`:

```kotlin
TextButton(onClick = viewModel::sendPasswordReset)
```

### 1.4 Mostrar errores inline en TextField

En `OutlinedTextField`:

```kotlin
isError = state.emailError != null,
supportingText = {
    state.emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
}
```

Esto sigue la practica M3 de feedback de formulario cercano al input.

---

## Paso 2: Persistir preferencias de tema con DataStore

### 2.1 Crear `ThemePreferences.kt`

Se define un DataStore de preferencias con 2 claves booleanas:

- `dynamic_color_enabled`
- `high_contrast_enabled`

Y se exponen:

- `Flow<Boolean>` para lectura reactiva,
- funciones `set...` para escritura.

### 2.2 Agregar dependencia

En `app/build.gradle.kts`:

```kotlin
implementation("androidx.datastore:datastore-preferences:1.1.1")
```

---

## Paso 3: Integrar preferencias en MainActivity + Theme

### 3.1 Leer preferencias como estado Compose

En `MainActivity.kt`:

```kotlin
val preferences = remember { ThemePreferences(context.applicationContext) }
val dynamicColorEnabled by preferences.dynamicColorEnabled.collectAsState(initial = false)
val highContrastEnabled by preferences.highContrastEnabled.collectAsState(initial = false)
```

### 3.2 Aplicarlas al tema

```kotlin
EventosYATheme(
    dynamicColor = dynamicColorEnabled,
    contrastLevel = if (highContrastEnabled) ContrastLevel.High else ContrastLevel.Default,
)
```

### 3.3 Guardar cambios de toggles

```kotlin
onDynamicColorChange = { enabled ->
    scope.launch { preferences.setDynamicColorEnabled(enabled) }
}
```

Resultado: al cerrar/reabrir app, el tema se conserva.

---

## Paso 4: Navegacion adaptativa Admin (mobile y tablet)

En `AdminNavigation.kt` se usa `BoxWithConstraints` para elegir navegacion segun ancho:

- `maxWidth < 600.dp` -> `NavigationBar` (bottom).
- `maxWidth >= 600.dp` -> `NavigationRail` (lateral).

```kotlin
val useRail = maxWidth >= 600.dp
```

Por que: mejora uso en tablets y desktop-like sin duplicar pantallas.

---

## Paso 5: Pantalla de eventos admin con list-detail y estados

`AdminHomeScreen.kt` ahora cubre:

1. `loading`
2. `empty`
3. `content`

con `AnimatedContent` para transicion suave.

### 5.1 Pattern list-detail para ancho grande

En `>= 840.dp`:

- panel izquierdo: lista de eventos,
- panel derecho: detalle del evento seleccionado (`EventDetailPane`).

Para mobile se mantiene lista tradicional.

### 5.2 Corregir affordance enganoso

Antes: `SuggestionChip(onClick = {})` parecia clickeable sin accion real.

Ahora: estado se representa con `Surface + Text` (componente visual, no interactivo).

---

## Paso 6: Perfil admin (rol real + toggles de tema)

`AdminProfileScreen.kt`:

- Lee usuario actual de Firebase Auth.
- Consulta rol real desde Firestore.
- Muestra switches para `Dynamic Color` y `Alto contraste`.

Ejemplo de lectura de rol:

```kotlin
val role = FirebaseFirestore.getInstance()
    .collection("users")
    .document(uid)
    .get()
    .await()
    .getString("role")
```

Con esto evitamos mostrar un rol fijo hardcodeado.

---

## Paso 7: Pantalla de tickets con estados y motion

`AdminTicketsScreen.kt` usa `AnimatedContent` para transicionar entre:

- cargando,
- sin ordenes,
- lista de ordenes.

Ademas, los textos ya salen de `strings.xml`.

---

## Paso 8: Internacionalizacion de textos

Se migro gran parte de textos a `app/src/main/res/values/strings.xml`.

Ejemplo:

```kotlin
Text(stringResource(R.string.login_subtitle))
```

Beneficios:

- facil traduccion futura,
- consistencia de copy,
- menos hardcodeo repetido.

---

## Conceptos clave explicados

### `collectAsState()`
Convierte `Flow` en estado de Compose reactivo.

### `AnimatedContent` y `Crossfade`
Animan cambio de estado/pantalla sin transiciones bruscas.

### `BoxWithConstraints`
Permite decisiones de layout segun el tamano disponible.

### `isError` + `supportingText` en `OutlinedTextField`
Patron M3 recomendado para errores de formulario.

### DataStore vs SharedPreferences
DataStore es asincrono, con Flow, y mas seguro para Compose moderno.

---

## Errores comunes y soluciones

1. **No compila por imports de Compose runtime**
   - Verifica `import androidx.compose.runtime.setValue` / `getValue` / `mutableStateOf`.

2. **Dynamic Color no se refleja**
   - Solo aplica completo en Android 12+.
   - Verifica que no este activado alto contraste (en este proyecto, alto contraste tiene prioridad).

3. **Forgot password no envia correo**
   - Valida email correcto.
   - Revisa que Firebase Auth Email/Password este habilitado.

4. **No aparece rol en perfil**
   - Revisa documento `users/{uid}` en Firestore y campo `role`.

5. **Textos siguen hardcodeados**
   - Busca con `rg 'Text\("' app/src/main/java` y migra a `stringResource`.

---

## Checklist final para alumnos

- [ ] Login con validacion inline funcionando.
- [ ] Recuperacion de contrasena funcionando.
- [ ] Dynamic Color y alto contraste persisten al reiniciar app.
- [ ] Navegacion admin cambia entre bottom bar y rail segun ancho.
- [ ] Home admin usa list-detail en pantallas grandes.
- [ ] Perfil muestra rol real de Firestore.
- [ ] Pantallas principales usan strings en recursos.
- [ ] `assembleDebug` y `testDebugUnitTest` en verde.

---

## Proxima practica recomendada

1. Implementar pantalla de editar evento (el TODO pendiente).
2. Agregar tests UI de Compose para login/admin.
3. Crear guia de "Staff + escaneo QR" como siguiente modulo.
