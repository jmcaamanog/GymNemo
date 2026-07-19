# 🔱 GymNemo — Offline Workout Tracker (Wear OS & Android)

![Profesión](https://img.shields.io/badge/Profesi%C3%B3n-Arquitectos%20T%C3%A9cnicos-2e7d32?logo=micro%3Abit&logoColor=white&style=flat-square)
![Role](https://img.shields.io/badge/Role-BIM%20%26%20ConTech-007ACC?logo=bim360&style=flat-square)
![Location](https://img.shields.io/badge/Location-A%20Coru%C3%B1a%20%F0%9F%8C%8A-005B94?logo=lighthouse&logoColor=white&style=flat-square)
![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white&style=flat-square)
![WearOS](https://img.shields.io/badge/Platform-WearOS-4285F4?logo=wearos&logoColor=white&style=flat-square)
![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white&style=flat-square)
![Stars](https://img.shields.io/github/stars/jmcaamanog/GymNemo?style=flat-square&color=yellow&logo=github)
![License](https://img.shields.io/github/license/jmcaamanog/GymNemo?style=flat-square&color=green)

### La herramienta libre y 100% offline para el control y registro de entrenamientos de fuerza.
*Desarrollado con foco en la usabilidad táctil en Wear OS y analíticas fluidas en Android Companion.*

---

## ⚡ Descarga Rápida (v2.5.0)

> [!IMPORTANT]
> ### 📥 Instalación de Aplicaciones
> *   **⌚ Wear OS — Módulo Reloj (`:app`):**
>     [**Descargar app-debug.apk**](https://github.com/jmcaamanog/GymNemo/releases/latest) *(Instalar vía ADB / Bugjaeger en Galaxy Watch o Pixel Watch)*
> *   **📱 Android Móvil — App Companion (`:mobile`):**
>     [**Descargar mobile-debug.apk**](https://github.com/jmcaamanog/GymNemo/releases/latest) *(Sincronización, historial, analítica y exportación)*
>
> 🔒 **Privacidad:** La aplicación es 100% local. Tus datos permanecen en tus dispositivos.
> ☁️ **Opcional:** La exportación a Google Sheets es completamente opcional y configurable.

---

## 👨‍💻 Creador y Diseñador
Desarrollado y diseñado por `José Manuel Caamaño González` ([LinkedIn](https://www.linkedin.com/in/jmcaamanog/) · [Web](https://jmcaamanog.pages.dev)), Arquitecto Técnico y BIM Manager.
*Filosofía: software libre de egos, utilidad técnica real y soberanía absoluta sobre los datos de entrenamiento.*

---

## 🌟 Características Principales

| Módulo | Icono | Funcionalidades Destacadas |
| :--- | :---: | :--- |
| **UX Radial "Y-Split"** | ⌚ | Tres botones gigantes a 120° en el reloj, ideales para dedos sudados al levantar peso. |
| **Sincronización Automática** | 📡 | Wear OS Data Layer: el reloj envía cada sesión al móvil en tiempo real al terminar. |
| **Forzar Sincronización Manual** | 📶 | Botón Wifi (cian) en el menú del dashboard del reloj para forzar el envío inmediato. |
| **Google Sheets** | 📊 | Exportación automática opcional de cada entrenamiento a tu propia hoja de Google. |
| **Registro Manual** | ✏️ | Registra entrenamientos desde el móvil cuando no llevas el reloj. |
| **Resplandor Neon por FC** | 💓 | Borde circular reactivo que pulsa y cambia de color según tu zona de ritmo cardíaco. |
| **Sobrecarga Progresiva** | 📈 | Badge `+2.5 KG SUGERIDO` si lograste tu rango de reps en la sesión previa. |
| **Mapa de Calor Anatómico** | 🧍 | Silueta humana en el móvil que ilumina los músculos entrenados en la semana. |
| **Copia de Seguridad JSON/CSV** | 💾 | Exportación manual y automática al recibir cada sesión del reloj. |
| **Actualizaciones OTA** | 🔄 | El móvil comprueba si hay nueva versión en GitHub Releases con un solo toque. |
| **Récords Personales (PR)** | 🏆 | Pantalla de récords por ejercicio con historial de peso máximo levantado. |

---

## 📊 Integración con Google Sheets (Opcional)

GymNemo puede exportar automáticamente cada entrenamiento a una hoja de cálculo de Google de tu propiedad. Esta función es completamente opcional, gratuita y no requiere ninguna cuenta adicional ni servicio de pago.

### ¿Cómo funciona?
El móvil envía los datos del entrenamiento (fecha, hora, ejercicio, series, pesos) a un **Google Apps Script** que tú controlas. Este script escribe una fila por serie en tu hoja de Google. Nadie más tiene acceso a tus datos.

---

### Paso 1 — Crear el Google Apps Script

1. Abre [script.google.com](https://script.google.com) con tu cuenta de Google.
2. Haz clic en **`+ Nuevo proyecto`**.
3. **Borra todo** el código del editor y pega el siguiente:

```javascript
/**
 * GymNemo — Google Apps Script
 * Recibe los datos de entrenamiento desde la app GymNemo via GET.
 * Escribe una fila por serie en la hoja activa del documento.
 */
function doGet(e) {
  try {
    const sheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();

    // Si no llegan datos, confirmamos que el script está activo
    if (!e.parameter || !e.parameter.date) {
      return ContentService
        .createTextOutput(JSON.stringify({ status: "activo", mensaje: "GymNemo Script listo." }))
        .setMimeType(ContentService.MimeType.JSON);
    }

    // Crear cabeceras la primera vez
    if (sheet.getLastRow() === 0) {
      sheet.appendRow([
        "Fecha", "Hora Inicio", "Hora Fin", "Duración (min)",
        "Kcal", "Parte Cuerpo", "Ejercicio", "Reps", "Peso (Kg)", "Descanso (s)"
      ]);
      sheet.getRange(1, 1, 1, 10)
        .setFontWeight("bold")
        .setBackground("#1A1A2E")
        .setFontColor("#00E5FF");
    }

    const p         = e.parameter;
    const date      = p.date         || "";
    const startTime = p.startTime    || "";
    const endTime   = p.endTime      || "";
    const durMin    = Math.round(parseInt(p.durationSeconds || "0") / 60);
    const kcal      = parseInt(p.totalKcal || "0");
    const bodyPart  = p.bodyPart     || "";
    const setsJson  = p.sets         || "[]";

    let sets = [];
    try { sets = JSON.parse(setsJson); } catch(err) { sets = []; }

    if (sets.length === 0) {
      sheet.appendRow([date, startTime, endTime, durMin, kcal, bodyPart, "", "", "", ""]);
    } else {
      sets.forEach(function(set, i) {
        sheet.appendRow([
          date,
          i === 0 ? startTime : "",
          i === 0 ? endTime   : "",
          i === 0 ? durMin    : "",
          i === 0 ? kcal      : "",
          i === 0 ? bodyPart  : "",
          set.exerciseName || "",
          set.reps         || 0,
          set.weightKg     || 0,
          set.restSeconds  || 90
        ]);
      });
    }

    return ContentService
      .createTextOutput(JSON.stringify({ status: "ok", series: sets.length }))
      .setMimeType(ContentService.MimeType.JSON);

  } catch (err) {
    return ContentService
      .createTextOutput(JSON.stringify({ status: "error", mensaje: err.toString() }))
      .setMimeType(ContentService.MimeType.JSON);
  }
}

// Por si Google llama a doPost, redirigimos a doGet
function doPost(e) { return doGet(e); }
```

4. Guarda el proyecto (`Ctrl+S`) con el nombre que quieras, por ejemplo `GymNemo Sheets`.

---

### Paso 2 — Desplegar como Aplicación Web

1. Haz clic en **Implementar → Nueva implementación**.
2. Selecciona el tipo **Aplicación web**.
3. Configura así:
   - **Ejecutar como:** `Yo (tu correo de Google)`
   - **Quién tiene acceso:** `Cualquier persona` ← ⚠️ **No** "con cuenta de Google", sino **Cualquier persona** (anónimo)
4. Haz clic en **Implementar** y autoriza los permisos.
5. **Copia la URL** que aparece (empieza por `https://script.google.com/macros/s/...`).

---

### Paso 3 — Verificar el Script

Pega la URL directamente en tu navegador. Si ves esto, el script está activo:

```json
{"status":"activo","mensaje":"GymNemo Script listo."}
```

---

### Paso 4 — Configurar en la App GymNemo

1. Abre **GymNemo** en tu móvil.
2. Ve a la pestaña **Respaldos**.
3. Desplázate hasta la tarjeta **"Integración con Google Sheets"**.
4. Pega la URL del script y sal del campo (se guarda automáticamente).

A partir de este momento, cada entrenamiento sincronizado desde el reloj o registrado manualmente aparecerá automáticamente en tu hoja de Google.

---

## 📁 Estructura del Directorio

*   **[`/app`](./app)**: Módulo Wear OS (Kotlin, Jetpack Compose).
    *   [MainActivity.kt](./app/src/main/java/com/jmcaamanog/gymnemo/presentation/MainActivity.kt): Controlador principal y sincronización.
    *   [WorkoutViewModel.kt](./app/src/main/java/com/jmcaamanog/gymnemo/viewmodel/WorkoutViewModel.kt): Gestión de estados y timer.
    *   [WorkoutRepository.kt](./app/src/main/java/com/jmcaamanog/gymnemo/data/repository/WorkoutRepository.kt): Capa de datos y envío al móvil via Data Layer.
*   **[`/mobile`](./mobile)**: Módulo Android Companion.
    *   [MainActivity.kt](./mobile/src/main/java/com/jmcaamanog/gymnemo/mobile/MainActivity.kt): Interfaz por pestañas con historial y analítica.
    *   [DataLayerListenerService.kt](./mobile/src/main/java/com/jmcaamanog/gymnemo/mobile/data/DataLayerListenerService.kt): Receptor Wear OS y exportación a Google Sheets.

---

## 🚀 Cómo Compilar e Instalar

### Prerrequisitos
* Android Studio (Ladybug o superior).
* JDK 17 configurado en el sistema.

### Compilación
```bash
./gradlew assembleDebug
```
Instala el módulo `:app` en el reloj y el `:mobile` en el teléfono.

> [!IMPORTANT]
> Ambos módulos deben instalarse con el mismo `applicationId` (`com.jmcaamanog.gymnemo`) para que la sincronización Wear OS funcione.

---

## 📅 Historial de Versiones

### v2.5.0 — Julio 2026 (Actual)
* 🔑 **Fix crítico:** Añadido permiso `INTERNET` en el Manifest del móvil (necesario para todas las conexiones de red).
* 📊 **Google Sheets funcional:** Exportación via GET con parámetros URL (evita el problema del redirect 302 de Google Apps Script).
* 📶 **Sincronización manual:** Botón Wifi en el menú del dashboard del reloj.
* ✏️ **Registro Manual:** Tarjeta en la pestaña Historial del móvil para registrar entrenamientos sin reloj.
* 🔄 **Actualizador mejorado:** Mensajes claros de estado (al día / nueva versión / error de conexión).
* 🔒 **Privacidad:** Eliminada URL personal del código fuente público.

### v2.4.0 — Julio 2026
* 🔗 Unificación del `applicationId` en `com.jmcaamanog.gymnemo` para Wear OS Data Layer.
* ☁️ Integración Google Sheets con webhook configurable.
* 📐 Ajustes visuales en selector de peso del reloj.
* 🛡️ Carpetas privadas excluidas del repositorio público.

### v1.0.0 — Julio 2026
* 🚀 Lanzamiento inicial con sincronización Bluetooth, 15 mejoras premium, mapa de calor anatómico y exportador JSON/CSV.

---

## 👨‍💻 Autor

**Jose Manuel Caamaño González** | Arquitecto Técnico & BIM Manager
Digital Product Lead | ConTech & Digital Twin SaaS | Data Analytics (SQL, Power BI)

Hecho con código y café desde A Coruña. ☕
[LinkedIn](https://www.linkedin.com/in/jmcaamanog/) · [Web](https://jmcaamanog.pages.dev)
