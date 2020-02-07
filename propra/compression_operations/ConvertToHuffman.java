package propra.compression_operations;

import propra.file_types.FileTypeSuper;
import propra.helpers.Huffman;
import propra.helpers.ProjectConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class ConvertToHuffman extends ConversionSuper {

    private ConversionSuper inputCompressionToUncompressedConverter;
    private ToHuffmanConverter toHuffmanConverter;
    private byte[] tempByteArray;
    private int processedByte = 0;

    public ConvertToHuffman(FileTypeSuper inputFile) {
        super(inputFile);
    }

    private void chooseConverterToUncompressedPropra(FileTypeSuper inputFile) {
        if (inputFile.getCompression().equals(ProjectConstants.RLE)) {
            inputCompressionToUncompressedConverter = new RLEToUncompressedV4(inputFile);
        } else {
            inputCompressionToUncompressedConverter = new UncompressedToUncompressed(inputFile);
        }
    }

    @Override
    public void initializeConversion(FileChannel fileChannel, FileTypeSuper inputFile, ByteBuffer byteBuffer, String compression) throws IOException {

        //1. determine color frequencies
        //2. build Huffman tree.
        //3. prepare initialise the Converters required.
        //4. write the encoded Huffman tree to the ByteArrayOutputStream, that will be used to write to file.

        //1.
        long[] colorFrequencies = Huffman.getColorFrequencies(inputFile.getCompression(), inputFile.getFilepath().toFile(), inputFile.getHeader().length, inputFile);
        Huffman.Tree tree = Huffman.buildHuffmanTreeFromFrequencies(colorFrequencies);
        //2.
        StringBuilder stringBuilder = new StringBuilder();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Huffman.generatePreOrderHuffmanTreeStringAsPerSpecification(tree, stringBuilder);
        Huffman.writeHuffmanTreeToBAoS(stringBuilder, byteArrayOutputStream); // Does not include the last byte, if it is not
        // complete.

        //3.
        int offset = tree.getHuffmanTreeStartingBit();
        if (stringBuilder.length() < 8) {
            stringBuilder.append("00000000");
        }
        byte firstByteOfHuffmanEncoding = (byte) Integer.parseInt(stringBuilder.substring(stringBuilder.length() - 8, stringBuilder.length()), 2);
        chooseConverterToUncompressedPropra(inputFile);
        toHuffmanConverter = new ToHuffmanConverter(tree, offset, firstByteOfHuffmanEncoding, inputFile); // firstByteOfHuffmanEncoding: the first byte you manipulate.

        //4.
        writeEncodedHuffmanTreeToHuffmanConverterOutputStream(byteArrayOutputStream);

    }

    @Override
    public byte[] returnByteArray() {
        if (toHuffmanConverter.byteArrayOutputStream.size() > 0) {
            byte[] temp = toHuffmanConverter.byteArrayOutputStream.toByteArray();
            toHuffmanConverter.byteArrayOutputStream.reset();
            return temp;
        } else {
            return null;
        }
    }

    @Override
    public void run(byte singleByte) throws IOException {
        //1. takes the input files image data byte by byte and uncompresses it
        //2. if Pixels have been decompressed and converted to .propra send them on to the Huffman converter.
        //3. count the number of processed Pixels

        inputCompressionToUncompressedConverter.run(singleByte); //1.
        tempByteArray = inputCompressionToUncompressedConverter.returnByteArray();
        if (tempByteArray != null) {
            toHuffmanConverter.runIteratingOverArray(tempByteArray.length, tempByteArray); //2.
        }
        processedPixels = inputCompressionToUncompressedConverter.getProcessedPixels(); //3.

    }

    @Override
    public byte[] transferChunkOfProcessedData() {
        if (toHuffmanConverter.byteArrayOutputStream.size() >= ProjectConstants.BUFFER_CAPACITY || processedPixels == totalNumberOfPixelsInInputImage) {
            byte[] temp = toHuffmanConverter.byteArrayOutputStream.toByteArray();
            toHuffmanConverter.byteArrayOutputStream.reset();
            return temp;
        } else {
            return null;
        }
    }

    private void writeEncodedHuffmanTreeToHuffmanConverterOutputStream(ByteArrayOutputStream byteArrayOutputStream) throws IOException {
        toHuffmanConverter.byteArrayOutputStream.write(byteArrayOutputStream.toByteArray());
    }

    /**
     * Is responsible for the stepwise compression of uncompressed image data (already converted to .propra) to
     * Huffman encoded data.
     */
    class ToHuffmanConverter extends ConversionSuper {

        StringBuilder bitStringBuilder = new StringBuilder();
        Huffman.Tree tree;
        String[] codeTable;
        int offset;
        byte buffer;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        ToHuffmanConverter(Huffman.Tree tree, int offset, byte buffer, FileTypeSuper inputFile) {
            super(inputFile);
            this.tree = tree;
            codeTable = tree.generateEncodingTable();
            this.offset = offset;
            this.buffer = buffer;
            this.inputFile = inputFile;
        }

        @Override
        public void run(byte singleByte) {


            processedByte++;
            bitStringBuilder.append(codeTable[(singleByte & 0xff)]); // adds the code of the encoded byte to the desired to the bitstring
            for (int i = 0; i < bitStringBuilder.length(); i++) { // write the the bits through the bite by iterating through the String.
                char stringBinary = bitStringBuilder.charAt(i);
                if (stringBinary == '1') {
                    buffer |= Huffman.bitMaskForDecoding[offset];
                }
                offset--;
                if (offset < 0) { // The byte is finished and can be sent on. and a new byte for writing gets started.
                    byteArrayOutputStream.write(buffer);
                    buffer = 0;
                    offset = 7;
                }
            }
            // End of the compression operation is reached when this condition is met, before the last byte has been sent on.
            if (((totalNumberOfPixelsInInputImage) * 3 == processedByte) && offset != 7) {
                byteArrayOutputStream.write(buffer);
            }
            bitStringBuilder.setLength(0);
        }

        @Override
        public void runIteratingOverArray(int limit, byte[] tempByteArray) throws IOException {
            for (byte b : tempByteArray) {
                run(b);
            }

        }


    }
}
