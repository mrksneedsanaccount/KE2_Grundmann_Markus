package propra.compression_operations;

import propra.file_types.FileTypeSuper;
import propra.helpers.Pixel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;


/**
 * Responsible for compressing uncompressed datasegments to RLE datasegments and converting to the output imageformat.
 */
public class UncompressedToRLE3 extends ConversionSuper {

    private final int imagewidth;
    private int currentwidth = 0;
    private int byteCounter = 0;
    private int counter = -1;
    private byte[] pixelOne = new byte[3];
    private byte[] pixelPrevious = new byte[3];
    private int totalSizeOfDatasegment = 0;
    private Mode mode = Mode.GET_TWO_PIXELS;
    private int bytePixelCounter;
    private ByteArrayOutputStream interimStorageBAoS = new ByteArrayOutputStream(900);
    private boolean isPixelsEqual;

    public UncompressedToRLE3(FileTypeSuper fileTypeSuper, ByteBuffer byteBuffer) {
        super(fileTypeSuper);
        this.imagewidth = inputFile.getWidth();
        this.byteBuffer = byteBuffer;
    }

    public UncompressedToRLE3(FileTypeSuper inputFile) {
        super(inputFile);
        this.imagewidth = inputFile.getWidth();

    }

    public void run(byte singleByte) throws IOException {
        //1. compare 2 Pixels.
        //   1. New segments
        //       1. if they are the same -> RLE Segment and change mode to RLE mode
        //          if they are different write the first byte and start a RAW segment.
        //   2. Continue the segments: Compare the new pixel to the one that has already been processed.
        //       1. Increase the respective counters, and if it is an RAW segment add the pixel to the BAoS.
        //   2. At the end of a segment write to BAoS and basically go back to step 1.

        pixelOne[bytePixelCounter] = singleByte;
        bytePixelCounter++;
        if (bytePixelCounter == 3) {
            bytePixelCounter = 0;
            if (processedPixels > totalNumberOfPixelsInInputImage) { // End of the input data has been reached
                flag = Flags.LAST_PIXEL_HAS_BEEN_PROCESSED;
                return;
            }
            if (imagewidth == 1 && processedPixels == totalNumberOfPixelsInInputImage) { // special case if imnagewidth == 1, oderwise there would always be an extra bit at the end.
                flag = Flags.LAST_PIXEL_HAS_BEEN_PROCESSED;
                return;
            }
            if (true) {// a new Pixel has been prepared
                pixelOne = Pixel.transformPixel(pixelOne);
                if (imagewidth == 1 && processedPixels < totalNumberOfPixelsInInputImage) {// special case where you just output the pixel.
                    counter++;
                    interimStorageBAoS.write(pixelOne, 0, 3);
                    saveToOutputStream(counter);
                    byteCounter++;
                    processedPixels++;
                    return;
                }
                processedPixels++;
                currentwidth++; // how many pixels you are into the line of the image
                isPixelsEqual = Arrays.equals(pixelOne, pixelPrevious);

                if (mode == Mode.GET_TWO_PIXELS) {
                    mode = null;
                } else if (mode == null && isPixelsEqual) { // Starts a new RLE segment
                    if (counter != -1) {
                        saveToOutputStream(counter);
                    }
                    counter = 1;
                    interimStorageBAoS.write(pixelOne);
                    mode = Mode.RLE;
                } else if (mode == Mode.RLE && isPixelsEqual) { // continues a RLE segment.
                    counter++;
                    mode = Mode.RLE;
                    if (counter == 127) { // full RLE segment write to output stream and reset.
                        saveToOutputStream(counter | 0x80);
                    }

                } else if (!isPixelsEqual) { // Starts RAW packet.
                    if (mode == Mode.RLE) {
                        saveToOutputStream(counter | 0x80);
                    } else {
                        interimStorageBAoS.write(pixelPrevious, 0, 3);
                        counter++;
                    }
                    if (counter == 127) {
                        saveToOutputStream(counter);
                    }
                }
                if (currentwidth == imagewidth) {
                    if ((isPixelsEqual && mode == Mode.RLE) && counter < 127) { // output an RLE packet that is not full
                        saveToOutputStream(counter | 0x80);
                    } else if ((isPixelsEqual && mode == Mode.GET_TWO_PIXELS) && counter == -1) {
                        // do nothing, beause the second pixel has already been processed. Basically if the line ends with a full RLE packet.
                    } else if (!isPixelsEqual && mode == null && counter < 127) { // add to a RLE packet that is not full.
                        counter++;
                        interimStorageBAoS.write(pixelOne, 0, 3);
                        saveToOutputStream(counter);
                    } else {
                        counter++;
                        byteArrayOutputStream.write(counter);
                        byteArrayOutputStream.write(pixelOne, 0, 3);
                        totalSizeOfDatasegment += 4;
                    }// reset the state for the next line.
                    counter = -1;
                    mode = Mode.GET_TWO_PIXELS;
                    currentwidth = 0;
                }
                System.arraycopy(pixelOne, 0, pixelPrevious, 0, 3);
            }
        }

    }

    @Override
    public void runIteratingOverArray(int limit, byte[] byteArray) throws IOException {
        for (int i = 0; i < limit; i++) {

            if (getProcessedPixels() == totalNumberOfPixelsInInputImage) {
                System.out.println("Input image file has a tail." + '\n' + "The tail has been ignored");
                break;
            }

            run(byteArray[i]);


        }


    }


    /**
     * Is responsible for writing the RLE, or Raw packet to the output stream and
     *
     * @param counter the counter of the RAW, or RLE packet
     * @throws IOException
     */
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
