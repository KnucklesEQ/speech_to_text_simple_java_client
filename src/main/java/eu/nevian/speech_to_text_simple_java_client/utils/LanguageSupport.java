package eu.nevian.speech_to_text_simple_java_client.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LanguageSupport {
    public static final String DEFAULT_LANGUAGE = "en";

    private static final Set<String> SUPPORTED_LANGUAGES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "af", "ar", "hy", "az", "be", "bs", "bg", "ca", "zh", "hr", "cs", "da", "nl", "en", "et", "fi", "fr",
            "gl", "de", "el", "he", "hi", "hu", "is", "id", "it", "ja", "kn", "kk", "ko", "lv", "lt", "mk", "ms",
            "mr", "mi", "ne", "no", "fa", "pl", "pt", "ro", "ru", "sr", "sk", "sl", "es", "sw", "sv", "tl", "ta",
            "th", "tr", "uk", "ur", "vi", "cy"
    )));

    private LanguageSupport() {
    }

    public static boolean isSupported(String language) {
        if (language == null) {
            return false;
        }

        return SUPPORTED_LANGUAGES.contains(language);
    }

    public static boolean isNotSupported(String language) {
        return !isSupported(language);
    }
}
