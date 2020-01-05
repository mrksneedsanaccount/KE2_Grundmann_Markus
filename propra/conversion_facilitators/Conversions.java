package propra.conversion_facilitators;


import propra.compression_operations.*;
import propra.file_types.FileTypeSuper;
import propra.file_types.ProPra;
import propra.file_types.TGA;
import propra.helpers.HelperMethods;
import propra.helpers.Pixel;
import propra.helpers.ProjectConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import static propra.conversion_facilitators.CommandLineInterpreter.*;

public class Conversions {

    FileTypeSuper inputFile;
    FileTypeSuper outputFile;
    CommandLineInterpreter commandLineInterpreter;
    ConversionSuper conversionSuper = null;

    public Conversions(CommandLineInterpreter commandLineInterpreter) {
        this.commandLineInterpreter = commandLineInterpreter;
    }

    public void AddHeaderToOutputFile(FileTypeSuper outputFile, FileTypeSuper inputFile) throws IOException {
        outputFile
                .ausgabeHeaderFertigInitialisieren(inputFile);
        RandomAccessFile outputStream;
        byte[] outputheader = outputFile
                .buildHeader(inputFile);
        outputStream = new RandomAccessFile(outputFile.getFilepath().toFile(), "rw");
        outputStream.write(outputheader);
        outputStream.close();
        System.out.println("Fertig");
    }

    public void executeConversion() {

        if (outputFile.getCompression().equals("auto")) {
            AutoModule autoModule = new AutoModule(inputFile, commandLineInterpreter.outputSuffix);
            try {
                autoModule.calculateFrequenciesForAuto();
                outputFile.setCompression(autoModule.getPrefferedCompression());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (commandLineInterpreter.getInputSuffix().equals(commandLineInterpreter.outputSuffix) & inputFile.getCompression().equals(outputFile.getCompression())) {
            System.out.println("The input file and the requested output file are of the same format and compression." +
                    "\n" + "No action was taken.");
            try {
                Files.copy(inputFile.getFilepath(), outputFile.getFilepath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("File could not be copied to provided output destination.");
                System.exit(123);
            }
        } else {
            try {
                FileChannel fileChannel = HelperMethods.initialiseInputChannel(inputFile.getFilepath().toFile(), inputFile.getHeader().length);
                HelperMethods.initialiseOutputFile(outputFile.getFilepath().toFile(), outputFile.getHeader().length);
                ByteBuffer byteBuffer = ByteBuffer.allocate(ProjectConstants.BUFFER_CAPACITY);
                switch (inputFile.getCompression()) {
                    case ProjectConstants.UNCOMPRESSED:
                        if (outputFile
                                .getCompression().equals(ProjectConstants.UNCOMPRESSED)) {
                            conversionSuper = new UncompressedToUncompressed(inputFile);
                        } else if (outputFile
                                .getCompression().equals(ProjectConstants.RLE)) {
                            conversionSuper = new UncompressedToRLE3(inputFile);
                        } else if (outputFile
                                .getCompression().equals(ProjectConstants.HUFFMAN)) {
                            conversionSuper = new ConvertToHuffman(inputFile);
                        }
                        break;


                    case ProjectConstants.RLE:
                        if (outputFile
                                .getCompression() == ProjectConstants.UNCOMPRESSED) {
                            conversionSuper = new RLEToUncompressedV4(inputFile);


                        } else if (outputFile
                                .getCompression() == ProjectConstants.RLE) {
                            conversionSuper = new RLEtoRLE2(inputFile);


                        } else if (outputFile
                                .getCompression() == ProjectConstants.HUFFMAN) {
                            conversionSuper = new ConvertToHuffman(inputFile);
                        }
                        break;
                    case ProjectConstants.HUFFMAN:
                        conversionSuper = new FromHuffmanToOutputcompression(inputFile);
                        break;
                }

                //TODO Check inputheader?

                conversionSuper.initializeConversion(fileChannel, inputFile, byteBuffer, outputFile.getCompression());

                int limit;
//                Mode mode = null;
//                if ( conversionSuper.getEndOfFile() == ConversionSuper.Flags.EOF_REACHED){
//                   mode= Mode.SPECIAL_CASE;
//                }
                int counter = 0;

                while ((limit = fileChannel.read(byteBuffer)) > -1) {
//                    mode = null;
                    byteBuffer.flip();
                    inputFile.calculateChecksumOfByteBuffer(byteBuffer, limit);

                    ByteArrayOutputStream toFileBAoStream = new ByteArrayOutputStream(ProjectConstants.BUFFER_CAPACITY);
                    while (byteBuffer.hasRemaining()) {
//                    for (int i = 0; i < limit; i++) {

                        if (conversionSuper.getProcessedPixels() == inputFile.getWidth() * inputFile.getHeight()) {
                            System.out.println("Input image file has a tail.");
                            break;
                        }
//                        conversionSuper.run(byteBuffer.array()[i]);
                        conversionSuper.run(byteBuffer.get());
                        counter++;
                        byte[] temp = conversionSuper.outputForWritingToFile();

                        if (temp != null) {
                            outputFile.calculateChecksumOfArray(temp);
                            Files.write(outputFile.getFilepath(), temp, StandardOpenOption.APPEND);
                        }
                    }
                    byteBuffer.clear();
                }


                assert conversionSuper.getProcessedPixels() == inputFile.getWidth() * inputFile.getHeight() : "Number of processed pixels does not correspond to dimensions provided in the header";

                inputFile.fehlerausgabe();
                //Header Nachbearbeiten (Hinzufügen der Prüfsumme usw.)


                //Header zur Ausgabedatei hinzufügen.
                AddHeaderToOutputFile(outputFile, inputFile);


            } catch (IOException e) {
                e.printStackTrace();
                System.exit(123);
            }

        }
        System.out.println("TEST");

    }

    public CommandLineInterpreter getCommandLineInterpreter() {
        return commandLineInterpreter;
    }

    public ConversionSuper getConversionSuper() {
        return conversionSuper;
    }

    public FileTypeSuper getInputFile() {
        return inputFile;
    }

    public FileTypeSuper getOutputFile() {
        return outputFile;
    }

    public void initializeConversions() {
        switch (commandLineInterpreter.inputSuffix) {
            case TGA:
                inputFile = new TGA(commandLineInterpreter);
                break;
            case PROPRA:
                inputFile = new ProPra(commandLineInterpreter);
                break;
            default:
                System.err.println("Illegal image format. This program only supports.tga and .propra. ");
                System.exit(123);
        }
        if (commandLineInterpreter.getMode().equals(KE1CONVERSION)) {
            commandLineInterpreter.setMode(inputFile.getCompression());
        }

        switch (commandLineInterpreter.outputSuffix) {
            case ProjectConstants.TGA:
                outputFile = new TGA(commandLineInterpreter, inputFile);
                break;
            case ProjectConstants.PROPRA:
                outputFile = new ProPra(commandLineInterpreter, inputFile);
                break;
            default:
                System.err.println("File extension not supported.");
                System.exit(123);
        }

    }

    public void setPixels() {
        Pixel.setInputFormat(inputFile.getImageFormat());
        Pixel.setOutputFormat(outputFile.getImageFormat());
    }

    enum Mode {
        SPECIAL_CASE
    }

}
