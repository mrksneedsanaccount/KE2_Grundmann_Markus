package src.propra.compressionoperations;

import src.filetypes.FileTypeSuper;
import src.helperclasses.Huffman;
import src.helperclasses.ProjectConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static src.helperclasses.Huffman.getObejctThatPerformsOutputCompression;

public class FromHuffmanToOutputcompression extends ConversionSuper {

    private HuffmanToUncompressed huffmanUncompressedConverter;
    private ConversionSuper uncompressedTooutputCompressionConverter;

    public ConversionSuper getUncompressedTooutputCompressionConverter() {
        return uncompressedTooutputCompressionConverter;
    }

    public FromHuffmanToOutputcompression() {
    }

    public FromHuffmanToOutputcompression(FileTypeSuper inputFile) {
        super(inputFile);
    }

    @Override
    public void initializeConversion(FileChannel fileChannel, FileTypeSuper inputFile, ByteBuffer byteBuffer, String compression) throws IOException {

        int limit;
        limit = fileChannel.read(byteBuffer);
        inputFile.calculateChecksumOfByteBuffer(byteBuffer, limit);
        byteBuffer.flip();

        Huffman.Tree tree = new Huffman.Tree(new Huffman.Node(0));
        tree.buildHuffmanTree2(new StringBuilder(), byteBuffer, tree.getRoot(), fileChannel);
        byteBuffer.position(tree.getDiskSpaceOccuppiedByHuffmanTree());
        byteBuffer.compact();

        int count = tree.getHuffmanTreeStartingBit();
        huffmanUncompressedConverter = new HuffmanToUncompressed(byteBuffer, count, tree);
        if (compression.equals(ProjectConstants.AUTO)){
            uncompressedTooutputCompressionConverter = new AutoModule.UncompressedToRLEForFileSize(inputFile);
        }
            else
        {
            uncompressedTooutputCompressionConverter = getObejctThatPerformsOutputCompression(compression, inputFile);
        }

    }

    @Override
    public byte[] outputForWritingToFile() {
        if (uncompressedTooutputCompressionConverter.byteArrayOutputStream.size() >= ProjectConstants.BUFFER_CAPACITY || processedPixels == inputFile.getWidth() * inputFile.getHeight()) {
            byte[] temp = uncompressedTooutputCompressionConverter.byteArrayOutputStream.toByteArray();
            uncompressedTooutputCompressionConverter.byteArrayOutputStream.reset();
            return temp;
        } else {
            return null;
        }
    }

    @Override
    public byte[] returnByteArray() {

        if (uncompressedTooutputCompressionConverter.byteArrayOutputStream.size() > 0) {
            byte[] temp = uncompressedTooutputCompressionConverter.byteArrayOutputStream.toByteArray();
            uncompressedTooutputCompressionConverter.byteArrayOutputStream.reset();
            return temp;
        } else {
            return null;
        }
    }

    @Override
    public void run(byte singleByte) throws IOException {
        huffmanUncompressedConverter.run(singleByte);
        if (huffmanUncompressedConverter.howManyBytesProcessed() > 0) {
            uncompressedTooutputCompressionConverter.run(huffmanUncompressedConverter.returnByteArray());
            processedPixels = uncompressedTooutputCompressionConverter.getProcessedPixels();
        }
    }


    public static class HuffmanToUncompressed extends ConversionSuper{

        int offset;
        ByteBuffer byteBuffer;
        Huffman.Node hoffmanNode;
        Huffman.Tree tree;



        HuffmanToUncompressed(ByteBuffer byteBuffer, int offset, Huffman.Tree tree) {
            this.byteBuffer = byteBuffer;
            this.offset = offset;
            this.tree = tree;
            this.hoffmanNode = tree.getRoot();

        }

        public void getOutputArray(ByteArrayOutputStream outputStream) throws IOException {
            byteArrayOutputStream.writeTo(outputStream);
            byteArrayOutputStream.reset();
        }

        public void run(byte singleByte) {

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


