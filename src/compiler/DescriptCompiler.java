package compiler;

import code.CodeGenerator;
import lexer.Lexer;
import lexer.token.Token;
import parser.ErrorHandler;
import parser.Parser;
import parser.variables.SymbolTable;
import parser.nodes.ASTNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class DescriptCompiler {
    public static void run(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: Descript <command>");
            return;
        }

        String command = args[0];
        if ("build".equals(command)) {
            if (args.length != 3) {
                System.out.println("Usage: Descript build <src.ds> <output_name>");
                return;
            }
            final String inputFile = args[1];
            final String outputClassFile = "GeneratedClass.class";
            final String outputJarFile = args[2] + ".jar";

            try {
                final String sourceCode = Files.readString(Path.of(inputFile));

                Process jarProcess = compileAndCreateJar(sourceCode, outputJarFile);
                if (jarProcess == null)
                    return;
                jarProcess.waitFor();

                if (!new File(outputClassFile).delete()) {
                    System.out.println("Could not delete temp files");
                }

                System.out.println("JAR file created: " + outputJarFile);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        } else if (command.equals("run")) {
            if (args.length != 2) {
                System.out.println("Usage: Descript run <file.jar>");
                return;
            }

            String jarFilePath = args[1];

            try {
                ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", jarFilePath);
                processBuilder.inheritIO();
                Process process = processBuilder.start();

                int exitCode = process.waitFor();
                System.out.println("Process exited with code: " + exitCode);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        } else if (command.equals("help")) {
            System.out.println("Descript build <src.ds> <output_name>");
            System.out.println("Descript run <file.jar>");
        } else {
            System.out.println("Unknown command: " + command);
            System.out.println("Use Descript help for help");
        }
    }

    private static Process compileAndCreateJar(String sourceCode, String outputJarFile) throws IOException {
        final Lexer lexer = new Lexer(sourceCode);
        final List<Token> tokens = lexer.tokenize();

        final ErrorHandler errorHandler = new ErrorHandler();
        final SymbolTable symbolTable = new SymbolTable();
        final Parser parser = new Parser(tokens, errorHandler, symbolTable);
        final ASTNode tree = parser.parse();

        if (errorHandler.hasErrors()) {
            errorHandler.printErrors();
            return null;
        }

        CodeGenerator codeGenerator = new CodeGenerator(symbolTable);
        codeGenerator.startMainMethod();
        codeGenerator.generate(tree);
        codeGenerator.endMainMethod();
        byte[] bytecode = codeGenerator.generateClass();

        try (FileOutputStream fos = new FileOutputStream("GeneratedClass.class")) {
            fos.write(bytecode);
        }

        ProcessBuilder jarProcessBuilder = new ProcessBuilder(
            "jar", "cfe", outputJarFile, "GeneratedClass", "GeneratedClass.class"
        );
        jarProcessBuilder.inheritIO();
        return jarProcessBuilder.start();
    }
}
