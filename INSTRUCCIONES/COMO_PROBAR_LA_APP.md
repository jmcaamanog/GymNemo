🔱 GymNemo: Guía de Instalación y Pruebas (v1.0)
==============================================

Esta guía detalla los pasos para instalar y probar tanto la aplicación del reloj (Wear OS) como la del móvil en tus dispositivos reales o emuladores utilizando Android Studio.

---

1. Compilación del Proyecto
----------------------------

Dado que Android Studio bloquea los archivos de compilación intermedios cuando está abierto, **no compiles desde una terminal externa** si el entorno está en ejecución. Sigue estos pasos:

1.  Abre el proyecto **GymNemo** en Android Studio.
2.  Haz clic en **"Sync Project with Gradle Files"** (el icono del elefante con una flecha circular arriba a la derecha) si has realizado cambios en la configuración.
3.  Ve al menú superior de Android Studio: **Build > Rebuild Project**.
4.  Esto generará de forma limpia los dos ejecutables APK:
    *   **Reloj (Wear OS):** `c:\Users\Jose\OneDrive\GITHUB\GymNemo\app\build\outputs\apk\debug\app-debug.apk`
    *   **Móvil (Smartphone):** `c:\Users\Jose\OneDrive\GITHUB\GymNemo\mobile\build\outputs\apk\debug\mobile-debug.apk`

---

2. Instalación de la Aplicación Móvil
-------------------------------------

Como tu proyecto está alojado en tu carpeta de **OneDrive**, puedes instalarlo directamente en tu smartphone de forma inalámbrica:

1.  Abre la aplicación de **OneDrive** en tu teléfono móvil.
2.  Navega a la carpeta de tu repositorio: `GymNemo/mobile/build/outputs/apk/debug/`.
3.  Toca el archivo **`mobile-debug.apk`**.
4.  Si tu teléfono te indica que el navegador o OneDrive no tienen permisos para instalar aplicaciones de origen desconocido, pulsa en **Ajustes** en el diálogo de alerta y activa la casilla **"Permitir desde esta fuente"**.
5.  Completa la instalación.

---

3. Instalación de la Aplicación del Reloj (Samsung Galaxy Watch 7)
------------------------------------------------------------------

Para instalar la aplicación en tu Galaxy Watch 7 de forma inalámbrica:

### Paso A: Activar la Depuración en el Reloj
1.  En tu reloj, ve a **Ajustes > Acerca del reloj > Información de software**.
2.  Pulsa **7 veces** seguidas sobre **Versión de software** hasta ver el aviso: *"Las opciones de desarrollador se han activado"*.
3.  Vuelve atrás y entra en el nuevo menú **Opciones de desarrollador**.
4.  Activa **Depuración ADB** (ADB Debugging).
5.  Activa **Depurar con Wi-Fi** (Wireless Debugging) y conéctate a la misma red Wi-Fi que tu ordenador.

### Paso B: Conectar desde el Ordenador
1.  En el reloj (dentro de "Depurar con Wi-Fi"), anota la dirección IP y el puerto (ej: `192.168.1.50:5555`).
2.  Abre una terminal o el terminal integrado de Android Studio y ejecuta:
    ```powershell
    adb connect 192.168.1.50:5555
    ```
3.  Acepta la clave de depuración en la pantalla del reloj eligiendo *"Permitir siempre"*.

### Paso C: Enviar el APK al Reloj
1.  Una vez conectado, ejecuta en tu terminal:
    ```powershell
    adb install -r c:\Users\Jose\OneDrive\GITHUB\GymNemo\app\build\outputs\apk\debug\app-debug.apk
    ```
2.  El APK se transferirá e instalará. El icono de GymNemo aparecerá en tu reloj.

---

4. Ciclo de Pruebas de Sincronización
-------------------------------------

1.  Abre la aplicación **GymNemo Mobile** en tu teléfono.
2.  Abre **GymNemo** en tu reloj.
3.  En el reloj, pulsa **Entrenar** (icono de pesa) -> Selecciona una categoría (ej. Brazo) -> Selecciona un ejercicio -> Espera la cuenta atrás e inicia el entrenamiento.
4.  Al pulsar el **botón rojo minimalista** del reloj, registra las repeticiones y el peso.
5.  Entrarás en la pantalla de pausa. Pulsa **Terminar** (cuadrado rojo) para finalizar el ejercicio.
6.  En la pantalla de categorías del reloj, pulsa **END**. Esto guardará la sesión en la base de datos local y, si el Bluetooth está encendido, enviará los datos automáticamente por la Data Layer API de Google.
7.  Abre la aplicación móvil y verifica que el entrenamiento aparezca reflejado de inmediato en tu listado de historial sincronizado.
