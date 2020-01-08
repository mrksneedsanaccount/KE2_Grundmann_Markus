package propra.compression_operations;

import propra.file_types.FileTypeSuper;
import propra.helpers.HelperMethods;
import propra.helpers.Huffman;
import propra.helpers.Pixel;
import propra.helpers.ProjectConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


/**
 * Responsible for dealing with the "auto" compression.
 */
public class AutoModule {


    private final int totalNumberOfPixelsInInputImage;
    private FileTypeSuper inputFile;
    private long uncompressedSize;
    private long rleCompressedSize;
    private long huffmanSize;
    private Mode mode = Mode.COUNTER;
    private int counter = 0;
    private int pixelByteCounter = 0;
    private int pixelCounter = 0;
    private byte[] pixel = new byte[3];
    private int totalBytesprocessed = 0;
    private String outputFileSuffix;
    private TreeMap<Long, String> treeMap = new TreeMap<Long, String>();
    private String prefferedCompression;


    public AutoModule(FileTypeSuper inputFile, String outputFileSuffix) {
        this.inputFile = inputFile;
        this.outputFileSuffix = outputFileSuffix;
        uncompressedSize = (inputFile.getWidth() * inputFile.getHeight()) * 3;
        this.totalNumberOfPixelsInInputImage = inputFile.getWidth() * inputFile.getHeight();
        treeMap.put(uncompressedSize, ProjectConstants.UNCOMPRESSED);
    }


    /**
     * This method builds the treemap, that contains the expected file sizes.
     *
     * @throws IOException
     */
    public void calculateFrequenciesForAuto() throws IOException {

        ByteBuffer byteBuffer = ByteBuffer.allocate(ProjectConstants.BUFFER_CAPACITY);
        FileChannel fileChannel = HelperMethods.initialiseInputChannel(inputFile.getFilepath().toFile(), inputFile.getHeader().length);

        // initialise the required objects to perform the stepwise determination of expected sizes of the debasements.
        AutoInterface autoInterface = null;
        FromHuffmanToOutputcompression fromHuffmanToOutputcompression = null;
        if (inputFile.getCompression().equals(ProjectConstants.UNCOMPRESSED)) {
            autoInterface = new UncompressedToRLEForFileSize(inputFile);
        } else if (inputFile.getCompression().equals(ProjectConstants.RLE)) { // Just there in case of tails in the input file. Could probably be skipped for performance reasons.
            autoInterface = new RLEtoRLEFileSIze(inputFile);
        } else if (inputFile.getCompression().equals(ProjectConstants.HUFFMAN)) {
            if (outputFileSuffix.equals(ProjectConstants.PROPRA)) {// Huffman compression is only relevant if the output file is a .propra file.
                huffmanSize = (inputFile.getFilepath().toFile().length() - inputFile.getHeader().length);
                treeMap.put(huffmanSize, ProjectConstants.HUFFMAN);
            }
            // if the input compression is Huffman we have to decompress it to determine the size of an RLE segment.
            fromHuffmanToOutputcompression = new FromHuffmanToOutputcompression(inputFile);
            fromHuffmanToOutputcompression.initialiseConversionForAuto(fileChannel, byteBuffer); // a special method that performs an RLE compression and only counts.
        }


        long[] colourValueFrequencies = new long[ProjectConstants.MAX_COLOR_VALUES];
        byte singleByte;
        // The stepwise loop to determine the potential sizes of the datasegments for RLE and Huffman compression.
// Same principle as in Conversions.java but just with up to 2 compressions
        while ((fileChannel.read(byteBuffer)) > 0) {
            byteBuffer.flip();
            while (byteBuffer.hasRemaining()) {

                //
                singleByte = byteBuffer.get();
                if (autoInterface != null) // either uncompressed -> rle, or rle->rle;
                    autoInterface.run(singleByte);


                // Huffman related operations: Either determining the size of a possible RLE datasegment for Huffman -> other,
                // or filling the frequency array in order to determine the size of a
                // Huffman encoded datasegment (including the encoded Huffman tree) for a possible Huffman -ü> other.

                if (pixelCounter >= totalNumberOfPixelsInInputImage) {//ignore tails, mainly relevant for huffman compression.
                    break;
                }

                if (inputFile.getCompression().equals(ProjectConstants.HUFFMAN)) {
                    assert fromHuffmanToOutputcompression != null;
                    fromHuffmanToOutputcompression.run(singleByte);
                } else if (inputFile.getCompression().equals(ProjectConstants.UNCOMPRESSED) & outputFileSuffix.equals(ProjectConstants.PROPRA)) {
                    fillFrequencyArray(colourValueFrequencies, singleByte);
                } else if (inputFile.getCompression().equals(ProjectConstants.RLE) & (outputFileSuffix.equals(ProjectConstants.PROPRA))) {

                    fillfreuencyArrayRLE(colourValueFrequencies, singleByte);
                }
            }
            byteBuffer.clear();
        }


        // Finalizing the tree map that contains the 2-3 relevant datasegment sizes.
        if (!inputFile.getCompression().equals(ProjectConstants.HUFFMAN)) {
            assert autoInterface != null;
            rleCompressedSize = autoInterface.getTotalSizeOfRLEDatasegment();
            treeMap.put(rleCompressedSize, ProjectConstants.RLE);

            if ((outputFileSuffix.equals(ProjectConstants.PROPRA))) {// determine size of the Huffman data segment + encoded Huffman tree.
                // Determine the size of Huffman compressed datasegment.
                Huffman.Tree tree = Huffman.buildHuffmanTreeFromFrequencies(colourValueFrequencies);
                HashMap hashMap = tree.createHashMap();
                long fileSize = Huffman.calculateBitLengthOfHuffmanEncodedDatasegment(colourValueFrequencies, hashMap);
                long numberOfNodes = getNumberOfNodesInTreeBasedOnFrequencies(colourValueFrequencies);
                double temp = (fileSize + Huffman.getCountOfBitsInStoredHuffmanTree(numberOfNodes)) / 8.0;
                huffmanSize = (long) (Math.ceil(temp));
                treeMap.put(huffmanSize, ProjectConstants.HUFFMAN);
            }
        }
        if (inputFile.getCompression().equals(ProjectConstants.HUFFMAN)) {
            assert fromHuffmanToOutputcompression != null;
            autoInterface = (AutoInterface) fromHuffmanToOutputcompression.getUncompressedToOutputCompressionConverter();
            rleCompressedSize = autoInterface.getTotalSizeOfRLEDatasegment();
            treeMap.put(rleCompressedSize, ProjectConstants.RLE);
        }

        //
        for (Map.Entry<Long, String> entry : treeMap.entrySet()) {
            Long key = entry.getKey();
            String value = entry.getValue();
            System.out.println("Size of datasegment if " + value + ": " + key + " bytes");
        }
        System.out.println("Best compression: " + (prefferedCompression = treeMap.firstEntry().getValue()));


        fileChannel.close();


    }


    /**
     * Responsible for filling up the frequency array for Huffman compression if input is uncompressed compressed.
     *
     * @param singleByte
     */
    private void fillFrequencyArray(long[] colourValueFrequencies, byte singleByte) {
        totalBytesprocessed++;
        pixelCounter = totalBytesprocessed / 3;
        colourValueFrequencies[singleByte & 0xff]++;
    }


    /**
     * Responsible for filling up the frequency array for Huffman compression if input is RLE compressed.
     *
     * @param frequencies
     * @param singleByte
     */
    private void fillfreuencyArrayRLE(long[] frequencies, byte singleByte) {
        totalBytesprocessed++;
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

                if (mode == Mode.RAW_PACKET) {
                    frequencies[pixel[0] & 0xff]++;
                    frequencies[pixel[1] & 0xff]++;
                    frequencies[pixel[2] & 0xff]++;
                    counter--;
                    pixelCounter++;
                } else {
                    frequencies[pixel[0] & 0xff] += counter;
                    frequencies[pixel[1] & 0xff] += counter;
                    frequencies[pixel[2] & 0xff] += counter;
                    pixelCounter += counter;
                    counter = 0;
                }
            }
            if (counter == 0) {
                mode = Mode.COUNTER;
            }
            this.pixelByteCounter++;
        }
    }


    private long getNumberOfNodesInTreeBasedOnFrequencies(long[] colourValueFrequencies) {
        int size = 0;
        for (long colourValueFrequency : colourValueFrequencies) {
            if (colourValueFrequency > 0) {
                size++;
            }
        }
        return size;
    }

    public String getPrefferedCompression() {
        return prefferedCompression;
    }


    enum Mode {
        RAW_PACKET, RLE_PACKET, COUNTER
    }


    /**
     * An inner class that is responsible for facilitating the stepwise compression of an given input to the TARGA-RLE format.
     * For each step it uses a single Byte as an input.
     * <p>
     * In principle it is a modified version of UncompressedToRLE3.
     */
    public static class UncompressedToRLEForFileSize extends ConversionSuper implements AutoInterface {

        private final int imagewidth;
        int currentwidth = 0;
        int byteCounter = 0;
        int counter = -1;
        byte[] pixelOne = new byte[3];
        byte[] pixelPrevious = new byte[3];
        int totalSizeOfRLEDatasegment = 0;
        Mode mode = Mode.START;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        public UncompressedToRLEForFileSize(FileTypeSuper inputFile) {
            super(inputFile);
            this.imagewidth = inputFile.getWidth();

        }


        public int getTotalSizeOfRLEDatasegment() {
            return totalSizeOfRLEDatasegment;
        }


        /**
         * This is basically the same as the run(Byte singleByte) Method that you can find in UncompressedToRLE3,
         * without the save to file logic for memory and performance reasons.
         *
         * @return
         */
        public void run(byte singleByte) throws IOException {

            pixelOne[byteCounter % 3] = singleByte;

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
                    outputStream.write(pixelOne);
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
                        outputStream.write(pixelPrevious);
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
                        outputStream.write((pixelOne));
                        saveToOutputStream(counter);
                    } else {
                        counter++;

                        totalSizeOfRLEDatasegment += 4;
                    }
                    counter = -1;
                    mode = Mode.START;
                    currentwidth = 0;
                }
                System.arraycopy(pixelOne, 0, pixelPrevious, 0, 3);
            }
            byteCounter++;
        }


        /**
         * This is basically the same as the saveToOutputStream(int counter) Method that you can find in UncompressedToRLE3,
         * without the save to file logic for memory and performance reasons.
         *
         * @return
         */
        private void saveToOutputStream(int counter) {
            totalSizeOfRLEDatasegment++;
            totalSizeOfRLEDatasegment += outputStream.size();
            outputStream.reset();
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


    /**
     * Responsible for stepwise counting of frequencies.
     *
     * Is probably unnecessary, because tails are not very common.
     */
    public static class RLEtoRLEFileSIze extends ConversionSuper implements AutoInterface {

        Mode mode = Mode.COUNTER;
        int counter = 0;
        int pixelByteCounter = 0;
        byte[] pixel = new byte[3];
        int totalSizeOfRLEDatasegment = 0;

        public RLEtoRLEFileSIze(FileTypeSuper inputFile) {
            super(inputFile);
        }


        public int getTotalSizeOfRLEDatasegment() {
            return totalSizeOfRLEDatasegment;
        }


        /**
         * This is basically the same as the run(Byte singleByte) Method that you can find in RLEToRLE2,
         * without the save to file logic for memory and performance reasons.
         *
         * @return
         */
        public void run(byte singleByte) {
            totalSizeOfRLEDatasegment++;

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
                    if (mode == Mode.RAW_PACKET) {
                        counter--;
                        processedPixels++;
                    } else {
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
    }
}
