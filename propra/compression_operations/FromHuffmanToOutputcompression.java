package propra.compression_operations;

import propra.file_types.FileTypeSuper;
import propra.helpers.Huffman;
import propra.helpers.ProjectConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static propra.helpers.Huffman.getObejctThatPerformsOutputCompression;

public class FromHuffmanToOutputcompression extends ConversionSuper {

    int bytesprocessed = 0;
    private HuffmanToUncompressed huffmanToUncompressedConverter;
    private ConversionSuper uncompressedToOutputCompressionConverter;


    public FromHuffmanToOutputcompression() {
    }

    public FromHuffmanToOutputcompression(FileTypeSuper inputFile) {
        super(inputFile);
    }

    public ConversionSuper getUncompressedToOutputCompressionConverter() {
        return uncompressedToOutputCompressionConverter;
    }


    @Override
    public void initializeConversion(FileChannel fileChannel, FileTypeSuper inputFile, ByteBuffer byteBuffer, String compression) throws IOException {

        int limit;
        limit = fileChannel.read(byteBuffer);
        byteBuffer.flip();

        Huffman.Tree tree = new Huffman.Tree(new Huffman.Node(0));
        tree.buildHuffmanTree2(new StringBuilder(), byteBuffer, tree.getRoot(), fileChannel);
        byteBuffer.position(tree.getDiskSpaceOccuppiedByHuffmanTree());

        for (int i = 0; i < tree.getDiskSpaceOccuppiedByHuffmanTree(); i++) {
            inputFile.calculateChecksum(byteBuffer.array()[i]);
        }

        byteBuffer.clear();
        fileChannel.position(tree.getDiskSpaceOccuppiedByHuffmanTree() + inputFile.getHeader().length);

        int count = tree.getHuffmanTreeStartingBit();
        huffmanToUncompressedConverter = new HuffmanToUncompressed(count, tree);
        if (compression.equals(ProjectConstants.AUTO)) {
            uncompressedToOutputCompressionConverter = new AutoModule.UncompressedToRLEForFileSize(inputFile);
        } else {
            uncompressedToOutputCompressionConverter = getObejctThatPerformsOutputCompression(compression, inputFile);
        }

        byteArrayOutputStream = uncompressedToOutputCompressionConverter.byteArrayOutputStream;
        flag = uncompressedToOutputCompressionConverter.flag;


    }

    public void initializeConversionForAuto(FileChannel fileChannel, FileTypeSuper inputFile, ByteBuffer byteBuffer) throws IOException {

        fileChannel.read(byteBuffer);
        byteBuffer.flip();
        Huffman.Tree tree = new Huffman.Tree(new Huffman.Node(0));
        tree.buildHuffmanTree2(new StringBuilder(), byteBuffer, tree.getRoot(), fileChannel);
        byteBuffer.position(tree.getDiskSpaceOccuppiedByHuffmanTree());
        byteBuffer.clear();
        fileChannel.position(tree.getDiskSpaceOccuppiedByHuffmanTree() + inputFile.getHeader().length);
        int count = tree.getHuffmanTreeStartingBit();
        huffmanToUncompressedConverter = new HuffmanToUncompressed(count, tree);

        uncompressedToOutputCompressionConverter = new AutoModule.UncompressedToRLEForFileSize(inputFile);
        byteArrayOutputStream = uncompressedToOutputCompressionConverter.byteArrayOutputStream;
        flag = uncompressedToOutputCompressionConverter.flag;


    }


    @Override
    public byte[] outputForWritingToFile() {

        if (uncompressedToOutputCompressionConverter.byteArrayOutputStream.size() >= ProjectConstants.BUFFER_CAPACITY
                || uncompressedToOutputCompressionConverter.flag == Flags.LAST_PIXEL_HAS_BEEN_PROCESSED ||
                uncompressedToOutputCompressionConverter.getProcessedPixels() == inputFile.getHeight() * inputFile.getWidth()) {
            byte[] temp = uncompressedToOutputCompressionConverter.byteArrayOutputStream.toByteArray();
            uncompressedToOutputCompressionConverter.byteArrayOutputStream.reset();
            return temp;
        } else {
            return null;
        }
    }

    @Override
    public byte[] returnByteArray() {

        if (uncompressedToOutputCompressionConverter.byteArrayOutputStream.size() > 0) {
            byte[] temp = uncompressedToOutputCompressionConverter.byteArrayOutputStream.toByteArray();
            uncompressedToOutputCompressionConverter.byteArrayOutputStream.reset();
            return temp;
        } else {
            return null;
        }
    }

    @Override
    public void run(byte singleByte) throws IOException {
        huffmanToUncompressedConverter.run(singleByte);
        if (huffmanToUncompressedConverter.howManyBytesProcessed() > 0) {
            uncompressedToOutputCompressionConverter.run(huffmanToUncompressedConverter.returnByteArray());
            processedPixels = uncompressedToOutputCompressionConverter.getProcessedPixels();
        }
    }


    public class HuffmanToUncompressed extends ConversionSuper {

        int offset;
        Huffman.Node hoffmanNode;
        Huffman.Tree tree;


        HuffmanToUncompressed(int offset, Huffman.Tree tree) {
            this.offset = offset;
            this.tree = tree;
            this.hoffmanNode = tree.getRoot();

        }

        public void getOutputArray(ByteArrayOutputStream outputStream) throws IOException {
            byteArrayOutputStream.writeTo(outputStream);
            byteArrayOutputStream.reset();
        }

        public void run(byte singleByte) {
            bytesprocessed++;

            while (true) {
                byte bit = (byte) (singleByte & Huffman.bitMaskForDecoding[offset]);
                if (bit != 0) {
                    hoffmanNode = hoffmanNode.getRightNode();
                } else {
                    hoffmanNode = hoffmanNode.getLeftNode();
                }
                if (hoffmanNode.getValue() == 1) {
                    byteArrayOutputStream.write(hoffmanNode.getByteValue());
                    hoffmanNode = tree.getRoot();
                }
                offset--;
                if (offset == -1) {
                    offset = 7;
                    break;
                }
            }

        }


    }


}


