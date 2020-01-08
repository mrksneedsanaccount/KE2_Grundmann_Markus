package propra.conversion_facilitators;

import propra.exceptions.IllegalCommandLineInputException;
import propra.helpers.ProjectConstants;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class CommandLineInterpreter {

    public static final String RLE = "rle";
    public static final String UNCOMPRESSED = "uncompressed";
    public static final String KE1CONVERSION = "ke1conversion";
    public static final String DECODE = "--decode-";
    public static final String ENCODE = "--encode-";
    static final String TGA = ".tga";
    static final String PROPRA = ".propra";
    static final String BASEN = ".base-n";
    static final String BASE32 = ".base-32";
    private static final String HUFFMAN = "huffman";
    private static final String AUTO = "auto";
    private static final String INPUT = "--input=";
    private static final String OUTPUT = "--output=";
    private static final String COMPRESSION = "--compression=";
    private static final String[] FILETYPES = {".tga", ".propra", ".base-32", ".base-n"};
    public String mode = null;
    String inputSuffix;
    String outputSuffix;
    private Path inputPath;
    private Path outputPath;
    private String alphabet;

    public CommandLineInterpreter(String[] args) throws IllegalCommandLineInputException {

        interpretInputs(args);


    }

    public CommandLineInterpreter() {

    }

    public String getAlphabet() {
        return alphabet;
    }

    public String getFileExtensionFromString(String inputString) {
        return inputString.substring(inputString.lastIndexOf("."));
    }

    public Path getInputPath() {
        return inputPath;
    }

    public String getInputSuffix() {
        return inputSuffix;
    }

    public String getMode() {
        return mode;
    }

    public Path getOutputPath() {
        return outputPath;
    }

    public String getOutputSuffix() {
        return outputSuffix;
    }

    public void interpretInputs(String[] args) throws IllegalCommandLineInputException {

        // Checks the first argument of the program's input.
        // determines the input path and the input's file type.
        // Also checks if the file format is supported
        if (args[0].startsWith(INPUT)) {
            String inputPathString = args[0].substring(INPUT.length());


            boolean temp = Files.exists(Paths.get(inputPathString));
            if (!temp) {
                throw new IllegalCommandLineInputException("Can not locate source file at the provided path.");
            }

            inputPath = Paths.get(inputPathString);

            inputSuffix = getFileExtensionFromString(inputPathString);

        } else {
            throw new IllegalCommandLineInputException("The first argument has to start with " + INPUT);
        }

        // Checks the second argument.
        //Determines if it is an encode/decode operation, or if it is a compression of some type.
        //Generally determines the output path and file format. (Also in case of encoding/decoding which alphabet is used)
        if (args[1].startsWith(OUTPUT)) {
            if (args.length == 2) {
                mode = KE1CONVERSION;
            }
            String outputPathString = args[1].substring(OUTPUT.length());
            outputPath = Paths.get(outputPathString);
        } else if (args.length == 2 && args[1].startsWith(DECODE)) {
            mode = DECODE;
            String outputPathString = args[0].substring(INPUT.length(), args[0].length() - inputSuffix.length());
            outputPath = Paths.get(outputPathString);
        } else if (args.length == 2 && args[1].startsWith(ENCODE)) {
            mode = ENCODE;
            String outputPathString = args[0].substring(INPUT.length());
            String encode = args[1].substring(ENCODE.length());
            if (encode.startsWith("base-32")) {
                outputSuffix = BASE32;
                alphabet = ProjectConstants.BASE32HEX;
            } else if (encode.startsWith("base-n")) {
                outputSuffix = BASEN;
                alphabet = encode.substring(encode.indexOf('=') + 1);
            }
            outputPath = Paths.get(outputPathString + outputSuffix);
        } else {
            throw new IllegalCommandLineInputException("Illegal Prefix for second Argument. Only " + ENCODE + " or " + DECODE + " or " + COMPRESSION + " are allowed.");
        }


        boolean temp = new File(outputPath.toString()).getParentFile().exists();
        if (!temp) {
            throw new IllegalCommandLineInputException("The output path does not exist.");
        }
        outputSuffix = getFileExtensionFromString(outputPath.toString());

        //Determines the compression of the outputfile, or if
        if (args.length == 3 && args[2].startsWith(COMPRESSION)) {
            String compressiontype = args[2].substring(COMPRESSION.length());
            if (compressiontype.equals(RLE)) {
                mode = RLE;
            } else if (compressiontype.equals(UNCOMPRESSED)) {
                mode = UNCOMPRESSED;
            } else if (compressiontype.equals(HUFFMAN)) {
                mode = HUFFMAN;
                if (outputSuffix.equals(TGA)) {
                    throw new IllegalCommandLineInputException("Huffman compression is only available for .propra files. Please check your chosen output format.");
                }
            } else if (compressiontype.equals(AUTO)) {
                mode = AUTO;
            } else {
                throw new IllegalCommandLineInputException("The requested compression is not supported. Only 'rle', 'uncompressed'. 'huffman' (.propra only) and 'auto' are supported.");
            }
        }
        if (args.length > 3) {
            throw new IllegalCommandLineInputException("This program only accepts two, or three arguments as input.");
        }


        // check the suffix of the inputfile.
        if (!Arrays.stream(FILETYPES)
                .anyMatch(inputSuffix::equals) // https://stackoverflow.com/questions/1128723/how-do-i-determine-whether-an-array-contains-a-particular-value-in-java
        ) {
            throw new IllegalCommandLineInputException("Input is of an unsupported file type");
        }
        if (!Arrays.stream(FILETYPES)
                .anyMatch(outputSuffix::equals) // https://stackoverflow.com/questions/1128723/how-do-i-determine-whether-an-array-contains-a-particular-value-in-java
        ) {
            throw new IllegalCommandLineInputException("Input is of an unsupported file type");
        }

        if (mode == null) {
            throw new IllegalCommandLineInputException("Could not resolve requested operation.");
        }

        if (mode.equals(ENCODE) && alphabet == null) {
            throw new IllegalCommandLineInputException("Could not determine an alphabet.");
        }


        System.out.println("Origin: " + inputPath + " Destination: " + outputPath);

        System.out.println("Conversion: " + inputSuffix + " -> " + outputSuffix);


    }

    public void setMode(String mode) {
        this.mode = mode;
    }

}
