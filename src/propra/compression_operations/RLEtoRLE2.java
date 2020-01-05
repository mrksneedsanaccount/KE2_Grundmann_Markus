package src.propra.compression_operations;


import src.propra.file_types.FileTypeSuper;
import src.propra.helpers.Pixel;

import java.io.IOException;

public class RLEtoRLE2 extends ConversionSuper {

    private Mode mode = Mode.COUNTER;
    private int counter = 0;
    private int pixelByteCounter = 0;
    int pixelCounter = 0;
    private byte[] pixel = new byte[3];

    public RLEtoRLE2(FileTypeSuper inputFile) {
        super(inputFile);
    }

    public void run(byte singleByte) throws IOException {

        if (processedPixels > inputFile.getWidth() * inputFile.getHeight()) {
            flag = Flags.LAST_PIXEL_HAS_BEEN_PROCESSED;
            return;
        }



        if (mode == Mode.COUNTER) {
            pixelByteCounter = 0;
            counter = singleByte & 0x7f;
            counter++;
            byteArrayOutputStream.write(singleByte);

            if (singleByte > 0) {
                mode = Mode.RAW_PACKET;
            } else {
                mode = Mode.RLE_PACKET;
            }
        } else if (counter > 0) {

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
