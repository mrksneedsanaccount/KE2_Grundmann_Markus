package src.propra.compressionoperations;

import src.filetypes.FileTypeSuper;
import src.helperclasses.Huffman;
import src.helperclasses.ProjectConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class ConvertToHuffman extends ConversionSuper {

    ConversionSuper inputCompressionToUncompressedConverter;
    ToHuffmanConverter toHuffmanConverter;
    byte[] tempByteArray;

    public ConvertToHuffman(FileTypeSuper inputFile) {
        super(inputFile);
    }

    @Override
    public void initializeConversion(FileChannel fileChannel, FileTypeSuper inputFile, ByteBuffer byteBuffer, String compression) throws IOException {

        long[] colorFrequencies = Huffman.getColorFrequencies(inputFile.getCompression(), inputFile.getFilepath().toFile(), inputFile.getHeader().length);
        Huffman.Tree tree = Huffman.buildHuffmanTreeFromFrequencies(colorFrequencies);

        StringBuilder stringBuilder = new StringBuilder();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Huffman.generatePreOrderHuffmanTreeStringAsPerSpecification(tree, stringBuilder);
        Huffman.writeHuffmanTreeToBAoS(stringBuilder, byteArrayOutputStream); // Does not include the last byte, if it is not
        // complete.


        int offset = tree.getHuffmanTreeStartingBit();
        byte firstByteOfHuffmanEncoding = (byte) Integer.parseInt(stringBuilder.substring(stringBuilder.length() - 8, stringBuilder.length()), 2);

        chooseConverterToUncompressedPropra(inputFile);
        toHuffmanConverter = new ToHuffmanConverter(tree, offset, firstByteOfHuffmanEncoding, inputFile);
        writeEncodedHuffmanTreeToHuffmanConverter(byteArrayOutputStream);


    }

    public void chooseConverterToUncompressedPropra(FileTypeSuper inputFile) {
        if (inputFile.getCompression().equals(ProjectConstants.RLE)) {
            inputCompressionToUncompressedConverter = new RLEToUncompressedV4(inputFile);
        } else {
            inputCompressionToUncompressedConverter = new UncompressedToUncompressed(inputFile);
        }
    }

    public void writeEncodedHuffmanTreeToHuffmanConverter(ByteArrayOutputStream byteArrayOutputStream) throws IOException {
        toHuffmanConverter.byteArrayOutputStream.write(byteArrayOutputStream.toByteArray());
    }

    @Override
    public void run(byte singleByte) throws IOException {
        inputCompressionToUncompressedConverter.run(singleByte);
        tempByteArray = inputCompressionToUncompressedConverter.returnByteArray();
        if (tempByteArray != null) {
            toHuffmanConverter.run(tempByteArray);
        }
        processedPixels = inputCompressionToUncompressedConverter.getProcessedPixels();

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
    public byte[] outputForWritingToFile() {
        if (toHuffmanConverter.byteArrayOutputStream.size() >= ProjectConstants.BUFFER_CAPACITY || processedPixels == inputFile.getWidth() * inputFile.getHeight()) {
            byte[] temp = toHuffmanConverter.byteArrayOutputStream.toByteArray();
            toHuffmanConverter.byteArrayOutputStream.reset();
            return temp;
        } else {
            return null;
        }
    }

    static class ToHuffmanConverter extends ConversionSuper {

        StringBuilder stringBuffer = new StringBuilder();
        Huffman.Tree tree;
        String[] encodingTable;
        int offset;
        byte buffer;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        public ToHuffmanConverter(Huffman.Tree tree, int offset, byte buffer, FileTypeSuper inputfile) {
            this.tree = tree;
            encodingTable = tree.generateEncodingTable();
            this.offset = offset;
            this.buffer = buffer;
            this.inputFile = inputfile;
        }

        @Override
        public void run(byte[] tempByteArray) throws IOException {
            for (byte b : tempByteArray) {
                run(b);
            }

        }

        @Override
        public void run(byte singleByte) throws IOException {
            processedPixels++;

            stringBuffer.append(encodingTable[(singleByte & 0xff)]);
            for (int i = 0; i < stringBuffer.length(); i++) {
                char stringBinary = stringBuffer.charAt(i);
                if (stringBinary == '0') {
                } else {
                    buffer |= Huffman.bitMaskForDecoding[offset];
                }
                offset--;
                if (offset < 0) { // Ab hier wird jedes vollständige Byte geoutputted
                    byteArrayOutputStream.write(buffer);
                    buffer = 0;
                    offset = 7;
                }
            }
            if ((inputFile.getWidth()*inputFile.getHeight())*3 == processedPixels & offset != 7){
                byteArrayOutputStream.write(buffer);
            }
            stringBuffer.setLength(0);

        }


    }
}
