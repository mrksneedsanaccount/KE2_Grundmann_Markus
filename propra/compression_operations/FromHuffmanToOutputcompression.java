package propra.compression_operations;

import propra.exceptions.ConversionException;
import propra.file_types.FileTypeSuper;
import propra.helpers.Huffman;
import propra.helpers.ProjectConstants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static propra.helpers.Huffman.getObjectThatPerformsOutputCompression;

public class FromHuffmanToOutputcompression extends ConversionSuper {

    int bytesprocessed = 0;
    private HuffmanToUncompressed huffmanToUncompressedConverter;
    private ConversionSuper uncompressedToOutputCompressionConverter;


    public FromHuffmanToOutputcompression(FileTypeSuper inputFile) {
        super(inputFile);
    }

    public ConversionSuper getUncompressedToOutputCompressionConverter() {
        return uncompressedToOutputCompressionConverter;
    }


    /**
     * This method is a variant of the regular initialise without the the Checksum calculation.
     *
     * @param fileChannel
     * @param byteBuffer
     * @throws IOException
     */
    public void initialiseConversionForAuto(FileChannel fileChannel, ByteBuffer byteBuffer) throws IOException {

        fileChannel.read(byteBuffer);
        byteBuffer.flip();
        Huffman.Tree tree = new Huffman.Tree(new Huffman.Node(0));
        tree.buildHuffmanTree(new StringBuilder(), byteBuffer, tree.getRoot(), fileChannel);
        byteBuffer.position(tree.getDiskSpaceOccuppiedByHuffmanTree());
        byteBuffer.clear();
        fileChannel.position(tree.getDiskSpaceOccuppiedByHuffmanTree() + inputFile.getHeader().length);
        int count = tree.getHuffmanTreeStartingBit();
        huffmanToUncompressedConverter = new HuffmanToUncompressed(count, tree, inputFile);
        uncompressedToOutputCompressionConverter = new AutoModule.UncompressedToRLEForFileSize(inputFile);
        byteArrayOutputStream = uncompressedToOutputCompressionConverter.byteArrayOutputStream;
        flag = uncompressedToOutputCompressionConverter.flag;


    }

    @Override
    public void initializeConversion(FileChannel fileChannel, FileTypeSuper inputFile, ByteBuffer byteBuffer, String compression) throws IOException, ConversionException {

        // 1. start filling up the buffer.
        // 2, Start building the Huffman tree from the encoded Huffman tree.
        // 3. Start the checksum calculation
        // 4. reset the buffer to end of the encoded Huffman tree.
        //5. initialise the required Converter Objects. First Huffman -> uncompressed and then uncompressed -> output compression and format


        //1.
        fileChannel.read(byteBuffer);
        byteBuffer.flip();

        //2.
        Huffman.Tree tree = new Huffman.Tree(new Huffman.Node(0));
        tree.buildHuffmanTree(new StringBuilder(), byteBuffer, tree.getRoot(), fileChannel);
        byteBuffer.position(tree.getDiskSpaceOccuppiedByHuffmanTree());
        //3.
        for (int i = 0; i < tree.getDiskSpaceOccuppiedByHuffmanTree(); i++) {
            inputFile.calculateChecksum(byteBuffer.array()[i]);
        }
        //4.
        byteBuffer.clear();
        fileChannel.position(tree.getDiskSpaceOccuppiedByHuffmanTree() + inputFile.getHeader().length);

        //5.
        int count = tree.getHuffmanTreeStartingBit();
        huffmanToUncompressedConverter = new HuffmanToUncompressed(count, tree, inputFile);
        if (compression.equals(ProjectConstants.AUTO)) {
            uncompressedToOutputCompressionConverter = new AutoModule.UncompressedToRLEForFileSize(inputFile);
        } else {
            uncompressedToOutputCompressionConverter = getObjectThatPerformsOutputCompression(compression, inputFile);
        }
        byteArrayOutputStream = uncompressedToOutputCompressionConverter.byteArrayOutputStream;
        flag = uncompressedToOutputCompressionConverter.flag;
        // flag is required for signaling that there is no more relevant data to read and now the rest of the processed
        // data should be written to the output file.

    }

    /**
     * Returns a byte array.
     * This bytearray that contains the Huffman -> uncompressed pixels.
     * Those bytes will get further processed by the second stepwise converter in the FromHuffmantoOutputcompression
     * object.
     *
     * @return Byte array of uncompressed data.
     */
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

    /**
     * This method takes in a byte, then decodes the Huffman compression, and then converts the file into the desired
     * compression.
     * It also converts the Pixel into the desired format.
     *
     * @param singleByte
     * @throws IOException
     */
    @Override
    public void run(byte singleByte) throws IOException {
        huffmanToUncompressedConverter.run(singleByte);
        if (huffmanToUncompressedConverter.howManyBytesProcessed() > 0) {
            uncompressedToOutputCompressionConverter.runIteratingOverArray(huffmanToUncompressedConverter.returnByteArray());
            processedPixels = uncompressedToOutputCompressionConverter.getProcessedPixels();
        }
    }

    @Override
    public byte[] transferChunkOfProcessedData() {
        // the different conditions that need to be met in order to write to file, I did not dare to remove any of them.
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

    /**
     * This class is responsible for stepwise converting of Huffman encoded data to
     */
    public class HuffmanToUncompressed extends ConversionSuper {

        int offset;
        Huffman.Node hoffmanNode;
        Huffman.Tree tree;


        HuffmanToUncompressed(int offset, Huffman.Tree tree, FileTypeSuper inputFile) {
            super(inputFile);
            this.offset = offset;
            this.tree = tree;
            this.hoffmanNode = tree.getRoot();

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


