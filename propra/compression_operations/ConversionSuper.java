package propra.compression_operations;

import propra.exceptions.ConversionException;
import propra.file_types.FileTypeSuper;
import propra.helpers.ProjectConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


/**
 * abstract superclass of the classes that are responsible for all conversion and compression operations.
 */
public abstract class ConversionSuper {

    final int totalNumberOfPixelsInInputImage;
    ByteBuffer byteBuffer;
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(40000);
    int processedPixels = 0;
    FileTypeSuper inputFile;
    Flags flag = null;


    public ConversionSuper(FileTypeSuper inputFile) {
        this.inputFile = inputFile;
        totalNumberOfPixelsInInputImage = inputFile.getWidth() * inputFile.getHeight();
    }


    public int getProcessedPixels() {
        return processedPixels;
    }


    /**
     * Tells you how many bytes are ready to be written to the output file.
     *
     * @return Returns the number of bytes ready to be written to the output file.
     */
    public int howManyBytesProcessed() {
        return byteArrayOutputStream.size();
    }


    /**
     * This method is responsible for
     *
     * @param fileChannel
     * @param inputFile
     * @param byteBuffer
     * @param compression
     * @throws IOException
     * @throws ConversionException
     */
    public void initializeConversion(FileChannel fileChannel, FileTypeSuper inputFile, ByteBuffer byteBuffer, String compression) throws IOException, ConversionException {

    }

    /**
     * Provides the second step of Huffman -> other, or other -> Huffman with an array of preprocessed image data.
     *
     * @return byteArray byte array containing image data.
     */
    public byte[] returnByteArray() {

        if (byteArrayOutputStream.size() > 0) {
            byte[] temp = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.reset();
            return temp;
        } else {
            return null;
        }
    }

    public void runByteBuffer() throws IOException {
        for (int i = 0; i < byteBuffer.limit(); i++) {

            run(byteBuffer.get());


        }


    }

    /**
     * Responsible for stepwise processing of the input file.
     *
     * @param singleByte a single byte from the input file.
     * @throws IOException
     */
    abstract public void run(byte singleByte) throws IOException;

    /**
     * Responsible for stepwise processing of preprocessed source file inputs.
     * Only necessary for Huffman -> other, or other -> Huffman.
     *
     * @param limit
     * @param byteArray byte array containing image data.
     * @throws IOException
     */
    public void runIteratingOverArray(int limit, byte[] byteArray) throws IOException {


        for (int i = 0; i < limit; i++) {

            if (getProcessedPixels() == totalNumberOfPixelsInInputImage) {
                System.out.println("Input image file has a tail." + '\n' + "The tail has been ignored");
                break;
            }
            run(byteArray[i]);


        }
    }

    /**
     * This method is responsible for creating appropriately large enough chunks for writing to the target destination.
     * After each time it outputs data from the conversion object it resets the ByteArrayOutputstream.
     * This should prevent having to write to file too often.
     *
     * @return
     */
    public byte[] transferChunkOfProcessedData() {
        if (byteArrayOutputStream.size() >= ProjectConstants.BUFFER_CAPACITY ||
                processedPixels == totalNumberOfPixelsInInputImage) {
            byte[] temp = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.reset();
            return temp;
        } else {
            return null;
        }
    }


    public enum Flags {LAST_PIXEL_HAS_BEEN_PROCESSED}

}



