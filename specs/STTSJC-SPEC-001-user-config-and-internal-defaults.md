# STTSJC-SPEC-001 - Separacion De Config De Usuario Y Defaults Internos

## 1. Objetivo

Implementar una separacion clara entre:

- la configuracion que pertenece al usuario;
- los valores tecnicos internos que pertenecen a la aplicacion.

La meta es eliminar la mezcla actual entre `config.properties`, valores hardcodeados en codigo y datos internos versionados en el repo.

Esta spec esta escrita como documento de handoff para otro modelo de IA que realizara la implementacion.

## 2. Problema Actual

Hoy el proyecto mezcla tres responsabilidades en el mismo espacio:

- configuracion externa del usuario: `api_key`, `language`, `audio_file_limit_size_in_bytes`;
- valores tecnicos internos: modelo, URLs, organizacion;
- estado mutable del usuario: `language` se persiste en `config.properties` cuando se usa `-l`.

Consecuencias actuales:

- el `config.properties` del repo no coincide con lo que el runtime espera;
- existen valores duplicados entre fichero y codigo;
- el limite de tamano depende de un fichero externo que el usuario no deberia gestionar;
- `WhisperApiService` mantiene hardcodes internos repartidos;
- la persistencia de `language` opera sobre un fichero cuya responsabilidad no esta bien definida.

## 3. Decisiones Ya Validadas

Estas decisiones ya estan cerradas y no deben reabrirse durante la implementacion:

- `api_key` se obtiene solo desde `config.properties` externo.
- `language` sigue siendo una opcion del usuario.
- `language` se persiste en el mismo `config.properties` externo.
- `language` solo se persiste cuando el usuario pasa `-l` por CLI.
- si el fichero externo contiene solo `api_key`, la aplicacion debe poder anadir `language` a ese mismo fichero.
- `audio_file_limit_size_in_bytes` deja de ser configurable por el usuario.
- `audio_file_limit_size_in_bytes` pasa a vivir en un `.properties` interno empaquetado con la app.
- los modelos y endpoints seguiran siendo internos, no configurables por el usuario.
- la futura ampliacion a varios modelos/endpoints se gestionara internamente, no via `config.properties` del usuario.
- al guardar `language`, se acepta que el formato/comentarios/orden del `.properties` puedan cambiar.
- si en el `config.properties` externo existen claves legacy desconocidas, deben ignorarse al leer y preservarse al guardar.
- la decision de versionar o no `config.properties` y/o `config.properties.example` queda fuera de esta implementacion.

## 4. Resultado Esperado

### 4.1 Config Externa De Usuario

El contrato funcional de `config.properties` externo debe quedar reducido a:

```properties
api_key=TU_CLAVE
language=es
```

Reglas:

- `api_key` es obligatoria.
- `language` es opcional.
- el usuario no debe definir `audio_file_limit_size_in_bytes`.
- el usuario no debe definir modelo, endpoint, organizacion ni otros detalles internos.

### 4.2 Defaults Internos Empaquetados

Debe existir un recurso interno empaquetado en el jar, por ejemplo:

`src/main/resources/application-defaults.properties`

Contenido minimo esperado:

```properties
default_language=en
audio_file_limit_size_in_bytes=20971520
```

Notas:

- este fichero es interno a la aplicacion;
- no forma parte del contrato con el usuario;
- se usa para defaults y parametros tecnicos simples;
- no debe usarse para modelar ahora el catalogo futuro de multiples modelos/endpoints.

### 4.3 Catalogo Interno De Servicio

Los datos internos del servicio de transcripcion no deben permanecer como hardcodes dispersos dentro de `WhisperApiService`.

Deben centralizarse en una definicion interna tipada, por ejemplo:

- `TranscriptionServiceDefinition`;
- `WhisperServiceDefinition`;
- o una `enum`/`record` equivalente.

Campos minimos esperados:

- `modelName`
- `modelCheckUrl`
- `transcriptionUrl`
- `organization` si sigue siendo necesaria

Importante:

- esta definicion es interna;
- no debe leerse desde `config.properties` del usuario;
- debe dejar preparado el terreno para soportar varios modelos/endpoints en el futuro;
- para esta iteracion basta con centralizar una definicion activa, no hace falta construir aun un selector completo de multiples servicios.

## 5. Reglas De Comportamiento

### 5.1 Resolucion De Valores

La resolucion final debe ser:

- `api_key`: solo desde `config.properties` externo.
- `language`: CLI `-l` > `config.properties` externo > `application-defaults.properties`.
- `audio_file_limit_size_in_bytes`: solo desde `application-defaults.properties`.

### 5.2 Validacion De `language`

Reglas:

- si `-l` contiene un valor invalido o no soportado, la aplicacion debe fallar, mostrar ayuda y no continuar;
- si `language` almacenado en `config.properties` es invalido o no soportado, la aplicacion debe avisar y caer al default interno;
- el valor persistido debe guardarse normalizado en minusculas.

### 5.3 Persistencia De `language`

Reglas:

- solo se persiste si el usuario paso `-l`;
- si no se paso `-l`, la aplicacion no debe reescribir `config.properties`;
- si `config.properties` contiene solo `api_key`, al guardar debe anadirse `language`;
- al guardar, deben mantenerse `api_key` y las claves legacy desconocidas;
- si el guardado falla, la aplicacion debe avisar pero continuar.

### 5.4 Compatibilidad Con Claves Legacy

Si el fichero externo contiene claves antiguas como:

- `audio_file_limit_size_in_bytes`
- `model_ai_name`
- `model_ai_url`
- `model_ai_url_service`
- `openai_organization`

la implementacion debe:

- ignorarlas para la logica actual;
- no fallar por su presencia;
- preservarlas cuando se escriba `language`.

### 5.5 Errores De Config Externa

Reglas:

- si falta `config.properties` externo, la aplicacion debe fallar como hasta ahora;
- si falta `api_key`, la aplicacion debe fallar como hasta ahora;
- la aplicacion no debe autocrear `config.properties`;
- el comportamiento de anadir `language` aplica solo cuando el fichero externo ya existe y contiene al menos una `api_key` valida.

## 6. Alcance Y No Alcance

### 6.1 Incluido En Esta Implementacion

- separacion de config de usuario y defaults internos;
- movimiento del limite de tamano a recurso interno;
- centralizacion interna de modelo/endpoints actuales;
- refactor de carga y guardado de config;
- actualizacion de mensajes, docs y tests.

### 6.2 Fuera De Alcance

- exponer al usuario seleccion de modelo o endpoint;
- redisenar ahora el sistema completo de multiples modelos/endpoints;
- decidir el versionado final de `config.properties` o `config.properties.example`;
- limpiar automaticamente claves legacy del fichero del usuario.

## 7. Diseno Tecnico Propuesto

### 7.1 Nuevos Tipos Recomendados

Se recomienda introducir tipos pequenos y explicitos en lugar de propagar `Properties` por toda la app.

#### `UserConfig`

Responsabilidad: representar la config externa del usuario.

Campos sugeridos:

- `apiKey`
- `language` nullable

#### `ApplicationDefaults`

Responsabilidad: representar defaults internos simples cargados desde classpath.

Campos sugeridos:

- `defaultLanguage`
- `audioFileLimitSizeInBytes`

#### `TranscriptionServiceDefinition`

Responsabilidad: representar la configuracion interna del servicio/modelo activo.

Campos sugeridos:

- `modelName`
- `modelCheckUrl`
- `transcriptionUrl`
- `organization` nullable si aplica

#### Opcional: `ResolvedApplicationConfig`

Responsabilidad: contener los valores efectivos ya resueltos para que `Main` no tenga que mezclar politicas.

Campos sugeridos:

- `apiKey`
- `effectiveLanguage`
- `audioFileLimitSizeInBytes`
- `serviceDefinition`
- `shouldPersistLanguage`

### 7.2 `ConfigLoader`

`ConfigLoader` debe dejar de asumir que todo sale del mismo fichero.

Responsabilidades esperadas tras el refactor:

- resolver la ruta del `config.properties` externo;
- cargar el fichero externo del usuario;
- cargar `application-defaults.properties` desde classpath;
- devolver tipos de config, no solo `Properties` crudas;
- guardar `language` en el fichero externo.

Capacidades concretas:

- carga de `api_key` desde config usuario;
- carga tolerante de `language` desde config usuario;
- carga de `default_language` y `audio_file_limit_size_in_bytes` desde defaults internos;
- error claro si el recurso interno no existe o es invalido;
- preservacion de claves desconocidas al guardar `language`.

### 7.3 `Main`

`Main` debe orquestar, no contener toda la politica de configuracion.

Flujo esperado:

1. parsear CLI;
2. resolver ruta de `config.properties` externo;
3. cargar config de usuario;
4. cargar defaults internos;
5. validar y resolver `language` efectiva;
6. persistir `language` solo si vino por `-l`;
7. obtener `audio_file_limit_size_in_bytes` desde defaults internos;
8. construir la definicion interna del servicio;
9. instanciar `WhisperApiService` con esa definicion;
10. continuar el flujo actual de validacion, ffmpeg, particionado y transcripcion.

### 7.4 `WhisperApiService`

`WhisperApiService` no debe contener hardcodes dispersos de servicio.

Debe recibir una definicion interna centralizada por constructor, por ejemplo:

```java
new WhisperApiService(httpClient, transcriptionServiceDefinition)
```

Comportamiento esperado:

- la URL de comprobacion sale de la definicion interna;
- la URL de transcripcion sale de la definicion interna;
- el nombre del modelo sale de la definicion interna;
- la organizacion sale de la definicion interna si aplica.

## 8. Cambios Por Archivo

Esta seccion es orientativa. Los nombres exactos pueden ajustarse si el otro modelo encuentra una estructura mejor, pero debe respetar la intencion.

### 8.1 Nuevos Archivos

#### `src/main/resources/application-defaults.properties`

Crear recurso interno con defaults simples.

Contenido minimo:

- `default_language`
- `audio_file_limit_size_in_bytes`

#### `src/main/java/.../config/UserConfig.java`

Representa el contrato externo del usuario.

#### `src/main/java/.../config/ApplicationDefaults.java`

Representa defaults internos simples.

#### `src/main/java/.../transcriptionservice/TranscriptionServiceDefinition.java`

Centraliza la definicion interna del servicio/modelo activo.

#### Opcional: `src/main/java/.../config/ResolvedApplicationConfig.java`

Reduce complejidad en `Main`.

### 8.2 Archivos Existentes A Modificar

#### `src/main/java/.../utils/ConfigLoader.java`

Cambios:

- separar lectura de usuario y lectura interna;
- dejar de leer `audio_file_limit_size_in_bytes` desde config usuario;
- anadir carga del recurso interno via classpath;
- mantener `saveLanguage(...)` sobre el fichero externo;
- preservar claves desconocidas al guardar;
- devolver mensajes de error correctos.

#### `src/main/java/.../Main.java`

Cambios:

- usar la nueva carga separada de config;
- resolver `language` con la precedencia decidida;
- persistir `language` solo con `-l`;
- obtener el limite de tamano desde defaults internos;
- construir/inyectar la definicion interna del servicio.

#### `src/main/java/.../transcriptionservice/WhisperApiService.java`

Cambios:

- eliminar hardcodes actuales de URLs/modelo/organizacion;
- recibir una definicion interna centralizada;
- mantener tests offline con `OkHttpClient` mockeado.

#### `src/main/java/.../utils/MessageManager.java`

Cambios:

- actualizar ejemplo minimo de config faltante;
- eliminar referencias a `audio_file_limit_size_in_bytes` como dato del usuario;
- anadir mensaje claro para fallo al cargar defaults internos si hiciera falta.

#### `README.md`

Cambios:

- documentar que el usuario solo aporta `api_key` y opcionalmente `language`;
- documentar que `-l` persiste `language` en el mismo fichero;
- documentar que el limite de tamano es interno;
- eliminar cualquier instruccion que pida al usuario definir `audio_file_limit_size_in_bytes`.

#### `docs/functional-context.md`

Cambios:

- alinear seccion de configuracion con el nuevo contrato;
- eliminar `audio_file_limit_size_in_bytes` de la config del usuario;
- dejar claro que `language` persiste si se fija por CLI;
- aclarar que existen defaults internos no editables por el usuario.

## 9. Estrategia De Implementacion Recomendada

Orden sugerido:

1. crear `application-defaults.properties`;
2. crear tipos `UserConfig`, `ApplicationDefaults` y `TranscriptionServiceDefinition`;
3. refactorizar `ConfigLoader`;
4. adaptar `WhisperApiService` para recibir definicion interna;
5. adaptar `Main` al nuevo flujo de resolucion;
6. actualizar `MessageManager`;
7. anadir tests;
8. actualizar docs.

Motivo del orden:

- primero se fijan fuentes de datos;
- luego se adapta la orquestacion;
- despues se asegura comportamiento con tests;
- al final se actualiza documentacion.

## 10. Tests A Implementar

Se recomienda seguir el estilo behavior-driven ya presente en el repo.

### 10.1 Tests De Config De Usuario

Posibles nombres:

- `LoadUserConfigurationTest`
- `PersistCommandLineLanguagePreferenceTest`

Casos minimos:

- carga correcta de `api_key` desde un `config.properties` con solo esa clave;
- ausencia de `language` devuelve `null` o equivalente;
- `language` con espacios se normaliza correctamente;
- si falta `api_key`, la carga falla de forma clara;
- si se guarda `language`, el `api_key` original permanece intacto;
- si el fichero solo tenia `api_key`, `language` se anade correctamente;
- claves legacy desconocidas siguen presentes despues de guardar.

### 10.2 Tests De Resolucion De Idioma

Posibles nombres:

- `ResolveEffectiveLanguageTest`

Casos minimos:

- CLI pisa config usuario;
- config usuario pisa default interno;
- config usuario invalido o no soportado cae a default interno;
- CLI invalido provoca error y no continua;
- sin `-l`, no se persiste nada.

### 10.3 Tests De Defaults Internos

Posibles nombres:

- `LoadBundledApplicationDefaultsTest`

Casos minimos:

- `audio_file_limit_size_in_bytes` se carga desde `application-defaults.properties`;
- `default_language` se carga desde `application-defaults.properties`;
- si el recurso interno falta o es invalido, el error es claro y controlado.

### 10.4 Tests Del Servicio De Transcripcion

Posibles nombres:

- `UseInternalTranscriptionServiceDefinitionTest`

Casos minimos:

- `WhisperApiService` usa la URL de check de la definicion interna;
- `WhisperApiService` usa la URL de transcripcion de la definicion interna;
- `WhisperApiService` usa el nombre de modelo de la definicion interna;
- `WhisperApiService` aplica la organizacion interna si corresponde.

## 11. Criterios De Aceptacion

La implementacion se considerara correcta si se cumplen todos estos puntos:

1. la app funciona con un `config.properties` externo que contenga solo `api_key`;
2. `audio_file_limit_size_in_bytes` ya no se lee del fichero del usuario;
3. el limite de tamano se obtiene del recurso interno empaquetado;
4. si el usuario ejecuta `-l es`, la app guarda `language=es` en el mismo fichero externo;
5. si el usuario no ejecuta `-l`, la app no reescribe `config.properties`;
6. si `language` guardado es invalido o no soportado, la app avisa y usa el default interno;
7. si `-l` es invalido o no soportado, la app falla y no continua;
8. las claves legacy desconocidas del fichero externo no rompen la ejecucion;
9. esas claves legacy se preservan tras guardar `language`;
10. `WhisperApiService` ya no contiene URLs/modelo/organizacion hardcodeados de forma dispersa;
11. `README.md` y `docs/functional-context.md` quedan alineados con el nuevo contrato.

## 12. Riesgos Y Cuidados

- no duplicar la politica de `language` entre `Main` y `ConfigLoader` mas de lo necesario;
- no olvidar cubrir el caso critico de fichero con solo `api_key`;
- no introducir dependencia del filesystem para cargar defaults internos;
- no borrar claves legacy al guardar;
- no reabrir en esta implementacion la decision de versionado del fichero.

## 13. No Hacer

Durante esta implementacion, no hacer lo siguiente:

- no volver a pedir al usuario `audio_file_limit_size_in_bytes`;
- no exponer modelo/endpoint como configuracion del usuario;
- no limpiar automaticamente claves legacy del fichero externo;
- no persistir `language` cuando no se ha usado `-l`;
- no dejar los datos del servicio repartidos en hardcodes sueltos.

## 14. Decision Pendiente Fuera De Esta Spec

Queda explicitamente fuera de esta implementacion decidir:

- si `config.properties` real se versiona o no;
- si se introduce `config.properties.example`.

La implementacion debe quedar preparada para cualquiera de las dos decisiones, pero no resolverla por su cuenta.

## 15. Nota Sobre El `config.properties` Actual Del Repo

Actualmente existe un `config.properties` en la raiz del repo con claves internas legacy.

Para esta implementacion, ese fichero no debe considerarse la fuente de verdad del nuevo contrato de usuario. La nueva logica debe limitarse a leer de ese fichero solo las claves de usuario que correspondan (`api_key`, `language`) e ignorar el resto.

Si durante la implementacion resulta necesario tocar su contenido para pruebas locales, esa decision no debe consolidarse como parte del contrato final mientras no se cierre la decision de versionado.
