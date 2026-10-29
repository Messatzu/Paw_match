# Historial de desarrollo de Paw Match

Fecha de la conversación: 22 de octubre de 2026.

Este documento resume los pedidos, los cambios realizados y los pendientes para poder retomar el trabajo en futuras conversaciones.

## Login y registro

Se pidió conectar el botón de registrarse de `fragment_login` con `fragment_register`.

En `app/src/main/java/com/example/paw_match/ui/auth/LoginFragment.java` se corrigió el listener existente de `btnGoToRegister`:

- Se utiliza `getParentFragmentManager()`.
- El registro reemplaza el login en su mismo contenedor mediante `replace(getId(), new RegisterFragment())`, en lugar de utilizar `android.R.id.content`.
- Se agregó `setReorderingAllowed(true)`.
- Se conservó `addToBackStack(null)` para volver al login con Atrás.

## Pantalla principal

Después se pidió reformular los layouts porque el IDE señalaba la falta de la pantalla principal. `MainActivity.java` existía, pero su layout `activity_main.xml` había sido eliminado y la actividad todavía iniciaba una demo.

- Se recreó `app/src/main/res/layout/activity_main.xml` con un `FrameLayout` identificado como `main` y un `FragmentContainerView` identificado como `fragment_container`.
- Se ajustó `app/src/main/java/com/example/paw_match/MainActivity.java` para usar ese layout y abrir `LoginFragment` al iniciar.
- El login se carga solamente cuando `savedInstanceState == null`, permitiendo que Android restaure la pantalla actual al recrear la actividad.
- Se mantuvo el ajuste del contenido a las barras del sistema.
- Se retiró de la actividad la navegación de la demo, los controles de cambio de modo, explorar, solicitudes y reiniciar, y el guardado de `pm_refugio`.

El flujo resultante es: inicio de la app → login → botón Regístrate → registro → Atrás → login.

## Ajustes adicionales para compilar

Además de la navegación y el contenedor principal, se corrigieron referencias a recursos inexistentes. En total, el agente cambió nueve archivos de la aplicación:

| Archivo | Cambio |
| --- | --- |
| `MainActivity.java` | Inicio en el login y retiro de la navegación de la demo. |
| `ui/auth/LoginFragment.java` | Corrección de la navegación al registro. |
| `res/layout/activity_main.xml` | Recreación del contenedor principal de fragments. |
| `res/layout/fragment_login.xml` | Reemplazo del `ImageView` que utilizaba el recurso inexistente `logo_paw_match` por el texto «Paw Match», verde y en negrita. |
| `ui/demo/DemoFragment.java` | Eliminación de la clase, que dependía del layout ya eliminado `fragment_pm_demo.xml`. |
| `ui/profile/ProfileFragment.java` | Retiro de la búsqueda y configuración de `recyclerAdoptedPets`, que no existía en el layout, junto con sus imports. |
| `res/layout/fragment_profile.xml` | Eliminación del atributo incorrecto `text="Mi Mascota Adoptada"`; se conservó el atributo válido `android:text`. |
| `res/layout/fragment_publisher_pets.xml` | Incorporación de un contenedor `FrameLayout` y del botón flotante `fabAddPet`, que la clase Java buscaba pero el layout no tenía. |
| `res/values/strings.xml` | Incorporación de `paw_match_brand` («Paw Match») y `add_pet` («Registrar mascota»). |

Los archivos Java de la tabla pertenecen a `app/src/main/java/com/example/paw_match/`; los recursos pertenecen a `app/src/main/`.

## Revisión de backend

Se consultó si la aplicación estaba utilizando la carpeta `backend`. La revisión encontró que:

- La app no realizaba llamadas HTTP al backend ni tenía una conexión implementada con una base de datos.
- Login y registro validaban campos y mostraban mensajes mediante `Toast`; el mensaje «Conectando con PHP...» no correspondía a una petición real.
- `RegisterFragment.java` tenía un TODO para implementar el envío de datos a PHP.
- Cuando se inspeccionó, `backend/public/index.php` solo devolvía un JSON de prueba con el nombre de la aplicación y `estado: ok`.
- El código de datos de demo, como `PawMatchRepository` y `DatosDemo`, trabaja en memoria y es independiente del backend.

El agente solamente inspeccionó `backend`; no modificó ni eliminó sus archivos.

## Verificación y estado de Git

La primera ejecución de Gradle quedó bloqueada por restricciones de escritura en la caché del usuario. Después se utilizó el Gradle instalado con un directorio de usuario temporal y la caché de dependencias en modo de solo lectura.

La tarea `:app:assembleDebug` terminó con `BUILD SUCCESSFUL`. También se verificaron los XML, las referencias a recursos locales y el formato del diff. No se realizó una prueba manual del flujo en un emulador o dispositivo.

Al guardar este resumen:

- Las eliminaciones de `fragment_pm_demo.xml`, `backend/database/schema.sql` y `backend/public/index.php` que aparecen en Git no fueron realizadas por el agente.
- La eliminación original de `activity_main.xml` ya estaba preparada en Git antes de recrear su contenido. Por eso puede aparecer como eliminado en el índice y como archivo nuevo en el directorio de trabajo.
- El agente sí eliminó `DemoFragment.java` y recreó `activity_main.xml` como parte de los cambios descritos.
- El agente no creó commits ni modificó el índice de Git.

## Pendientes y continuidad

- Implementar la autenticación real y el envío de datos del registro al backend.
- Definir la conexión con una base de datos y los endpoints correspondientes.
- Comprobar el flujo login → registro → Atrás en un emulador o dispositivo.
- Para retomar una conversación guardada desde la terminal se explicó el uso de `codex resume` y `codex resume --last`; desde el IDE, la selección de conversaciones recientes.
- Se pidió conservar este resumen en `docs/Historial.md`. Puede actualizarse con futuras decisiones y versionarse junto con el proyecto.

## Continuación: pantalla negra del emulador

Se revisó el emulador conectado por ADB y se confirmó que su pantalla estaba apagada: `mWakefulness=Asleep` y `Display Power: state=OFF`. No había cierres inesperados en el registro.

- Se despertó el emulador y apareció el login correctamente.
- Se comprobó el login al detener y volver a iniciar la app.
- Se amplió el tiempo de apagado automático del emulador de un minuto a treinta minutos.
- Para encender la pantalla manualmente puede utilizarse el botón de encendido de la barra del emulador.

## Continuación: registro e inicio de sesión locales

Se pidió que el registro permita crear usuarios y conservarlos localmente para probar la navegación de la app.

- Se agregó `UsuarioLocal` con nombre, correo, ubicación y rol (`adoptante` o `publicador`).
- `LocalUserRepository` conserva varias cuentas y la sesión actual en la base SQLite privada `paw_match_users.db`. No utiliza el backend.
- Las contraseñas se almacenan como hashes PBKDF2 con sal aleatoria, sin guardar el texto original. El correo se normaliza para impedir duplicados por mayúsculas o espacios externos.
- `LocalAuthViewModel` realiza las operaciones fuera del hilo de interfaz y mantiene su estado al recrear la actividad.
- El formulario valida nombre, correo, contraseña de al menos seis caracteres y ubicación. Al registrar la cuenta se inicia sesión y se abre el perfil.
- El login comprueba las credenciales locales y muestra un error cuando no son válidas.
- El perfil muestra los datos de la cuenta actual y permite cerrar sesión. Cerrar sesión conserva la cuenta para volver a ingresar; cerrar el proceso conserva también la sesión que estuviera activa.
- Se retiraron del perfil visible los datos ficticios de identidad y adopción. La edición de perfil sigue pendiente.
- Para publicadores se conectó la navegación perfil → Mis publicaciones → formulario de mascota, con regreso mediante Atrás y utilizando siempre el mismo contenedor de fragments.
- El alcance de la persistencia nueva es cuentas y sesiones. La lógica de mascotas y solicitudes conserva su implementación anterior.

### Verificación de esta continuación

- Compilación de `assembleDebug` y del APK de pruebas: exitosa.
- Cinco pruebas unitarias existentes: exitosas.
- Cuatro pruebas instrumentadas: exitosas, incluidas tres nuevas que verifican persistencia, cierre y recuperación de sesión, credenciales exactas, múltiples cuentas y rechazo de duplicados sin sobrescribir datos.
- Pruebas de interfaz en el emulador: registro de adoptante y publicador, perfil con datos correctos, conservación de sesión después de detener el proceso, logout persistente, contraseña incorrecta, reingreso, correo duplicado y navegación de publicaciones al formulario de mascota y de regreso.
- Registro de cierres inesperados después de probar: vacío.
- La app actualizada quedó instalada en el emulador y abierta en el login.

Para las pruebas de interfaz se crearon las cuentas locales `adoptante.prueba@pawmatch.local` y `publicador.prueba@pawmatch.local`, ambas con contraseña `PawMatch123`. Posteriormente se reemplazaron por los [accesos cortos actuales](#accesos-de-prueba-actuales).

La autenticación con servidor y la conexión al backend siguen pendientes; el registro local ya funciona de manera independiente.

## Continuación: solicitudes del publicador con el estilo de Explorar

Se pidió que `fragment_publisher_requests.xml` comparta la estética de Explorar y muestre las solicitudes pendientes de respuesta del publicador.

- El layout ahora representa una pantalla completa con el mismo logo, fondo crema, márgenes, tipografía, tarjetas blancas redondeadas y barra inferior que Explorar.
- Las tarjetas se separaron en `item_publisher_request.xml` y muestran la mascota, su edad y ciudad, el estado de la solicitud, el nombre, ubicación y presentación del solicitante, y los botones Aceptar y Rechazar.
- `AdoptionRequestAdapter` utiliza solicitudes, mascotas y adoptantes del repositorio compartido en lugar de nombres y correos fijos. Las mascotas sin imagen utilizan el logo como alternativa, sin recortarlo.
- `PublisherRequestsFragment` observa los cambios del repositorio y del rol activo. Para publicadores presenta únicamente solicitudes pendientes del refugio de la demo; responder actualiza su estado, retira la tarjeta y actualiza el contador. Cuando la cola queda vacía se muestra un mensaje específico.
- Los adoptantes pueden consultar sus propias solicitudes y sus estados, sin los botones de respuesta del publicador.
- Se conectó la navegación entre Explorar o Publicaciones, Solicitudes y Perfil. Una sesión de publicador abre Solicitudes pendientes; una sesión de adoptante abre Explorar.
- Se integraron las pantallas del merge con el acceso local ya implementado. Se recreó `activity_main.xml` con el contenedor `pm_contenedor`, se retiraron las referencias a la demo eliminada y se recuperaron las conexiones de login, registro y perfil que el merge había desplazado.
- Se corrigió la aplicación repetida de los espacios de las barras del sistema y se unificó la altura de la barra inferior para que sus iconos y etiquetas sean visibles.

Las solicitudes continúan utilizando `PawMatchRepository` y los perfiles ficticios de `DatosDemo`. El refugio y el adoptante de esa base son compartidos para las pruebas de navegación; las solicitudes están en memoria y se reinician al terminar el proceso. La persistencia de cuentas y sesiones locales se mantiene independiente.

### Verificación de esta continuación

- Compilación final de `assembleDebug`: exitosa.
- Seis pruebas unitarias y cuatro pruebas instrumentadas: exitosas. La prueba nueva verifica que la cola del publicador excluya solicitudes aprobadas, rechazadas y de otro refugio, conservando el historial del adoptante.
- Se comprobó en el emulador la creación de solicitudes desde Explorar, la consulta del adoptante sin acciones de respuesta, el cambio a una cuenta de publicador, aceptar y rechazar, los contadores, el estado vacío y la navegación entre secciones.
- Tras el ajuste visual se verificaron nuevamente los iconos y etiquetas de la barra inferior, el acceso con ambos roles, una sesión de publicador al iniciar un proceso nuevo, las tarjetas y la imagen alternativa.
- La app actualizada quedó instalada y abierta en Solicitudes pendientes con una solicitud de prueba de Luna. El registro de cierres inesperados estaba vacío durante las verificaciones.

Las capturas de la comprobación se guardaron temporalmente en `/tmp/pawmatch-explorar-reference.png`, `/tmp/pawmatch-requests-pending.png` y `/tmp/pawmatch-requests-empty.png`.

## Continuación: aplicación en Java

Se pidió utilizar Java porque Kotlin no forma parte de lo visto en el instituto.

- Se convirtió `ui/explorar/ExplorarFragment.kt` en `ExplorarFragment.java`, conservando los botones Pasar y Me interesa, las tres formas de abrir el detalle y el guardado de la mascota seleccionada al recrear la actividad.
- Se retiraron `Color.kt`, `Theme.kt` y `Type.kt`, que eran archivos de Compose sin uso. Los colores, temas y layouts de la aplicación continúan definidos en los recursos XML existentes.
- Se eliminaron el plugin y las dependencias de Compose del catálogo de versiones y de la configuración de compilación.
- Se convirtieron `build.gradle.kts`, `settings.gradle.kts` y `app/build.gradle.kts` al formato Groovy (`.gradle`). Estos archivos configuran Gradle; el código de la aplicación está en Java y conserva la compatibilidad con Java 11.
- Se deshabilitó la compilación de Kotlin del módulo con `enableKotlin = false`, siguiendo la [documentación de Android para módulos sin fuentes Kotlin](https://developer.android.com/build/migrate-to-built-in-kotlin).
- No quedan archivos `.kt` ni `.kts` en las fuentes o la configuración del proyecto.

### Verificación de esta continuación

- Compilación limpia de `assembleDebug` y `assembleRelease`: exitosa.
- Las 31 pruebas unitarias y las tres pruebas instrumentadas de usuarios locales: exitosas.
- En el emulador se comprobaron el registro y el acceso, Pasar, Me interesa, la solicitud visible en Solicitudes, la apertura del detalle desde la tarjeta, la imagen y el botón, el regreso a la misma mascota y su conservación al cambiar de tema.

### Incidencia durante la verificación

El primer intento de ejecutar `connectedDebugAndroidTest` utilizó una clave de depuración distinta de la habitual. La instalación falló por el conflicto de firmas y la limpieza automática del ejecutor desinstaló la aplicación del emulador, borrando sus cuentas y sesión locales. No se encontró una copia de esos datos.

Se volvió a compilar con una copia de la clave de depuración habitual y se instalaron los APK mediante `adb install -r`. Las pruebas instrumentadas se ejecutaron directamente con `am instrument`, evitando la limpieza automática de la aplicación.

Se crearon nuevamente las dos cuentas de prueba documentadas, `adoptante.prueba@pawmatch.local` y `publicador.prueba@pawmatch.local`, con contraseña `PawMatch123`. Cualquier otra cuenta que existiera en ese emulador deberá registrarse otra vez. La app quedó instalada y abierta en Explorar con la cuenta de adoptante.

## Accesos de prueba actuales

| Rol | Correo | Contraseña |
| --- | --- | --- |
| Adoptante | `a@pm.test` | `123456` |
| Publicador | `p@pm.test` | `123456` |

La versión 3 de la base local actualiza los correos y las contraseñas de las dos cuentas de prueba anteriores al abrir la app, conservando sus identificadores, perfiles y sesión. En una instalación nueva crea ambos accesos, sin iniciar una sesión automáticamente. Los demás usuarios conservan sus credenciales.

Las seis mascotas de demostración mantienen sus descripciones originales y tienen fotografías distintas según su especie. Las fuentes están en [FotosDemo.md](FotosDemo.md).
