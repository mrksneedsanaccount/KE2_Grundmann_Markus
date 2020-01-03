package src.propra.compressionoperations;

import src.filetypes.FileTypeSuper;
import src.helperclasses.HelperMethods;
import src.helperclasses.Huffman;
import src.helperclasses.Pixel;
import src.helperclasses.ProjectConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class AutoModule {


    FileTypeSuper inputFile;
    long[] frequencies;
    long uncompressedSize;
    long rleCompressedSize;
    long huffmanSize;
    Mode mode = Mode.COUNTER;
    byte singleByte;
    int counter = 0;
    int pixelByteCounter = 0;
    int pixelCounter = 0;
    byte[] pixel = new byte[3];
    int totalBytesprocessed = 0;
    String outputFileSuffix;
    TreeMap<Long, String> treeMap = new TreeMap<Long, String>();
    String prefferedCompression;

    public AutoModule(FileTypeSuper inputFile, String outputFileSuffix) {
        this.inputFile = inputFile;
        this.outputFileSuffix = outputFileSuffix;
        uncompressedSize = (inputFile.getWidth() * inputFile.getHeight()) * 3;
        treeMap.put(uncompressedSize, ProjectConstants.UNCOMPRESSED);
    }

    public void calculateFrequenciesForAuto() throws IOException {

        ByteBuffer byteBuffer = ByteBuffer.allocate(ProjectConstants.BUFFER_CAPACITY);
        FileChannel fileChannel = HelperMethods.initialiseInputChannel(inputFile.getFilepath().toFile(), inputFile.getHeader().length);

        AutoInterface autoInterface = null;
        FromHuffmanToOutputcompression fromHuffmanToOutputcompression = null;
        if (inputFile.getCompression().equals(ProjectConstants.UNCOMPRESSED)) {
            autoInterface = new UncompressedToRLEForFileSize(inputFile);
        } else if (inputFile.getCompression().equals(ProjectConstants.RLE)) {
            autoInterface = new RLEtoRLEFileSIze(inputFile);
        } else if (inputFile.getCompression().equals(ProjectConstants.HUFFMAN)) {
            huffmanSize = (inputFile.getFilepath().toFile().length() - inputFile.getHeader().length);
            treeMap.put(huffmanSize, ProjectConstants.HUFFMAN);


                fromHuffmanToOutputcompression = new FromHuffmanToOutputcompression();
                fromHuffmanToOutputcompression.initializeConversion(fileChannel, inputFile, byteBuffer, ProjectConstants.AUTO);
        }

        long[] colourValueFrequencies = new long[ProjectConstants.MAX_COLOR_VALUES];
        byte singleByte;
        while ((fileChannel.read(byteBuffer)) > 0) {
            byteBuffer.flip();
            while (byteBuffer.hasRemaining()) {
                singleByte = byteBuffer.get();
                if (autoInterface != null)
                    autoInterface.run(singleByte);
                if (inputFile.getCompression().equals(ProjectConstants.HUFFMAN)) {
                    fromHuffmanToOutputcompression.run(singleByte);
                } else if (inputFile.getCompression().equals(ProjectConstants.UNCOMPRESSED) & outputFileSuffix.equals(ProjectConstants.PROPRA)) {
                    fillFrequencyArray(colourValueFrequencies, singleByte);
                } else if (inputFile.getCompression().equals(ProjectConstants.RLE) & (outputFileSuffix.equals(ProjectConstants.PROPRA))) {
                    fillfreuencyArrayRLE(colourValueFrequencies, singleByte);
                }
            }
            byteBuffer.clear();
        }



        if (!inputFile.getCompression().equals(ProjectConstants.HUFFMAN)) {
            assert autoInterface != null;
            rleCompressedSize = autoInterface.getTotalSizeOfRLEDatasegment();
            treeMap.put(rleCompressedSize, ProjectConstants.RLE);


            if ((outputFileSuffix.equals(ProjectConstants.PROPRA))) {
                // Determine the size of Huffman compressed datasegment.
                Huffman.Tree tree = Huffman.buildHuffmanTreeFromFrequencies(colourValueFrequencies);
                HashMap<String, Byte> hashMap = tree.createHashMap();



                int fileSize = 0;
                https://stackoverflow.com/questions/46898/how-do-i-efficiently-iterate-over-each-entry-in-a-java-map
                for (Map.Entry<String, Byte> entry : hashMap.entrySet()) {
                    fileSize += entry.getKey().length() * colourValueFrequencies[entry.getValue() & 0xff];
                }

                long numberOfNodes = getNumberOfNodesInTreeBasedOnFrequencies(colourValueFrequencies);
                double temp = (fileSize + getCountOfBitsInStoredHuffmanTree(numberOfNodes))/ 8.0;
                huffmanSize = (long) (Math.ceil(temp));
                treeMap.put(huffmanSize, ProjectConstants.HUFFMAN);
            }
        }
        if (inputFile.getCompression().equals(ProjectConstants.HUFFMAN)) {
            assert fromHuffmanToOutputcompression != null;
            autoInterface = (AutoInterface) fromHuffmanToOutputcompression.getUncompressedTooutputCompressionConverter();
            rleCompressedSize = autoInterface.getTotalSizeOfRLEDatasegment();
            treeMap.put(rleCompressedSize, ProjectConstants.RLE);
        }


        for(Map.Entry<Long,String> entry : treeMap.entrySet()) {
            Long key = entry.getKey();
            String value = entry.getValue();
            System.out.println("Size of datasegment if "+ value +": "+ key + " bytes");
        }
        System.out.println("Best compression: " + (prefferedCompression = treeMap.firstEntry().getValue()));



        fileChannel.close();


    }

    public long getCountOfBitsInStoredHuffmanTree(long numberOfNodes) {
        return numberOfNodes*9+numberOfNodes-1;
    }

    public long getNumberOfNodesInTreeBasedOnFrequencies(long[] colourValueFrequencies) {
        int size = 0;
        for (long colourValueFrequency : colourValueFrequencies) {
            if(colourValueFrequency > 0){
               size++;
            }
        }
        return size;
    }

    public long fillFrequencyArray(long[] colourValueFrequencies, byte singleByte) {
        totalBytesprocessed++;
        return colourValueFrequencies[singleByte & 0xff]++;
    }

    public void fillfreuencyArrayRLE(long[] frequencies, byte singleByte) {
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
                    counter = 0;
                }
            }
            if (counter == 0) {
                mode = Mode.COUNTER;
            }
            this.pixelByteCounter++;
        }
    }

    public String getPrefferedCompression() {
        return prefferedCompression;
    }


    enum Mode {
        RAW_PACKET, RLE_PACKET, COUNTER
    }

    public static class UncompressedToRLEForFileSize extends ConversionSuper implements AutoInterface {

        private final int imagewidth;
        int currentwidth = 0;
        ByteBuffer buffer;
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

        private void saveToOutputStream(int counter) throws IOException {
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

    public static class RLEtoRLEFileSIze extends ConversionSuper implements AutoInterface {

        Mode mode = Mode.COUNTER;
        int counter = 0;
        int pixelByteCounter = 0;
        int pixelCounter = 0;
        byte[] pixel = new byte[3];
        int totalSizeOfRLEDatasegment = 0;

        public RLEtoRLEFileSIze(FileTypeSuper inputFile) {
            super(inputFile);
        }


        public int getTotalSizeOfRLEDatasegment() {
            return totalSizeOfRLEDatasegment;
        }

        public void run(byte singleByte) throws IOException {
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
