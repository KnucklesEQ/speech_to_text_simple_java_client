# STTSJC-SPEC-002 - Prompt Por Defecto Segun Idioma Para Mejorar La Puntuacion De La Transcripcion

## 1. Objetivo

Implementar el envio de un `prompt` por defecto en las peticiones de transcripcion para mejorar la puntuacion, capitalizacion y acentuacion del texto devuelto por el modelo, siguiendo la recomendacion oficial de la documentacion del proveedor.

Esta spec esta escrita como documento de handoff para otro modelo de IA que realizara la implementacion.

## 2. Contexto Actual

Actualmente, la aplicacion envia en la peticion de transcripcion los campos principales del multipart, pero no envia ningun `prompt`.

En el estado actual del codigo, la construccion del body de transcripcion se hace dentro de:

- `src/main/java/eu/nevian/speech_to_text_simple_java_client/transcriptionservice/WhisperApiService.java`

El metodo afectado es:

- `transcribeAudioFile(String apiKey, String language, String audioFilePath)`

El `language` que recibe ese metodo ya llega resuelto por capas superiores. Esta spec no cambia como se resuelve ese idioma; solo decide si, para ese idioma efectivo, debe enviarse ademas un `prompt` por defecto.

## 3. Problema A Resolver

Se ha observado que, en algunas transcripciones, el texto devuelto aparece sin puntuacion suficiente o con una calidad baja de capitalizacion/acentuacion.

La documentacion oficial del modelo indica que esto puede mejorarse enviando un `prompt` corto que ya contenga puntuacion correcta. La referencia funcional que motiva esta spec es equivalente a:

> Sometimes the model skips punctuation in the transcript. To prevent this, use a simple prompt that includes punctuation: "Hello, welcome to my lecture."

Por tanto, la solucion deseada es:

- anadir un `prompt` por defecto en algunos idiomas principales;
- no exponer esta capacidad al usuario;
- no convertirlo en una nueva opcion de CLI ni de `config.properties`.

## 4. Estado Del Proyecto Relevante Para Esta Spec

La spec previa de separacion de configuracion ya se ha implementado. Eso significa que ahora existe una separacion entre config del usuario e internos de la aplicacion.

Sin embargo, esta nueva spec no debe apoyarse en esa infraestructura para introducir el `prompt`.

Decision importante para esta iteracion:

- el `prompt` no debe ir a `config.properties` del usuario;
- el `prompt` no debe ir a `application-defaults.properties`;
- el `prompt` debe resolverse dentro del servicio de transcripcion, de forma aislada.

Motivo:

- reduce el alcance;
- evita mezclar esta feature con la logica de carga de configuracion;
- evita acoplarla al bug conocido de resolucion de `config.properties` fuera de `java -jar`.

## 5. Decisiones Ya Validadas

Estas decisiones ya estan cerradas y no deben reabrirse durante la implementacion:

- el `prompt` es interno, no configurable por el usuario;
- no se anadira ninguna nueva opcion de linea de comandos;
- no se anadira ninguna nueva clave en `config.properties` del usuario;
- no se anadira ningun detalle tecnico de esta feature al documento funcional del proyecto;
- si se considera oportuno, se pueden anadir comentarios en el codigo;
- no se implementaran tests en esta iteracion;
- el `prompt` debe resolverse segun el idioma efectivo recibido por `WhisperApiService`;
- solo se definiran prompts para algunos idiomas principales;
- si un idioma soportado no tiene prompt definido, no se enviara `prompt` en la request;
- no debe haber fallback a otro idioma;
- no debe haber fallback a ingles;
- no debe haber fallback a espanol.

Idiomas iniciales cerrados para esta iteracion:

- `es`
- `en`
- `fr`
- `de`
- `it`
- `pt`

## 6. Resultado Esperado

### 6.1 Para Idiomas Curados

Si el idioma efectivo de la transcripcion es uno de estos:

- `es`
- `en`
- `fr`
- `de`
- `it`
- `pt`

la request multipart debe incluir un campo adicional:

```text
prompt=<PROMPT_CORRESPONDIENTE>
```

### 6.2 Para Otros Idiomas Soportados

Si el idioma efectivo es otro idioma soportado por la aplicacion pero no esta en la lista anterior, la request debe comportarse como hoy respecto al prompt:

- no debe anadirse el campo `prompt`.

### 6.3 Para El Usuario

Desde el punto de vista del usuario:

- no cambia el contrato de `config.properties`;
- no cambia la CLI;
- no aparece ninguna opcion nueva;
- no tiene que indicar ni gestionar ningun prompt manualmente.

## 7. Prompts Exactos Cerrados

Los textos exactos a utilizar son estos:

- `es`: `Hola, bienvenidos a mi conferencia.`
- `en`: `Hello, welcome to my lecture.`
- `fr`: `Bonjour, bienvenue à ma conférence.`
- `de`: `Hallo, willkommen zu meinem Vortrag.`
- `it`: `Ciao, benvenuti alla mia conferenza.`
- `pt`: `Olá, bem-vindos à minha palestra.`

Notas sobre estos textos:

- deben mantenerse tal cual;
- incluyen puntuacion y ortografia nativa a proposito;
- son cortos para reducir riesgo de sesgar demasiado el contenido;
- no deben sustituirse por prompts mas largos o instructivos en esta iteracion.

## 8. Alcance Y No Alcance

### 8.1 Incluido En Esta Implementacion

- resolucion interna de un prompt por defecto segun idioma;
- anadir el campo `prompt` al multipart cuando exista prompt curado;
- comentarios breves en codigo explicando la intencion de la logica;
- implementacion limitada al servicio de transcripcion.

### 8.2 Fuera De Alcance

- hacer el prompt configurable por usuario;
- almacenar prompts en `config.properties`;
- almacenar prompts en `application-defaults.properties`;
- crear un sistema general de prompts dinamicos;
- usar como prompt el transcript del fragmento anterior;
- postprocesar la transcripcion con otro modelo;
- anadir tests automatizados;
- modificar el documento funcional para reflejar esta mejora interna;
- arreglar problemas no relacionados del refactor de configuracion.

## 9. Restricciones Importantes

Esta implementacion debe ser deliberadamente pequena.

Reglas:

- no tocar `ConfigLoader` para esta feature;
- no tocar la resolucion de `config.properties`;
- no aprovechar esta spec para refactorizar `Main`;
- no mover ahora esta logica a `TranscriptionServiceDefinition`;
- no introducir una abstraccion de mas niveles solo para 6 prompts;
- no anadir dependencias nuevas.

## 10. Diseno Tecnico Recomendado

### 10.1 Ubicacion De La Logica

La logica debe vivir en:

- `src/main/java/eu/nevian/speech_to_text_simple_java_client/transcriptionservice/WhisperApiService.java`

Motivo:

- ese servicio ya construye el multipart de transcripcion;
- recibe el `language` efectivo ya resuelto;
- el cambio queda aislado del resto del sistema;
- no acopla esta feature a la configuracion del usuario.

### 10.2 Forma Recomendada

La forma recomendada para esta iteracion es un helper privado en codigo, por ejemplo:

```java
private String getDefaultPromptForLanguage(String language)
```

o equivalente.

Comportamiento esperado:

- si `language` es `es`, devuelve el prompt de espanol;
- si `language` es `en`, devuelve el prompt de ingles;
- idem para `fr`, `de`, `it`, `pt`;
- si no hay prompt definido, devuelve `null` o equivalente;
- no hace fallback.

### 10.3 Por Que No Usar `.properties` Aqui

Para esta iteracion, no se recomienda usar un recurso `.properties` para los prompts.

Motivos:

- son solo 6 entradas curadas;
- contienen tildes y signos propios del idioma;
- en codigo quedan mas visibles y menos expuestos a problemas de codificacion;
- este cambio no depende del sistema de defaults internos introducido en la spec anterior.

## 11. Cambios Esperados Por Archivo

### 11.1 `src/main/java/.../transcriptionservice/WhisperApiService.java`

Cambios esperados:

- introducir un helper privado para resolver prompt por idioma;
- construir el multipart de transcripcion como hoy;
- resolver el prompt a partir del `language` recibido;
- si existe prompt, anadir `addFormDataPart("prompt", prompt)`;
- si no existe, no anadir nada;
- mantener intactos `file`, `model` y `language`.

### 11.2 `src/main/java/.../transcriptionservice/ApiService.java`

Sin cambios.

La firma publica debe seguir siendo:

```java
String transcribeAudioFile(String apiKey, String language, String audioFilePath) throws IOException;
```

### 11.3 `src/main/java/.../Main.java`

Sin cambios funcionales requeridos para esta spec.

`Main` ya resuelve el idioma efectivo y lo pasa al servicio. Eso es suficiente.

### 11.4 `src/main/resources/application-defaults.properties`

Sin cambios.

No anadir prompts aqui.

### 11.5 `README.md`

Sin cambios requeridos.

### 11.6 `docs/functional-context.md`

Sin cambios requeridos.

## 12. Comportamiento Detallado Esperado

### 12.1 Construccion Del Multipart

El multipart de transcripcion debe seguir incluyendo siempre:

- `file`
- `model`
- `language`

Y ademas:

- `prompt`, solo cuando exista un prompt curado para el idioma efectivo.

### 12.2 Idiomas Sin Prompt Curado

Ejemplo conceptual:

- si el idioma efectivo es `nl` y no existe prompt definido para `nl`, no se envia `prompt`.

El servicio no debe:

- sustituir `nl` por `en`;
- enviar un prompt generico;
- fallar por ausencia de prompt.

### 12.3 Archivos Divididos En Fragmentos

La aplicacion ya puede dividir audios grandes en multiples fragmentos antes de transcribirlos.

Para esta spec, el comportamiento esperado es:

- cada request de transcripcion de cada fragmento recibe el mismo `prompt` si el idioma tiene uno curado;
- no debe implementarse ahora una logica mas avanzada basada en prompts dependientes del fragmento anterior.

## 13. Comentarios En Codigo

Se esperan comentarios breves y utiles, no verbosos.

### 13.1 Comentario En El Catalogo/Helper

Debe explicar algo equivalente a:

- que existen prompts curados para algunos idiomas frecuentes;
- que su objetivo es mejorar la puntuacion/capitalizacion del transcript;
- que para idiomas sin prompt curado se omite el campo intencionadamente.

### 13.2 Comentario En La Construccion Del Multipart

Debe explicar algo equivalente a:

- que el `prompt` solo se envia cuando hay uno curado para el idioma efectivo.

## 14. No Hacer

Durante esta implementacion, no hacer lo siguiente:

- no introducir una nueva clase de configuracion solo para estos prompts, salvo que sea realmente minima y claramente mejor;
- no tocar la CLI;
- no tocar `config.properties` del usuario;
- no tocar `ConfigLoader`;
- no tocar `application-defaults.properties`;
- no anadir nuevos tests;
- no anadir docs funcionales para el usuario final;
- no convertir esta mejora en una feature visible/configurable;
- no mezclar esta tarea con el arreglo del bug de path-resolving detectado en la spec anterior.

## 15. Estrategia De Implementacion Recomendada

Orden sugerido:

1. localizar en `WhisperApiService` el punto exacto donde se crea el `MultipartBody.Builder`;
2. anadir helper privado para resolver prompt por idioma;
3. incorporar el `prompt` de forma condicional al builder;
4. anadir comentarios cortos en el helper y/o en el punto de uso;
5. hacer una verificacion manual del body generado si el otro modelo lo considera necesario.

Motivo del orden:

- mantiene el cambio local y controlado;
- minimiza riesgo de regresion;
- evita abrir frentes en otras capas.

## 16. Verificacion Manual Recomendada

Aunque esta spec no pide tests, si se hace una comprobacion manual, deberia validarse al menos esto:

1. con idioma `es`, la request incluye el `prompt` en espanol;
2. con idioma `en`, la request incluye el `prompt` en ingles;
3. con idioma `fr`, `de`, `it`, `pt`, la request incluye el `prompt` correcto de cada idioma;
4. con un idioma soportado sin entrada curada, por ejemplo `nl`, la request no incluye `prompt`;
5. en todos los casos, siguen presentes `file`, `model` y `language`.

## 17. Criterios De Aceptacion

La implementacion se considerara correcta si se cumplen todos estos puntos:

1. `WhisperApiService` resuelve internamente un prompt por defecto segun idioma;
2. los idiomas `es`, `en`, `fr`, `de`, `it`, `pt` usan exactamente los prompts definidos en esta spec;
3. para idiomas soportados fuera de esa lista no se envia `prompt`;
4. la firma publica de `ApiService` y `WhisperApiService` no cambia por esta feature;
5. no se modifica la CLI;
6. no se modifica `config.properties` del usuario;
7. no se modifica `application-defaults.properties`;
8. no se anaden tests;
9. se anaden comentarios breves en codigo explicando la intencion de la logica;
10. la implementacion queda aislada y no toca el problema conocido de resolucion de path de config.

## 18. Riesgos Y Cuidados

- no introducir accidentalmente un fallback cuando un idioma no tenga prompt;
- no cambiar la firma publica del servicio sin necesidad;
- no mover ahora los prompts a una capa de config mas compleja;
- no olvidar que el objetivo es mejorar puntuacion, no instruir ampliamente al modelo;
- no usar prompts largos que puedan sesgar demasiado la transcripcion;
- no mezclar esta tarea con refactors ajenos.

## 19. Nota De Futuro

En el futuro, cuando el proyecto soporte varios modelos/endpoints de transcripcion internos, podria tener sentido que la definicion del servicio declare algo como:

- si soporta prompts;
- que catalogo de prompts usa;
- o si el prompt debe omitirse para un modelo concreto.

Pero eso queda fuera de esta iteracion.

Para esta iteracion, basta con asumir que el servicio actual soporta `prompt` y resolverlo localmente dentro de `WhisperApiService`.
