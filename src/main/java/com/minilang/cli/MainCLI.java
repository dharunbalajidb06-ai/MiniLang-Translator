package com.minilang.cli;

import com.minilang.MiniLangTranslator;
import com.minilang.TranslationResult;
import com.minilang.errors.CompilerException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Command-Line Interface (CLI) for batch translating MiniLang files to Python, Java, or C.
 */
public class MainCLI {

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            runDemo();
            return;
        }

        String inputFilePath = null;
        String targetLang = "python";
        String outputFilePath = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-t":
                case "--target":
                    if (i + 1 < args.length) targetLang = args[++i].toLowerCase();
                    break;
                case "-o":
                case "--out":
                    if (i + 1 < args.length) outputFilePath = args[++i];
                    break;
                default:
                    if (!args[i].startsWith("-")) {
                        inputFilePath = args[i];
                    }
                    break;
            }
        }

        if (inputFilePath == null) {
            System.err.println("Error: No input .ml source file specified.");
            printUsage();
            System.exit(1);
        }

        try {
            String source = Files.readString(Paths.get(inputFilePath));
            MiniLangTranslator translator = new MiniLangTranslator();
            TranslationResult result = translator.translate(source);

            String generatedCode = switch (targetLang) {
                case "java" -> result.getJavaCode();
                case "c" -> result.getCCode();
                default -> result.getPythonCode();
            };

            if (outputFilePath != null) {
                Path outPath = Paths.get(outputFilePath);
                if (outPath.getParent() != null) {
                    Files.createDirectories(outPath.getParent());
                }
                Files.writeString(outPath, generatedCode);
                System.out.println("✓ Target " + targetLang.toUpperCase() + " code successfully written to: " + outputFilePath);
            } else {
                System.out.println("=== Generated " + targetLang.toUpperCase() + " Code ===");
                System.out.println(generatedCode);
            }

        } catch (CompilerException ex) {
            System.err.println(ex.getMessage());
            System.exit(2);
        } catch (IOException ex) {
            System.err.println("File Error: " + ex.getMessage());
            System.exit(3);
        }
    }

    private static void printUsage() {
        System.out.println("MiniLang Multi-Target Translator CLI");
        System.out.println("Usage: java -jar MiniLangTranslator.jar <source.ml> [-t python|java|c] [-o <output_file>]");
        System.out.println("Options:");
        System.out.println("  -t, --target <lang>    Target language: python (default), java, c");
        System.out.println("  -o, --out <file>       Output file path");
        System.out.println();
    }

    private static void runDemo() {
        System.out.println("Running Demo Translation:\n");
        String demoCode = """
                // Demo MiniLang Program
                int a = 10;
                int b = 20;
                int result;
                result = a + b * 2;
                print(result);
                """;

        MiniLangTranslator translator = new MiniLangTranslator();
        TranslationResult result = translator.translate(demoCode);

        System.out.println("--- Generated Python ---");
        System.out.println(result.getPythonCode());

        System.out.println("--- Generated Java ---");
        System.out.println(result.getJavaCode());

        System.out.println("--- Generated C ---");
        System.out.println(result.getCCode());
    }
}
