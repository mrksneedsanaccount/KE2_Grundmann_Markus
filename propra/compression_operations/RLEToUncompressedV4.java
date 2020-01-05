package propra.compression_operations;

import propra.file_types.FileTypeSuper;
import propra.helpers.Pixel;

import java.io.IOException;


public class RLEToUncompressedV4 extends ConversionSuper{

    Mode mode = Mode.COUNTER;
    int counter = 0;
    int pixelByteCounter = 0;
    int pixelCounter = 0;
    byte[] pixel = new byte[3];


    public RLEToUncompressedV4(FileTypeSuper inputFile) {
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

            if (singleByte > 0) {
                mode = Mode.RAW_PACKET;
            } else {
                mode = Mode.RLE_PACKET;
            }
        } else if (counter > 0) {
            pixel[pixelByteCounter % 3] = singleByte;
            if (pixelByteCounter % 3 == 2) {
                pixel = Pixel.transformPixel(pixel);

                if (mode == Mode.RAW_PACKET) {
                    byteArrayOutputStream.write(pixel);

                    counter--;
                    processedPixels++;

                } else {
                    while (counter > 0) {
                        byteArrayOutputStream.write(pixel);
                        counter--;
                        processedPixels++;
                    }
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

    public byte[] getOutputArray(){


        byteArrayOutputStream.reset();



        return null;
    }





}
