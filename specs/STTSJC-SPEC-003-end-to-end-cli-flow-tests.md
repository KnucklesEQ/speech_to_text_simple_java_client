# STTSJC-SPEC-003 - Suite De Tests End-To-End De Flujos CLI

## 1. Objetivo

Construir una suite de tests end-to-end que cubra los flujos funcionales principales de la aplicacion, incluyendo:

- flujos que terminan correctamente;
- flujos que finalizan con error;
- flujos de ayuda/version;
- flujos de validacion de entrada;
- flujos de configuracion;
- flujos de audio, video y audio grande;
- flujos de salida final de la transcripcion.

La suite debe estar pensada para ser ejecutada con `./gradlew test` y usar exclusivamente:

- JUnit 5;
- Mockito.

Esta spec esta escrita como documento de handoff para otro modelo de IA que realizara la implementacion.

## 2. Objetivo Real De La Suite

El objetivo no es cubrir clases aisladas ni pasos internos por separado.

El objetivo es validar el comportamiento observable de la aplicacion como flujo completo de usuario a traves de su punto de entrada principal.

En esta spec, "end-to-end" significa:

- usar la orquestacion real de `Main`;
- usar filesystem real;
- usar `ffmpeg` y `ffprobe` reales cuando el caso lo permita;
- simular la API externa de transcripcion mediante seams/mocks;
- no tocar codigo de produccion para hacerla testeable.

## 3. Restricciones Ya Validadas

Estas decisiones ya estan cerradas y no deben reabrirse durante la implementacion:

- no se puede tocar `src/main/java/**` para facilitar testabilidad;
- no se deben modificar ni borrar los tests actuales del proyecto;
- los tests actuales solo se usan como referencia de naming;
- la nueva suite debe ser una suite nueva, separada de los tests actuales;
- cualquier llamada a la API externa de transcripcion debe resolverse con seams o mocks;
- el resto del flujo puede y debe ejecutarse sobre el sistema real cuando sea razonable;
- solo se puede usar JUnit 5 y Mockito;
- no deben introducirse otras herramientas de testing;
- la suite debe orientarse a flujos completos, no a comprobaciones paso a paso de implementacion interna.

## 4. Contexto Actual Del Proyecto

Situacion actual relevante para esta spec:

- la app se orquesta desde `Main`;
- `Main.main(...)` hace `System.exit(...)`;
- el flujo principal vive en `Main.run(String[] args)`;
- `Main.run(...)` es privado;
- el servicio externo se instancia dentro del flujo usando `new WhisperApiService(...)`;
- los helpers multimedia usan `ffmpeg` y `ffprobe` reales;
- ya existen recursos de prueba reales en `src/test/resources/`.

Recursos multimedia disponibles actualmente:

- `sample_audio.mp3`
- `sample_audio_big.mp3`
- `sample_audio.wav`
- `sample_video.mp4`

Datos observados del video de prueba:

- `src/test/resources/sample_video.mp4`
- tamano aproximado: `553913` bytes;
- duracion aproximada: `15.8` segundos.

## 5. Decisiones Arquitectonicas De Test

### 5.1 Punto De Entrada A Testear

La suite debe ejecutar el flujo real llamando a `Main.run(String[] args)` por reflexion.

No se recomienda basar la suite en `Main.main(...)`, porque eso ejecuta `System.exit(...)` y complica innecesariamente el aislamiento del proceso de test.

No se recomienda basar la suite en un subproceso `java ...` como estrategia principal, porque eso dificulta mockear la API externa sin tocar produccion.

La estrategia principal debe ser:

- invocar `Main.run(...)` por reflexion dentro de la JVM de test;
- capturar `System.in`, `System.out`, `System.err`;
- cambiar temporalmente `user.dir` a un `@TempDir`;
- dejar que el flujo real haga el resto.

### 5.2 Que Es Real Y Que Va Mockeado

Debe ir real:

- parseo CLI;
- lectura y escritura de `config.properties`;
- persistencia de `language`;
- validacion de ficheros;
- deteccion de audio/video;
- extraccion real de audio desde video;
- calculo real de duracion y tamano;
- split real de audio grande;
- guardado de `transcription.txt`;
- movimiento final del fichero cuando aplica.

Debe ir mockeado:

- `WhisperApiService` como frontera con la API externa;
- errores raros de infraestructura que no sean reproducibles de forma determinista sin tocar produccion.

### 5.3 Que No Debe Cubrir Esta Suite

Esta suite no debe intentar cubrir internals no observables o dependientes del empaquetado/distribucion cuando eso rompa el enfoque end-to-end principal.

Quedan fuera de alcance de esta suite concreta:

- tests internos de parsing JSON de `WhisperApiService` frente a respuestas HTTP concretas;
- diferencias de resolucion de `config.properties` entre `java -jar` y otros modos de packaging;
- warnings de limpieza de temporales muy especificos al cerrar `TemporaryWorkspaceHelper`.

Si en el futuro se desean cubrir esos puntos, deberian ir en otra spec o en otra capa de tests.

## 6. Naming Requerido

Los tests existentes no se reutilizan, pero si sirven como referencia de naming.

Reglas obligatorias:

- cada feature debe tener exactamente una clase top-level propia;
- el nombre de la clase debe terminar en `Test`;
- el nombre de la clase debe describir el flujo o feature, no una clase productiva;
- el nombre del metodo debe usar estilo `givenXWhenYThenZ`.

Como los tests antiguos no deben tocarse y algunos nombres podrian colisionar, la nueva suite debe usar el sufijo `EndToEndTest` en las clases top-level.

Ejemplos validos:

- `DisplayApplicationHelpMessageEndToEndTest`
- `TranscribeAudioFileEndToEndTest`
- `RejectInvalidInputFileEndToEndTest`

Ejemplos no deseados:

- `MainTest`
- `ConfigLoaderTest`
- `AudioFileHelperFlowTest`

Los `@Nested` solo deben usarse para agrupar variantes del mismo flujo cuando aporten claridad. No deben reproducir una estructura "paso a paso" del pipeline interno.

## 7. Regla De Aislamiento Global

Estos tests van a mutar estado global del proceso:

- `System.in`
- `System.out`
- `System.err`
- `user.dir`

Por tanto, la suite debe ejecutarse en el mismo hilo y evitar paralelismo.

Recomendacion obligatoria:

- anotar estas clases con `@Execution(ExecutionMode.SAME_THREAD)`.

Si el implementador considera necesario un aislamiento extra, puede anadir mecanismos equivalentes de JUnit, siempre sin introducir nuevas librerias.

## 8. Infraestructura De Soporte A Crear En `src/test/java`

La suite necesitara helpers de test. Estos helpers son parte esperada de la implementacion.

Ubicacion recomendada:

- `src/test/java/eu/nevian/speech_to_text_simple_java_client/support/`

### 8.1 `MainEndToEndExecutionSupport`

Responsabilidad:

- invocar `Main.run(String[] args)` por reflexion;
- establecer stdin/stdout/stderr temporales;
- cambiar temporalmente `user.dir`;
- devolver un resultado estructurado.

Capacidades minimas:

- recibir `String[] args`;
- recibir input simulado de consola, por ejemplo `"n\n"` o `"y\n"`;
- recibir un `Path` como directorio de trabajo;
- devolver exit code, stdout y stderr normalizados.

Pseudoforma esperada:

```java
Method runMethod = Main.class.getDeclaredMethod("run", String[].class);
runMethod.setAccessible(true);
int exitCode = (int) runMethod.invoke(null, (Object) args);
```

Notas:

- usar el cast `(Object) args` para evitar problemas con reflection y varargs;
- restaurar siempre streams y propiedades del sistema en `finally`;
- normalizar saltos de linea a `\n`.

### 8.2 `MainExecutionResult`

Record o helper simple con:

- `exitCode`
- `stdout`
- `stderr`

### 8.3 `TestConfigurationFactory`

Responsabilidad:

- crear rapidamente `config.properties` dentro de `@TempDir`.

Capacidades minimas:

- crear config con solo `api_key`;
- crear config con `api_key` + `language`;
- crear config con claves legacy extra si algun test lo necesita.

### 8.4 `TestResourceCopier`

Responsabilidad:

- copiar recursos multimedia desde `src/test/resources` a `@TempDir`.

Capacidades minimas:

- copiar `sample_audio.mp3`;
- copiar `sample_audio_big.mp3`;
- copiar `sample_video.mp4`.

No se debe trabajar directamente contra los recursos originales del repo cuando el flujo va a escribir al lado del fichero o a cambiar `user.dir`.

## 9. Estrategia De Mocking

### 9.1 Frontera De API Externa

La frontera principal de mocking debe ser la construccion de `WhisperApiService`.

Como `Main` crea el servicio internamente, la tecnica recomendada es:

- `MockedConstruction<WhisperApiService>` de Mockito.

Objetivo:

- interceptar `new WhisperApiService(...)`;
- simular `checkAiModelIsAvailable(...)`;
- simular `transcribeAudioFile(...)`;
- verificar cuantas veces se llama y con que argumentos.

Esto convierte el servicio externo en un seam sin tocar produccion.

### 9.2 Errores De Infraestructura Poco Deterministas

Para errores dificilmente reproducibles de forma limpia, se permite usar `MockedStatic` de Mockito sobre clases con metodos estaticos.

Casos esperados:

- `FfmpegProcessHelper`
- `TemporaryWorkspaceHelper`
- `TextFileHelper`
- `ConfigLoader`

Importante:

- usar mocks estaticos solo en tests de error muy concretos;
- no usarlos como base de la suite feliz;
- cuando sea necesario preservar el comportamiento real de otros metodos estaticos, usar una estrategia equivalente a `CALLS_REAL_METHODS`.

### 9.3 Nota Sobre Mockito Inline

Si constructor mocking o static mocking no funcionaran con la configuracion actual, no debe anadirse otra libreria.

La salida permitida, si fuera necesaria, es usar la extension propia de Mockito en `src/test/resources/mockito-extensions/` para habilitar su mock maker inline.

Eso sigue cumpliendo la restriccion de usar solo JUnit y Mockito.

## 10. Estructura Esperada De La Nueva Suite

Los tests actuales no deben tocarse. La nueva suite debe vivir en nuevas clases.

Clases top-level recomendadas:

1. `DisplayApplicationHelpMessageEndToEndTest`
2. `DisplayApplicationVersionEndToEndTest`
3. `RejectInvalidCommandLineInvocationEndToEndTest`
4. `RejectInvalidConfigurationEndToEndTest`
5. `RejectInvalidInputFileEndToEndTest`
6. `TranscribeAudioFileEndToEndTest`
7. `TranscribeVideoFileEndToEndTest`
8. `TranscribeLargeAudioFileEndToEndTest`
9. `FinalizeTranscriptionOutputEndToEndTest`
10. `HandleUnavailableExternalMediaToolsEndToEndTest`
11. `HandleTranscriptionServiceFailureEndToEndTest`
12. `HandleRuntimeInfrastructureFailureEndToEndTest`

Estos nombres siguen el estilo narrativo del repo y evitan colision con los tests antiguos.

## 11. Cobertura Requerida Por Feature

### 11.1 `DisplayApplicationHelpMessageEndToEndTest`

Debe cubrir:

- `-h` muestra ayuda y termina con exit `0`;
- `--help` muestra ayuda y termina con exit `0`;
- ayuda con argumento de fichero presente sigue priorizando ayuda;
- ayuda con `--version` presente sigue priorizando ayuda;
- no se valida el fichero;
- no se genera `transcription.txt`.

Detalles utiles:

- no hace falta crear `config.properties`;
- se debe comprobar que no aparece `Welcome!` ni `Validating file...`.

### 11.2 `DisplayApplicationVersionEndToEndTest`

Debe cubrir:

- `-v` muestra version y termina con exit `0`;
- `--version` muestra version y termina con exit `0`;
- no se genera `transcription.txt`.

Nota:

- bajo tests, la version puede ser `unknown`, asi que debe validarse el prefijo, no un literal de version concreto.

### 11.3 `RejectInvalidCommandLineInvocationEndToEndTest`

Debe cubrir:

- ejecucion sin fichero;
- ejecucion con demasiados ficheros;
- opcion CLI desconocida;
- `-l` sin valor;
- `-l` con codigo de idioma invalido o no soportado.

Comportamiento esperado:

- exit `1`;
- stderr con error apropiado;
- ayuda visible cuando corresponda segun comportamiento actual.

Nota importante:

- para el caso de `-l` invalido, el flujo actual exige que exista `config.properties`, porque esa validacion ocurre despues de la carga de config; el test debe montar ese prerequisito.

### 11.4 `RejectInvalidConfigurationEndToEndTest`

Debe cubrir:

- falta `config.properties`;
- falta `api_key`;
- `language` invalido en config genera warning y fallback a default;
- `-l` valida y persiste `language` en `config.properties`;
- fallo al guardar `language` produce warning pero no impide continuar.

Detalles:

- para el fallback por `language` invalido, usar API mockeada y flujo feliz sobre un audio pequeno;
- para el fallo al guardar, usar seam/mocking sobre `ConfigLoader.saveLanguage(...)` o equivalente permitido por Mockito.

### 11.5 `RejectInvalidInputFileEndToEndTest`

Debe cubrir:

- ruta inexistente;
- ruta que apunta a directorio;
- fichero no legible;
- fichero de tipo no soportado.

Estrategia:

- inexistente: ruta inventada;
- directorio: crear subdirectorio en `@TempDir`;
- no soportado: crear fichero `.txt`;
- no legible: crear fichero real y retirar permisos de lectura.

Nota para el caso no legible:

- usar un test condicionado a filesystem con permisos POSIX;
- restaurar permisos al final para no romper la limpieza de `@TempDir`.

### 11.6 `TranscribeAudioFileEndToEndTest`

Debe cubrir el flujo feliz de audio pequeno.

Setup:

- copiar `sample_audio.mp3` a `@TempDir`;
- crear `config.properties` con `api_key`;
- mockear `WhisperApiService`;
- alimentar consola con `"n\n"`.

Comportamiento esperado:

- exit `0`;
- stdout contiene validacion, comprobacion de API, mensaje `DONE!`, resumen final;
- se crea `transcription.txt` en el cwd;
- el contenido coincide con la transcripcion mockeada;
- la API se comprueba una vez y se transcribe una vez.

Tambien debe cubrir una variante con `-l es` para validar que:

- el idioma efectivo usado en la llamada mockeada es `es`;
- `language=es` queda persistido en `config.properties`.

### 11.7 `TranscribeVideoFileEndToEndTest`

Debe cubrir el flujo feliz de video.

Setup:

- copiar `sample_video.mp4` a `@TempDir`;
- crear `config.properties` con `api_key`;
- mockear `WhisperApiService`;
- responder `"n\n"` al prompt final.

Comportamiento esperado:

- la app detecta video;
- extrae audio real con `ffmpeg`;
- se realiza la transcripcion mockeada;
- exit `0`;
- se genera `transcription.txt`;
- la transcripcion final coincide con el texto mockeado.

Tambien debe comprobarse visualmente en stdout algo equivalente a:

- deteccion de video;
- extraccion de audio;
- continuacion del flujo.

### 11.8 `TranscribeLargeAudioFileEndToEndTest`

Debe cubrir el flujo feliz de audio que supera el limite interno.

Setup:

- copiar `sample_audio_big.mp3` a `@TempDir`;
- crear `config.properties` con `api_key`;
- mockear `WhisperApiService` para devolver textos distintos por llamada;
- responder `"n\n"`.

Comportamiento esperado:

- se detecta que el fichero es grande;
- se divide realmente con `ffmpeg`;
- la API mock se invoca multiples veces;
- el contenido final del `transcription.txt` recompone el resultado en una sola salida;
- contiene el separador visible actual entre partes;
- exit `0`.

Para no acoplar demasiado el test al numero exacto de partes, se recomienda validar:

- `atLeast(2)` llamadas a `transcribeAudioFile(...)`;
- presencia del mensaje de split;
- presencia del separador visible en el resultado final.

### 11.9 `FinalizeTranscriptionOutputEndToEndTest`

Debe cubrir la decision interactiva final sobre la ubicacion del archivo.

Casos minimos:

- respuesta `n`: el fichero permanece como `transcription.txt` en cwd;
- respuesta `y`: el fichero se mueve a la carpeta del archivo original con nombre `<BASE>_TRANSCRIPTION.txt`.

Tambien debe cubrir:

- fallo al mover el fichero: se informa del problema, pero el flujo no debe convertirse en error fatal al final.

Para ese fallo concreto, se permite seam con static mocking de `TextFileHelper.moveTranscriptionFile(...)`, manteniendo el resto del flujo real.

### 11.10 `HandleUnavailableExternalMediaToolsEndToEndTest`

Debe cubrir:

- `ffmpeg` no disponible;
- `ffprobe` no disponible.

Aunque en el entorno actual existan ambas herramientas, estos casos deben forzarse con Mockito sobre `FfmpegProcessHelper` para que sean deterministas.

Comportamiento esperado:

- exit `1`;
- stderr con mensaje claro;
- no se llega al resto del flujo.

### 11.11 `HandleTranscriptionServiceFailureEndToEndTest`

Debe cubrir la reaccion de la app ante errores del servicio de transcripcion, vistos desde el boundary del servicio, no desde internals HTTP.

Casos minimos:

- `checkAiModelIsAvailable(...)` lanza `IOException`;
- `transcribeAudioFile(...)` lanza `IOException`.

Comportamiento esperado:

- exit `1`;
- stderr contiene `Error fetching data from API:`;
- no se deja flujo exitoso incompleto como si hubiera acabado bien.

Importante:

- esta suite no debe intentar verificar el parsing interno JSON de `WhisperApiService`;
- el nivel de cobertura aqui es el boundary `Main <-> ApiService`.

### 11.12 `HandleRuntimeInfrastructureFailureEndToEndTest`

Debe cubrir errores operativos relevantes que el usuario puede observar y que no son sencillos de provocar con sistema real sin tocar produccion.

Casos minimos:

- fallo al crear el temporary workspace;
- fallo al cargar defaults internos;
- defaults internos invalidos.

Estrategia:

- usar Mockito sobre `TemporaryWorkspaceHelper.createTemporaryWorkspace()` y/o `ConfigLoader.loadApplicationDefaults()` segun el caso.

Comportamiento esperado:

- exit `1`;
- stderr muestra el error correspondiente.

## 12. Flujos Del Documento Funcional Cubiertos Por Esta Suite

La implementacion debe dejar trazabilidad clara con `docs/functional-context.md`.

La suite debe cubrir, como minimo, estos flujos funcionales observables:

- ayuda;
- version;
- audio valido;
- video valido;
- audio grande con split;
- idioma indicado por CLI;
- sin archivo;
- demasiados archivos;
- idioma CLI invalido o no soportado;
- ruta inexistente;
- ruta no regular;
- archivo no legible;
- tipo no soportado;
- configuracion inexistente o incompleta;
- herramientas externas ausentes;
- fallo del servicio externo;
- fallo de recursos temporales;
- decision final de mover/no mover la transcripcion.

## 13. Reglas Practicas De Implementacion

### 13.1 Directorio De Trabajo Temporal

Cada test debe ejecutarse dentro de su propio `@TempDir`.

Reglas:

- copiar al temp dir solo los recursos que necesite;
- crear ahi el `config.properties` de test;
- usar rutas relativas en CLI cuando tenga sentido, para parecerse mas al uso real;
- no escribir en la raiz del repo durante los tests.

### 13.2 Captura De Consola

La infraestructura de soporte debe capturar:

- `System.out`
- `System.err`

y devolverlos como `String` UTF-8 con saltos de linea normalizados.

### 13.3 Input Interactivo

Cuando el flujo requiera responder a la pregunta final de mover/no mover:

- usar `System.in` simulado con `"y\n"` o `"n\n"`.

### 13.4 Restauracion De Estado Global

Despues de cada ejecucion, debe restaurarse siempre:

- `System.in`
- `System.out`
- `System.err`
- `user.dir`

Esto es obligatorio para evitar contaminacion entre tests.

## 14. No Hacer

Durante esta implementacion, no hacer lo siguiente:

- no modificar ni borrar los tests existentes;
- no reusar la estructura paso a paso de esos tests como base de la nueva suite;
- no tocar produccion para inyectar dependencias;
- no introducir WireMock, Testcontainers, servidores fake externos ni otras librerias;
- no convertir la suite en una coleccion de unit tests por clase;
- no usar procesos Java hijos como estrategia principal;
- no llamar a la API externa real;
- no escribir en el repo real durante la ejecucion de tests;
- no basar la verificacion en detalles demasiado fragiles del formateo exacto de stdout cuando no aporten valor funcional.

## 15. Orden Recomendado De Implementacion

Orden sugerido:

1. crear infraestructura de soporte (`MainEndToEndExecutionSupport`, `MainExecutionResult`, factories);
2. implementar `DisplayApplicationHelpMessageEndToEndTest`;
3. implementar `DisplayApplicationVersionEndToEndTest`;
4. implementar `RejectInvalidCommandLineInvocationEndToEndTest`;
5. implementar `RejectInvalidConfigurationEndToEndTest`;
6. implementar `RejectInvalidInputFileEndToEndTest`;
7. implementar `TranscribeAudioFileEndToEndTest`;
8. implementar `TranscribeVideoFileEndToEndTest`;
9. implementar `TranscribeLargeAudioFileEndToEndTest`;
10. implementar `FinalizeTranscriptionOutputEndToEndTest`;
11. implementar `HandleUnavailableExternalMediaToolsEndToEndTest`;
12. implementar `HandleTranscriptionServiceFailureEndToEndTest`;
13. implementar `HandleRuntimeInfrastructureFailureEndToEndTest`;
14. ejecutar `./gradlew test` y estabilizar la suite.

Motivo:

- primero se valida el harness;
- despues se cubren salidas tempranas y simples;
- luego flujos felices multimedia;
- finalmente errores complejos e infraestructura.

## 16. Criterios De Aceptacion

La implementacion se considerara correcta si se cumplen todos estos puntos:

1. la nueva suite se ejecuta con `./gradlew test`;
2. solo usa JUnit 5 y Mockito;
3. no modifica produccion;
4. no toca los tests antiguos;
5. cubre los flujos funcionales principales y sus errores relevantes;
6. la API externa nunca se invoca realmente;
7. los flujos multimedia felices usan `ffmpeg`/`ffprobe` reales;
8. los tests estan nombrados con estilo narrativo y metodos `givenXWhenYThenZ`;
9. la suite se apoya en `Main.run(...)` por reflexion como base del flujo real;
10. los tests restauran correctamente el estado global del proceso.

## 17. Riesgos Y Cuidados

- como `Main.run(...)` es privado, la suite queda acoplada a reflection; esto es aceptado en esta spec;
- los tests que usan permisos de lectura pueden depender de filesystem POSIX;
- los tests que mutan streams y `user.dir` no deben correr en paralelo;
- el boundary de API queda cubierto a nivel de comportamiento de app, no de parsing HTTP interno;
- esta suite no resolvera por si misma problemas de packaging-specific path resolution.

## 18. Decision Explicita Sobre Los Tests Existentes

Los tests ya presentes en el repo:

- no deben modificarse;
- no deben eliminarse;
- no deben convertirse en la base de esta suite;
- solo sirven como referencia de naming.

La nueva implementacion debe ser una suite nueva, claramente separada y coherente con el enfoque end-to-end definido aqui.
