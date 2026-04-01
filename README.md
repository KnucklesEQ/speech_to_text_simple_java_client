# Speech to Text Simple Java Client

## Overview
- Command line application that transcribes one local audio or video file per execution using OpenAI Whisper.
- Video inputs are handled directly: the application extracts the audio automatically before transcription.
- Large audio files are split automatically when they exceed the configured size limit.
- The final transcription is written to `transcription.txt` in the working directory.
- After a successful transcription, the application shows a short summary and asks whether the file should be moved to the original file's folder as `<ORIGINAL_NAME>_TRANSCRIPTION.txt`.

## Requirements
- Java 21 or newer.
- `ffmpeg` and `ffprobe` available on `PATH`.
- Internet access.
- A valid OpenAI API key.

Install ffmpeg on Debian/Ubuntu:

```bash
sudo apt install ffmpeg
```

## Build
Build the project:

```bash
./gradlew build
```

Build the runnable fat JAR:

```bash
./gradlew shadowJar
```

## Configuration
The application expects an external `config.properties` file.

- When you run the fat JAR, `config.properties` must be next to the JAR.
- In other execution modes, `config.properties` is read from the current working directory.

Required and supported properties:

```properties
api_key=YOUR_OPENAI_API_KEY
language=en
```

- `api_key`: required.
- `language`: optional; must be a supported two-letter ISO-639-1 code. If it is missing or invalid in `config.properties`, the app falls back to the bundled default `en`.
- If you pass `-l <code>` on the command line, that language is also saved back to `config.properties` for future runs.
- `audio_file_limit_size_in_bytes` is now an internal bundled default, not a user-facing property.

Keep real API keys out of version control.

## Usage
Show help:

```bash
java -jar build/libs/speech_to_text_simple_java_client-0.1.0.jar -h
```

Show version:

```bash
java -jar build/libs/speech_to_text_simple_java_client-0.1.0.jar -v
```

Transcribe an audio file:

```bash
java -jar build/libs/speech_to_text_simple_java_client-0.1.0.jar path/to/audio.mp3
```

Transcribe a video file:

```bash
java -jar build/libs/speech_to_text_simple_java_client-0.1.0.jar path/to/video.mp4
```

Force the language for the current run:

```bash
java -jar build/libs/speech_to_text_simple_java_client-0.1.0.jar -l es path/to/audio.mp3
```

Run with Gradle:

```bash
./gradlew run --args="path/to/audio.mp3"
```

Notes:
- The application requires exactly one input file per execution.
- Input paths with spaces should be quoted.
- When a large file is split, the final output is merged into a single transcription with `//` separators between parts.

## Output behavior
- The transcription is first saved as `transcription.txt` in the working directory.
- After that, the app prints the number of characters and words in the final transcription.
- The app then asks whether `transcription.txt` should be moved to the original file's folder.
- If you answer `y`, the file is moved and renamed to `<ORIGINAL_NAME>_TRANSCRIPTION.txt`.
- If the destination file already exists, it is overwritten.
- Any answer other than `y` keeps `transcription.txt` in the working directory.

## Troubleshooting
- `ffmpeg` or `ffprobe` not found: install ffmpeg and make sure both commands are available on `PATH`.
- `config.properties` errors: ensure the file exists in the expected location and that `api_key` is present.
- Invalid language code: use a supported two-letter language code such as `en` or `es`.
- File rejected: make sure the path exists, points to a readable regular file, and that the file is detected as audio or video.
- API errors: verify your API key and network connectivity.

## License
See `LICENSE`.
