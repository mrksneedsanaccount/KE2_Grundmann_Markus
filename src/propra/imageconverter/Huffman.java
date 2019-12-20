package src.propra.imageconverter;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;


public class Huffman {


    static byte[] bitMaskForDecoding = {1, 2, 4, 8, 16, 32, 64, -128};
    static byte[] bitMaskForEncodingEmptyByte = {-128, -64, -32, -16, -8, -4, -2, -1};
    static byte[] bitMaskForEncodingFilledByte = {1, 3, 7, 15, 31, 63, 127, -1};


    public static byte[] ProPraHuffmanToUncompressed(byte[] datasegment) {
        ByteBuffer datasegmentBuffer = ByteBuffer.wrap(datasegment);
        Tree tree = new Tree(new Node(0));
        tree.buildHuffmanTree(new StringBuilder(), datasegmentBuffer, tree.root);
        HashMap hashMap = tree.createHashMap();
        datasegmentBuffer.position(tree.getDiskSpaceOccuppiedByHuffmanTree());
        byte buffer = datasegmentBuffer.get();
        int count = tree.getHuffmanTreeStartingBit();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Node hoffmanNode = tree.root;
        for (int i = 0; i < datasegmentBuffer.limit() * 8 - 1; i++) {
            byte bit = (byte) (buffer & bitMaskForDecoding[count]);
            if (bit != 0) {
                hoffmanNode = hoffmanNode.rightNode;
            } else {
                hoffmanNode = hoffmanNode.getLeftNode();
            }
            if (hoffmanNode.value == 1) {
                byteArrayOutputStream.write(hoffmanNode.byteValue);
                hoffmanNode = tree.root;
            }
            count--;
            if (count == -1) {
                count = 7;
                int i1 = datasegmentBuffer.limit() - tree.getDiskSpaceOccuppiedByHuffmanTree();
                int i2 = i / 8;
                if (i2 == i1 - 1) {
                    break;
                }
                buffer = datasegmentBuffer.get();
            }
        }


        return byteArrayOutputStream.toByteArray();
    }

    private static List<Node> buildBasedOnFrequency(long[] frequenzen) {
        TreeMap integerNodeTreeMap = new TreeMap<Integer, Node>();
        List<Node> list = new LinkedList<>();
        for (int i = 0; i < frequenzen.length; i++) {
            if (frequenzen[i] != 0) {
                list.add(new Node(1, i, frequenzen[i]));
            }
            Comparator<Node> comparator = new Comparator<Node>() {
                @Override
                public int compare(Node o1, Node o2) {// Sortiere nach Frequenz
                    int returnValue = (int) (o1.getFrequency() - o2.getFrequency());

//                    if (returnValue == 0) {// Falls Frequenzen gleich sind, dann Vergleiche ByteWerte
//                        returnValue = o1.getByteValue() - o2.getByteValue();
//                    }
                    return returnValue;
                }
            };
            Collections.sort(list, comparator);
        }
        return list;
    }

    private static long[] calculateFrequencies(long[] frequenzen, File inputFile, int offset) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(64 * 1024);
//        64 * 1024

        FileChannel fileChannel = new FileInputStream(inputFile).getChannel();
        fileChannel.position(offset + 1);
        int limit;
        while ((limit = fileChannel.read(byteBuffer)) > 0) {
            byteBuffer.rewind();
            for (int i = 0; i < limit; i++) {
                frequenzen[byteBuffer.get() & 0xff]++;
            }
            byteBuffer.clear();
        }


        return frequenzen;
    }


    private static long[] calculateFrequenciesRLE(long[] frequenzen, File inputFile, int offset) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(64 * 1024);
//        64 * 1024

        FileChannel fileChannel = new FileInputStream(inputFile).getChannel();
        fileChannel.position(offset + 1);
        byte singleByteBuffer;
        int limit;
        int laeufer = 0;
        int rawCounter = 0;
        int rleCounter = 0;
        while ((limit = fileChannel.read(byteBuffer)) > 0) {
            byteBuffer.rewind();

            if (rawCounter == 0 & rleCounter ==0){
                singleByteBuffer =byteBuffer.get();
                if (singleByteBuffer < 0){
                    rleCounter = (singleByteBuffer & 0xff)+1;
                }
                else {
                    rawCounter = singleByteBuffer+1;
                }

            }




            byteBuffer.clear();
        }


        return frequenzen;
    }

















    private static Tree generateHuffmanTreeFromNodeList(List<Node> nodeList) {
//        Map tempMap = MapUtil.sortByValue(hashMap);
//        List<Map.Entry<Integer, Long>> list = new LinkedList(tempMap.entrySet());
        Comparator<Node> comparator = new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {// Sortiere nach Frequenz
                int returnValue = (int) (o1.getFrequency() - o2.getFrequency());

                if (returnValue == 0) {// Falls Frequenzen gleich sind, dann Vergleiche ByteWerte
                    returnValue = -1*(o1.getNodeDepth() - o2.getNodeDepth());
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
            root.setNodeDepth(Math.max(root.rightNode.getNodeDepth(), root.leftNode.getNodeDepth())+1);
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
        List<Node> nodeList = buildBasedOnFrequency(frequenzen);
        //Baue baum
        Tree tree = generateHuffmanTreeFromNodeList(nodeList);
        tree.preOrderTraversaslFuerHuffmanDatei(tree.root, "");
        System.out.println("Version2");


        return tree;
    }


    public static void main(String[] args) throws IOException {

//
//        File frequencyFile = new File("../KE3_TestBilder/test_03_uncompressed.propra");
//        Tree tree2 = generateHuffmanTreeFromUncompressedDatasegments(frequencyFile, 28);


        File file = new File("G:/ProPra/KE3_TestBilder/test_05_huffman.propra");
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
        bufferedInputStream.skip(28);
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < 200; i++) {

            stringBuilder.append(String.format("%8s", Integer.toBinaryString(bufferedInputStream.read())).replace(' ', '0'));
        }

        if (stringBuilder == null) {

            //falls rest länger 8 einfach machen,

            // sonst append?

        }
        //TEST
        bufferedInputStream.close();
        bufferedInputStream = new BufferedInputStream(new FileInputStream(file));

        bufferedInputStream.skip(28);
        ByteBuffer dataasegmentBuffer = ByteBuffer.allocate((int) (file.length() - 28));
        bufferedInputStream.read(dataasegmentBuffer.array());
        Tree tree3 = new Tree(new Node(0));
        tree3.buildHuffmanTree(new StringBuilder(), dataasegmentBuffer, tree3.root);
        tree3.inOrderTraversaslFuerHuffman(tree3.root, "");
        HashMap hashMap = tree3.createHashMap();

//TEST


//        11111111
        StringBuilder stringBuilder1 = new StringBuilder();
        stringBuilder1.append("001000000001000000010100000010100000011");


        Tree tree1 = new Tree(new Node(0));
        tree1.buildHuffmanTree(stringBuilder1, tree1.root);
        tree1.inOrderTraversaslFuerHuffman(tree1.root, "");


        System.out.println(stringBuilder.toString());


        System.out.println("Tree: " + stringBuilder.substring(0, stringBuilder.toString().indexOf("1") + 1));

        Tree tree = new Tree(new Node(0));
        tree.buildHuffmanTree(stringBuilder, tree.root);
        tree.inOrderTraversaslFuerHuffman(tree.root, "");
        HashMap hashMap2 = tree.createHashMap();
        boolean bool = hashMap.equals(hashMap2);

        stringBuilder.append(String.format("%8s", Integer.toBinaryString(bufferedInputStream.read())).replace(' ', '0'));
        //TODO Decode Loop

        byte test = 1;

        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            bytes[i] = test;
            test = (byte) (test << 1);
//            System.out.println(test);
        }

        FileChannel fileChannel = null;
        try {
            fileChannel = new FileInputStream(new File("G:/ProPra/KE3_TestBilder/test_05_huffman.propra")).getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        ByteBuffer byteBuffer = ByteBuffer.allocate((int) (new File("G:/ProPra/KE3_TestBilder/test_05_huffman.propra").length() - 86));
        try {
            fileChannel.position(27 + tree.getDiskSpaceOccuppiedByHuffmanTree() + 1); // +1 da es sich ja um das nächste Bit handelt.
            fileChannel.read(byteBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byteBuffer.flip();
        byte buffer = byteBuffer.get();
        int count = 2;
        ByteBuffer bb2 = ByteBuffer.allocate(500 * 602 * 3);

        Node hoffmanNode = tree.root;
        for (int i = 0; i < byteBuffer.limit() * 8 - 1; i++) {
            int bit = buffer & bitMaskForDecoding[count];
            if (bit != 0) {
                hoffmanNode = hoffmanNode.rightNode;
            } else {
                hoffmanNode = hoffmanNode.getLeftNode();
            }
            if (hoffmanNode.value == 1) {
                bb2.put((byte) hoffmanNode.byteValue);
                hoffmanNode = tree.root;
            }
            count--;
            if (count == -1) {
                count = 7;
                int i1 = byteBuffer.limit();
                int i2 = i / 8;
                if (i2 == i1 - 1) {
                    break;
                }
                buffer = byteBuffer.get();
            }


        }

        System.out.println("TEST");


        File outputfile = new File("G:/ProPra/KE3_TestBilder/test_05_huffman.tga");
        if (outputfile.exists()){
            outputfile.delete();
        }
        Set<StandardOpenOption> options = new HashSet<>();
        options.add(StandardOpenOption.CREATE);
        options.add(StandardOpenOption.APPEND);
        Path path = Paths.get(outputfile.getAbsolutePath());
        FileChannel fileChannel3 = FileChannel.open(path, options);
        fileChannel3.write(ByteBuffer.allocate(18));
        bb2.rewind();
        fileChannel3.write(bb2);


        //byteArray
        Tree testtree = generateHuffmanTreeFromUncompressedDatasegments(outputfile, 18);

        HashMap hashMap1 = testtree.createHashMap();
        bool = hashMap.equals(hashMap1);
        StringBuilder stringBuilder2 = new StringBuilder();
        testtree.preOrderTraversaslFuerHuffmanDatei2(testtree.root, "", stringBuilder2);
        for (int i = 0; i < stringBuilder2.length() % 8; i++) {
            stringBuilder2.append("0");
        }
        byte[] bval = new BigInteger(stringBuilder2.toString(), 2).toByteArray();
        String[] encodingTable = testtree.generateEncodingTable();
        StringBuilder stringBuffer = new StringBuilder();

        // Kodierer
        bb2.rewind();

        int offset;
        byte buffer1 = bval[bval.length - 1];
        offset = tree.getDiskSpaceOccuppiedByHuffmanTree() % 8;
        ByteBuffer huffmanKomprimierteBilddaten = ByteBuffer.allocate(64 * 1024);

        byte zuKodierendesByte;
        fileChannel3.close();
//        Inputs inputbyebuffer und outputbytebuffer und leerer StringBuilder??


        ByteBuffer byteBuffer1 = byteBuffer.allocate(64*1024*1000);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        while (bb2.position() < bb2.limit()) { //position mit Integer ersetzen?
            // get Chunk ?
            stringBuffer.append(encodingTable[(bb2.get()&0xff)]);
            while (stringBuffer.length() > 0) {
                char stringBinary = stringBuffer.charAt(0);
                if (stringBinary == '0') {
                } else {
                    buffer1 |= bitMaskForDecoding[offset];
                }
                stringBuffer.deleteCharAt(0);
                offset--;
                if (offset < 0) {
                    // Propra Prüfsumme

                    huffmanKomprimierteBilddaten.put(buffer1);


                    if ((huffmanKomprimierteBilddaten.position() == huffmanKomprimierteBilddaten.limit()) || bb2.position() == bb2.limit()){
                        //outputlogik input huffmanKomprimierteBilddaten; || bb2.position() == bb2.limit()



                        //outputten
                        byteArrayOutputStream.write( huffmanKomprimierteBilddaten.array(), 0, huffmanKomprimierteBilddaten.position());
                        huffmanKomprimierteBilddaten.clear();
                        //
                    }
                    buffer1 = 0;
                    offset = 7;
                }
                if (stringBuffer.length() == 0) {
                    break;
                }
            }
        }


        System.out.println("TEST");

    }

    static class Node {

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

    static class Tree {

        Node root;
        int bitZaehler = 0;


        public Tree(Node root) {
            this.root = root;


        }

        public Node buildHuffmanTree(StringBuilder subtree, ByteBuffer datasegmentByteBuffer, Node node) {
            // TODO Fehler bei mehr als 256 Blättern
            if (subtree.length() < 8) {
                subtree.append(String.format("%8s", Integer.toBinaryString(datasegmentByteBuffer.get() & 0xFF)).replace(' ', '0'));
            }
            if (subtree.charAt(0) == '0') {
                node.setValue(Integer.parseInt(String.valueOf(subtree.charAt(0))));
                subtree.deleteCharAt(0);
                bitZaehler++;
            } else {
                node.setValue(1);
                subtree.deleteCharAt(0);
                if (subtree.length() < 8) {
                    subtree.append(String.format("%8s", Integer.toBinaryString(datasegmentByteBuffer.get() & 0xFF)).replace(' ', '0'));
                }
                System.out.println(subtree.substring(0, 8));
                node.setByteValue(Integer.parseInt(subtree.substring(0, 8), 2));
                subtree.delete(0, 8);
                bitZaehler += 9;
            }
            Node newnode = new Node(-1);
            if (subtree.length() > 0) {
                newnode = new Node(Integer.parseInt(String.valueOf(subtree.charAt(0))));
            }
            if (node.leftNode == null && node.value == 0) {
                newnode = new Node(Integer.parseInt(String.valueOf(subtree.charAt(0))));
                node.leftNode = buildHuffmanTree(subtree, datasegmentByteBuffer, newnode);
            }
            if (node.rightNode == null && node.value == 0) {
                subtree.append(String.format("%8s", Integer.toBinaryString(datasegmentByteBuffer.get() & 0xFF)).replace(' ', '0'));
                newnode = new Node(Integer.parseInt(String.valueOf(subtree.charAt(0))));
                node.rightNode = buildHuffmanTree(subtree, datasegmentByteBuffer, newnode);
            }
            return node;
        }

        // Alte Version
        public Node buildHuffmanTree(StringBuilder subtree, Node node) {
            // TODO Fehler bei mehr als 256 Blättern

            if (subtree.charAt(0) == '0') {
                node.setValue(Integer.parseInt(String.valueOf(subtree.charAt(0))));
                subtree.deleteCharAt(0);
                bitZaehler++;
            } else {
                node.setValue(1);
                subtree.deleteCharAt(0);
                System.out.println(subtree.substring(0, 8));
                node.setByteValue(Integer.parseInt(subtree.substring(0, 8), 2));
                subtree.delete(0, 8);
                bitZaehler += 9;
            }
            Node newnode = new Node(-1);
            if (subtree.length() > 0) {
                newnode = new Node(Integer.parseInt(String.valueOf(subtree.charAt(0))));
            }
            if (node.leftNode == null && node.value == 0) {
                newnode = new Node(Integer.parseInt(String.valueOf(subtree.charAt(0))));
                node.leftNode = buildHuffmanTree(subtree, newnode);
            }
            if (node.rightNode == null && node.value == 0) {
                newnode = new Node(Integer.parseInt(String.valueOf(subtree.charAt(0))));
                node.rightNode = buildHuffmanTree(subtree, newnode);
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

        int getDiskSpaceOccuppiedByHuffmanTree() {
            return bitZaehler / 8;
        }

        int getHuffmanTreeStartingBit() {
            return 7 - (bitZaehler % 8);
        }

        public void inOrderTraversaslFuerHuffman(Node node, String string) {

            if (node == null) {
                return;
            }
            if (node.getValue() == 1) {
                System.out.println(string);
                System.out.println("WERT: " + node.getByteValue());
            }
            inOrderTraversaslFuerHuffman(node.getLeftNode(), string + "0");
            inOrderTraversaslFuerHuffman(node.getRightNode(), string + "1");
        }

        public void inOrderTraversaslFuerHuffmanHashMap(Node node, String string, HashMap<String, Byte> hashMap) {

            if (node == null) {
                return;
            }
            if (node.getValue() == 1) {
                System.out.println(string);
                System.out.println("WERT: " + node.getByteValue());
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
                System.out.println(string + "1" + Integer.toBinaryString(node.getByteValue()));
            }
            preOrderTraversaslFuerHuffmanDatei(node.getLeftNode(), string);
            preOrderTraversaslFuerHuffmanDatei(node.getRightNode(), string);

        }

        public void preOrderTraversaslFuerHuffmanDatei2(Node node, String string, StringBuilder stringBuilder) {
            if (node == null) {
                return;
            }
            bitZaehler++;
            if (node.getValue() == 0) {
                string = string + "0";
                stringBuilder.append("0");

            }
            if (node.getValue() == 1) {
                System.out.println(string + "1" + String.format("%8s", Integer.toBinaryString(node.getByteValue() & 0xFF)).replace(' ', '0'));
                stringBuilder.append("1" + String.format("%8s", Integer.toBinaryString(node.getByteValue() & 0xFF)).replace(' ', '0'));
                bitZaehler += 8;
                System.out.println("Bitzähler: " + bitZaehler);
            }

            preOrderTraversaslFuerHuffmanDatei2(node.getLeftNode(), string, stringBuilder);
            preOrderTraversaslFuerHuffmanDatei2(node.getRightNode(), string, stringBuilder);

        }
    }


}
