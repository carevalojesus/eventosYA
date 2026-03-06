# Guia paso a paso: Pantalla de Login con Firebase Auth y Google Sign-In

## Tabla de contenido

1. [Prerequisitos](#1-prerequisitos)
2. [Estructura del proyecto](#2-estructura-del-proyecto)
3. [Paso 1: Configurar el tema Material 3](#paso-1-configurar-el-tema-material-3)
4. [Paso 2: Agregar dependencias](#paso-2-agregar-dependencias)
5. [Paso 3: Configurar AndroidManifest](#paso-3-configurar-androidmanifest)
6. [Paso 4: Crear el LoginViewModel](#paso-4-crear-el-loginviewmodel)
7. [Paso 5: Crear el LoginScreen](#paso-5-crear-el-loginscreen)
8. [Paso 6: Conectar todo en MainActivity](#paso-6-conectar-todo-en-mainactivity)
9. [Paso 7: Configurar Firebase Console](#paso-7-configurar-firebase-console)
10. [Conceptos clave explicados](#conceptos-clave-explicados)
11. [Errores comunes y soluciones](#errores-comunes-y-soluciones)

---

## 1. Prerequisitos

Antes de comenzar, asegurate de tener:

- Android Studio instalado (version mas reciente recomendada)
- Un proyecto Android con Jetpack Compose habilitado
- Firebase configurado en el proyecto
- El archivo `google-services.json` descargado desde Firebase Console y ubicado **dentro de la carpeta `app/`** (no en la raiz del proyecto)
- Plugin `com.google.gms.google-services` aplicado en Gradle

> **Importante:** El plugin `google-services` genera automaticamente el `default_web_client_id` que se usa para Google Sign-In. No necesitas configurarlo manualmente en `strings.xml`.

---

## 2. Estructura del proyecto

Al finalizar esta guia, la estructura de archivos sera:

```
app/src/main/
├── java/com/tupackage/eventosya/
│   ├── MainActivity.kt                         ← Punto de entrada
│   └── ui/
│       ├── screens/
│       │   └── login/
│       │       ├── LoginScreen.kt               ← UI de la pantalla
│       │       └── LoginViewModel.kt            ← Logica de negocio
│       └── theme/
│           ├── Color.kt                         ← Paleta de colores
│           ├── Theme.kt                         ← Configuracion del tema
│           └── Type.kt                          ← Tipografia
├── res/
│   ├── drawable/
│   │   └── isotipo.png                          ← Logo de la app
│   ├── values/
│   │   ├── strings.xml                          ← Web Client ID de Google
│   │   └── font_certs.xml                       ← Certificados para Google Fonts
│   └── ...
```

> **Nota:** Crea la carpeta `ui/screens/login/` manualmente desde Android Studio haciendo clic derecho en el paquete `ui` > New > Package.

---

## Paso 1: Configurar el tema Material 3

### 1.1 Color.kt — La paleta de colores

Este archivo define **todos los colores** que usara la app. Fueron generados desde [Material Theme Builder](https://m3.material.io/theme-builder).

**Ubicacion:** `ui/theme/Color.kt`

```kotlin
package com.carevalojesus.eventosya.ui.theme

import androidx.compose.ui.graphics.Color

// ===== TEMA CLARO (Light) =====
val primaryLight = Color(0xFF4B5C92)         // Color principal de la marca
val onPrimaryLight = Color(0xFFFFFFFF)       // Texto/iconos SOBRE el color primary
val primaryContainerLight = Color(0xFFDBE1FF) // Fondo de contenedores con enfasis
val onPrimaryContainerLight = Color(0xFF334478)
// ... (el archivo completo contiene ~200 colores para Light, Dark y variantes de contraste)
```

**Conceptos importantes:**

| Prefijo | Significado |
|---------|-------------|
| `primary` | Color principal de la marca |
| `onPrimary` | Color del texto/iconos que van ENCIMA del primary |
| `primaryContainer` | Version suave del primary para fondos |
| `secondary` | Color de apoyo |
| `surface` | Color de fondo de tarjetas y superficies |
| `error` | Color para estados de error |
| `Light` / `Dark` | Variante para tema claro u oscuro |

> Material 3 usa el sistema **"on"**: si el fondo es `primary`, el texto debe ser `onPrimary` para garantizar contraste y accesibilidad.

### 1.2 Theme.kt — Configuracion del tema

Este archivo **asigna los colores a los roles** de Material 3 y crea la funcion composable que envuelve toda la app.

**Ubicacion:** `ui/theme/Theme.kt`

```kotlin
package com.carevalojesus.eventosya.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
```

**El esquema de colores claro** usa `lightColorScheme()` y mapea cada color:

```kotlin
private val lightScheme = lightColorScheme(
    primary = primaryLight,                    // Botones principales, AppBar
    onPrimary = onPrimaryLight,                // Texto dentro de botones principales
    primaryContainer = primaryContainerLight,  // Chips, FABs tonales
    // ... todos los roles de color
    surfaceContainerHighest = surfaceContainerHighestLight,
)
```

**El esquema oscuro** hace lo mismo con los colores `Dark`:

```kotlin
private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    // ... mismos roles, diferentes valores
)
```

**La funcion del tema** decide que esquema usar:

```kotlin
@Composable
fun EventosYATheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Detecta modo oscuro del sistema
    dynamicColor: Boolean = false,               // Dynamic Color (Android 12+)
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Si dynamicColor = true y Android 12+, usa colores del wallpaper
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        // Si no, usa nuestros esquemas personalizados
        darkTheme -> darkScheme
        else -> lightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,  // Los colores
        typography = AppTypography, // La tipografia (definida en Type.kt)
        content = content           // El contenido de la app
    )
}
```

> `dynamicColor = false` por defecto para preservar la identidad visual de la marca. Si lo pones en `true`, Android 12+ usara colores basados en el wallpaper del usuario.

### 1.3 Type.kt — Tipografia con Google Fonts

Este archivo configura la fuente **Inter** descargada en tiempo de ejecucion desde Google Fonts.

**Ubicacion:** `ui/theme/Type.kt`

```kotlin
package com.carevalojesus.eventosya.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.carevalojesus.eventosya.R
```

**Proveedor de Google Fonts** — Se conecta a Google Play Services para descargar fuentes:

```kotlin
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",  // Autoridad del proveedor
    providerPackage = "com.google.android.gms",           // Paquete de GMS
    certificates = R.array.com_google_android_gms_fonts_certs // Certificados de seguridad
)
```

> Los certificados estan en `res/values/font_certs.xml`. Son necesarios para verificar la autenticidad del proveedor de fuentes.

**Familias de fuentes** — Se definen dos familias (pueden ser diferentes):

```kotlin
val bodyFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Inter"),  // Nombre de la fuente en Google Fonts
        fontProvider = provider,
    )
)

val displayFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Inter"),  // Misma fuente para titulos
        fontProvider = provider,
    )
)
```

**Tipografia completa** — Sobreescribe cada estilo de Material 3:

```kotlin
val baseline = Typography()  // Tipografia por defecto de Material 3

val AppTypography = Typography(
    // Titulos grandes (Display)
    displayLarge = baseline.displayLarge.copy(fontFamily = displayFontFamily),
    displayMedium = baseline.displayMedium.copy(fontFamily = displayFontFamily),
    displaySmall = baseline.displaySmall.copy(fontFamily = displayFontFamily),

    // Encabezados (Headline)
    headlineLarge = baseline.headlineLarge.copy(fontFamily = displayFontFamily),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = displayFontFamily),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = displayFontFamily),

    // Titulos de seccion (Title)
    titleLarge = baseline.titleLarge.copy(fontFamily = displayFontFamily),
    titleMedium = baseline.titleMedium.copy(fontFamily = displayFontFamily),
    titleSmall = baseline.titleSmall.copy(fontFamily = displayFontFamily),

    // Texto de cuerpo (Body)
    bodyLarge = baseline.bodyLarge.copy(fontFamily = bodyFontFamily),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = bodyFontFamily),
    bodySmall = baseline.bodySmall.copy(fontFamily = bodyFontFamily),

    // Etiquetas (Label) — botones, chips, tabs
    labelLarge = baseline.labelLarge.copy(fontFamily = bodyFontFamily),
    labelMedium = baseline.labelMedium.copy(fontFamily = bodyFontFamily),
    labelSmall = baseline.labelSmall.copy(fontFamily = bodyFontFamily),
)
```

> Usamos `.copy()` para mantener los tamanos y pesos por defecto de Material 3 y solo cambiar la familia de fuente.

### 1.4 font_certs.xml — Certificados de Google Fonts

**Ubicacion:** `res/values/font_certs.xml`

Este archivo contiene los certificados que Android usa para verificar que las fuentes vienen realmente de Google Play Services. **No modifiques este archivo**, es estandar para todos los proyectos.

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <array name="com_google_android_gms_fonts_certs">
        <item>@array/com_google_android_gms_fonts_certs_dev</item>
        <item>@array/com_google_android_gms_fonts_certs_prod</item>
    </array>
    <string-array name="com_google_android_gms_fonts_certs_dev">
        <item>MIIEqDCCA5CgAwIBAgIJA...</item>  <!-- Certificado de desarrollo -->
    </string-array>
    <string-array name="com_google_android_gms_fonts_certs_prod">
        <item>MIIEQzCCAyugAwIBAgIJA...</item>  <!-- Certificado de produccion -->
    </string-array>
</resources>
```

---

## Paso 2: Agregar dependencias

**Archivo:** `app/build.gradle.kts`

Agrega las siguientes dependencias en el bloque `dependencies`:

```kotlin
dependencies {
    // ... dependencias existentes de Compose ...

    // Google Fonts para Compose (tipografia Inter)
    implementation("androidx.compose.ui:ui-text-google-fonts:1.7.8")

    // Firebase BOM (Bill of Materials) — controla versiones de todos los modulos Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.9.0"))

    // Firebase Analytics — seguimiento de eventos
    implementation("com.google.firebase:firebase-analytics")

    // Firebase Authentication — login con email/password y proveedores externos
    implementation("com.google.firebase:firebase-auth")

    // Credential Manager — API moderna de Android para manejar credenciales
    implementation("androidx.credentials:credentials:1.5.0")

    // Soporte de Credential Manager con Google Play Services
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")

    // Google Identity — para obtener el ID token de Google
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // ViewModel para Compose — permite usar viewModel() en composables
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
}
```

**Explicacion de cada dependencia:**

| Dependencia | Para que sirve |
|-------------|----------------|
| `ui-text-google-fonts` | Descargar fuentes de Google Fonts en runtime |
| `firebase-bom` | Gestiona versiones compatibles de todos los modulos Firebase |
| `firebase-auth` | Autenticacion con email/password y Google |
| `credentials` | API moderna de Android para login (reemplaza al viejo GoogleSignInClient) |
| `credentials-play-services-auth` | Implementacion de Credential Manager con Play Services |
| `googleid` | Obtener el token de Google del usuario |
| `lifecycle-viewmodel-compose` | Funcion `viewModel()` para crear ViewModels en Compose |

> **Importante:** Despues de agregar las dependencias, haz clic en **"Sync Now"** en la barra superior de Android Studio.

---

## Paso 3: Configurar AndroidManifest

**Archivo:** `app/src/main/AndroidManifest.xml`

Agrega el permiso de Internet **antes** del tag `<application>`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Permiso necesario para conectarse a Firebase y Google -->
    <uses-permission android:name="android.permission.INTERNET" />

    <application
        ...>
        <!-- Tu activity -->
    </application>
</manifest>
```

> Sin este permiso, las llamadas a Firebase Auth y Google Sign-In fallaran silenciosamente.

---

## Paso 4: Crear el LoginViewModel

**Archivo:** `ui/screens/login/LoginViewModel.kt`

El ViewModel es la capa que maneja la **logica de negocio** y el **estado de la UI**. Separa la logica de la pantalla para que sea testeable y sobreviva a cambios de configuracion (como rotar el dispositivo).

### 4.1 El estado de la UI (data class)

```kotlin
data class LoginUiState(
    val email: String = "",           // Texto del campo email
    val password: String = "",        // Texto del campo password
    val isLoading: Boolean = false,   // Mostrar/ocultar indicador de carga
    val errorMessage: String? = null, // Mensaje de error (null = sin error)
    val isLoginSuccess: Boolean = false // Flag para navegar al Home
)
```

**Por que un `data class`?**
- Es inmutable: cada cambio crea una **copia nueva** del estado
- Compose detecta automaticamente los cambios y **recompone** solo lo necesario
- Todos los valores tienen **defaults**: el estado inicial esta listo sin parametros

### 4.2 La clase LoginViewModel

```kotlin
class LoginViewModel : ViewModel() {

    // Estado observable por Compose
    var uiState by mutableStateOf(LoginUiState())
        private set  // Solo el ViewModel puede modificarlo

    // Instancia de Firebase Auth
    private val auth = FirebaseAuth.getInstance()
```

**`mutableStateOf`** — Crea un estado observable. Cuando cambia, Compose recompone automaticamente los composables que lo leen.

**`private set`** — La UI puede leer `uiState` pero NO modificarlo directamente. Solo a traves de las funciones del ViewModel.

### 4.3 Funciones para actualizar campos

```kotlin
fun onEmailChange(email: String) {
    uiState = uiState.copy(email = email, errorMessage = null)
}

fun onPasswordChange(password: String) {
    uiState = uiState.copy(password = password, errorMessage = null)
}
```

**`.copy()`** — Crea una copia del estado actual cambiando solo los campos indicados. Tambien limpia el error al escribir.

### 4.4 Login con email y password

```kotlin
fun loginWithEmail() {
    val email = uiState.email.trim()  // Elimina espacios en blanco
    val password = uiState.password

    // Validacion basica
    if (email.isBlank() || password.isBlank()) {
        uiState = uiState.copy(errorMessage = "Completa todos los campos")
        return
    }

    // Lanza una coroutine en el scope del ViewModel
    viewModelScope.launch {
        uiState = uiState.copy(isLoading = true, errorMessage = null)
        try {
            // Llama a Firebase Auth y ESPERA el resultado
            auth.signInWithEmailAndPassword(email, password).await()
            uiState = uiState.copy(isLoading = false, isLoginSuccess = true)
        } catch (e: Exception) {
            uiState = uiState.copy(
                isLoading = false,
                errorMessage = e.localizedMessage ?: "Error al iniciar sesion"
            )
        }
    }
}
```

**Conceptos clave:**

| Concepto | Explicacion |
|----------|-------------|
| `viewModelScope.launch` | Inicia una **coroutine** que se cancela automaticamente si el ViewModel se destruye |
| `.await()` | Convierte el `Task` de Firebase en una **suspend function** (espera sin bloquear el hilo principal) |
| `try/catch` | Si Firebase lanza error (password incorrecto, usuario no existe, etc.), lo capturamos |
| `isLoading = true/false` | Activa/desactiva el indicador de carga en la UI |

### 4.5 Login con Google

El flujo de Google Sign-In tiene dos partes:

**Parte 1 — Recibir la credencial de Google** (llamada desde la UI):

```kotlin
fun handleGoogleSignInResult(credential: Credential) {
    // Verificar que es una credencial de Google valida
    if (credential is CustomCredential &&
        credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

        // Extraer el token de Google
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val idToken = googleIdTokenCredential.idToken

        // Usarlo para autenticar en Firebase
        firebaseAuthWithGoogle(idToken)
    } else {
        uiState = uiState.copy(errorMessage = "Credencial de Google no valida")
    }
}
```

**Parte 2 — Autenticar en Firebase con el token de Google**:

```kotlin
private fun firebaseAuthWithGoogle(idToken: String) {
    viewModelScope.launch {
        uiState = uiState.copy(isLoading = true, errorMessage = null)
        try {
            // Crear credencial de Firebase a partir del token de Google
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

            // Iniciar sesion en Firebase
            auth.signInWithCredential(firebaseCredential).await()
            uiState = uiState.copy(isLoading = false, isLoginSuccess = true)
        } catch (e: Exception) {
            uiState = uiState.copy(
                isLoading = false,
                errorMessage = e.localizedMessage ?: "Error con Google Sign-In"
            )
        }
    }
}
```

**Flujo completo de Google Sign-In:**

```
Usuario pulsa boton
       ↓
Credential Manager muestra selector de cuentas Google
       ↓
Usuario selecciona cuenta
       ↓
Se obtiene un ID Token de Google
       ↓
Se crea una credencial de Firebase con ese token
       ↓
Firebase autentica al usuario
       ↓
isLoginSuccess = true → navegar al Home
```

---

## Paso 5: Crear el LoginScreen

**Archivo:** `ui/screens/login/LoginScreen.kt`

Esta es la **pantalla visual** del login, construida completamente con Jetpack Compose.

### 5.1 Firma de la funcion

```kotlin
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,             // Recibe el ViewModel
    onLoginSuccess: () -> Unit = {},       // Callback cuando el login es exitoso
    onNavigateToRegister: () -> Unit = {}  // Callback para ir a registro
)
```

> Usamos **callbacks** (`onLoginSuccess`, `onNavigateToRegister`) en vez de navegar directamente. Esto hace que el composable sea **reutilizable** y **testeable**.

### 5.2 Variables de estado locales

```kotlin
val state = viewModel.uiState                           // Estado del ViewModel
val snackbarHostState = remember { SnackbarHostState() } // Estado del Snackbar
val scope = rememberCoroutineScope()                     // Scope para coroutines
val context = LocalContext.current                       // Contexto de Android
```

| Variable | Para que sirve |
|----------|----------------|
| `state` | Acceso directo al estado actual (email, password, loading, etc.) |
| `snackbarHostState` | Controla la aparicion/desaparicion del Snackbar de errores |
| `scope` | Necesario para lanzar el flujo de Google Sign-In (es una suspend function) |
| `context` | Necesario para Credential Manager y para leer el string del Web Client ID |

### 5.3 Efectos secundarios (LaunchedEffect)

```kotlin
// Cuando el login es exitoso, ejecuta el callback
LaunchedEffect(state.isLoginSuccess) {
    if (state.isLoginSuccess) onLoginSuccess()
}

// Cuando hay un error, muestra el Snackbar
LaunchedEffect(state.errorMessage) {
    state.errorMessage?.let {
        snackbarHostState.showSnackbar(it)
    }
}
```

**`LaunchedEffect(key)`** — Se ejecuta cuando el valor de `key` cambia. Es la forma de ejecutar **efectos secundarios** (navegacion, mostrar snackbars, etc.) de forma segura en Compose.

### 5.4 Estructura visual (de arriba a abajo)

```
┌─────────────────────────────────┐
│          Spacer (64dp)          │
│                                 │
│        [Isotipo 120x120]        │  ← Image con painterResource
│                                 │
│         "eventosYA"             │  ← headlineLarge, color primary
│   "Inicia sesion para continuar"│  ← bodyMedium, color onSurfaceVariant
│                                 │
│  ┌───────────────────────────┐  │
│  │  Correo electronico       │  │  ← OutlinedTextField
│  └───────────────────────────┘  │
│  ┌───────────────────────────┐  │
│  │  Contrasena          [👁] │  │  ← OutlinedTextField + toggle
│  └───────────────────────────┘  │
│           ¿Olvidaste tu...?     │  ← TextButton alineado a la derecha
│                                 │
│  ┌───────────────────────────┐  │
│  │     Iniciar sesion        │  │  ← Button (filled)
│  └───────────────────────────┘  │
│                                 │
│  ────── o continua con ──────   │  ← HorizontalDivider + Text
│                                 │
│  ┌───────────────────────────┐  │
│  │  G  Continuar con Google  │  │  ← OutlinedButton
│  └───────────────────────────┘  │
│                                 │
│  ¿No tienes cuenta? Registrate  │  ← Text + TextButton
└─────────────────────────────────┘
```

### 5.5 El isotipo (logo)

```kotlin
Image(
    painter = painterResource(id = R.drawable.isotipo), // Carga el PNG de res/drawable
    contentDescription = "eventosYA",                    // Accesibilidad
    modifier = Modifier.size(120.dp)                     // Tamano fijo
)
```

> Coloca tu archivo `isotipo.png` en `app/src/main/res/drawable/`. Compose lo cargara automaticamente.

### 5.6 Titulo y subtitulo

```kotlin
Text(
    text = "eventosYA",
    style = MaterialTheme.typography.headlineLarge,  // Estilo grande del tema
    color = MaterialTheme.colorScheme.primary        // Color principal de la marca
)

Text(
    text = "Inicia sesion para continuar",
    style = MaterialTheme.typography.bodyMedium,          // Estilo mediano
    color = MaterialTheme.colorScheme.onSurfaceVariant    // Color secundario de texto
)
```

> Siempre usa `MaterialTheme.colorScheme.xxx` y `MaterialTheme.typography.xxx` en vez de colores/tamanos hardcodeados. Asi el tema se aplica consistentemente.

### 5.7 Campo de email

```kotlin
OutlinedTextField(
    value = state.email,                        // Valor actual del estado
    onValueChange = viewModel::onEmailChange,   // Funcion que actualiza el estado
    label = { Text("Correo electronico") },     // Etiqueta flotante
    singleLine = true,                          // No permite salto de linea
    keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Email,       // Teclado con @ y .com
        imeAction = ImeAction.Next               // Boton "Siguiente" en el teclado
    ),
    modifier = Modifier.fillMaxWidth(),          // Ancho completo
    enabled = !state.isLoading                   // Deshabilitado durante carga
)
```

### 5.8 Campo de password con toggle de visibilidad

```kotlin
var passwordVisible by remember { mutableStateOf(false) }  // Estado LOCAL

OutlinedTextField(
    value = state.password,
    onValueChange = viewModel::onPasswordChange,
    label = { Text("Contrasena") },
    singleLine = true,
    // Si passwordVisible = true, muestra texto normal; si no, muestra puntos
    visualTransformation = if (passwordVisible)
        VisualTransformation.None
        else PasswordVisualTransformation(),
    // Icono de ojo para mostrar/ocultar
    trailingIcon = {
        IconButton(onClick = { passwordVisible = !passwordVisible }) {
            Icon(
                painter = painterResource(
                    id = if (passwordVisible) android.R.drawable.ic_menu_view
                    else android.R.drawable.ic_secure
                ),
                contentDescription = if (passwordVisible)
                    "Ocultar contrasena" else "Mostrar contrasena"
            )
        }
    },
    keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Password,  // Teclado de contrasena
        imeAction = ImeAction.Done              // Boton "Listo"
    ),
    modifier = Modifier.fillMaxWidth(),
    enabled = !state.isLoading
)
```

> **`passwordVisible`** es un estado **local** del composable (no del ViewModel) porque solo afecta la visualizacion, no la logica de negocio.

### 5.9 Boton de login con indicador de carga

```kotlin
Button(
    onClick = viewModel::loginWithEmail,      // Llama al ViewModel
    modifier = Modifier
        .fillMaxWidth()
        .height(50.dp),
    enabled = !state.isLoading                // Deshabilitado durante carga
) {
    if (state.isLoading) {
        // Muestra un spinner mientras carga
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.onPrimary,  // Color blanco sobre primary
            strokeWidth = 2.dp
        )
    } else {
        Text("Iniciar sesion")
    }
}
```

> El boton muestra un `CircularProgressIndicator` durante la carga y se deshabilita para evitar multiples clicks.

### 5.10 Divisor "o continua con"

```kotlin
Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
) {
    HorizontalDivider(modifier = Modifier.weight(1f))  // Linea izquierda
    Text(
        text = "o continua con",
        modifier = Modifier.padding(horizontal = 16.dp),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    HorizontalDivider(modifier = Modifier.weight(1f))  // Linea derecha
}
```

> **`Modifier.weight(1f)`** hace que ambas lineas ocupen el espacio restante equitativamente.

### 5.11 Boton de Google Sign-In

```kotlin
OutlinedButton(
    onClick = {
        scope.launch {
            try {
                // 1. Configurar la opcion de Google ID
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false) // Mostrar TODAS las cuentas
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .build()

                // 2. Crear la solicitud de credencial
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                // 3. Lanzar el selector de cuentas
                val credentialManager = CredentialManager.create(context)
                val result = credentialManager.getCredential(context, request)

                // 4. Enviar la credencial al ViewModel
                viewModel.handleGoogleSignInResult(result.credential)

            } catch (e: GetCredentialCancellationException) {
                // El usuario cancelo — no hacer nada
            } catch (e: Exception) {
                viewModel.onGoogleSignInError(
                    e.localizedMessage ?: "Error al iniciar con Google"
                )
            }
        }
    },
    modifier = Modifier.fillMaxWidth().height(50.dp),
    enabled = !state.isLoading
) {
    Icon(/* icono de Google */)
    Spacer(modifier = Modifier.width(8.dp))
    Text("Continuar con Google")
}
```

**Credential Manager** es la API moderna de Android (reemplaza al viejo `GoogleSignInClient`):

| Paso | Que hace |
|------|----------|
| `GetGoogleIdOption` | Configura que tipo de credencial queremos (Google ID Token) |
| `setFilterByAuthorizedAccounts(false)` | Muestra todas las cuentas, no solo las que ya autorizaron la app |
| `setServerClientId(...)` | Le dice a Google cual es nuestro servidor (Firebase) |
| `GetCredentialRequest` | Empaqueta las opciones en una solicitud |
| `credentialManager.getCredential()` | Muestra el bottom sheet del sistema para seleccionar cuenta |
| `handleGoogleSignInResult()` | Envia el resultado al ViewModel para autenticar en Firebase |

---

## Paso 6: Conectar todo en MainActivity

**Archivo:** `MainActivity.kt`

```kotlin
package com.carevalojesus.eventosya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carevalojesus.eventosya.ui.screens.login.LoginScreen
import com.carevalojesus.eventosya.ui.screens.login.LoginViewModel
import com.carevalojesus.eventosya.ui.theme.EventosYATheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()  // La app se dibuja detras de las barras del sistema
        setContent {
            EventosYATheme {  // Envuelve todo con nuestro tema
                // Crea el ViewModel (sobrevive a rotaciones de pantalla)
                val loginViewModel: LoginViewModel = viewModel()

                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = {
                        // TODO: navegar a la pantalla principal
                    },
                    onNavigateToRegister = {
                        // TODO: navegar a la pantalla de registro
                    }
                )
            }
        }
    }
}
```

**Elementos clave:**

| Elemento | Explicacion |
|----------|-------------|
| `enableEdgeToEdge()` | Permite que la app use toda la pantalla, incluyendo detras de la barra de estado y navegacion |
| `EventosYATheme { }` | Aplica nuestros colores y tipografia a toda la app |
| `viewModel()` | Crea o recupera una instancia del ViewModel. Sobrevive a cambios de configuracion |
| `onLoginSuccess` | Aqui pondras la navegacion al Home cuando implementemos Navigation |
| `onNavigateToRegister` | Aqui pondras la navegacion al registro |

---

## Paso 7: Configurar Firebase Console

Para que el login funcione, necesitas configurar Firebase.

### 7.1 Ubicar correctamente google-services.json

El archivo `google-services.json` **debe estar dentro de la carpeta `app/`**, no en la raiz del proyecto.

```
eventosYA/
├── app/
│   ├── google-services.json   ← CORRECTO: aqui debe estar
│   ├── build.gradle.kts
│   └── src/
├── build.gradle.kts
└── settings.gradle.kts
```

> Si lo descargaste desde Firebase Console y lo pusiste en la raiz del proyecto, muevelo a `app/`. Sin este archivo en la ubicacion correcta, el build fallara con el error `File google-services.json is missing`.

### 7.2 Sobre el Web Client ID (default_web_client_id)

**No necesitas configurar manualmente el Web Client ID.** El plugin `com.google.gms.google-services` lee el archivo `google-services.json` y genera automaticamente el string resource `default_web_client_id` a partir del campo `oauth_client` con `client_type: 3`.

Es decir, esta linea en el codigo:
```kotlin
context.getString(R.string.default_web_client_id)
```
Funciona automaticamente gracias al plugin. No necesitas agregar nada en `strings.xml`.

### 7.3 Habilitar Email/Password

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona tu proyecto
3. Ve a **Authentication** > **Sign-in method**
4. Habilita **Correo electronico/Contrasena**
5. Guarda los cambios

### 7.4 Habilitar Google Sign-In

1. En la misma seccion **Sign-in method**
2. Habilita **Google**
3. Pon un nombre de soporte y un email
4. Guarda los cambios

### 7.5 Configurar SHA-1 (necesario para Google Sign-In)

Ejecuta en la terminal de Android Studio:

```bash
./gradlew signingReport
```

Copia el **SHA1** del variant `debug` y agregalo en:

1. Firebase Console > **Configuracion del proyecto** (engranaje) > **Tus apps**
2. Selecciona tu app Android
3. Click en **Agregar huella digital**
4. Pega el SHA-1
5. Descarga el nuevo `google-services.json` y reemplazalo en `app/`

---

## Conceptos clave explicados

### Patron MVVM (Model-View-ViewModel)

```
┌──────────┐     observa     ┌──────────────┐     llama     ┌──────────┐
│          │ ◄───────────── │              │ ──────────► │          │
│   View   │                │  ViewModel   │              │  Model   │
│ (Screen) │ ──────────► │ (LoginVM)    │ ◄────────── │(Firebase)│
│          │   eventos      │              │   resultado  │          │
└──────────┘                └──────────────┘              └──────────┘
```

- **View (LoginScreen):** Solo muestra datos y envia eventos al ViewModel
- **ViewModel (LoginViewModel):** Procesa la logica y actualiza el estado
- **Model (Firebase Auth):** Accede a los datos reales

### State Hoisting (Elevacion de estado)

El estado vive en el **ViewModel**, no en el composable. El composable solo:
- **Lee** el estado (`state.email`, `state.isLoading`)
- **Envia eventos** al ViewModel (`viewModel::onEmailChange`, `viewModel::loginWithEmail`)

Esto se llama **"unidirectional data flow"** (flujo de datos unidireccional):

```
Estado (ViewModel) ──► UI (Composable) ──► Evento ──► ViewModel ──► Nuevo Estado
```

### Coroutines y viewModelScope

Las llamadas a Firebase son **asincronas** (toman tiempo). No podemos bloquear el hilo principal o la app se congela. Por eso usamos:

- **`viewModelScope.launch { }`** — Inicia codigo asincrono que se cancela automaticamente
- **`.await()`** — Espera el resultado de Firebase sin bloquear
- **`try/catch`** — Captura errores de red, credenciales invalidas, etc.

### Credential Manager vs GoogleSignInClient (viejo)

| Aspecto | GoogleSignInClient (viejo) | Credential Manager (moderno) |
|---------|----------------------------|------------------------------|
| API | Deprecated | Recomendado por Google |
| UI | Intent separado | Bottom sheet nativo del sistema |
| Soporte | Solo Google | Google, passkeys, passwords |
| Disponible desde | Siempre | Android 4.4+ (via Play Services) |

---

---

## Errores comunes y soluciones

### Error: `File google-services.json is missing`

**Causa:** El archivo `google-services.json` no esta en la carpeta `app/`.

**Solucion:** Descarga el archivo desde Firebase Console (Configuracion del proyecto > Tus apps > Descargar `google-services.json`) y colocalo en la carpeta `app/` del proyecto (al mismo nivel que `build.gradle.kts` del modulo app).

```
eventosYA/
├── app/
│   ├── google-services.json   ← Debe estar AQUI
│   ├── build.gradle.kts
│   └── src/
```

> **Error frecuente:** Si lo descargas y lo pones en la raiz del proyecto (`eventosYA/google-services.json`), Gradle NO lo encontrara. Debe estar dentro de `app/`.

### Error: `default_web_client_id not found` o `Unresolved reference: default_web_client_id`

**Causa:** El archivo `google-services.json` no tiene un `oauth_client` con `client_type: 3`, o el plugin `google-services` no esta aplicado.

**Solucion:**
1. Verifica que en `build.gradle.kts` (nivel proyecto) tengas: `id("com.google.gms.google-services") version "4.4.4" apply false`
2. Verifica que en `build.gradle.kts` (nivel app) tengas: `id("com.google.gms.google-services")`
3. En Firebase Console, habilita Google Sign-In en Authentication > Sign-in method
4. Descarga de nuevo el `google-services.json` y reemplazalo

> El plugin genera automaticamente `R.string.default_web_client_id` a partir del `google-services.json`. **No necesitas definirlo manualmente en `strings.xml`.**

### Error: Google Sign-In falla silenciosamente

**Causa:** Falta el SHA-1 fingerprint en Firebase.

**Solucion:**
1. Ejecuta `./gradlew signingReport` en la terminal
2. Copia el SHA1 del variant `debug`
3. Agregalo en Firebase Console > Configuracion del proyecto > Tus apps > Agregar huella digital
4. Descarga el nuevo `google-services.json` y reemplazalo en `app/`

### Error: `DEVELOPER_ERROR` en Google Sign-In

**Causa:** El SHA-1 del dispositivo/emulador no coincide con el registrado en Firebase, o el `package_name` en `google-services.json` no coincide con el `applicationId` del `build.gradle.kts`.

**Solucion:**
1. Verifica que `applicationId` en `build.gradle.kts` sea exactamente igual al `package_name` en `google-services.json`
2. Agrega tanto el SHA-1 de debug como el de release en Firebase Console
3. Descarga de nuevo el `google-services.json`

---

## Checklist final

- [ ] `google-services.json` esta en `app/` (NO en la raiz del proyecto)
- [ ] Dependencias agregadas y sincronizadas (Sync Now)
- [ ] Permiso `INTERNET` en AndroidManifest
- [ ] `isotipo.png` en `res/drawable/`
- [ ] `font_certs.xml` en `res/values/`
- [ ] Email/Password habilitado en Firebase Console
- [ ] Google habilitado en Firebase Console
- [ ] SHA-1 agregado en Firebase Console
- [ ] Nuevo `google-services.json` descargado tras agregar SHA-1
