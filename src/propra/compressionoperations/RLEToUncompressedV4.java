package src.propra.compressionoperations;

import src.filetypes.FileTypeSuper;
import src.helperclasses.Pixel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

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
