# Sistema de Eventos y Tickets con Google Wallet

**Plataforma objetivo:** Android (Kotlin) + Firebase

## 1. Descripción del Proyecto

El proyecto consiste en el desarrollo de una aplicación móvil Android utilizando Kotlin que permita gestionar eventos y vender tickets digitales. Los tickets generados podrán guardarse en Google Wallet y ser validados mediante escaneo QR durante el ingreso al evento. El backend del sistema será construido utilizando Firebase (Authentication, Firestore y Cloud Functions).

## 2. Objetivo del Sistema

1. Permitir a organizadores crear eventos y tipos de ticket.
2. Permitir a usuarios comprar tickets desde una app Android.
3. Emitir tickets digitales compatibles con Google Wallet.
4. Validar tickets mediante escaneo QR usando la cámara del dispositivo.
5. Registrar asistencia y prevenir el uso duplicado de tickets.

## 3. Arquitectura General

1. Aplicación Android desarrollada en Kotlin.
2. Arquitectura MVVM usando Jetpack components.
3. Firebase Authentication para manejo de usuarios.
4. Cloud Firestore como base de datos principal.
5. Cloud Functions para lógica crítica del backend.
6. Integración con Google Wallet para almacenamiento de tickets.

## 4. Arquitectura de la App Android

| Capa | Responsabilidad |
|------|-----------------|
| **UI Layer** | Pantallas construidas con Jetpack Compose o XML |
| **ViewModel Layer** | Manejo de estado de la UI |
| **Domain Layer** | Casos de uso del sistema |
| **Data Layer** | Repositorios Firebase |
| **Network Layer** | Llamadas a Cloud Functions |

## 5. Roles del Sistema

| Rol | Descripción |
|-----|-------------|
| **Admin** | Crea eventos y administra tipos de tickets |
| **Staff** | Escanea tickets en el ingreso al evento |
| **Usuario** | Compra tickets y los guarda en Google Wallet |

## 6. Modelo de Datos (Firestore)

### Colección: `events`

| Campo | Descripción |
|-------|-------------|
| `title` | Título del evento |
| `description` | Descripción del evento |
| `startAt` | Fecha y hora de inicio |
| `venueName` | Nombre del lugar |
| `venueAddress` | Dirección del lugar |
| `status` | Estado del evento |
| `walletClassId` | ID de clase de Google Wallet |

### Subcolección: `ticketTypes`

| Campo | Descripción |
|-------|-------------|
| `name` | Nombre del tipo de ticket |
| `price` | Precio |
| `currency` | Moneda |
| `capacity` | Capacidad máxima |
| `sold` | Cantidad vendida |

### Colección: `orders`

| Campo | Descripción |
|-------|-------------|
| `userId` | ID del usuario |
| `eventId` | ID del evento |
| `status` | Estado de la orden |
| `total` | Total de la orden |
| `currency` | Moneda |
| `createdAt` | Fecha de creación |

### Colección: `tickets`

| Campo | Descripción |
|-------|-------------|
| `ticketTypeId` | ID del tipo de ticket |
| `eventId` | ID del evento |
| `userId` | ID del usuario |
| `code` | Identificador único |
| `status` | Estado (`ISSUED`, `USED`, `REFUNDED`) |
| `usedAt` | Fecha de uso |
| `walletObjectId` | ID del objeto en Google Wallet |

## 7. Cloud Functions

| Función | Descripción |
|---------|-------------|
| `createOrderAndTickets` | Crea órdenes y tickets controlando cupos |
| `getWalletSaveUrl` | Genera el enlace para guardar tickets en Google Wallet |
| `scanTicket` | Valida y registra el ingreso al evento |

## 8. Flujo del Sistema

1. El administrador crea un evento.
2. Define los tipos de tickets disponibles.
3. El usuario compra tickets desde la aplicación.
4. El sistema genera tickets únicos.
5. El usuario puede guardar el ticket en Google Wallet.
6. El staff escanea el ticket al ingresar al evento.
7. El sistema valida el ticket y registra el acceso.

## 9. Módulo de Escaneo

1. Uso de **CameraX** para acceder a la cámara.
2. Uso de **ML Kit Barcode Scanner** para detectar códigos QR.
3. Envía el código del ticket a Cloud Functions.
4. Recibe respuesta de validación.
5. Muestra resultado en pantalla (válido o inválido).

## 10. Plan de Desarrollo

| Sprint | Objetivo |
|--------|----------|
| Sprint 1 | Configuración Firebase + autenticación |
| Sprint 2 | Creación y listado de eventos |
| Sprint 3 | Compra de tickets |
| Sprint 4 | Integración Google Wallet |
| Sprint 5 | Escaneo de tickets |
| Sprint 6 | Reportes y mejoras |

## 11. Resultado Esperado

Al finalizar el proyecto se contará con una aplicación Android funcional capaz de gestionar eventos, vender tickets digitales y validar el ingreso mediante Google Wallet y escaneo QR utilizando Firebase como backend serverless.
