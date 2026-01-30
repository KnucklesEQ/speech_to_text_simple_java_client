# Speech to Text Simple Linux Java Client (Whisper/OpenAI)

## Overview
- Simple Java client that sends audio to OpenAI Whisper and saves a transcription.
- Targets Linux environments where `ffmpeg` and `ffprobe` are available on PATH.
- Writes the output to `transcription.txt` in the working directory.

## Development environment (for context, not a requirement)
- Java 17.0.16
- Debian GNU/Linux 13 (Trixie)

## Requirements
- Java 17 or newer.
- A Linux distribution with `ffmpeg` installed and available on PATH (`ffprobe` is part of the ffmpeg suite).
- A valid OpenAI API key (create one at https://platform.openai.com/account/api-keys).

Install ffmpeg on Debian/Ubuntu:
```bash
sudo apt install ffmpeg
```

## Build / Install
- Clone the repository.
- Build and run tests:

```bash
./gradlew build
```

- Build the fat JAR:

```bash
./gradlew shadowJar
```

## Configuration
- Create `config.properties` in the repository root.
- Add your API key and (optionally) a size limit in bytes.

```properties
api_key=YOUR_OPENAI_API_KEY
audio_file_limit_size_in_bytes=25_000_000
```

- Keep real API keys out of version control.

## Usage
- Run via Gradle:

```bash
./gradlew run --args "<PATH_TO_AUDIO>"
```

- Or run the JAR directly:

```bash
java -jar build/libs/speech_to_text_simple_java_client-0.1.0.jar <INPUT_FILE>
```

## Troubleshooting
- `ffmpeg`/`ffprobe` not found: install ffmpeg and confirm it is on PATH.
- Config errors: ensure `config.properties` exists and `api_key` is set.
- File size errors: adjust `audio_file_limit_size_in_bytes` or use a smaller file.
- Video inputs: extract audio first, then pass the audio file.

Example video to audio conversion:
```bash
ffmpeg -i input.mp4 -vn -acodec libmp3lame -b:a 64k output.mp3
```

## License
- See `LICENSE`.
