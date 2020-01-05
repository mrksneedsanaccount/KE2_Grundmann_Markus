package src.propra.compression_operations;

import src.propra.file_types.FileTypeSuper;
import src.propra.helpers.ProjectConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public abstract class ConversionSuper {

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    int processedPixels = 0;
    FileTypeSuper inputFile;
    Flags flag = null;


    public ConversionSuper() {
    }

    public ConversionSuper(FileTypeSuper inputFile) {
        this.inputFile = inputFile;
    }


    public int getProcessedPixels() {
        return processedPixels;
    }

    public int howManyBytesProcessed() {
        return byteArrayOutputStream.size();
    }

    public void initializeConversion(FileChannel fileChannel, FileTypeSuper inputFile, ByteBuffer byteBuffer, String compression) throws IOException {

    }

    public byte[] outputForWritingToFile() {
        if (byteArrayOutputStream.size() >= ProjectConstants.BUFFER_CAPACITY ||
                processedPixels == inputFile.getWidth() * inputFile.getHeight()) {
            byte[] temp = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.reset();
            return temp;
        } else {
            return null;
        }
    }

    public byte[] returnByteArray() {

        if (byteArrayOutputStream.size() > 0) {
            byte[] temp = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.reset();
            return temp;
        } else {
            return null;
        }
    }

    abstract public void run(byte singleByte) throws IOException;

    void run(byte[] byteArray) throws IOException {

        for (byte b : byteArray) {
            run(b);
        }
    }


    public enum Flags {LAST_PIXEL_HAS_BEEN_PROCESSED}

}



