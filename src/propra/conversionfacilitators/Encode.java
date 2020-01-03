package src.propra.conversionfacilitators;


import src.helperclasses.HelperMethods;
import src.helperclasses.ProjectConstants;
import src.propra.imageconverter.BaseNConverter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Encode {

    private final CommandLineInterpreter commandLineInterpreter;
    Path inputPath;
    Path outputPath;
    String alphabet;



    public Encode(CommandLineInterpreter commandLineInterpreter) {
        this.commandLineInterpreter = commandLineInterpreter;
        this.inputPath = commandLineInterpreter.getInputPath();
        this.outputPath = commandLineInterpreter.getOutputPath();
        this.alphabet = commandLineInterpreter.getAlphabet();

//        outputFile = new BaseN(commandLineInterpreter.getOutputSuffix(), commandLineInterpreter.getAlphabet());


    }

    /**
     * berechnet die Anzahl, der Bits welche für das Alphabet benötigt werden.
     */
    public int calculateBitLengthOfAlphabet(String alpahbet) {

        double temp = (int) (Math.log(alpahbet.length()) / Math.log(2));
        if (temp % 1 != 0) {
            System.err.println("Invalid number of elements in Alphabet.");
            System.exit(123);
        }


        return (int) temp;
    }

    public void exectueConversion() {

        try {
            FileChannel fileChannel = HelperMethods.initialiseInputChannel(commandLineInterpreter.getInputPath().toFile(), 0);
            HelperMethods.initialiseOutputFile(commandLineInterpreter.getOutputPath().toFile(), 0);
            ByteBuffer byteBuffer = ByteBuffer.allocate(ProjectConstants.BUFFER_CAPACITY);

            int bitsInAlphabet = calculateBitLengthOfAlphabet(alphabet);

            if (commandLineInterpreter.getOutputSuffix().equals(ProjectConstants.BASEN)) {
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputPath.toString()));
                bufferedWriter.write(alphabet);
                bufferedWriter.newLine();
                bufferedWriter.close();
            }



            int encodedCharactersInFile = (int) inputPath.toFile().length();

            BaseNConverter baseNConverter = new BaseNConverter( encodedCharactersInFile,
                    commandLineInterpreter.getAlphabet(), bitsInAlphabet, commandLineInterpreter.getMode());


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
            System.err.println("Could not create file at the outputpath, that was provided by user.");
            System.exit(123);

        }

        System.out.println("TEST");
    }


}
