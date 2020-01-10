package propra.compression_operations;


import propra.file_types.FileTypeSuper;
import propra.helpers.Pixel;

import java.io.IOException;


/**
 * Responsible for converting .tga RLE datasegments to .propra RLE files and vice versa
 *
 */
public class RLEtoRLE2 extends ConversionSuper {

    private Mode mode = Mode.COUNTER;
    private int counter = 0;
    private int pixelByteCounter = 0;
    private byte[] pixel = new byte[3];

    public RLEtoRLE2(FileTypeSuper inputFile) {
        super(inputFile);
    }


    // How this method is supposed to work:
    // 1. This method determines if the passed byte is a or counter part of a Pixel.
    //      1. If it is a counter it determines the size of the following RLE, or RAW packet.
    //      2. If it is a pixel byte it gathers 3, converts the pixel to the output format
    //      and then processes them and counts the number of 'uncompressed' pixels.

    //
    public void run(byte singleByte) throws IOException {

        // in order to ignore the tail and s
        // ignal that the last bytes from the output stream should be written to file.
        if (processedPixels > inputFile.getWidth() * inputFile.getHeight()) {
            flag = Flags.LAST_PIXEL_HAS_BEEN_PROCESSED;
            return;
        }

        if (mode == Mode.COUNTER) { // 1.1
            pixelByteCounter = 0;
            counter = singleByte & 0x7f;
            counter++;
            byteArrayOutputStream.write(singleByte);

            if (singleByte > 0) {
                mode = Mode.RAW_PACKET;
            } else {
                mode = Mode.RLE_PACKET;
            }
        } else if (counter > 0) { // 1.2
            pixel[pixelByteCounter % 3] = singleByte;
            if (pixelByteCounter % 3 == 2) {

                if (mode == Mode.RAW_PACKET) {
                    byteArrayOutputStream.write(Pixel.transformPixel(pixel));
                    counter--;
                    processedPixels++;

                } else {
                    byteArrayOutputStream.write(Pixel.transformPixel(pixel));
                    processedPixels += counter;
                    counter = 0;
                }
            }
            if (counter == 0) {
                mode = Mode.COUNTER;

            }
            pixelByteCounter++;
        }
    }

    enum Mode {
        RAW_PACKET, RLE_PACKET, COUNTER
    }


}
