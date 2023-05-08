package eu.nevian.speech_to_text_simple_java_client.commandlinemanagement;

import org.apache.commons.cli.*;

public class CommandLineManagement {
    private final Options options;

    public CommandLineManagement() {
        options = new Options();
        initOptions();
    }

    private void initOptions() {
        Option helpOption = new Option("h", "help", false, "Show help");
        helpOption.setArgName(" ");
        options.addOption(helpOption);

        Option versionOption = new Option("v", "version", false, "Show version");
        versionOption.setArgName(" ");
        options.addOption(versionOption);

        Option languageOption = new Option("l", "language", true, "Language of the audio file in ISO-639-1 format");
        languageOption.setArgName("language");
        options.addOption(languageOption);
    }

    public CommandLineOptions parseCommandLineArguments(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();

        CommandLine cmd = parser.parse(options, args);

        return new CommandLineOptions(cmd, options);
    }
}
