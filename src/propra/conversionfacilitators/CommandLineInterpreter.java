package src.propra.conversionfacilitators;

import src.helperclasses.ProjectConstants;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class CommandLineInterpreter {

    public static final String RLE = "rle";
    public static final String UNCOMPRESSED = "uncompressed";
    public static final String KE1CONVERSION = "ke1conversion";
    private static final String HUFFMAN = "huffman";
    private static final String AUTO = "auto";
    public static final String TGA = ".tga";
    static final String PROPRA = ".propra";
    static final String BASEN = ".base-n";
    public static final String BASENTYP = "base-n";
    static final String BASE32 = ".base-32";
    private static final String BASE32TYP = "base-32";
    private static final String INPUT = "--input=";
    private static final String OUTPUT = "--output=";
    private static final String COMPRESSION = "--compression=";
    public static final String DECODE = "--decode-";
    public static final String ENCODE = "--encode-";
    private static final String[] FILETYPES = {".tga", ".propra", ".base-32", ".base-n"};
    public String mode;
    Path inputPath;
    String inputSuffix;
    Path outputPath;
    String outputSuffix;
    String outputCompression;
    String alphabet;

    public CommandLineInterpreter(String[] args) {

        interpretInputs(args);


    }

    public CommandLineInterpreter() {

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

    public String getOutputCompression() {
        return outputCompression;
    }

    public Path getOutputPath() {
        return outputPath;
    }

    public String getOutputSuffix() {
        return outputSuffix;
    }

    public String getAlphabet() {
        return alphabet;
    }

    public void interpretInputs(String[] args) {
        if (args[0].startsWith(INPUT)) {
            String inputPathString = args[0].substring(INPUT.length());
            inputPath = Paths.get(inputPathString);
            inputSuffix = getFileExtensionFromString(inputPathString);

            if (!Arrays.stream(FILETYPES)
                    .anyMatch(getFileExtensionFromString(args[0])::equals) // https://stackoverflow.com/questions/1128723/how-do-i-determine-whether-an-array-contains-a-particular-value-in-java
            ) {
                System.err.println("Input is of an unsupported filetype");
                System.exit(123);
            }
        }
        else{
            System.err.println("Command line input has to start with --input-...");
            System.exit(123);
        }

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
            }
            else if (encode.startsWith("base-n")){
                outputSuffix = BASEN;
                alphabet = encode.substring(encode.indexOf('=')+1);
            }

            outputPath = Paths.get(outputPathString + outputSuffix);

        } else {
            System.err.println(
                    "Illegal Prefix for second Argument. Only " + ENCODE + " or " + DECODE + " or " + COMPRESSION + " are allowed.");
            System.exit(123);
        }

        boolean temp = new File(outputPath.toString()).getParentFile().exists();
        if (!temp) {
            System.err.println(
                    "Das Ausgabeverzeichnis existiert nicht.");
            System.exit(123);
        }
        outputSuffix = getFileExtensionFromString(outputPath.toString());



        if (args.length == 3 && args[2].startsWith(COMPRESSION)) {
            String compressiontype = args[2].substring(COMPRESSION.length());
            if (compressiontype.equals(RLE)) {
                mode = RLE;
            } else if (compressiontype.equals(UNCOMPRESSED)) {
                mode = UNCOMPRESSED;
            } else if (compressiontype.equals(HUFFMAN)) {
                mode = HUFFMAN;
            } else if (compressiontype.equals(AUTO)) {
                mode = AUTO;
            } else {
                System.err.println("Unbekannte Kompression");
                System.exit(123);
            }
        }
    }

    public void setMode(String mode) {
        this.mode = mode;
    }


    enum CLIMode {
        DECODE, ENCODE, CONVERSION
    }


}
