package src.propra.compressionoperations;

import src.filetypes.FileTypeSuper;
import src.helperclasses.Pixel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class UncompressedToUncompressed extends ConversionSuper{


    int byteCounter = 0;

    private byte[] pixel = new byte[3];


    public UncompressedToUncompressed(FileTypeSuper inputFile) {
        super(inputFile);
    }


    @Override
    public void run(byte singleByte) throws IOException {

        pixel[byteCounter % 3] = singleByte;
        if (byteCounter % 3 == 2) {
            pixel = Pixel.transformPixel(pixel);
            byteArrayOutputStream.write(pixel);
            processedPixels++;
        }
        byteCounter++;
    }
}
