package com.github.parker8283.bon2;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileNotFoundException;

import com.github.parker8283.bon2.cli.CLIErrorHandler;
import com.github.parker8283.bon2.cli.CLIProgressListener;
import com.github.parker8283.bon2.data.IErrorHandler;
import com.github.parker8283.bon2.exception.InvalidMappingsVersionException;
import com.github.parker8283.bon2.util.BONUtils;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class BON2 {
    public static final String VERSION = "Bearded Octo Nemesis v${DEV} by Parker8283. BON v1 by immibis.";

    public static void main(String[] args) throws Exception {
        if(args.length > 0) {
            parseArgs(args);
        } else {
            launchGui();
        }
    }

    private static void parseArgs(String[] args) throws Exception {
        OptionParser parser = new OptionParser();
        parser.accepts("help", "Prints this help menu").forHelp();
        parser.accepts("version", "Prints the version string").forHelp();
        parser.accepts("inputJar", "The jar file to deobfuscate").withRequiredArg().required();
        parser.accepts("outputJar", "The location and name of the output jar. Defaults to same dir and appends \"-deobf\"").withRequiredArg();
        parser.accepts("mappingsVer", "The version of the mappings to use. Must exist in Gradle cache. Format is \"mcVer-forgeVer-mappingVer\"").withRequiredArg().required();
        parser.accepts("debug", "Enables extra debug logging. Helpful to figure out what the hell is going on.");

        try {
            OptionSet options = parser.parse(args);
            if(options.has("help")) {
                System.out.println(VERSION);
                parser.printHelpOn(System.out);
                System.exit(0);
            }
            if(options.has("version")) {
                System.out.println(VERSION);
                System.exit(0);
            }

            String inputJar = (String)options.valueOf("inputJar");
            String outputJar = options.has("outputJar") ? (String)options.valueOf("outputJar") : inputJar.replace(".jar", "-deobf.jar");
            String mappingsVer = (String)options.valueOf("mappingsVer");
            boolean debug = options.has("debug");
            if(!new File(inputJar).exists()) {
                System.err.println("The provided inputJar does not exist");
                new FileNotFoundException(inputJar).printStackTrace();
                System.exit(1);
            }
            if(!BONUtils.buildValidMappings().contains(mappingsVer)) {
                System.err.println("The provided mappingsVer are invalid. The mappings must exist in your Gradle cache. Format is \"mcVer-forgeVer-mappingVer\"");
                new InvalidMappingsVersionException(mappingsVer).printStackTrace();
                System.exit(1);
            }
            IErrorHandler errorHandler = new CLIErrorHandler();

            log(VERSION);
            log("Input JAR:  " + inputJar);
            log("Output JAR: " + outputJar);
            log("Mappings:   " + mappingsVer);
            log("Debug:      " + debug);

            try {
                BON2Impl.remap(new File(inputJar), new File(outputJar), mappingsVer, errorHandler, new CLIProgressListener());
            } catch(Exception e) {
                logErr(e.getMessage(), e);
                System.exit(1);
            }
        } catch(OptionException e) {
            e.printStackTrace();
            parser.printHelpOn(System.err);
        }
    }

    private static void log(String message) {
        System.out.println(message);
    }

    private static void logErr(String message, Throwable t) {
        System.err.println(message);
        t.printStackTrace();
    }

    private static void launchGui() {
        log(VERSION);
        log("No arguments passed. Launching gui...");
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    BON2Gui frame = new BON2Gui();
                    frame.setVisible(true);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
