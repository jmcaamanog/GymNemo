🔱 GymNemo: Guión de Interacciones y Esquema de Pantallas (v1.0)
============================================================

Este documento unifica todo el flujo de navegación, árbol de jerarquías, caminos de usuario, catálogo de iconos y especificaciones de diseño visual de las pantallas de GymNemo para Wear OS.

---

1. Diagrama de Flujo del Usuario (Mermaid)
------------------------------------------

```mermaid
stateDiagram-v2
    [*] --> 1_Main_Portada : Iniciar App

    state 1_Main_Portada {
        [*] --> Y_Split_Main
        Y_Split_Main --> 1_1_Dashboard_Menu : Seleccionar Arriba (ic_dashboard)
        Y_Split_Main --> 1_2_Train_Category : Seleccionar Abajo-Izq (ic_pesa_gym)
        Y_Split_Main --> 1_3_Settings : Seleccionar Abajo-Der (ic_ajustes)
    }

    state 1_1_Dashboard_Menu {
        [*] --> Y_Split_Dashboard_Menu
        Y_Split_Dashboard_Menu --> 1_1_1_Dashboard_Rings : Seleccionar Arriba (ic_dashboard)
        Y_Split_Dashboard_Menu --> 1_1_2_PR_Category : Seleccionar Abajo-Der (TrackChanges)
        Y_Split_Dashboard_Menu --> 1_Main_Portada : Seleccionar Abajo-Izq (ArrowBack)
    }

    state 1_1_1_Dashboard_Rings {
        [*] --> Canvas_Progress_Rings
        Canvas_Progress_Rings --> 1_1_Dashboard_Menu : Swipe Derecho (Atrás)
    }

    state 1_1_2_PR_Category {
        [*] --> Y_Split_PR_Muscles
        Y_Split_PR_Muscles --> 1_1_2_1_PR_Carousel : Seleccionar Brazo / Pierna / Torso
    }

    state 1_1_2_1_PR_Carousel {
        [*] --> Scaling_Scroll_PR_Exercises
        Scaling_Scroll_PR_Exercises --> 1_1_2_2_PR_Detail : Click en Ejercicio Seleccionado
    }

    state 1_1_2_2_PR_Detail {
        [*] --> Personal_Record_Screen
    }

    state 1_2_Train_Category {
        [*] --> Y_Split_Muscles
        Y_Split_Muscles --> 1_2_1_Carousel : Seleccionar Brazo / Pierna / Torso
        Y_Split_Muscles --> 1_2_5_Workout_Complete : Clic en "END" (Rojo)
    }

    state 1_2_1_Carousel {
        [*] --> Scaling_Scroll_Exercises : Escoger 1 de los 7 ejercicios
        Scaling_Scroll_Exercises --> 1_2_2_Countdown : Click en Ejercicio Seleccionado
        Scaling_Scroll_Exercises --> 1_2_Train_Category : Swipe Derecho (Atrás)
    }

    state 1_2_2_Countdown {
        [*] --> Timer_5s : Cuenta regresiva animada
        Timer_5s --> 1_2_3_Active_Workout : Finaliza (Vibración)
    }

    state 1_2_3_Active_Workout {
        [*] --> Active_Timer_HeartRate
        Active_Timer_HeartRate --> 1_2_3_Pause_Screen : Clic en Botón Rojo (Círculo Minimalista)
    }

    state 1_2_3_Pause_Screen {
        [*] --> Y_Split_Pause_Options
        note right of Y_Split_Pause_Options
            El centro muestra el tiempo de descanso (MM:SS)
        end note
        Y_Split_Pause_Options --> log_reps_repeat : REPETIR (Arriba - Click)
        Y_Split_Pause_Options --> 1_2_2_Countdown : REPETIR (Arriba - Long Press) o NUEVA (Abajo-Izq - Click)
        Y_Split_Pause_Options --> log_reps_finish : TERMINAR (Abajo-Der - Click)
    }

    state log_reps_repeat {
        [*] --> Picker_Reps_Repeat
        Picker_Reps_Repeat --> log_weight_repeat : Clic en Cifra REPS
    }
    state log_weight_repeat {
        [*] --> Picker_Weight_Repeat
        Picker_Weight_Repeat --> 1_2_3_Pause_Screen : Clic en Cifra KG (Guarda Serie y Vuelve a Pausa)
    }

    state log_reps_finish {
        [*] --> Picker_Reps_Finish
        Picker_Reps_Finish --> log_weight_finish : Clic en Cifra REPS
    }
    state log_weight_finish {
        [*] --> Picker_Weight_Finish
        Picker_Weight_Finish --> 1_2_Train_Category : Clic en Cifra KG (Guarda Sesión y Vuelve a Zonas)
    }

    state 1_2_5_Workout_Complete {
        [*] --> Summary_Total_Kcal
        Summary_Total_Kcal --> Y_Split_Main : Clic en Botón Check Verde (Cerrar Día)
    }
```

---

2. Tabla de Referencia de Pantallas y Rutas
-------------------------------------------

| Identificador | Nombre de Pantalla | Ruta Composable | Archivo Fuente (Kotlin) | Tipo de Layout | Iconos / UI Utilizada |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **1.0** | Portada Principal | `"main"` | [MainActivity.kt](file:///c:/Users/Jose/OneDrive/GITHUB/GymNemo/app/src/main/java/com/jmcaamanog/gymnemo/presentation/MainActivity.kt) | Radial Y-Split | `ic_dashboard` (arriba -> navigates to `"dashboard_menu"`), `ic_pesa_gym` (abajo-izq), `ic_ajustes` (abajo-der). |
| **1.1** | Menú Intermedio Dashboard | `"dashboard_menu"` | [MainActivity.kt](file:///c:/Users/Jose/OneDrive/GITHUB/GymNemo/app/src/main/java/com/jmcaamanog/gymnemo/presentation/MainActivity.kt) | Radial Y-Split | `ic_dashboard` (arriba -> rings), `Icons.Default.TrackChanges` (abajo-der -> PRs), `Icons.Default.ArrowBack` (abajo-izq -> portada). |
| **1.1.1** | Dashboard (Progreso) | `"dashboard"` | [DashboardScreen.kt](file:///c:/Users/Jose/OneDrive/GITHUB/GymNemo/app/src/main/java/com/jmcaamanog/gymnemo/ui/screens/DashboardScreen.kt) | Canvas Circular | 3 Anillos concéntricos: Cyan (brazo), Magenta (pierna), Amarillo (torso). |
| **1.1.2** | PR Categoría Ejercicios | `"pr_category"` | [MainActivity.kt](file:///c:/Users/Jose/OneDrive/GITHUB/GymNemo/app/src/main/java/com/jmcaamanog/gymnemo/presentation/MainActivity.kt) | Radial Y-Split | `ic_ejercicio_brazo` (arriba), `ic_ejercicio_pierna` (abajo-izq), `ic_ejercicio_torso` (abajo-der). |
| **1.1.2.1**| PR Lista Ejercicios | `"pr_carousel/{part}"`| [ExerciseCarouselScreen.kt](file:///c:/Users/Jose/OneDrive/GITHUB/GymNemo/app/src/main/java/com/jmcaamanog/gymnemo/ui/screens/ExerciseCarouselScreen.kt)| Scaling Lazy Scroll | Lista vertical de 7 ejercicios por zona para escoger cuál consultar. |
| **1.1.2.2**| Detalle de Récord (PR) | `"pr_detail/{exerciseName}"`| [PersonalRecordScreen.kt](file:///c:/Users/Jose/OneDrive/GITHUB/GymNemo/app/src/main/java/com/jmcaamanog/gymnemo/ui/screens/PersonalRecordScreen.kt)| Centralizado | Nombre, icono diana, peso gigante, reps y fecha formateada del récord. |
| **1.2** | Categoría Ejercicio | `"train_category"` | [MainActivity.kt](file:///c:/Users/Jose/OneDrive/GITHUB/GymNemo/app/src/main/java/com/jmcaamanog/gymnemo/presentation/MainActivity.kt) | Radial Y-Split + Botón Inferior | Iconos musculares. Texto inferior rojo "END". |
| **1.2.1** | Carrusel de Ejercicios | `"carousel/{part}"` | [ExerciseCarouselScreen.kt](file:///c:/Users/Jose/OneDrive/GITHUB/GymNemo/app/src/main/java/com/jmcaamanog/gymnemo/ui/screens/ExerciseCarouselScreen.kt) | Scaling Lazy Scroll | Lista vertical escalada de 7 ejercicios por zona muscular. |
| **1.2.2** | Cuenta Atrás | `"countdown"` | [CountdownScreen.kt](file:///c:/Users/Jose/OneDrive/GITHUB/GymNemo/app/src/main/java/com/jmcaamanog/gymnemo/ui/screens/CountdownScreen.kt) | Centralizado Animado | Cuenta atrás animada 5s en verde neón. Haptic al llegar a 0. |
| **1.2.3** | Ejercicio Activo | `"active_workout"` | [ActiveWorkoutScreen.kt](file:///c:/Users/Jose/OneDrive/GITHUB/GymNemo/app/src/main/java/com/jmcaamanog/gymnemo/ui/screens/ActiveWorkoutScreen.kt) | Column Vertical | Nombre, MM:SS gigante, BPM, y botón circular rojo de parada sin texto. Soporte Ambient Mode. |
| **1.2.3.P**| Pantalla de Pausa | `"pause_screen"` | [WorkoutPauseScreen.kt](file:///c:/Users/Jose/OneDrive/GITHUB/GymNemo/app/src/main/java/com/jmcaamanog/gymnemo/ui/screens/WorkoutPauseScreen.kt) | Radial Y-Split + Hub Central | Cronómetro positivo central en Cyan. Arriba: Repetir (Click: log; Long: countdown), Izq: Nueva (dumbbell - Click: countdown), Der: Terminar (Click: log). Alerta Hipoxia (SpO2). |
| **1.2.4.R**| Selector Repeticiones | `"log_reps/{action}"` | [NumberPickerScreen.kt](file:///c:/Users/Jose/OneDrive/GITHUB/GymNemo/app/src/main/java/com/jmcaamanog/gymnemo/ui/screens/NumberPickerScreen.kt) | Radial Y-Split (Picker) | Arriba: Cifra + reps. Abajo-izq: `ic_subir` (↑), Abajo-der: `ic_bajar` (↓). |
| **1.2.4.W**| Selector Peso (Kg) | `"log_weight/{reps}/{action}"`| [NumberPickerScreen.kt](file:///c:/Users/Jose/OneDrive/GITHUB/GymNemo/app/src/main/java/com/jmcaamanog/gymnemo/ui/screens/NumberPickerScreen.kt) | Radial Y-Split (Picker) | Arriba: Cifra + kg (peso sugerido autocompletado). Abajo-izq: Subir, Abajo-der: Bajar. |
| **1.2.5** | Fin de Sesión Diaria | `"workout_complete"` | [WorkoutCompleteScreen.kt](file:///c:/Users/Jose/OneDrive/GITHUB/GymNemo/app/src/main/java/com/jmcaamanog/gymnemo/ui/screens/WorkoutCompleteScreen.kt) | Centralizado | Total kcal del día, texto verde neón, confirmación con botón Check. |
| **1.3** | Ajustes | `"settings"` | [MainActivity.kt](file:///c:/Users/Jose/OneDrive/GITHUB/GymNemo/app/src/main/java/com/jmcaamanog/gymnemo/presentation/MainActivity.kt) | Radial Y-Split | `ic_calendario` (arriba), `ic_cuerpo_persona` (abajo-izq), `ic_peso_bascula` (abajo-der). |
| **1.3.1** | Objetivos (Brazo/etc) | `"objectives"` | [MainActivity.kt](file:///c:/Users/Jose/OneDrive/GITHUB/GymNemo/app/src/main/java/com/jmcaamanog/gymnemo/presentation/MainActivity.kt) | Radial Y-Split | Iconos musculares. |
| **1.3.1.B**| Ajuste Zona Muscular | `"body_settings/{part}"`| [BodySettingsScreen.kt](file:///c:/Users/Jose/OneDrive/GITHUB/GymNemo/app/src/main/java/com/jmcaamanog/gymnemo/ui/screens/BodySettingsScreen.kt) | T-Split (50/50 Vert) | Mitad sup: Días (`ic_reloj_arena`), Mitad inf: Kcal (`ic_kcal`). |
| **1.3.1.F**| Ajuste Frecuencia (Días)| `"frequency/{part}"` | [FrequencySelectionScreen.kt](file:///c:/Users/Jose/OneDrive/GITHUB/GymNemo/app/src/main/java/com/jmcaamanog/gymnemo/ui/screens/FrequencySelectionScreen.kt) | T-Split (50/50 Vert) | Mitad sup: Botones semanales L-M-X-J-V-S-D. Mitad inf: Reloj. |
| **1.3.1.K**| Ajuste Meta Kcal | `"kcal/{part}"` | [NumberPickerScreen.kt](file:///c:/Users/Jose/OneDrive/GITHUB/GymNemo/app/src/main/java/com/jmcaamanog/gymnemo/ui/screens/NumberPickerScreen.kt) | Radial Y-Split (Picker) | Arriba: Cifra + kcal (pasos de 100). Abajo-izq: Subir, Abajo-der: Bajar. |
| **1.3.2** | Perfil Persona | `"profile"` | [ProfileScreen.kt](file:///c:/Users/Jose/OneDrive/GITHUB/GymNemo/app/src/main/java/com/jmcaamanog/gymnemo/ui/screens/ProfileScreen.kt) | Radial Y-Split | `ic_altura` (arriba), Texto `"19XX"` (abajo-izq), sexo (abajo-der). |
| **1.3.2.A**| Ajuste Altura (cm) | `"picker_height"` | [NumberPickerScreen.kt](file:///c:/Users/Jose/OneDrive/GITHUB/GymNemo/app/src/main/java/com/jmcaamanog/gymnemo/ui/screens/NumberPickerScreen.kt) | Radial Y-Split (Picker) | Arriba: Cifra + cm. Abajo-izq: Subir, Abajo-der: Bajar. |
| **1.3.2.Y**| Ajuste Año Nacimiento | `"picker_year"` | [NumberPickerScreen.kt](file:///c:/Users/Jose/OneDrive/GITHUB/GymNemo/app/src/main/java/com/jmcaamanog/gymnemo/ui/screens/NumberPickerScreen.kt) | Radial Y-Split (Picker) | Arriba: Cifra + año. Abajo-izq: Subir, Abajo-der: Bajar. |
| **1.3.3** | Ajuste Peso Rápido (kg)| `"picker_weight"` | [NumberPickerScreen.kt](file:///c:/Users/Jose/OneDrive/GITHUB/GymNemo/app/src/main/java/com/jmcaamanog/gymnemo/ui/screens/NumberPickerScreen.kt) | Radial Y-Split (Picker) | Arriba: Cifra + kg. Abajo-izq: Subir, Abajo-der: Bajar. |

---

3. Los Tres Caminos Clave del Usuario en Pausa
---------------------------------------------

Cuando el usuario está fatigado o sudado en el gimnasio y presiona el **círculo rojo minimalista** de parada, no hay auto-detección falible. El usuario toma control absoluto eligiendo uno de los 3 caminos:

### Camino A: "Registrar Serie" (REPETIR - Botón Superior Verde)
*   **Flujo:**
    1. Clic en **REPETIR** (Botón superior verde neón).
    2. Modifica si es necesario las repeticiones (ej. `12 reps`) en la pantalla radial y confirma pulsando el número.
    3. Modifica si es necesario el peso (ej. `60 kg`, precargado con tu última serie o valor histórico) y confirma pulsando el número.
    4. La serie se guarda en Room DB y el navegador vuelve a la **Pantalla de Pausa** (`pause_screen`), donde el temporizador positivo de descanso sigue corriendo.
    5. **Iniciar serie:** Cuando el usuario esté listo para empezar la siguiente serie activa, puede mantener pulsado (long-press) el botón **REPETIR** en la pausa, lo cual iniciará la cuenta atrás de 5 segundos para volver a la sesión activa.

### Camino B: "Nueva Serie Directa" (NUEVA - Botón Abajo-Izq Dumbbell)
*   **Flujo:**
    1. Clic en **NUEVA** (Botón abajo-izquierda con el icono de la pesa de gimnasio).
    2. Se inicia inmediatamente la cuenta atrás de 5 segundos (`countdown`).
    3. Comienza la sesión de entrenamiento activa de ese mismo ejercicio.

### Camino C: "Terminar Ejercicio" (TERMINAR - Botón Abajo-Der Rojo)
*   **Flujo:**
    1. Clic en **TERMINAR** (Botón abajo-derecha con el icono de stop rojo).
    2. Se abre el flujo radial para guardar las repeticiones y el peso de la última serie realizada.
    3. Tras la confirmación, se abre el test de recuperación cardíaca **HRR (60 segundos)** para medir la fatiga cardiovascular.
    4. Una vez completado o saltado el test, el navegador le devuelve a la pantalla de **Selección de Zona Muscular** (`train_category`).
    5. Aquí el usuario puede elegir otra zona para entrenar (Brazo/Torso/Pierna) o presionar **"END"** (al fondo de la pantalla) para cerrar la sesión de entrenamientos del día completo.

---

4. Catálogo de Iconos XML (drawable/)
-------------------------------------

*   `ic_dashboard.xml`: Panel de control / Anillos de progreso diario.
*   `ic_pesa_gym.xml`: Botón principal de entrenamiento (Entrenar).
*   `ic_ajustes.xml`: Configuración del sistema.
*   `ic_calendario.xml`: Selección de días de entrenamiento (Objetivos semanales).
*   `ic_cuerpo_persona.xml`: Datos del perfil físico.
*   `ic_peso_bascula.xml`: Acceso rápido al peso corporal actual.
*   `ic_altura.xml`: Configuración de altura.
*   `ic_sexo_hombre.xml` / `ic_sexo_mujer.xml`: Indicadores/Toggles de sexo biológico.
*   `ic_ejercicio_brazo.xml`, `ic_ejercicio_pierna.xml`, `ic_ejercicio_torso.xml`: Iconografía de zonas musculares.
*   `ic_reloj_arena.xml`: Configuración del tiempo semanal objetivo.
*   `ic_kcal.xml`: Configuración de meta calórica.
*   `ic_subir.xml` / `ic_bajar.xml`: Flechas para los Selectores de número (Pickers).
*   `splash_icon.xml`: Icono de marca para el arranque de la aplicación.
