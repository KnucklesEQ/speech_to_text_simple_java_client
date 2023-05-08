package eu.nevian.speech_to_text_simple_java_client.commandlinemanagement;

import eu.nevian.speech_to_text_simple_java_client.Main;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.List;

public class CommandLineOptions {
    private final CommandLine cmd;
    private final Options options;

    public CommandLineOptions(CommandLine cmd, Options options) {
        this.cmd = cmd;
        this.options = options;
    }

    public boolean hasHelpOption() {
        return cmd.hasOption("help");
    }

    public String getVersionOption() {
        if (cmd.hasOption("version")) {
            String version = Main.class.getPackage().getImplementationVersion();
            return version != null ? version : "unknown";
        } else {
            return null;
        }
    }

    public String getLanguageOption() {
        if (cmd.hasOption("language")) {
            return cmd.getOptionValue("language");
        }
        return null;
    }

    public List<String> getRemainingArgs() {
        return cmd.getArgList();
    }

    public void printCustomHelp() {
        System.out.println("Usage:");
        System.out.println("  java -jar speech_to_text_simple_java_client.jar [options] <FILE>");
        System.out.println("\nOptions:");
        System.out.printf("  %-2s %-8s  %s%n", "<FILE>", "", "Path to audio file or video file to transcribe");
        for (Option option : options.getOptions()) {
            System.out.printf("  -%s,--%-10s  %s%n", option.getOpt(), option.getLongOpt(), option.getDescription());
        }
    }
}
