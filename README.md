# 🏍️ MotoGear Colombia

Aplicación móvil nativa para **Android** orientada al comercio electrónico (E-commerce) especializado en accesorios, indumentaria certificada de protección (cascos, chaquetas, guantes) y repuestos para motociclistas en Colombia.

Desarrollada en **Kotlin** bajo los lineamientos visuales de **Material Design 3**, integrando servicios en la nube de **Firebase**, sensores nativos del dispositivo (cámara, biometría, GPS) y flujo de pago electrónico.

---

## 🚀 Funcionalidades Principales

### 1. Autenticación y Control de Acceso por Roles
* **Registro e inicio de sesión** con correo electrónico y contraseña mediante **Firebase Authentication**, además de recuperación de contraseña.
* **Autenticación Biométrica (`BiometricPrompt`):** Acceso rápido mediante huella dactilar vinculada a la cuenta del usuario en dispositivos físicos compatibles.
* **Separación de 3 Roles:**
  * **Comprador (`cliente`):** Navegación por catálogo en tiempo real, detalle de productos, gestión de carrito de compras, captura de ubicación GPS para entrega, pago electrónico e historial de órdenes.
  * **Vendedor (`vendedor`):** Registro protegido mediante código dinámico de autorización almacenado en Firestore (`configuracion/seguridad`). Acceso a gestión completa (CRUD) de inventario con captura de fotografías por cámara o galería.
  * **Administrador (`admin`):** Panel de control global con administración de usuarios (asignación/cambio de roles y baja de perfiles), gestión de inventario y reportes.

### 2. Gestión de Productos (CRUD con Cámara y Galería)
* Creación, lectura en tiempo real, edición y eliminación de productos en **Cloud Firestore**.
* Captura directa desde la **cámara del dispositivo** o selección desde **galería** con gestión de permisos en tiempo de ejecución (`Manifest.permission.CAMERA`).
* Optimización mediante redimensionamiento y compresión JPEG codificada en Base64 para persistencia eficiente en Firestore.

### 3. Carrito de Compras, Geolocalización y Pasarela de Pagos
* **Carrito dinámico (`CartManager`):** Cálculo automático de totales en pesos colombianos (COP), actualización de cantidades y descuento automático de stock en Firestore al confirmar la compra.
* **Geolocalización (`LocationActivity`):** Obtención de coordenadas GPS en tiempo real (`ACCESS_FINE_LOCATION`) vinculadas a la dirección de envío del cliente.
* **Pasarela de Pagos (`PaymentActivity`):** Integración de checkout **ePayco (Modo Sandbox)** con validación de tarjeta de prueba, generación de referencia única de transacción (`EPAYCO-XXXXXX`) y registro del comprobante en la colección `pedidos`.

---

## ⚖️ Marco Normativo Colombiano Aplicado

* **Ley 1480 de 2011 (Estatuto del Consumidor):** Transparencia en precios en COP, disponibilidad de inventario y canales de atención/PQR en la aplicación.
* **Ley 1581 de 2012 (Habeas Data):** Consentimiento informado en el registro y tratamiento seguro de datos personales mediante Firebase.
* **Ley 527 de 1999 y Ley 1273 de 2009:** Generación de comprobantes digitales de transacción con referencia única, autenticación biométrica y reglas de seguridad en Firestore (sin almacenar datos sensibles de tarjetas en base de datos).

---

## 🛠️ Stack Tecnológico

* **Lenguaje:** Kotlin
* **Entorno de Desarrollo:** Android Studio
* **UI / UX:** XML Layouts + Material Design 3 (Modo Oscuro `#121212` / Énfasis Naranja `#FF6D00`)
* **Backend & Base de Datos:** Firebase Authentication & Cloud Firestore
* **APIs Nativas Android:** `BiometricPrompt`, `ActivityResultContracts` (Cámara y Galería), `LocationManager`

---

## 📲 Instalación y Prueba (APK)

1. Ve a la sección **[Releases](../../releases/latest)** de este repositorio.
2. Descarga el archivo instalable **`app-release.apk`** (firmado digitalmente) en tu dispositivo Android.
3. Habilita la instalación desde orígenes desconocidos si el sistema lo solicita e instala la aplicación.

### Flujo de Prueba Sugerido
* **Como Comprador:** Regístrate seleccionando el rol *Comprador*, explora el catálogo, añade productos al carrito, captura tu ubicación GPS y completa el pago mediante **ePayco Sandbox**.
* **Como Vendedor:** Regístrate seleccionando el rol *Vendedor* (ingresando el código de autorización configurado en Firestore) para publicar, editar o eliminar productos con foto.
* **Como Administrador:** Inicia sesión con una cuenta con rol `admin` para administrar los roles de los usuarios registrados y supervisar el inventario.

---

## 👨‍💻 Autor

* **José Nicolás Bastidas Romero** — Ingeniería de Software, Fundación Universitaria Compensar (Bogotá D.C., Colombia).