# 🔱 GymNemo — Offline Workout Tracker (Wear OS & Android)

### La herramienta libre y 100% offline para el control y registro de entrenamientos de fuerza.
*Desarrollado con foco en la usabilidad táctil en Wear OS y analíticas fluidas en Android Companion.*

---

<p align="center">
  <a href="https://github.com/jmcaamanog/GymNemo/releases/latest"><img src="https://img.shields.io/github/v/release/jmcaamanog/GymNemo?label=Version&color=00e5ff&logo=github" alt="Versión"></a>
  <a href="https://github.com/jmcaamanog/GymNemo/stargazers"><img src="https://img.shields.io/github/stars/jmcaamanog/GymNemo?style=flat&label=Stars&color=ff007f&logo=github" alt="Stars"></a>
  <a href="https://github.com/jmcaamanog/GymNemo/blob/main/LICENSE"><img src="https://img.shields.io/badge/Licencia-MIT-8b5cf6.svg" alt="Licencia"></a>
  <a href="https://jmcaamanog.github.io/GymNemo/landing.html"><img src="https://img.shields.io/badge/Plataforma-Wear%20OS%20%7C%20Android-39ff14" alt="Plataformas"></a>
  <a href="https://www.linkedin.com/in/jmcaamanog/"><img src="https://img.shields.io/badge/Profesi%C3%B3n-Arquitectos%20T%C3%A9cnicos-2e7d32?logo=micro%3Abit&logoColor=white" alt="Profesión"></a>
</p>

---

## ⚡ Enlaces y Accesos Rápidos

| 🌟 Recurso | 🚀 Acción / Enlace | 📝 Descripción |
| :--- | :--- | :--- |
| **Repositorio Remoto** | 👉 **[GitHub Repository](https://github.com/jmcaamanog/GymNemo)** | Acceso al código fuente privado de la aplicación. |
| **Documentación** | 📁 **[Ver Instrucciones](./INSTRUCCIONES)** | Planes de desarrollo, roadmap e ideas del proyecto. |
| **Fichero de Datos** | 📊 **[Estructura de Datos](./INSTRUCCIONES/DATOS_GUARDADOS.md)** | Detalles sobre el esquema relacional y el protocolo Bluetooth. |

---

> [!IMPORTANT]
> ### 📥 Descarga de Aplicaciones e Instalación (V1.0.0 Oficial)
> *   **⌚ Wear OS Reloj (Módulo `:app`):**
>     *   [Descargar app-debug.apk](./app/build/outputs/apk/debug/app-debug.apk) *(Para instalar vía ADB / Bugjaeger en Galaxy Watch o Pixel Watch)*.
> *   **📱 Android Móvil (Módulo `:mobile`):**
>     *   [Descargar mobile-debug.apk](./mobile/build/outputs/apk/debug/mobile-debug.apk) *(App Companion para sincronización, historial y analítica)*.
>
> 🔒 **Privacidad:** La aplicación es 100% local, no requiere servidores ni cuentas externas. Tus datos permanecen en tus dispositivos.

---

### 👨‍💻 Creador y Diseñador
Desarrollado y diseñado por `José Manuel Caamaño González` ([LinkedIn](https://www.linkedin.com/in/jmcaamanog/)), Arquitecto Técnico y BIM Manager.
*Siguiendo la filosofía del software libre de egos, utilidad técnica real y soberanía absoluta sobre los datos de entrenamiento y salud de los usuarios.*

---

## 🌟 Características Principales (GymNemo Premium)

| Módulo | Icono | Funcionalidades Destacadas |
| :--- | :---: | :--- |
| **UX Radial "Y-Split"** | ⌚ | Tres botones gigantes colocados a 120° en el reloj, ideales para dedos sudados o vibraciones al levantar peso. |
| **Resplandor Neon por FC** | 💓 | Borde circular reactivo en el reloj que pulsa y cambia de color según tu zona de ritmo cardíaco (Cyan < 120, Amarillo < 140, Magenta >= 140 BPM). |
| **Sobrecarga Progresiva** | 📈 | Badge neón `+2.5 KG SUGERIDO` en el selector de peso si lograste tu rango de repeticiones en la sesión previa de ese ejercicio. |
| **Guía de Tempo Rítmico** | ⏱️ | Indicador visual dinámico en el reloj de las fases excéntrica (bajar), isométrica (pausa) y concéntrica (subir) para control de hipertrofia. |
| **Alerta de Hipoxia Celeste** | 🫁 | Al caer el SpO2 por debajo de 92%, vibra y despliega una animación de ola de agua celeste que inunda la pantalla hasta recuperarse. |
| **Test de Recuperación (HRR)** | 🩺 | Test de 60 segundos tras el entrenamiento que evalúa la caída de BPM y cataloga tu salud cardíaca (Excelente, Buena, Mejorable). |
| **Always-On en Descanso** | 🔋 | Pantalla de pausa a 1Hz en grises y negros con una línea circular exterior que se consume indicando el descanso restante. |
| **Rotación 3D en Carrusel** | 🌀 | Perspectiva cilíndrica tridimensional interactiva en el menú de carrusel de ejercicios del reloj. |
| **Guardado de Escenario** | 💾 | Auto-guardado en DataStore cada 5s; pregunta "¿Reanudar entreno?" en un diálogo nativo si la app se cierra inesperadamente. |
| **Confeti de Récord (PR)** | 🎉 | Animación de partículas de colores neón sobre la pantalla del reloj cuando bates tu récord de fuerza en un ejercicio. |
| **Anillos Latientes** | 🫀 | Los anillos concéntricos del móvil (Frecuencia, Kcal, Tiempo) pulsan imitando el ritmo cardíaco promedio de la semana. |
| **Mapa de Calor Anatómico** | 🧍 | Silueta humana Canvas en el móvil que ilumina en neón los grupos musculares entrenados durante la semana (Brazo, Torso, Pierna). |
| **Exportador GPX (Strava)** | 🗺️ | Generación automática de archivos GPX con datos de pulso y telemetría por segundo, listos para importarse en Strava o Garmin. |
| **Caché Transaccional** | 📡 | Encolamiento inteligente de entrenamientos pendientes de sincronizar por si el reloj pierde conexión Bluetooth con el móvil. |

---

## Historial de Versiones

### 📅 17/07/2026 — Versión 1.0.0 (Actual)
* **15 Mejoras Premium Integradas:** Lanzamiento conjunto del módulo de reloj y aplicación companion móvil con soporte para telemetría avanzada, test HRR, hipoxia, ola de agua Canvas, confeti de récords y mapa de calor anatómico.
* **Capa de Sincronización Bluetooth:** Implementación de Data Layer API de Google Play Services con formato transaccional `"ejercicio:peso:reps:tempo"`.
* **Copia de Seguridad JSON:** Exportador manual y automático al recibir sesiones del reloj.

---

## 📁 Estructura del Directorio

*   **[`/app`](./app)**: Módulo de la aplicación nativa para Wear OS (Kotlin, Jetpack Compose).
    *   [MainActivity.kt](./app/src/main/java/com/jmcaamanog/gymnemo/presentation/MainActivity.kt): Controlador principal, navegación y diálogo de recuperación.
    *   [WorkoutViewModel.kt](./app/src/main/java/com/jmcaamanog/gymnemo/viewmodel/WorkoutViewModel.kt): Gestión de estados, timer de entreno y guardado automático.
    *   [ActiveWorkoutScreen.kt](./app/src/main/java/com/jmcaamanog/gymnemo/ui/screens/ActiveWorkoutScreen.kt): UI de entreno activo con resplandor neón y guía de tempo.
    *   [WorkoutPauseScreen.kt](./app/src/main/java/com/jmcaamanog/gymnemo/ui/screens/WorkoutPauseScreen.kt): Temporizador de descanso, Canvas de hipoxia, confeti y arco AOD.
    *   [NumberPickerScreen.kt](./app/src/main/java/com/jmcaamanog/gymnemo/ui/screens/NumberPickerScreen.kt): Pickers Y-Split con morphing elástico y badge de sobrecarga progresiva.
*   **[`/mobile`](./mobile)**: Módulo de la app Companion para teléfonos Android.
    *   [MainActivity.kt](./mobile/src/main/java/com/jmcaamanog/gymnemo/mobile/MainActivity.kt): Interfaz por pestañas con Canvas de evolución, mapa de calor corporal y anillos latientes.
    *   [DataLayerListenerService.kt](./mobile/src/main/java/com/jmcaamanog/gymnemo/mobile/data/DataLayerListenerService.kt): Receptor de sincronización y disparador de respaldos JSON.
*   **[`/INSTRUCCIONES`](./INSTRUCCIONES)**: Guías, Roadmap e historial de ideas.
    *   [DATOS_GUARDADOS.md](./INSTRUCCIONES/DATOS_GUARDADOS.md): Protocolo de sincronización y esquemas de Room DB.

---

## 🚀 Cómo Compilar e Instalar

### Prerrequisitos
* Tener instalado Android Studio (Ladybug o superior).
* JDK 17 configurado en el sistema.

### Compilación Completa
1. Abre el proyecto en Android Studio.
2. Sincroniza Gradle.
3. Para compilar desde terminal:
   ```bash
   ./gradlew assembleDebug
   ```
4. El reloj instalará la app del módulo `:app` y el teléfono la del módulo `:mobile`.

---

## 👨‍💻 Autor de la versión mejorada

**Jose Manuel Caamaño González** | Arquitecto Técnico & BIM Manager.
Digital Product Lead | ConTech & Digital Twin SaaS | BIM, Energy Modeling & Sustainability | Data Analytics (SQL, Power BI)

Hecho con código y café desde A Coruña. ☕
[LinkedIn](https://www.linkedin.com/in/jmcaamanog/)
