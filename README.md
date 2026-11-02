# MiCarro — Sistema Móvil para el Control de Mantenimiento Vehicular

![Android](https://img.shields.io/badge/Android-Native-3DDC84?style=flat-square)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF?style=flat-square)

MiCarro es una aplicación nativa para el sistema operativo Android diseñada para administrar de forma centralizada y organizada el mantenimiento preventivo y correctivo de vehículos particulares, el registro de kilometraje, repuestos, costos asociados y documentos legales.

---

## Problema que Resuelve

Muchos propietarios de vehículos conservan facturas impresas, registros dispersos o dependen de la memoria para realizar actividades periódicas como el cambio de aceite, revisión de frenos o la renovación de documentos legales (SOAT y revisión técnico-mecánica). MiCarro consolida esta información y genera recordatorios oportunos basados en fechas y kilometraje reportado.

---

## Características Principales (MVP)

* **Gestión de Vehículos:** Registro de automóviles, camionetas y motocicletas con datos básicos (placa, marca, línea, modelo, año, odómetro actual) y selección de vehículo principal.
* **Control de Kilometraje:** Historial de lecturas con validaciones automáticas ante inconsistencias o lecturas inferiores a la anterior.
* **Plan de Mantenimiento:** Creación y programación de actividades preventivas por fecha, kilometraje o ambos criterios, con categorías personalizables.
* **Registro de Mantenimientos y Repuestos:** Control detallado de servicios realizados, mano de obra, repuestos instalados con datos de proveedor y garantía.
* **Alertas Locales y Documentos:** Recordatorios offline programados mediante WorkManager para actividades próximas a vencer y vencimientos de documentos legales.
* **Historial y Resumen de Gastos:** Filtros avanzados por fecha, categoría y taller, acompañados de resúmenes consolidados de costos.
* **Exportación y Respaldo:** Capacidad de exportar historiales y generar copias de seguridad locales.

---

## Stack Tecnológico y Arquitectura

* **Lenguaje de Programación:** [Kotlin](https://kotlinlang.org/)
* **Interfaz de Usuario:** [Jetpack Compose](https://developer.android.com/jetpack/compose) / Material Design 3
* **Persistencia de Datos:** [Room Database](https://developer.android.com/training/data-storage/room) (SQLite)
* **Procesos en Segundo Plano:** [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
* **Navegación:** Jetpack Navigation
* **Patrón de Arquitectura:** Model-View-ViewModel (MVVM) orientado a Clean Architecture

```
UI (Compose) ──> ViewModel ──> Repository ──> Room DAO
```

---

## Equipo de Desarrollo

* **Gilar** — Módulo de Vehículos, Lecturas de Kilometraje, Historial Cronológico y Resumen de Gastos.
* **Tomas** — Módulo de Plan de Mantenimiento, Registro de Servicios, Repuestos y Alertas/WorkManager.

---

## Requisitos de Instalación y Ejecución

### Prerrequisitos
* **Android Studio:** Ladybug (2024.2.1) o versión superior.
* **JDK:** Java 17 o superior.
* **Dispositivo / Emulador:** Android 8.0 (API Level 26) o superior.

### Pasos para Ejecutar el Proyecto
1. Clonar el repositorio:
   ```bash
   git clone https://github.com/GilarValentina/Proyecto_MiCarro.git
   cd Proyecto_MiCarro
   ```
2. Abrir el proyecto en **Android Studio**.
3. Sincronizar los archivos del proyecto con Gradle (*Sync Project with Gradle Files*).
4. Seleccionar un emulador o dispositivo físico con la opción de depuración USB activa.
5. Ejecutar la aplicación (*Run 'app'*).

---

## Privacidad y Modo Offline (RNF-04 / RNF-08)

* **Operación Offline:** La aplicación opera de manera local sin depender de conectividad a Internet. La totalidad de la información se conserva de forma segura en el almacenamiento interno del dispositivo.
* **Privacidad de Datos:** La solución no recopila ni transmite información sensible (como placas o números VIN) a servidores externos.

---

## Seguridad de proyecto y validación antes de ejecutar

Para evitar que Android Studio compile una copia antigua del proyecto, siempre debe mantenerse una sola carpeta activa y conocida:

- Ruta correcta: `C:\Users\ASUS\AndroidStudioProjects\Proyecto_MiCarro`
- Ruta vieja / duplicada que debe descartarse: `C:\Proyecto_MiCarro-main`

### Verificación de ruta antes de ejecutar
1. Abrir Android Studio.
2. Confirmar que la ventana del proyecto muestra la ruta `C:\Users\ASUS\AndroidStudioProjects\Proyecto_MiCarro`.
3. En la barra de estado o en el proyecto abierto, verificar que el directorio raiz coincide con esa carpeta.
4. Ejecutar el script PowerShell: `powershell -ExecutionPolicy Bypass -File .\verify-project.ps1`
5. Si la verificación falla, no compilar ni instalar APK; primero reabre el proyecto correcto.

### Checklist de validación para presentación
- [ ] Ruta del proyecto correcta.
- [ ] Rama Git correcta (`git status` y `git branch --show-current`).
- [ ] Sincronización Gradle exitosa (`Sync Project with Gradle Files`).
- [ ] Build exitoso con `./gradlew assembleDebug`.
- [ ] APK instalado corresponde a la última versión y no a la copia vieja.
- [ ] La app muestra el identificador visible de presentación.

### Recomendación permanente
- Eliminar la carpeta vieja del entorno cuando ya no sea necesaria.
- No abrir la carpeta `C:\Proyecto_MiCarro-main` en Android Studio.
- Eliminar cualquier proyecto reciente que apunte a la ruta antigua desde `File > Open Recent` or `File > Manage IDE Settings > Restore Default Settings` (si fuera necesario).
- Mantener solo una copia del repositorio en el sistema.

## Licencia y Contexto Académico
 
Proyecto Integrador desarrollado para el **Curso de Aplicaciones Móviles**.
