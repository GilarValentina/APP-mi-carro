# MiCarro: auditoría y mejoras para la versión de presentación

Rama: `gilar/mejoras-presentacion` (creada desde `main`, sin commits todavía).
Arquitectura: se conserva MVVM, inyección manual en `AppContainer`, Room, Navigation Compose y WorkManager. No se agregaron dependencias nuevas.

## 1. Análisis del estado inicial

| Estado | Qué había |
|---|---|
| **Existía** | Vehículos con CRUD y vehículo principal (RF-01..05); plan de mantenimiento con estados (RF-10..16); mantenimientos y repuestos (RF-17..27); documentos y alertas con WorkManager (RF-28..33); historial, resumen de gastos y CSV (RF-34..38); respaldo y borrado (RF-39, RF-40); splash; dashboard con resumen. |
| **Parcial** | Kilometraje: solo existía la primera lectura al crear el vehículo, sin pantalla (la tarjeta del inicio no hacía nada). Pico y placa: solo un texto genérico por ciudad. Formulario de vehículos: año, color y cilindraje se escribían a mano. Alertas: solo de documentos, sin mantenimientos ni citas. Columna `archivado` sin interfaz. |
| **Faltaba** | Onboarding y perfil del usuario, permiso de notificaciones explicado, ajustes, agenda de citas, guías de emergencia, plan preventivo por kilometraje con barras de progreso, fecha de expedición y adjuntos en documentos. |
| **Errores encontrados** | `dynamicColor = true` reemplazaba la paleta institucional por los colores del fondo de pantalla en Android 12+. Los colores "container" eran semitransparentes, por eso los botones flotantes se veían grises y los menús, lavanda. La placa perdía letras al escribirla, porque se transformaba mientras el teclado componía. El chip "Ver detalle" del inicio era invisible. La pantalla del plan no tenía botón Volver. |

> Nota: no se tuvo acceso al documento de requisitos. La correspondencia con los RF se basa en las referencias que ya estaban en el código.

## 2. Requisitos funcionales

| RF | Módulo | Estado tras esta versión |
|---|---|---|
| RF-01..05 | Vehículos | **Mejorados**: 6 tipos, placa validada en vivo por tipo, selectores con búsqueda, ciudad de matrícula, secretaría de tránsito, archivar y restaurar |
| RF-06..09 | Kilometraje | **Completados**: registro, validaciones, historial, gráfica, estadísticas y aviso de odómetro desactualizado |
| RF-10..16 | Plan | **Mejorados**: plan base de 5.000/10.000/20.000 km, barras de progreso, orden por urgencia, confirmación al eliminar |
| RF-17..27 | Mantenimientos y repuestos | Sin cambios |
| RF-28..33 | Alertas y documentos | **Mejorados**: expedición, adjunto, anticipación por documento (1/3/7/15/30 días), avisos de mantenimientos y citas |
| RF-34..36, RF-38 | Historial | Sin cambios |
| RF-37 | Inicio | **Mejorado**: saludo, vehículo principal, pico y placa del día, accesos rápidos, próxima cita, guías y skeleton loading |
| RF-39, RF-40 | Respaldo | Sin cambios (respalda el archivo completo de la BD, incluida la tabla `citas`) |

## 3. Funcionalidades nuevas

- **Onboarding** (`ui/perfil/OnboardingScreen.kt`): nombre, apellido, ciudad, correo y teléfono opcional, guardados en el dispositivo. Luego explica y solicita el permiso de notificaciones. Se muestra una sola vez.
- **Ajustes** (`ui/perfil/AjustesScreen.kt`): edición del perfil, estado del permiso de notificaciones (si se rechazó, abre los ajustes del sistema), interruptor de alertas, anticipación general e interruptor de *push* (solo guarda la preferencia).
- **Pico y placa** (`domain/picoyplaca/PicoYPlaca.kt`): interfaz `ProveedorPicoYPlaca`, preparada para una fuente en línea, con reglas **de referencia** para Bogotá, Medellín, Cali, Bucaramanga, Pereira y Cartagena. El panel siempre indica que se debe verificar con la secretaría.
- **Kilometraje** (`ui/kilometraje/`, `data/repository/KilometrajeRepository.kt`): cada lectura recalcula el odómetro del vehículo.
- **Plan preventivo base y progreso** (`domain/plan/Plantillas.kt`): `PlanPreventivoBase` y `ProgresoCalculator`. Los colores son verde (al día), amarillo (próximo) y rojo (vencido).
- **Agenda de citas** (`ui/agenda/`, `core/alertas/ProgramadorCitas.kt`): taller, tecnomecánica, SOAT, seguro y mantenimiento, con fecha en calendario, hora en reloj y recordatorio de WorkManager con la anticipación elegida.
- **Guías y ayuda en carretera** (`ui/guias/GuiaScreen.kt`, `domain/guias/Guias.kt`): accidente, pinchazo, batería, sobrecalentamiento, vehículo varado y números de emergencia (123, #767, 119, 132, 144). El botón abre el marcador del teléfono, pero no hace la llamada.
- **Componentes** (`ui/componentes/Selectores.kt`): selector desplegable con búsqueda, campo de hora, chips de anticipación, barra de progreso y skeleton. Los campos de fecha y hora se abren al tocar cualquier parte del campo.

## 4. Base de datos

La versión pasa de 2 a 3 con `MIGRATION_2_3`, que conserva los datos:

- `vehiculos`: + `ciudadMatricula`, `secretariaTransito`.
- `documentos`: + `fechaExpedicion`, `adjuntoUri`, `anticipacionDias`.
- Nueva tabla `citas`, con llave foránea en cascada hacia `vehiculos`.

El SQL de la migración coincide con `app/schemas/.../3.json`. La migración se probó en el emulador sobre una base de datos v2 con datos reales: los vehículos, documentos y actividades se conservaron.

## 5. Archivos

**Modificados (33):** `MainActivity`, `AlertasWorker`, `MiCarroDatabase`, `AppContainer`; los DAO `Actividad`, `Categoria`, `Kilometraje` y `Vehiculo`; las entidades `Documento` y `Vehiculo`; los repositorios `Alertas`, `PlanMantenimiento` y `Vehiculo`; los modelos `Documento`, `Enums`, `Plantillas`, `Vehiculo` y `VehiculoValidator`; las pantallas `AlertasScreen`, `DocumentoFormScreen`, `CampoFecha`, `HomeScreen` + `HomeViewModel`, `Destinos`, `MiCarroNavHost`, la ruta, pantalla y ViewModel del plan, `Theme`, y las pantallas y ViewModels de vehículos.

**Nuevos:** `ProgramadorCitas`, `CitaDao`, `CitaEntity`, `CitasRepository`, `KilometrajeRepository`, `PerfilRepository`, `domain/{agenda,guias,kilometraje,perfil,picoyplaca}`, `CatalogoFormulario`, `ui/{agenda,guias,kilometraje,perfil}`, `Selectores.kt`, `HomeSecciones.kt`, `NuevasFuncionesTest.kt` y el esquema `3.json`.

**Código retirado:** solo partes de presentación que tienen reemplazo directo: el helper privado `CampoTexto` y los menús desplegables manuales de `VehiculoFormScreen.kt` (ahora `SelectorDesplegable`), y los indicadores circulares de carga del inicio, el plan y los vehículos (ahora skeleton). Ninguna funcionalidad se perdió.

## 6. Verificación

- `assembleDebug` y `testDebugUnitTest` pasan: 70 pruebas, 0 fallos. Hay 16 pruebas nuevas de kilometraje, progreso, placas, pico y placa, perfil y citas.
- Pruebas en el emulador Pixel 7 (API 37), sin crashes: onboarding con validación, migración 2→3, panel, kilometraje (rechazo de valor menor y guardado), plan base, cita con reloj, registro de una moto con todos los selectores, archivar y restaurar, guías, ajustes y documento con validación de fechas.
- **No probado:** el selector de adjuntos (abre el selector del sistema), la entrega real de la notificación de una cita a su hora, el tema oscuro, ni otros tamaños de pantalla.

## 7. Publicidad (recomendación, no implementada)

1. **Vale la pena solo como banner y más adelante.** La app maneja emergencias, vencimientos y recordatorios: interrumpir esos flujos daña la confianza y la utilidad.
2. **Dónde:** un banner adaptable al final del Historial o del Resumen de gastos, que son pantallas de consulta sin urgencia. **Nunca** en guías de emergencia, onboarding, formularios ni alertas.
3. **Formato:**
   - **Banner adaptable de AdMob:** sí.
   - **Rewarded:** opcional, solo para funciones extra (por ejemplo, exportar un PDF del historial).
   - **Interstitial:** no, por el impacto en la experiencia y por el riesgo de incumplir las políticas de Google, que prohíben anuncios que interrumpen tareas o aparecen de forma inesperada.
4. **Requisitos previos:** formulario de consentimiento (UMP), política de privacidad publicada y declaración de anuncios en Play Console. Además, si la app sigue sin servidor, conviene mantenerla funcional sin conexión.
