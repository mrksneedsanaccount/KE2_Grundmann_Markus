package propra.compression_operations;

import propra.file_types.FileTypeSuper;
import propra.helpers.Pixel;

import java.io.IOException;

public class UncompressedToUncompressed extends ConversionSuper{


    int byteCounter = 0;

    private byte[] pixel = new byte[3];


    public UncompressedToUncompressed(FileTypeSuper inputFile) {
        super(inputFile);
    }


    @Override
    public void run(byte singleByte) throws IOException {

        if (processedPixels == inputFile.getWidth() * inputFile.getHeight()) {
            flag = Flags.LAST_PIXEL_HAS_BEEN_PROCESSED;
            return;
        }

        pixel[byteCounter % 3] = singleByte;
        if (byteCounter % 3 == 2) {
            pixel = Pixel.transformPixel(pixel);
            byteArrayOutputStream.write(pixel);
            processedPixels++;
        }
        byteCounter++;
    }
}
