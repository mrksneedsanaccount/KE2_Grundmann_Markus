package propra.conversion_facilitators;


import propra.helpers.HelperMethods;
import propra.helpers.ProjectConstants;
import propra.imageconverter.BaseNConverter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;


/**
 * Encode is responsible performing the stepwise encoding of the inputfile with the supplied alphabet. (Explicitily for
 * base-n, and implicity for .base-32)
 */
public class Encode {

    private final CommandLineInterpreter commandLineInterpreter;
    private Path inputPath;
    private Path outputPath;
    private String alphabet;


    /**
     * Constructor required to build the object that is going to facilitate the encoding.
     *
     * @param commandLineInterpreter
     */
    public Encode(CommandLineInterpreter commandLineInterpreter) {
        this.commandLineInterpreter = commandLineInterpreter;
        this.inputPath = commandLineInterpreter.getInputPath();
        this.outputPath = commandLineInterpreter.getOutputPath();
        this.alphabet = commandLineInterpreter.getAlphabet();

    }

    /**
     * Returns the number of bits required to encode all the elements in passed String.
     *
     * @param alpahbet A string containing all elements of the chosen alphabet.
     * @return The number of bits required to encode all elements of hte alphabet.
     */
    private int calculateBitLengthOfAlphabet(String alpahbet) {

        double temp = (int) (Math.log(alpahbet.length()) / Math.log(2));
        if (temp % 1 != 0 || alpahbet.length() > 64) {
            System.err.println("Invalid number of elements in Alphabet.");
            System.exit(123);
        }


        return (int) temp;
    }

    public void exectueConversion() {

        try {

            // Initializing the input FileChannel and creating the empty outputfile.
            FileChannel fileChannel = HelperMethods.initialiseInputChannel(commandLineInterpreter.getInputPath().toFile(), 0);
            HelperMethods.initialiseOutputFile(commandLineInterpreter.getOutputPath().toFile(), 0);
            ByteBuffer byteBuffer = ByteBuffer.allocate(ProjectConstants.BUFFER_CAPACITY);

            // This also checks if the alphabet is valid.
            int bitsInAlphabet = calculateBitLengthOfAlphabet(alphabet);
            if (commandLineInterpreter.getOutputSuffix().equals(ProjectConstants.BASEN)) {
                writeAlphabetToFile();
            }

            //
            int numberOfBytesToBeProcessed = (int) inputPath.toFile().length();
            BaseNConverter baseNConverter = new BaseNConverter(numberOfBytesToBeProcessed,
                    commandLineInterpreter.getAlphabet(), bitsInAlphabet, commandLineInterpreter.getMode());


            // This performs the stepwise encoding of the input file.
            while (fileChannel.read(byteBuffer) > -1) {
                byteBuffer.flip();

                while (byteBuffer.hasRemaining()) {
                    baseNConverter.runEncode(byteBuffer.get());
                    String temp = baseNConverter.outputStringForWritingToFile();
                    if (temp != null) {
                        Files.write(outputPath, temp.getBytes(), StandardOpenOption.APPEND);
                    }
                }
                byteBuffer.compact();
            }
            fileChannel.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("This error is most likely caused by problems related to reading the input," +
                    " or writing to the output path");
            System.exit(123);

        }


    }

    /**
     * writes the given alphabet to file.
     *
     * @throws IOException
     */
    private void writeAlphabetToFile() throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputPath.toString()));
        bufferedWriter.write(alphabet);
        bufferedWriter.write('\n');
//         bufferedWriter.newLine();
        //// Linux newline character
        // not used, because it is not system independent.
        bufferedWriter.close();
    }


}
