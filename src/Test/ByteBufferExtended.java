package src.Test;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.BitSet;

class ByteBufferExtended {




    static void maske(byte[] masks, int zaehler){


    }


        public static void main(String[] args) throws IOException {


//        BitSet bitSet = new BitSet();
//        System.out.println(bitSet.size());
//
//        bitSet.set(4555);
//        System.out.println(bitSet.size());
                byte test = 1;



                byte[] bytes = new byte[8];
                for (int i = 0; i < 8; i++) {
                bytes[i] = test;
                test = (byte) (test << 1);
//            System.out.println(test);
            }

            File file;
            FileChannel fileChannel = null;
            try {
                fileChannel = new FileInputStream( new File("G:/ProPra/KE3_TestBilder/test_05_huffman.propra") ).getChannel();
            } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        try {
            fileChannel.read(byteBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byteBuffer.flip();


        byte buffer = byteBuffer.get();


        int count = 7;
        for (int i = 0; i < 10; i++) {

            int bit = buffer & bytes[count];
            System.out.println(bit);

            count--;
            if (count == 0){
                count = 7;
                buffer = byteBuffer.get();
            };

        }






    }




}