package propra.conversion_facilitators;


import propra.compression_operations.*;
import propra.exceptions.ConversionException;
import propra.exceptions.IllegalHeaderException;
import propra.exceptions.InvalidChecksumException;
import propra.exceptions.UnknownCompressionException;
import propra.file_types.FileTypeSuper;
import propra.file_types.ProPra;
import propra.file_types.TGA;
import propra.helpers.HelperMethods;
import propra.helpers.Pixel;
import propra.helpers.ProjectConstants;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static propra.conversion_facilitators.CommandLineInterpreter.*;

/**
 * Is responsible for converting and writing the source file to the desired compression and image format.
 */
public class Conversions {

    private FileTypeSuper inputFile;
    private FileTypeSuper outputFile;
    private CommandLineInterpreter commandLineInterpreter;
    private ConversionSuper stepwiseConverter = null;

    public Conversions(CommandLineInterpreter commandLineInterpreter) {
        this.commandLineInterpreter = commandLineInterpreter;
    }

    /**
     * This method replaces the empty placeholder at the beginning of the output file with the appropriate header.
     *
     * @throws IOException
     * @throws UnknownCompressionException
     */
    private void addHeaderToOutputFile(FileTypeSuper outputFile, FileTypeSuper inputFile) throws IOException, UnknownCompressionException {
        outputFile
                .finalizeOutputHeader(inputFile);
        RandomAccessFile outputStream;
        byte[] outputheader = outputFile
                .buildHeader(inputFile);
        outputStream = new RandomAccessFile(outputFile.getFilepath().toFile(), "rw");
        outputStream.write(outputheader);
        outputStream.close();
        System.out.println("Done.");
    }

    private void chooseAppropriateStepWiseConverter() {
        switch (inputFile.getCompression()) {
            case ProjectConstants.UNCOMPRESSED:
                if (outputFile
                        .getCompression().equals(ProjectConstants.UNCOMPRESSED)) {
                    stepwiseConverter = new UncompressedToUncompressed(inputFile);
                } else if (outputFile
                        .getCompression().equals(ProjectConstants.RLE)) {
                    stepwiseConverter = new UncompressedToRLE3(inputFile);
                } else if (outputFile
                        .getCompression().equals(ProjectConstants.HUFFMAN)) {
                    stepwiseConverter = new ConvertToHuffman(inputFile);
                }
                break;


            case ProjectConstants.RLE:
                if (outputFile
                        .getCompression() == ProjectConstants.UNCOMPRESSED) {
                    stepwiseConverter = new RLEToUncompressedV4(inputFile);


                } else if (outputFile
                        .getCompression() == ProjectConstants.RLE) {
                    stepwiseConverter = new RLEtoRLE2(inputFile);


                } else if (outputFile
                        .getCompression() == ProjectConstants.HUFFMAN) {
                    stepwiseConverter = new ConvertToHuffman(inputFile);
                }
                break;


            case ProjectConstants.HUFFMAN:
                stepwiseConverter = new FromHuffmanToOutputcompression(inputFile);
                break;
        }
    }

    /**
     * Copies the input file to the to the output destination.
     *
     * @param inputFile
     * @param outputFile
     * @throws IOException
     * @throws IllegalHeaderException
     */
    private void copySourceFileToOutputDestination(FileTypeSuper inputFile, FileTypeSuper outputFile) throws IOException, InvalidChecksumException {
        byte[] inputByteArray = new byte[ProjectConstants.BUFFER_CAPACITY];
        byte[] outputByteArray = new byte[ProjectConstants.BUFFER_CAPACITY];
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(inputFile.getFilepath().toFile()));
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(outputFile.getFilepath().toFile()));
        int bytesRead;
        boolean headerSkipped = false;
        HelperMethods.deleteFileAtLocationIfNecessary(outputFile.getFilepath().toFile());

        while ((bytesRead = bufferedInputStream.read(inputByteArray)) != -1) {

            for (int i = 0; i < bytesRead; i++) {
                if (i >= inputFile.getHeader().length | headerSkipped) {
                    inputFile.calculateChecksum(inputByteArray[i]);
                    headerSkipped = true;
                }
                outputByteArray[i] = inputByteArray[i];

            }
            bufferedOutputStream.write(outputByteArray, 0, bytesRead);
        }
        inputFile.checkChecksum();
        bufferedInputStream.close();
        bufferedOutputStream.close();
    }

    /**
     * This method is contains the logic required to execute the format conversion, and/or compression.
     */
    public void executeConversion() throws IllegalHeaderException {

        // 1. Check if the program has to determine the optimal compression (case "auto")
        //2. Check if any conversion has to be done at all. If not then copy the file to the output destination. THe checksum will be checked just incase.
        //3. Determine which object is going to perform the stepwise


        if (outputFile.getCompression().equals("auto")) {
            AutoModule autoModule = new AutoModule(inputFile, commandLineInterpreter.outputSuffix);
            try {
                autoModule.calculateFrequenciesForAuto();
                outputFile.setCompression(autoModule.getPrefferedCompression());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        inputFile.checkForErrorsInHeader();
        if (commandLineInterpreter.getInputSuffix().equals(commandLineInterpreter.outputSuffix) & inputFile.getCompression().equals(outputFile.getCompression())) {
            System.out.println("The input file and the requested output file are of the same format and compression." +
                    "\n" + "Attempting to copy the file to the provided destination...");
            try {

                    copySourceFileToOutputDestination(inputFile, outputFile);

            } catch (IOException | InvalidChecksumException e) {
                e.printStackTrace();
                System.err.println("File could not be copied to provided output destination properly.");
                System.exit(123);
            }
        } else {
            try {

                FileChannel fileChannel = HelperMethods.initialiseInputChannel(inputFile.getFilepath().toFile(), inputFile.getHeader().length);
                HelperMethods.initialiseOutputFile(outputFile.getFilepath().toFile(), outputFile.getHeader().length);
                ByteBuffer byteBuffer = ByteBuffer.allocate(ProjectConstants.BUFFER_CAPACITY);


                // This instantiates the appropriate converter object and assigns it to the stepwiseConverter attribute.
                chooseAppropriateStepWiseConverter();
                // Initialising the Converter objects.
                //So far this is only required for Huffman related operations (building the Huffman tree (and writing
                // it to the target destination, if required). If the Converter is not of a Huffman Converter
                //then this method does nothing.
                stepwiseConverter.initializeConversion(fileChannel, inputFile, byteBuffer, outputFile.getCompression());

                int limit;
                int counter = 0;
                //This is the main conversion and compression loop.
                // Each byte from the source file's datasegment will get processed one by one.
                //The stepwise conversion happens in a Conversion object.
                //These objects contain all the logic and information required to identify.
                //Generally what happens each step is the following:
                // The buffer gets filled up.
                // While the buffer hass not been fully processed:
                //  1. The converter's .run(Byte byte)-method gets a new byte passed to it.
                //      1. the Object processes the byte. (In the case of Huffman encoding this is a two step process.
                //          Huffman -> outputcompression or vice versa.
                //      2. Each processed pixel will be counted. Once the enough pixels have been processed (width*height)
                //          the Converter stops saving them to its ByteArrayOutputstream.
                //  2. The processed data will be written to the output file.
                ByteArrayOutputStream toFileBAoStream = new ByteArrayOutputStream(ProjectConstants.BUFFER_CAPACITY);
                File file;
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(outputFile.getFilepath().toString(), true));


                while ((limit = fileChannel.read(byteBuffer)) > -1) {
                    byteBuffer.flip();
                    inputFile.calculateChecksumOfByteBuffer(byteBuffer, limit);


                    while (byteBuffer.hasRemaining()) {
//                    for (int i = 0; i < limit; i++) {

                        if (stepwiseConverter.getProcessedPixels() == inputFile.getWidth() * inputFile.getHeight()) {
                            System.out.println("Input image file has a tail." + '\n' + "The tail has been ignored");
                            break;
                        }
//                        conversionSuper.run(byteBuffer.array()[i]);
                        stepwiseConverter.run(byteBuffer.get());
                        counter++;
                        byte[] temp = stepwiseConverter.transferChunkOfProcessedData();
                        if (temp != null) {
                            outputFile.calculateChecksumOfArray(temp);
//                            Files.write(outputFile.getFilepath(), temp, StandardOpenOption.APPEND);
                            bufferedOutputStream.write(temp);


                        }
                    }
                    byteBuffer.clear();
                }


                assert stepwiseConverter.getProcessedPixels() == inputFile.getWidth() * inputFile.getHeight() : "Number of processed pixels does not correspond to dimensions provided in the header";


                // build the header for the output file and replace the current placeholder.
                addHeaderToOutputFile(outputFile, inputFile);
                inputFile.checkChecksum();

                bufferedOutputStream.close();

            } catch (IOException | UnknownCompressionException | InvalidChecksumException | ConversionException e) {
                e.printStackTrace();
                System.exit(123);
            }
        }


    }

    public FileTypeSuper getInputFile() {
        return inputFile;
    }


    public void initializeConversions() throws UnknownCompressionException {

        // instantiate the input Object
        switch (commandLineInterpreter.inputSuffix) {
            case TGA:
                inputFile = new TGA(commandLineInterpreter.getInputPath(), commandLineInterpreter.getInputPath().toFile(), commandLineInterpreter.getInputSuffix());
                break;
            case PROPRA:
                inputFile = new ProPra(commandLineInterpreter.getInputPath(), commandLineInterpreter.getInputPath(), commandLineInterpreter.getInputSuffix());
                break;
            default:
                System.err.println("Illegal image format. This program only supports.tga and .propra. ");
                System.exit(123);
        }
        if (commandLineInterpreter.getMode().equals(KE1CONVERSION)) {
            commandLineInterpreter.setMode(inputFile.getCompression());
        }
        // instantiate the output object
        switch (commandLineInterpreter.outputSuffix) {
            case ProjectConstants.TGA:
                outputFile = new TGA(inputFile, commandLineInterpreter.getOutputPath(), commandLineInterpreter.getOutputSuffix(), commandLineInterpreter.getMode());
                break;
            case ProjectConstants.PROPRA:
                outputFile = new ProPra(inputFile, commandLineInterpreter.getMode(), commandLineInterpreter.getOutputPath(), commandLineInterpreter.getOutputSuffix(), commandLineInterpreter.getMode());
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


}
