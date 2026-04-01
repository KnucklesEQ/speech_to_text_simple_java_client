# Contexto Funcional del Proyecto

## 1. Introducción

Este documento define la especificación funcional de la aplicación `Speech to Text Simple Java Client`. Su finalidad es describir, desde el punto de vista del usuario, qué hace el producto, cómo se utiliza, cuáles son sus requisitos y qué resultados genera.

El contenido se centra exclusivamente en el comportamiento observable de la aplicación. No describe detalles de implementación interna.

## 2. Propósito de la aplicación

La aplicación permite transcribir a texto un archivo multimedia local mediante una ejecución por línea de comandos.

Su objetivo es que el usuario pueda proporcionar un archivo de audio o de vídeo y obtener como resultado un archivo de texto con el contenido transcrito.

## 3. Alcance funcional

La aplicación ofrece las siguientes capacidades funcionales:

- procesa un único archivo por ejecución;
- admite archivos de audio y de vídeo;
- valida el archivo antes de iniciar la transcripción;
- extrae automáticamente el audio de un archivo de vídeo;
- divide automáticamente el audio en partes cuando supera el límite configurado;
- permite indicar el idioma del audio, siempre que sea uno de los soportados;
- guarda el resultado de la transcripción en un archivo de texto;
- muestra un resumen final del resultado obtenido;
- permite decidir al final si el archivo de transcripción permanece en el directorio de trabajo o se mueve a la carpeta del archivo original con un nombre predefinido;
- muestra mensajes de progreso, aviso y error durante la ejecución.

## 4. Usuario objetivo

La aplicación está orientada a usuarios que trabajan desde terminal y necesitan obtener una transcripción escrita de contenido multimedia local.

Se asume que el usuario puede:

- ejecutar comandos básicos en consola;
- preparar un archivo de configuración;
- identificar la ruta local del archivo que desea transcribir.

## 5. Requisitos previos de uso

Para poder utilizar la aplicación correctamente, el usuario debe contar con:

- Java 21 o una versión superior compatible con el proyecto;
- `ffmpeg` y `ffprobe` disponibles en el sistema y accesibles desde la línea de comandos;
- conexión a Internet;
- una clave válida de acceso al servicio de transcripción;
- un archivo de configuración correctamente preparado.

Si alguno de estos requisitos no se cumple, la aplicación no puede completar el proceso de transcripción.

## 6. Configuración funcional

La aplicación utiliza un archivo `config.properties` para recoger los datos de configuración funcional.

Cuando se ejecuta la aplicación mediante el archivo jar, ese fichero debe estar disponible junto al propio jar.

En otros contextos de ejecución, la aplicación utiliza el `config.properties` disponible en el directorio de trabajo actual.

### 6.1 Datos de configuración

Los parámetros funcionales utilizados son:

- `api_key`: credencial necesaria para acceder al servicio de transcripción;
- `audio_file_limit_size_in_bytes`: límite máximo de tamaño permitido para procesar un archivo sin necesidad de dividirlo;
- `language`: preferencia de idioma a utilizar por defecto en la transcripción, expresada mediante un código de dos letras soportado por la aplicación.

### 6.2 Carácter obligatorio u opcional

- `api_key` es obligatoria para el uso efectivo de la aplicación.
- `audio_file_limit_size_in_bytes` debe estar disponible para que la aplicación pueda decidir si necesita dividir el archivo.
- `language` es opcional. Si no existe, la aplicación utiliza un idioma por defecto. Si existe pero no es válida o no está soportada, la aplicación muestra un aviso y utiliza el idioma por defecto.

### 6.3 Comportamiento ante errores de configuración

Si falta la configuración requerida o contiene valores no utilizables, la aplicación informa del problema y detiene la ejecución cuando el error impide continuar.

Cuando el valor de `language` almacenado en la configuración no es válido o no está soportado, la aplicación avisa y continúa utilizando el idioma por defecto.

### 6.4 Ejemplo de configuración mínima

```properties
api_key=TU_CLAVE_DE_API
audio_file_limit_size_in_bytes=25000000
language=es
```

## 7. Modos de uso y opciones disponibles

La aplicación se utiliza por línea de comandos.

### 7.1 Operaciones disponibles

- consultar la ayuda de uso;
- consultar la versión de la aplicación;
- transcribir un archivo;
- indicar manualmente el idioma de la transcripción.

### 7.2 Restricciones de entrada por línea de comandos

- la aplicación requiere exactamente un archivo de entrada por ejecución;
- no admite varios archivos en una sola invocación;
- si no se proporciona ningún archivo, la ejecución se considera inválida.

### 7.3 Ejemplos de uso

#### 7.3.1 Consulta de ayuda

Qué hace: muestra la ayuda de uso y finaliza la ejecución.

```bash
java -jar build/libs/speech_to_text_simple_java_client-0.1.0.jar -h
```

Salida esperada:

- se muestra en consola la información de uso y las opciones disponibles;
- la ejecución finaliza sin procesar ningún archivo;
- no se genera `transcription.txt`.

#### 7.3.2 Consulta de versión

Qué hace: muestra la versión de la aplicación y finaliza la ejecución.

```bash
java -jar build/libs/speech_to_text_simple_java_client-0.1.0.jar -v
```

Salida esperada:

- se muestra en consola la versión de la aplicación;
- la ejecución finaliza sin procesar ningún archivo;
- no se genera `transcription.txt`.

#### 7.3.3 Transcripción básica de un archivo de audio

Qué hace: transcribe un archivo de audio válido utilizando la configuración disponible.

```bash
java -jar build/libs/speech_to_text_simple_java_client-0.1.0.jar ruta/al/audio.mp3
```

Salida esperada:

- se muestran mensajes de validación y progreso en consola;
- se genera `transcription.txt` en el directorio de trabajo;
- al finalizar, la aplicación muestra un resumen del resultado y solicita al usuario la decisión sobre la ubicación final del archivo;
- el archivo contiene el resultado textual de la transcripción.

#### 7.3.4 Transcripción de un archivo de vídeo

Qué hace: recibe un vídeo, extrae su audio y continúa el proceso de transcripción.

```bash
java -jar build/libs/speech_to_text_simple_java_client-0.1.0.jar ruta/al/video.mp4
```

Salida esperada:

- la consola informa de que se ha detectado un vídeo y de que se extrae su audio;
- la transcripción continúa automáticamente después de esa extracción;
- se genera `transcription.txt` en el directorio de trabajo;
- al finalizar, la aplicación solicita al usuario la decisión sobre la ubicación final del archivo.

#### 7.3.5 Transcripción de un archivo de audio que supera el límite configurado

Qué hace: procesa un archivo de audio grande mediante división automática en partes.

```bash
java -jar build/libs/speech_to_text_simple_java_client-0.1.0.jar ruta/al/audio_grande.mp3
```

Salida esperada:

- la consola informa de que el archivo supera el límite configurado;
- la aplicación divide automáticamente el contenido en fragmentos;
- se genera un único `transcription.txt` con el resultado conjunto;
- al finalizar, la aplicación muestra un resumen del texto y solicita al usuario la decisión sobre la ubicación final del archivo.

#### 7.3.6 Transcripción indicando explícitamente el idioma

Qué hace: transcribe un archivo de audio utilizando el idioma indicado en el propio comando.

```bash
java -jar build/libs/speech_to_text_simple_java_client-0.1.0.jar -l es ruta/al/audio.mp3
```

Salida esperada:

- la aplicación utiliza el idioma indicado para la transcripción;
- si el resto de requisitos se cumple, se genera `transcription.txt` y la aplicación solicita al usuario la decisión sobre su ubicación final;
- la preferencia de idioma queda disponible para futuras ejecuciones.

#### 7.3.7 Ejecución sin archivo de entrada

Qué hace: intenta ejecutar la aplicación sin indicar ningún archivo.

```bash
java -jar build/libs/speech_to_text_simple_java_client-0.1.0.jar
```

Salida esperada:

- la consola muestra un error indicando que falta el archivo de entrada;
- la consola muestra la ayuda de uso;
- la ejecución finaliza sin generar `transcription.txt`.

#### 7.3.8 Ejecución con demasiados archivos de entrada

Qué hace: intenta ejecutar la aplicación indicando más de un archivo.

```bash
java -jar build/libs/speech_to_text_simple_java_client-0.1.0.jar ruta/al/audio1.mp3 ruta/al/audio2.mp3
```

Salida esperada:

- la consola muestra un error indicando que se ha proporcionado más de un archivo;
- la consola muestra la ayuda de uso;
- la ejecución finaliza sin generar `transcription.txt`.

#### 7.3.9 Ejecución con idioma inválido o no soportado

Qué hace: intenta transcribir un archivo indicando un código de idioma no soportado.

```bash
java -jar build/libs/speech_to_text_simple_java_client-0.1.0.jar -l aa ruta/al/audio.mp3
```

Salida esperada:

- la consola muestra un error indicando que el código de idioma no es válido o no está soportado;
- la consola muestra la ayuda de uso;
- la ejecución finaliza sin generar `transcription.txt`.

#### 7.3.10 Ejecución con ruta inexistente

Qué hace: intenta transcribir un archivo cuya ruta no existe.

```bash
java -jar build/libs/speech_to_text_simple_java_client-0.1.0.jar ruta/que/no/existe.mp3
```

Salida esperada:

- la consola muestra un error indicando que el archivo no se ha encontrado;
- la ejecución finaliza sin generar `transcription.txt`.

## 8. Entradas admitidas

### 8.1 Tipos de archivo admitidos

La aplicación admite archivos multimedia cuyo contenido sea identificable como:

- audio;
- vídeo.

### 8.2 Condiciones de validez

Para que un archivo sea aceptado, debe cumplir, al menos, estas condiciones:

- existir en la ruta indicada;
- ser un archivo regular;
- ser legible;
- corresponder a un tipo admitido.

### 8.3 Casos de rechazo

La aplicación rechaza la entrada cuando:

- la ruta no existe;
- la ruta no corresponde a un archivo válido;
- el archivo no puede leerse;
- el tipo de contenido no es audio ni vídeo.

## 9. Tratamiento específico de vídeos

Cuando el usuario proporciona un archivo de vídeo, no es necesario que lo convierta manualmente a audio antes de usar la aplicación.

La aplicación incorpora ese paso dentro del flujo general y transforma el vídeo en un audio intermedio apto para su transcripción.

Desde el punto de vista funcional, el usuario sigue trabajando con un único comando y recibe un único resultado final.

## 10. Tratamiento de archivos grandes

La aplicación contempla el caso en que el archivo de entrada exceda el límite de tamaño configurado.

En ese escenario:

- divide automáticamente el contenido en fragmentos manejables;
- transcribe los fragmentos de forma sucesiva;
- recompone el resultado en una única salida final.

Este comportamiento busca evitar que el usuario tenga que preparar manualmente varias piezas de audio antes de utilizar la herramienta.

## 11. Gestión del idioma

La aplicación utiliza un criterio de prioridad para determinar el idioma de trabajo:

1. idioma indicado explícitamente por el usuario en la ejecución en curso;
2. idioma guardado en la configuración;
3. idioma por defecto de la aplicación.

Los valores de idioma deben indicarse mediante un código breve de dos letras soportado por la aplicación.

Además, cuando el usuario indica explícitamente un idioma en la línea de comandos, la aplicación conserva esa preferencia para futuras ejecuciones.

Si el valor de idioma disponible no es válido o no está soportado, la aplicación avisa y utiliza el idioma por defecto de la aplicación, que es inglés (`en`).

Si el usuario indica en la línea de comandos un idioma no válido o no soportado, la aplicación informa del error, muestra la ayuda y finaliza sin continuar el procesamiento.

## 12. Comportamientos automáticos

La aplicación realiza automáticamente varias acciones sin solicitar intervención adicional del usuario:

- validación de la entrada;
- conversión de vídeo a audio cuando procede;
- detección de necesidad de particionado del audio por tamaño de archivo;
- división automática del audio;
- uso de un idioma por defecto cuando no existe uno válido;
- persistencia de la preferencia de idioma cuando el usuario la fija explícitamente;
- creación y eliminación de archivos temporales de trabajo.

## 13. Resultado generado

### 13.1 Archivo de salida

La aplicación genera inicialmente un archivo llamado `transcription.txt`.

### 13.2 Ubicación del archivo de salida

Durante el proceso, el archivo de salida se guarda en el directorio de trabajo desde el que se ejecuta la aplicación.

Una vez completada la transcripción, la aplicación pregunta al usuario si desea mantener ese archivo en esa ubicación o moverlo a la carpeta del archivo original.

Si el usuario decide moverlo, el archivo final se guarda en la carpeta del archivo de entrada con el nombre `<NOMBRE_ORIGINAL>_TRANSCRIPTION.txt`.

Si el usuario decide no moverlo, el resultado permanece como `transcription.txt` en el directorio de trabajo.

### 13.3 Contenido funcional esperado

El archivo contiene el resultado textual devuelto por el proceso de transcripción.

Cuando el procesamiento se ha realizado a partir de varios fragmentos, la aplicación compone un único resultado final e introduce un separador visible entre partes.

Al finalizar, la aplicación muestra además un resumen con el número de caracteres y palabras del texto generado.

## 14. Información visible para el usuario durante la ejecución

La aplicación informa al usuario sobre el avance del proceso mediante mensajes en consola, entre ellos:

- inicio del flujo;
- validación del archivo;
- confirmación del tipo de entrada detectado;
- detección de vídeo y extracción de audio;
- detección de archivo grande y división en partes;
- comprobación de acceso al servicio de transcripción;
- presentación de un resumen final del texto generado;
- solicitud de decisión sobre la ubicación final del archivo de transcripción;
- finalización del proceso.

Asimismo, la aplicación puede mostrar avisos cuando detecta configuraciones no óptimas o valores no válidos que aún permiten continuar.

## 15. Errores y situaciones excepcionales

Entre las situaciones que la aplicación puede comunicar al usuario se encuentran:

- ausencia del archivo de entrada;
- presencia de demasiados argumentos de archivo;
- idioma inválido o no soportado;
- archivo no legible o no admitido;
- ausencia de herramientas externas necesarias;
- configuración inexistente, incompleta o incorrecta;
- imposibilidad de preparar el contenido para transcripción;
- fallo al contactar con el servicio de transcripción;
- imposibilidad de crear o limpiar recursos temporales.

Cuando el error impide continuar, la aplicación finaliza sin completar la generación de la transcripción.

## 16. Limitaciones

La aplicación presenta las siguientes limitaciones funcionales:

- procesa un único archivo por ejecución;
- no ofrece procesamiento por lotes;
- no dispone de interfaz gráfica;
- no permite elegir libremente el nombre final del archivo de salida;
- no permite elegir una ubicación arbitraria para la transcripción final;
- la decisión final sobre la ubicación del archivo se limita a mantenerlo en el directorio de trabajo o moverlo a la carpeta del archivo original;
- cuando el archivo se mueve, el nombre final queda fijado al patrón `<NOMBRE_ORIGINAL>_TRANSCRIPTION.txt`;
- depende de una conexión de red operativa y de la disponibilidad de un servicio externo;
- requiere preparación previa del entorno antes de su uso;

## 17. Escenarios de uso

### 17.1 Caso básico con audio

El usuario proporciona un archivo de audio válido. La aplicación genera `transcription.txt`, muestra un resumen final y permite decidir si el archivo permanece en el directorio de trabajo o se mueve a la carpeta del archivo original.

### 17.2 Caso con vídeo

El usuario proporciona un archivo de vídeo válido. La aplicación extrae automáticamente el audio necesario, realiza la transcripción, genera un único archivo de salida y solicita la decisión final sobre su ubicación.

### 17.3 Caso con archivo grande

El usuario proporciona un archivo cuyo tamaño supera el límite configurado. La aplicación lo divide, procesa las partes, recompone un único resultado final y permite decidir la ubicación final del archivo generado.

### 17.4 Caso con idioma indicado por el usuario

El usuario fija un idioma en la ejecución. La aplicación utiliza ese idioma y conserva la preferencia para futuras ejecuciones.

### 17.5 Caso con error de configuración o entrada

Si falta información necesaria o la entrada no es válida, la aplicación informa del problema y detiene la ejecución.

## 18. Consideraciones operativas, privacidad y coste

El uso de la aplicación implica el envío del contenido a un servicio externo de transcripción. Por ello, deben considerarse los siguientes aspectos:

- puede existir coste asociado al uso del servicio;
- es necesaria conectividad de red;
- el usuario debe custodiar adecuadamente su credencial de acceso;
- el usuario debe valorar la sensibilidad de los archivos que decide procesar.

## 19. Glosario breve

- `archivo de entrada`: archivo multimedia proporcionado por el usuario para su procesamiento.
- `transcripción`: resultado textual obtenido a partir del contenido de audio.
- `archivo de salida`: fichero generado por la aplicación con el resultado del proceso.
- `idioma`: preferencia utilizada para orientar la transcripción.
- `límite de tamaño`: umbral que determina si la aplicación debe dividir automáticamente el contenido.

## 20. Resumen final

La aplicación proporciona una capacidad funcional clara: recibir un archivo multimedia local y producir una salida textual con su transcripción.

La aplicación cubre el flujo principal de uso, incluyendo validación, tratamiento automático de vídeo, gestión de archivos grandes, configuración de idioma, generación inicial de `transcription.txt`, presentación de un resumen final y decisión sobre la ubicación final del archivo de transcripción.
