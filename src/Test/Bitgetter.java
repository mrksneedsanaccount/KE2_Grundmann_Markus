package src.Test;

public class Bitgetter {

    byte currentBit;

    int currentByteNumber;




    int getBit (byte[] inputByteArray, int bit){

        byte[] bitmasks = { 0x01, 0x02, 0x04, 0x08 ,0x10, 0x20  ,0x40, (byte) 0x80};



        return inputByteArray[1];
    }



}
