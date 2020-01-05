package src.propra.helpers;

import src.propra.compression_operations.ConversionSuper;
import src.propra.compression_operations.UncompressedToRLE3;
import src.propra.compression_operations.UncompressedToUncompressed;
import src.propra.file_types.FileTypeSuper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;


public class Huffman {


   public static byte[] bitMaskForDecoding = {1, 2, 4, 8, 16, 32, 64, -128};
    static byte[] bitMaskForEncodingEmptyByte = {-128, -64, -32, -16, -8, -4, -2, -1};
    static byte[] bitMaskForEncodingFilledByte = {1, 3, 7, 15, 31, 63, 127, -1};




    public static Tree buildHuffmanTreeFromFrequencies(long[] colorFrequencies) {
        // Baue Sortierte Liste von Nodes
        List<Node> nodeList = buildListBasedOnFrequencies(colorFrequencies);
        //Baue baum
        return generateHuffmanTreeFromNodeList(nodeList);
    }

    private static List<Node> buildListBasedOnFrequencies(long[] frequenzen) {
        List<Node> list = new LinkedList<>();
        for (int i = 0; i < frequenzen.length; i++) {
            if (frequenzen[i] != 0) {
                list.add(new Node(1, i, frequenzen[i]));
            }
            Comparator<Node> comparator = (o1, o2) -> {// Sortiere nach Frequenz
                return (int) (o1.getFrequency() - o2.getFrequency());
            };
            list.sort(comparator);
        }
        return list;
    }

    public static long calculateBitLengthOfHuffmanEncodedDatasegment(long[] colourValueFrequencies, HashMap<String, Byte> hashMap) {
        int fileSize = 0;
        https:
//stackoverflow.com/questions/46898/how-do-i-efficiently-iterate-over-each-entry-in-a-java-map
        for (Map.Entry<String, Byte> entry : hashMap.entrySet()) {
            fileSize += entry.getKey().length() * colourValueFrequencies[entry.getValue() & 0xff];
        }
        return fileSize;
    }

    private static long[] calculateFrequencies(long[] frequenzen, File inputFile, int offset) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(ProjectConstants.BUFFER_CAPACITY);
        FileChannel fileChannel = HelperMethods.initialiseInputChannel(inputFile, offset);

        byte singleByte;
        while ((fileChannel.read(byteBuffer)) > 0) {
            byteBuffer.flip();
            while (byteBuffer.hasRemaining()) {

                singleByte = byteBuffer.get();


                frequenzen[singleByte & 0xff]++;
            }
            byteBuffer.compact();
        }
        return frequenzen;
    }





    public static long[] calculateFrequenciesRLE(long[] frequenzen, File inputFile, int offset) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(ProjectConstants.BUFFER_CAPACITY);
        FileChannel fileChannel = HelperMethods.initialiseInputChannel(inputFile, offset);


        StepwiseFrequencyCalculatorRLE stepwiseFrequencyCalculatorRLE = new StepwiseFrequencyCalculatorRLE();

        while ((fileChannel.read(byteBuffer)) > 0) {
            byteBuffer.flip();
            while (byteBuffer.hasRemaining()) {
                stepwiseFrequencyCalculatorRLE.invoke(byteBuffer.get());
            }
            byteBuffer.compact();
        }
        return stepwiseFrequencyCalculatorRLE.getFrequenzen();
    }


    private static long[] calculateFrequenciesRLE2(long[] frequenzen, File inputFile, int offset) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(ProjectConstants.BUFFER_CAPACITY);
        FileChannel fileChannel = HelperMethods.initialiseInputChannel(inputFile, offset);
        Mode mode = null;
        byte singleByteBuffer;
        int sum = 0;
        int limit;
        int laeufer = 0;
        int rawCounter = 0;
        int rleCounter = 0;
        while ((limit = fileChannel.read(byteBuffer)) > 0) {
            byteBuffer.rewind();
            while (byteBuffer.position() < byteBuffer.limit()) { //da damit garanitiwer wird byteBuffer.position() < limit
                if (mode == null) {
                    singleByteBuffer = byteBuffer.get();
                    if (singleByteBuffer < 0) {
                        rleCounter = (singleByteBuffer & 0x7f) + 1;
                        mode = Mode.RLE;
                    } else {
                        rawCounter = singleByteBuffer + 1;
                        mode = Mode.RAW;
                    }
                }
                if (mode == Mode.RLE) {
                    if (byteBuffer.limit() - byteBuffer.position() < 3) {
                        byteBuffer.compact();
                        int position = byteBuffer.position();
                        limit = fileChannel.read(byteBuffer);
                        byteBuffer.limit(limit + position);
                        byteBuffer.rewind();
                    }
                    frequenzen[byteBuffer.get() & 0xff] += rleCounter;
                    frequenzen[byteBuffer.get() & 0xff] += rleCounter;
                    frequenzen[byteBuffer.get() & 0xff] += rleCounter;
                    rleCounter = 0;
                    mode = null;
                }
                if (mode == Mode.RAW) {
                    if (byteBuffer.limit() - byteBuffer.position() < 3 * rawCounter) {
                        byteBuffer.compact();
                        int position = byteBuffer.position();
                        limit = fileChannel.read(byteBuffer);
                        byteBuffer.limit(limit + position);
                        byteBuffer.rewind();
                    }
                    while (rawCounter > 0) {
                        frequenzen[byteBuffer.get() & 0xff]++;
                        frequenzen[byteBuffer.get() & 0xff]++;
                        frequenzen[byteBuffer.get() & 0xff]++;
                        rawCounter--;
                    }
                    mode = null;
                }
                sum = 0;
                for (long l : frequenzen) {
                    sum += l;
                }
            }
            byteBuffer.compact();
        }
        return frequenzen;
    }

    private static Tree generateHuffmanTreeFromNodeList(List<Node> nodeList) {
        Comparator<Node> comparator = new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {// Sortiere nach Frequenz
                int returnValue = (int) (o1.getFrequency() - o2.getFrequency());

                if (returnValue == 0) {// Falls Frequenzen gleich sind, dann Vergleiche ByteWerte
                    returnValue = -1 * (o1.getNodeDepth() - o2.getNodeDepth());
                }
                return returnValue;
            }
        };
        while (nodeList.size() > 1) {
            Node node1 = nodeList.get(0);
            nodeList.remove(0);
            Node node2 = nodeList.get(0);
            nodeList.remove(0);
            Node root = new Node(0);
            root.setRightNode(node1);
            root.setLeftNode(node2);
            root.setNodeDepth(Math.max(root.rightNode.getNodeDepth(), root.leftNode.getNodeDepth()) + 1);
            root.setFrequency(root.getLeftNode().getFrequency() + root.getRightNode().getFrequency());
            root.setByteValue(-1);
            nodeList.add(root);
            Collections.sort(nodeList, comparator);
        }
        Tree huffmanTree = new Tree(nodeList.get(0));
        HashMap hashMap = huffmanTree.createHashMap();
        return huffmanTree;
    }

    public static Tree generateHuffmanTreeFromUncompressedDatasegments(File inputFile, int offset) throws IOException {
        long[] frequenzen = new long[256];
        // Lese Array
        System.out.println("Version2");
        frequenzen = calculateFrequencies(frequenzen, inputFile, offset);
        // Baue Sortierte Liste von Nodes
        Tree tree = buildHuffmanTreeFromFrequencies(frequenzen);
        tree.preOrderTraversaslFuerHuffmanDatei(tree.root, "");
        System.out.println("Version2");


        return tree;
    }

    public static void generatePreOrderHuffmanTreeStringAsPerSpecification(Tree tree, StringBuilder stringBuilder) {
        tree.preOrderTraversaslFuerHuffmanDatei2(tree.root, "", stringBuilder);
        // die fehlenden Stellen im letzten Byte belegen.
        for (int i = 0; i < stringBuilder.length() % 8; i++) {
            stringBuilder.append("0");
        }
    }



    public static long[] getColorFrequencies(String compression, File inputFile, int headerLength) throws IOException {
        long[] frequenzen = new long[ProjectConstants.MAX_COLOR_VALUES];
        if (compression == ProjectConstants.RLE) {
            frequenzen = calculateFrequenciesRLE(frequenzen, inputFile, headerLength);
        } else {
            frequenzen = calculateFrequencies(frequenzen, inputFile, headerLength);
        }
        return frequenzen;
    }

    public static long getCountOfBitsInStoredHuffmanTree(long numberOfNodes) {
        return numberOfNodes * 9 + numberOfNodes - 1;
    }


    public static ConversionSuper getObejctThatPerformsOutputCompression(String compression, FileTypeSuper inputFile) {
        ConversionSuper conversionSuper = null;
        if (compression.equals(ProjectConstants.RLE)) {
            conversionSuper = new UncompressedToRLE3(inputFile);
        } else if  (compression.equals(ProjectConstants.UNCOMPRESSED)){
            conversionSuper = new UncompressedToUncompressed(inputFile);
        }
        else{
            System.err.println("Something went wrong while determining the required compression converter. " + "\n" +
                    "Check the suffix of your outputfile");
            System.exit(123);
        }
        return conversionSuper;
    }



//    public static void toHuffmanCompression2(Conversions convspec) throws IOException {
//        FileChannel fileChannel = HelperMethods.initialiseInputChannel(convspec.getInputFile().getFilepath().toFile(), convspec.getInputFile().getHeader().length);
//        HelperMethods.initialiseOutputFile(convspec.getInputFile().getFilepath().toFile(), convspec.getInputFile().getHeader().length);
//        ByteBuffer byteBuffer = ByteBuffer.allocate(ProjectConstants.BUFFER_CAPACITY);
//
//        long[] colorFrequencies = getColorFrequencies(convspec.getInputFile().getCompression(), convspec.getInputFile().getFilepath().toFile(), convspec.getInputFile().getHeader().length);
//        Tree tree = buildHuffmanTreeFromFrequencies(colorFrequencies);
//
//
//        StringBuilder stringBuilder = new StringBuilder();
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        generatePreOrderHuffmanTreeStringAsPerSpecification(tree, stringBuilder);
//
//        writeHuffmanTreeToBAoS(stringBuilder, byteArrayOutputStream); // Does not include the last byte if it is unfinished.
//
//
//        int offset = tree.getDiskSpaceOccuppiedByHuffmanTree() % 8;
//        byte buffer1 = (byte) Integer.parseInt(stringBuilder.substring(stringBuilder.length() - 8, stringBuilder.length()));
//
//
//        RLEToUncompressedV4 rleToUncompressed = null;
//        UncompressedToUncompressed uncompressedToUncompressed = null;
//        if (convspec.getInputFile().getCompression().equals(ProjectConstants.RLE)) {
//            rleToUncompressed = new RLEToUncompressedV4(convspec.getInputFile());
//        } else {
//            uncompressedToUncompressed = new UncompressedToUncompressed(convspec.getInputFile());
//        }


//        ToHuffmanConverter toHuffmanConverter = new ToHuffmanConverter(tree, offset, buffer1, convspec.getInputFile());
//        int limit;
//        byte[] tempByteArray;
//        while ((limit = fileChannel.read(byteBuffer)) > -1) {
//            byteBuffer.flip();
//            // Prüfsumme des Inputs berechnen.
//            convspec.getInputFile().calculateChecksumOfByteBuffer(byteBuffer, limit);
//            // Inputs sind ein ByteBuffer,
//            while (byteBuffer.hasRemaining()) {
//
//                if (convspec.getInputFile().getCompression().equals(ProjectConstants.RLE)) {
//                    assert rleToUncompressed != null;
//                    rleToUncompressed.run(byteBuffer.get());
//                    tempByteArray = rleToUncompressed.returnByteArray();
//                } else {
//                    assert uncompressedToUncompressed != null;
//                    uncompressedToUncompressed.run(byteBuffer.get());
//                    tempByteArray = uncompressedToUncompressed.returnByteArray();
//                }
//                if (tempByteArray != null) {
//                    toHuffmanConverter.run(tempByteArray);
//                }
//            }
//
//            byteBuffer.compact();
//        }
//
//
//        System.out.println("TEST");
//
//
//    }

    public static void writeHuffmanTreeToBAoS(StringBuilder stringBuilder, ByteArrayOutputStream byteArrayOutputStream) {
        //write HuffmanTree to OutputStream as per ProPra 3.0 specification.
        String temp2;
        for (int i = 0; i < stringBuilder.length() - 8; i += 8) {
            temp2 = stringBuilder.substring(i, i + 8);
            byteArrayOutputStream.write(Integer.parseInt(temp2, 2));
        }
    }


    enum Mode {
        COUNTER, RAW_PACKET, RLE_PACKET,
        RAW, RLE
    }

    static public class Node {

        private int value;
        private int byteValue;
        private long frequency;
        private Node leftNode;
        private Node rightNode;
        private int nodeDepth = 0;

        public Node(int value) {
            this.value = value;
        }

        public Node(int value, int byteValue, long frequency) {
            this.value = value;
            this.byteValue = byteValue;
            this.frequency = frequency;
        }

        public int getByteValue() {
            return byteValue;
        }

        public void setByteValue(int byteValue) {
            this.byteValue = byteValue;
        }

        public long getFrequency() {
            return frequency;
        }

        public void setFrequency(long frequency) {
            this.frequency = frequency;
        }

        public Node getLeftNode() {
            return leftNode;
        }

        public Node setLeftNode(Node leftNode) {
            this.leftNode = leftNode;
            return leftNode;
        }

        public int getNodeDepth() {
            return nodeDepth;
        }

        public void setNodeDepth(int nodeDepth) {
            this.nodeDepth = nodeDepth;
        }

        public Node getRightNode() {
            return rightNode;
        }

        public void setRightNode(Node rightNode) {
            this.rightNode = rightNode;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

    }

    public static class Tree {

        Node root;

        public Node getRoot() {
            return root;
        }

        int bitCounter = 0;


        public Tree(Node root) {
            this.root = root;


        }

        public Node buildHuffmanTree2(StringBuilder subtree, ByteBuffer byteBuffer, Node node, FileChannel inFileChannel) throws IOException {
            // TODO Fehler bei mehr als 256 Blättern
            if (subtree.length() < 8) {
                ByteBufferHelpers.refillEmptyBuffer(byteBuffer, inFileChannel);
                subtree.append(String.format("%8s", Integer.toBinaryString(byteBuffer.get() & 0xFF)).replace(' ', '0'));
            }
            if (subtree.charAt(0) == '0') {
                node.setValue(Integer.parseInt(String.valueOf(subtree.charAt(0))));
                subtree.deleteCharAt(0);
                bitCounter++;
            } else {
                node.setValue(1);
                subtree.deleteCharAt(0);
                if (subtree.length() < 8) {
                    ByteBufferHelpers.refillEmptyBuffer(byteBuffer, inFileChannel);
                    subtree.append(String.format("%8s", Integer.toBinaryString(byteBuffer.get() & 0xFF)).replace(' ', '0'));
                }
                node.setByteValue(Integer.parseInt(subtree.substring(0, 8), 2));
                subtree.delete(0, 8);
                bitCounter += 9;
            }
            Node newnode = new Node(-1);
            if (subtree.length() > 0) {
                newnode = new Node(Integer.parseInt(String.valueOf(subtree.charAt(0))));
            }
            if (node.leftNode == null && node.value == 0) {
                newnode = new Node(Integer.parseInt(String.valueOf(subtree.charAt(0))));
                node.leftNode = buildHuffmanTree2(subtree, byteBuffer, newnode, inFileChannel);
            }
            if (node.rightNode == null && node.value == 0) {
                ByteBufferHelpers.refillEmptyBuffer(byteBuffer, inFileChannel);
                subtree.append(String.format("%8s", Integer.toBinaryString(byteBuffer.get() & 0xFF)).replace(' ', '0'));
                newnode = new Node(Integer.parseInt(String.valueOf(subtree.charAt(0))));
                node.rightNode = buildHuffmanTree2(subtree, byteBuffer, newnode, inFileChannel);
            }
            return node;
        }


        public HashMap createHashMap() {
            HashMap hashMap = new HashMap<String, Byte>();
            inOrderTraversaslFuerHuffmanHashMap(root, "", hashMap);
            return hashMap;
        }

        public String[] generateEncodingTable() {
            // https://stackoverflow.com/questions/4234985/how-to-for-each-the-hashmap/25616206#25616206
            String[] encodingTable = new String[256];
            HashMap<String, Byte> hashMap = createHashMap();
            hashMap.forEach((k, v) -> encodingTable[v & 0xff] = k);

            return encodingTable;
        }

        public int getBitCounter() {
            return bitCounter;
        }

        public int getDiskSpaceOccuppiedByHuffmanTree() {
            return bitCounter / 8;
        }

        public int getHuffmanTreeStartingBit() {
            return 7 - (bitCounter % 8);
        }

        public void inOrderTraversaslFuerHuffman(Node node, String string) {

            if (node == null) {
                return;
            }
            if (node.getValue() == 1) {
//                System.out.println(string);
//                System.out.println("WERT: " + node.getByteValue());
            }
            inOrderTraversaslFuerHuffman(node.getLeftNode(), string + "0");
            inOrderTraversaslFuerHuffman(node.getRightNode(), string + "1");
        }

        public void inOrderTraversaslFuerHuffmanHashMap(Node node, String string, HashMap<String, Byte> hashMap) {
            if (node == null) {
                return;
            }
            if (node.getValue() == 1) {
//                System.out.println(string);
//                System.out.println("WERT: " + node.getByteValue());
                hashMap.put(string, (byte) node.getByteValue());
            }
            inOrderTraversaslFuerHuffmanHashMap(node.getLeftNode(), string + "0", hashMap);

            inOrderTraversaslFuerHuffmanHashMap(node.getRightNode(), string + "1", hashMap);

        }

        public void preOrderTraversaslFuerHuffmanDatei(Node node, String string) {
            if (node == null) {
                return;
            }
            if (node.getValue() == 0) {
                string = string + "0";
            }
            if (node.getValue() == 1) {
//                System.out.println(string + "1" + Integer.toBinaryString(node.getByteValue()));
            }
            preOrderTraversaslFuerHuffmanDatei(node.getLeftNode(), string);
            preOrderTraversaslFuerHuffmanDatei(node.getRightNode(), string);

        }

        public void preOrderTraversaslFuerHuffmanDatei2(Node node, String string, StringBuilder stringBuilder) {
            if (node == null) {
                return;
            }
            bitCounter++;
            if (node.getValue() == 0) {
                string = string + "0";
                stringBuilder.append("0");

            }
            if (node.getValue() == 1) {
//                System.out.println(string + "1" + String.format("%8s", Integer.toBinaryString(node.getByteValue() & 0xFF)).replace(' ', '0'));
                stringBuilder.append("1" + String.format("%8s", Integer.toBinaryString(node.getByteValue() & 0xFF)).replace(' ', '0'));
                bitCounter += 8;
//                System.out.println("Bitzähler: " + bitZaehler);
            }

            preOrderTraversaslFuerHuffmanDatei2(node.getLeftNode(), string, stringBuilder);
            preOrderTraversaslFuerHuffmanDatei2(node.getRightNode(), string, stringBuilder);

        }
    }


    private static class StepwiseFrequencyCalculatorRLE {
        Mode mode = Mode.COUNTER;

        int counter = 0;
        int pixelByteCounter = 0;
        int pixelCounter = 0;
        byte[] pixel = new byte[3];
        long[] frequenzen = new long[ProjectConstants.MAX_COLOR_VALUES];

        public long[] getFrequenzen() {
            return frequenzen;
        }

        public void invoke( byte singleByte) {
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
                        frequenzen[pixel[0] & 0xff]++;
                        frequenzen[pixel[1] & 0xff]++;
                        frequenzen[pixel[2] & 0xff]++;
                        counter--;
                        pixelCounter++;
                    } else {
                        frequenzen[pixel[0] & 0xff] += counter;
                        frequenzen[pixel[1] & 0xff] += counter;
                        frequenzen[pixel[2] & 0xff] += counter;
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
