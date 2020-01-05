package src.propra.compression_operations;

import src.propra.file_types.FileTypeSuper;
import src.propra.helpers.Pixel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;


public class UncompressedToRLE3 extends ConversionSuper {

    private final int imagewidth;
    int currentwidth = 0;
    ByteBuffer buffer;
    int pixelsAlreadyCounted;
    int byteCounter = 0;
    int counter = -1;
    byte[] pixelOne = new byte[3];
    byte[] pixelPrevious = new byte[3];
    int totalSizeOfDatasegment = 0;
    Mode mode = Mode.START;


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
            processedPixels++;
            currentwidth++;

            if (mode == Mode.START) {
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
                } else if ((Arrays.equals(pixelOne, pixelPrevious) && mode == Mode.START) && counter == -1) {
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
                mode = Mode.START;
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
            mode = Mode.START;
        } else {
            mode = null;
        }
    }

    enum Mode {
        RAW, RLE, START
    }
}
