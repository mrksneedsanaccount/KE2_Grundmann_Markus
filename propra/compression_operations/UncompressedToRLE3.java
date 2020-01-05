package propra.compression_operations;

import propra.file_types.FileTypeSuper;
import propra.helpers.Pixel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;


public class UncompressedToRLE3 extends ConversionSuper {

    private final int imagewidth;
    private int currentwidth = 0;
    ByteBuffer buffer;
    int pixelsAlreadyCounted;
    private int byteCounter = 0;
    private int counter = -1;
    private byte[] pixelOne = new byte[3];
    private byte[] pixelPrevious = new byte[3];
    private int totalSizeOfDatasegment = 0;
    private Mode mode = Mode.GET_TWO_PIXELS;


    ByteArrayOutputStream interimStorageBAoS = new ByteArrayOutputStream();

    public UncompressedToRLE3(FileTypeSuper inputFile) {
        super(inputFile);
        this.imagewidth = inputFile.getWidth();

    }


    public void run(byte singleByte) throws IOException {

        pixelOne[byteCounter % 3] = singleByte;


        if (processedPixels > inputFile.getWidth() * inputFile.getHeight()) {
            flag = Flags.LAST_PIXEL_HAS_BEEN_PROCESSED;
            return;
        }

        if (byteCounter % 3 == 2) {
            pixelOne = Pixel.transformPixel(pixelOne);

            if (imagewidth == 1) {
                counter++;
                interimStorageBAoS.write((pixelOne));
                saveToOutputStream(counter);
                byteCounter++;
                processedPixels++;
                return;
            }












            processedPixels++;
            currentwidth++;

            if (mode == Mode.GET_TWO_PIXELS) {
                mode = null;
            } else if (mode == null && Arrays.equals(pixelOne, pixelPrevious)) {
                if (counter != -1) {
                    saveToOutputStream(counter);
                }
                counter = 1;
                interimStorageBAoS.write(pixelOne);
                mode = Mode.RLE;
            } else if (mode == Mode.RLE && Arrays.equals(pixelOne, pixelPrevious)) {
                counter++;
                mode = Mode.RLE;
                if (counter == 127) {
                    saveToOutputStream(counter | 0x80);
                }

            } else if (!Arrays.equals(pixelOne, pixelPrevious)) {
                if (mode == Mode.RLE) {
                    saveToOutputStream(counter | 0x80);
                } else {
                    interimStorageBAoS.write(pixelPrevious);
                    counter++;
                }
                if (counter == 127) {
                    saveToOutputStream(counter);
                }
            }
            if (currentwidth == imagewidth) {
                if ((Arrays.equals(pixelOne, pixelPrevious) && mode == Mode.RLE) && counter < 127) {
                    saveToOutputStream(counter | 0x80);
                } else if ((Arrays.equals(pixelOne, pixelPrevious) && mode == Mode.GET_TWO_PIXELS) && counter == -1) {
                    // nichts tun, da das zweite Pixel schon gelesen wurde.
                } else if (!(Arrays.equals(pixelOne, pixelPrevious)) && mode == null && counter < 127) {
                    counter++;
                    interimStorageBAoS.write((pixelOne));
                    saveToOutputStream(counter);
                } else {
                    counter++;
                    byteArrayOutputStream.write(counter);
                    byteArrayOutputStream.write(pixelOne);
                    totalSizeOfDatasegment += 4;
                }
                counter = -1;
                mode = Mode.GET_TWO_PIXELS;
                currentwidth = 0;
            }
            System.arraycopy(pixelOne, 0, pixelPrevious, 0, 3);
        }
        byteCounter++;
    }

    private void saveToOutputStream(int counter) throws IOException {
        byteArrayOutputStream.write(counter);
        totalSizeOfDatasegment++;
        totalSizeOfDatasegment += interimStorageBAoS.size();
        interimStorageBAoS.writeTo(byteArrayOutputStream);
        interimStorageBAoS.reset();
        this.counter = -1;
        if (Arrays.equals(pixelOne, pixelPrevious)) {
            mode = Mode.GET_TWO_PIXELS;
        } else {
            mode = null;
        }
    }

    enum Mode {
        GET_ONE_PIXEL, RLE, GET_TWO_PIXELS
    }
}
