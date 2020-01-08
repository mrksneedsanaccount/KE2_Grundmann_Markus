package propra.conversion_facilitators;

import propra.helpers.HelperMethods;
import propra.helpers.ProjectConstants;
import propra.imageconverter.BaseNConverter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Decode {

    private Path inputPath;
    private Path outputPath;
    private CommandLineInterpreter commandLineInterpreter;
    private String alpahbet;


    public Decode(CommandLineInterpreter commandLineInterpreter) {
        this.commandLineInterpreter = commandLineInterpreter;
        this.inputPath = commandLineInterpreter.getInputPath();
        this.outputPath = commandLineInterpreter.getOutputPath();

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


    public void executeConversion() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(commandLineInterpreter.getInputPath().toString()));
            //
            int offset = 0;
            if (commandLineInterpreter.getInputSuffix().equals(ProjectConstants.BASEN)) {

                this.alpahbet = bufferedReader.readLine();
//                bufferedReader.close();
                offset = alpahbet.length() + 1;
            } else {
                alpahbet = ProjectConstants.BASE32HEX;
            }


            int bitsInAlphabet = calculateBitLengthOfAlphabet(alpahbet);


//            FileChannel fileChannel = HelperMethods.initialiseInputChannel(commandLineInterpreter.getInputPath().toFile(), offset);
            HelperMethods.initialiseOutputFile(commandLineInterpreter.getOutputPath().toFile(), 0);
//            ByteBuffer byteBuffer = ByteBuffer.allocate(ProjectConstants.BUFFER_CAPACITY);


            int encodedCharactersInFile;
            if (commandLineInterpreter.getInputSuffix().equals(ProjectConstants.BASEN)) {
                encodedCharactersInFile = (int) (inputPath.toFile().length() - alpahbet.length() - 1);
            } else {
                encodedCharactersInFile = (int) inputPath.toFile().length();
            }

            BaseNConverter baseNConverter = new BaseNConverter(encodedCharactersInFile,
                    this.alpahbet, bitsInAlphabet, commandLineInterpreter.getMode());

            char[] charArray = new char[ProjectConstants.BUFFER_CAPACITY];
            int chars_read;
            while ((chars_read = bufferedReader.read(charArray)) != -1) {


                for (int i = 0; i < chars_read; i++) {

                    baseNConverter.runDecode((byte) charArray[i]);
                    byte[] temp = baseNConverter.outputByteArrayForWritingToFile();
                    if (temp != null) {
                        Files.write(outputPath, temp, StandardOpenOption.APPEND);
                    }

                }
// TEST


            }
//            fileChannel.close();
            bufferedReader.close();


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
