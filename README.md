# Viajes Destinos - Gestión de Destinos Turísticos ✈️🌴

Aplicación móvil profesional desarrollada en **Android Studio** con **Kotlin** y **Jetpack Compose** para una Agencia de Viajes. Esta plataforma permite una gestión integral de paquetes turísticos, ofreciendo una experiencia de usuario premium con sincronización híbrida en la nube y diseño moderno.

---

## 📋 Datos de la Entrega

- **Nombre del Alumno:** Jesus Ernesto Sanabria Sibrian
- **Estado del Proyecto:** Completamente funcional con integración Firebase y UI Rediseñada.
- **APK Funcional:** Generada en `app/build/outputs/apk/debug/app-debug.apk`.

---

## 🎨 Diseño y UI Profesional (Actualizado)

La aplicación ha sido rediseñada para ofrecer un aspecto limpio y corporativo:
- **Fondo Premium:** Fondos en blanco puro y gris ultra-claro para mayor claridad visual.
- **Tipografía de Alta Visibilidad:** Texto principal en **Negro Puro (`#000000`)** y secundario en gris oscuro (`#333333`), eliminando problemas de legibilidad.
- **Iconografía Oficial:** Logo personalizado configurado como icono de la aplicación (Adaptive Icon) y presente en toda la interfaz.
- **Colores de Marca:**
    - **Dark Primary:** `#0097A7`
    - **Primary:** `#00BCD4`
    - **Accent:** `#8BC34A` (Badges de precio y botones de acción)

---

## ✅ Características Principales y Tecnologías

### 1. Sistema de Autenticación Multimodal
- **Firebase Auth:** Registro e inicio de sesión con correo y contraseña.
- **Google Sign-In:** Integración con **Credential Manager API** para un acceso rápido con cuentas de Google.
- **Validaciones:** Control estricto de formatos de email y seguridad de contraseñas.

### 2. Sincronización Híbrida de Datos
- **Cloud Firestore:** Almacenamiento de documentos para el catálogo de destinos.
- **Realtime Database:** Sincronización instantánea de alta velocidad para actualizaciones en tiempo real.
- **Persistencia Local:** Caché inteligente mediante `SharedPreferences` y almacenamiento interno para funcionamiento offline garantizado.

### 3. Gestión de Catálogo (CRUD Avanzado)
- **Creación:** Formulario con validaciones en tiempo vivo, selector de países expandido (toda América) y carga de imágenes.
- **Lectura:** Lista optimizada con filtros dinámicos por país y barra de búsqueda predictiva.
- **Actualización:** Edición completa de paquetes existentes con sincronización inmediata a la nube.
- **Eliminación:** Sistema seguro con diálogos de confirmación.

### 4. Multimedia y Rendimiento
- **Android Photo Picker:** Selección moderna y segura de imágenes de la galería.
- **Coil:** Procesamiento y carga eficiente de imágenes con manejo de caché y estados de error.
- **Arquitectura Limpia:** Repositorios centralizados para una gestión de datos escalable.

---

## 🚀 Instrucciones de Configuración

1. **Clonar el proyecto:**
   ```bash
   git clone https://github.com/jess300605/Viajes_Destinos
   ```
2. **Configuración de Firebase:**
    - Colocar el archivo `google-services.json` en la carpeta `/app`.
    - Configurar el `default_web_client_id` en `strings.xml` para habilitar el Login de Google.
3. **Compilación:**
   ```bash
   ./gradlew assembleDebug
   ```
4. **Requisitos:** Android 7.0 (API 24) o superior.

---
*Desarrollado con ❤️ para la gestión turística moderna.*
