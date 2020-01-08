package propra.compression_operations;

import propra.file_types.FileTypeSuper;
import propra.helpers.Pixel;

import java.io.IOException;

/**
 * Responsible for decompressing RLE datasegments and converting to the output imageformat.
 */
public class RLEToUncompressedV4 extends ConversionSuper {

    private Mode mode = Mode.COUNTER;
    private int counter = 0;
    private int pixelByteCounter = 0;
    private byte[] pixel = new byte[3];


    public RLEToUncompressedV4(FileTypeSuper inputFile) {
        super(inputFile);
    }

    public void run(byte singleByte) throws IOException {
        // How this method is supposed to work:
        // 1. This method determines if the passed byte is a or counter part of a Pixel.
        //      1. If it is a counter it determines the size of the following RLE, or RAW packet.
        //      2. If it is a pixel byte it gathers 3, converts the pixel to the output format
        //      and then decompresses the RLE fragments them and counts the number of 'uncompressed' pixels.


        if (processedPixels > inputFile.getWidth() * inputFile.getHeight()) {
            flag = Flags.LAST_PIXEL_HAS_BEEN_PROCESSED;
            return;
        }

        if (mode == Mode.COUNTER) {//1.
            pixelByteCounter = 0; // counters the number of bytes that are in a segment.
            counter = singleByte & 0x7f;
            counter++;

            if (singleByte > 0) {
                mode = Mode.RAW_PACKET;
            } else {
                mode = Mode.RLE_PACKET;
            }
        } else if (counter > 0) {//2.
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
            if (counter == 0) {// signals that the next byte is going to be a counter.
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
