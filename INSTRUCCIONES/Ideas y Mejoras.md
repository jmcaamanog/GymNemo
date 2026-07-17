🔱 GymNemo: Ideas y Mejoras (v2.0)
================================

Este documento recoge la filosofía general de la aplicación, el registro histórico detallado de todas las mejoras que hemos implementado con éxito, y la caja de nuevas ideas para el desarrollo futuro.

---

1. Ideas Generales de la App
----------------------------

### Filosofía y Principios de Diseño
*   **Soberanía de Datos:** 100% Offline-first. Almacenamiento local en el reloj (Room DB + DataStore). Sincronización opcional unidireccional con el móvil a través de la Data Layer API de Google Play Services. Cero bases de datos externas en la nube.
*   **Cero Fricción Táctica:** Botones gigantes en formatos optimizados para pantallas circulares: **Radial "Y-Split"** (3 botones en el reloj a 120° de separación) y **Horizontal "T-Split"** (mitades arriba y abajo). Diseñado para ser utilizable con sudor, fatiga o vibraciones durante el ejercicio.
*   **Silencio Visual:** Ausencia de efectos de pulsación invasivos (sin destellos, sin perímetros parpadeantes). El estado de carga y foco de los botones se muestra mediante cambio discreto de color y sutil respuesta háptica de vibración corta, reduciendo distracciones y consumo de energía.
*   **Ahorro de Energía (AMOLED):** Fondo negro puro (`#000000`) en toda la interfaz para apagar píxeles OLED. Uso exclusivo de **Vector Assets (XML)** optimizados en blanco puro (`#FFFFFF`) y gris de bajo brillo (`#808080`) aplicando `tint` dinámico para los estados de selección.

### Metodología y Arquitectura Técnica
*   **MVVM + Clean Architecture:** Módulo `:app` en Wear OS implementa una arquitectura desacoplada donde la capa de presentación (Jetpack Compose Wear OS) observa el estado de negocio expuesto por `WorkoutViewModel`, el cual a su vez consulta `WorkoutRepository` y persiste en la base de datos Room (`GymNemoDatabase`) y en el almacén de claves `UserPreferencesRepository`.
*   **Módulo Móvil `:mobile`:** Módulo complementario que actúa como receptor pasivo de sincronización de datos mediante `WearableListenerService` de Android, persistiendo en la base de datos local del smartphone `WorkoutDb` para mostrar gráficos e historial.

---

2. Mejoras Implementadas
------------------------

A continuación se detallan todas las mejoras tácticas, visuales y de rendimiento que se han integrado en el código del proyecto, ordenadas por su momento de implementación:

| Fecha y HORA | Nombre de la Mejora | Tipo | Qué función hace |
| :--- | :--- | :---: | :--- |
| **2026-07-16 16:30** | Botón de Parada Minimalista | **UX** | Elimina la palabra "STOP" de la pantalla de entrenamiento activo, dejando únicamente el círculo rojo sólido, logrando mayor elegancia. |
| **2026-07-16 17:15** | Selectores Focalizados Y-Split | **UX** | Reemplaza la rueda de scroll estándar por los selectores radiales divididos en tres (arriba número gigante de color, abajo controles de subir/bajar) optimizados para dedos sudados. |
| **2026-07-16 17:45** | Háptica por Eventos | **Eficiencia** | Genera una vibración diferenciada de respuesta al pulsar botones de control y al finalizar la cuenta regresiva de 5s, evitando tener que mirar el reloj. |
| **2026-07-16 18:00** | Registro en Dos Pantallas | **UX** | Divide el log de series en dos pantallas consecutivas independientes (Reps -> Kg) para evitar amontonar elementos en el reloj circular. |
| **2026-07-16 18:15** | Menú de Pausa de 3 Vías | **Funcionalidad** | Crea la pantalla de pausa con opciones radiales Y-Split: Repetir (ejercicio actual), Nueva (empezar ya) o Terminar (volver a zonas musculares). |
| **2026-07-16 18:22** | Botón de Cierre de Sesión Diaria | **Funcionalidad** | Añade el botón rojo "END" al pie de la selección muscular para cerrar la jornada y abrir el resumen del total calórico acumulado. |
| **2026-07-17 08:48** | Autocompletar con Peso Anterior | **Eficiencia** | Carga reactivamente en el picker de KG el último peso utilizado para el ejercicio actual en esta sesión (o el valor del historial de la BD si es el primer set). |
| **2026-07-17 08:49** | Always-On / Ambient Mode en Serie | **Eficiencia** | Pasa el refresco de pantalla a 1Hz en grises y negros puros al entrar en reposo para ahorrar batería, y apaga los clics del botón Stop para evitar toques accidentales. |
| **2026-07-17 08:50** | Alerta de Hipoxia Celeste (SpO2) | **Funcionalidad** | Si el oxígeno en sangre cae del 92% en la pausa, vibra y abre un círculo azul celeste gigante que crece del centro tapando la pantalla hasta que el usuario se recupera. |
| **2026-07-17 08:52** | Test de Recuperación Cardíaca (HRR) | **Funcionalidad** | Al finalizar el ejercicio, inicia un test opcional de 60 segundos que calcula la caída de BPM (HRR) y cataloga el nivel ("EXCELENTE", "BUENA", "MEJORABLE"), guardándolo en la BD. |
| **2026-07-17 09:00** | Estimación de 1RM Reactiva | **Funcionalidad** | Muestra el 1RM teórico en tiempo real (fórmula de Epley) en la esquina superior al ajustar el peso en el selector radial de KG. |
| **2026-07-17 09:00** | Temporizador de Descanso Inteligente | **UX / Eficiencia** | Cambia a verde neón y emite doble vibración en la pausa al alcanzar el tiempo de descanso recomendado basado en las reps de la serie previa (60s / 90s / 120s). |
| **2026-07-17 09:00** | Historial Rápido de Series | **UX** | Muestra de forma automática al pie de la pantalla de pausa la lista compacta de las series ya completadas hoy en ese ejercicio (ej. `12x20 \| 10x24`). |
| **2026-07-17 09:00** | Alerta de Ritmo Cardíaco Límite | **Funcionalidad** | Si las pulsaciones superan el 85% de tu máxima (220 - edad), parpadea el fondo en rojo oscuro de forma intermitente y emite vibraciones pulsantes de seguridad. |
| **2026-07-17 09:15** | Menú de Récords Personales (PR) | **Funcionalidad** | Crea un submenú radial intermedio desde Dashboard para consultar el récord histórico (peso máximo, reps y fecha DD/MM/AAAA) de cada ejercicio. |
| **2026-07-17 09:20** | Exportador CSV / Copias JSON | **Funcionalidad** | Integra un sistema de copias de seguridad en la app móvil para exportar todo el historial en formato CSV (compartible mediante Intent) y realizar backups completos en JSON (importar/exportar). |
| **2026-07-17 09:20** | Gráficas de Evolución en Canvas | **UX / Analítica** | Renderiza una gráfica interactiva en Canvas para cada ejercicio, mostrando la evolución histórica de tus pesos máximos levantados. |
| **2026-07-17 09:50** | Integración de Historial en Círculo | **UX / Estética** | Mueve el historial rápido de series de la pausa al interior del círculo del temporizador para una UI más despejada. |
| **2026-07-17 09:50** | Círculo de Proporción en Picker de KG | **UX / Estética** | Muestra el peso de la serie activa en un círculo concéntrico que detalla las repeticiones hechas arriba (`R: XX`) y el peso abajo. |
| **2026-07-17 09:50** | Botón de Cierre Centralizado (END) | **UX / Diseño** | Coloca el botón "END" de cierre de entrenamiento en un pequeño círculo rojo sólido con letras en negro justo en el centro libre de los selectores radiales. |
| **2026-07-17 09:50** | Rediseño de Tile Wear OS | **Estética** | Aplica los colores de identidad de la marca (negro, gris y azul cian neón) en los botones del mosaico de inicio rápido (Tile). |
| **2026-07-17 11:30** | Mapa de Calor Anatómico (Heatmap) | **UX / Analítica** | Ilustración del cuerpo humano en la pestaña Objetivos del móvil, coloreada en neón según los grupos entrenados. |
| **2026-07-17 11:30** | Esfera de Reloj Oficial (Complicación) | **Funcionalidad** | Refactorización de complicaciones para servir calorías diarias reales del usuario a la esfera oficial de Wear OS. |
| **2026-07-17 11:30** | Resplandor Neon por FC | **Estética / UX** | Borde degradado reactivo (cyan/amarillo/magenta) según pulsaciones en la pantalla de entreno activo del reloj. |
| **2026-07-17 11:30** | Línea de Descanso en AOD | **Eficiencia / Estética** | Fina línea circular minimalista al borde de la pantalla que se consume con el descanso en Always-On del reloj. |
| **2026-07-17 11:30** | Detección de Sobrecarga Progresiva | **Funcionalidad** | Badge neón de "+2.5 KG SUGERIDO" si en la sesión previa lograste las repeticiones objetivo. |
| **2026-07-17 11:30** | Guía y Registro de Tempo | **Funcionalidad** | Visualización en tiempo real de fase (ECC/ISO/CON) y almacenamiento de tempo ("3-0-1-0") por serie en Room DB. |
| **2026-07-17 11:30** | Guardar Escenario ante Cierre | **Infraestructura** | Auto-guardado cada 5s en DataStore y diálogo de reanudación automática al iniciar la app. |
| **2026-07-17 11:30** | Copia Automática al Sincronizar | **Infraestructura** | Exporta una copia JSON local y permite activar copias automatizadas al recibir datos del reloj. |
| **2026-07-17 11:30** | Exportador GPX (Strava) | **Infraestructura** | Generación de archivos XML de entreno estructurados con pulso cardíaco para subir directamente a Strava. |
| **2026-07-17 11:30** | Caché Transaccional de Sync | **Infraestructura** | Encolado de sesiones no sincronizadas en Room con reintentos automáticos ante pérdidas de Bluetooth. |
| **2026-07-17 11:30** | Morphing Dinámico en Pickers | **Animación** | Efecto elástico de escala con muelle (spring animation) en los números al incrementar/decrementar valores. |
| **2026-07-17 11:30** | Confeti en Récords Personales (PR) | **Animación** | Lluvia de partículas de confeti de colores neón sobre la pantalla al conseguir una marca personal en la pausa. |
| **2026-07-17 11:30** | Ola de Líquido en Hipoxia | **Animación** | Simulación Canvas de ola de agua celeste que inunda el reloj si el SpO2 baja de 92% y baja al recuperarse. |
| **2026-07-17 11:30** | Rotación 3D de Iconos en Carrusel | **Animación** | Perspectiva cilíndrica tridimensional (rotationX) según inercia y distancia al centro del scroll en el reloj. |
| **2026-07-17 11:30** | Anillos de Actividad Latientes | **Animación** | Pulso orgánico en los anillos del móvil sincronizado a la frecuencia cardíaca promedio de la semana. |

---

3. Ideas de Mejora (Caja de Ideas)
----------------------------------

Estas son las propuestas activas a considerar para futuras iteraciones del proyecto:

| Nombre de la Idea | Tipo | Qué función hace | Quién la sugirió |
| :--- | :---: | :--- | :--- |
| **Selector de Temas de Color** | Visual | Temas estéticos completos e intercambiables en la app móvil (Neon Cyberpunk, Steel Gym, Forest Green). | Antigravity AI |
| **Autodetección de Ejercicio** | Funcionamiento | Reconoce flexiones, bíceps o sentadillas con el acelerómetro del reloj y las preselecciona en el carrusel de inicio. | Antigravity AI |
| **Registro de RPE (Fatiga Percibida)** | Funcionamiento | Escala radial del 1 al 10 al acabar el entreno para registrar el nivel de esfuerzo percibido y calcular fatiga acumulada. | Antigravity AI |
| **Pausa Inteligente Anti-Olvidos** | Funcionamiento | Pausa automáticamente el entreno si tu pulso baja a niveles basales y no hay movimiento durante 3 minutos. | Antigravity AI |
| **Pantalla Espejo Móvil (Mirror Mode)** | Infraestructura | Visualiza en tiempo real en la pantalla del móvil el temporizador y pulso que registra tu reloj en la muñeca. | Antigravity AI |
