# Speech to Text Simple Linux Java Client, powered by Whisper (OpenAI)
A simple Java client for interacting with the Whisper model of the OpenAI API. Compatible with Linux only
(you need to have the FFMPEG application installed).

### SDK

* Java 17.0.6

### Development environment used:

* IntelliJ IDEA Community Edition 2023.1
* Debian GNU/Linux 11 (bullseye)

### Requirements
* You must have a valid OpenAI API key. You can create one [here](https://platform.openai.com/account/api-keys).
* You need to have the FFMPEG application installed on your machine. You can install it with the following command:

    ```sudo apt install ffmpeg```

### How to use

* Clone the repository to your local machine
* Compile the project using the following command 
 
    ```./gradlew shadowJar```

* Create a file called `config.properties` in the root directory of the project, with the following content:

    ```api_key=YOUR_OPENAI_API_KEY```

* Run the project (INPUT_FILE is the path to the audio or video file you want to transcribe):

    ```java -jar build/libs/speech_to_text_simple_java_client.jar INPUT_FILE```