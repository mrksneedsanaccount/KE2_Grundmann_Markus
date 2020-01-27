package propra.conversion_facilitators;

import propra.helpers.ProjectConstants;

import java.io.ByteArrayOutputStream;

/**
 * Contains the stepwise encode and decode methods.
 */
public class BaseNConverter {


    private static final int BITSINBYTE = 8;
    private final int numberOfBytesToBeProcessed;
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private int encodeLength;
    private int bitsInByteNeeded;
    private int indexInByte = 8;
    private int shift;
    private byte temp;
    private int maskSize;
    private String alphabet;
    private StringBuilder stringBuilder = new StringBuilder();
    private int bytesProcessed;
    private byte outputByte = 0;

    private int[] alphabetTable;


    public BaseNConverter(int numberOfBytesToBeProcessed, String alphabet, int encodeLength, String operation) {
        this.numberOfBytesToBeProcessed = numberOfBytesToBeProcessed;
        this.alphabet = alphabet;
        this.encodeLength = encodeLength;
        this.bitsInByteNeeded = encodeLength;
        if (operation.equals(ProjectConstants.DECODE)) {
            this.alphabetTable = createAlphabetTable(alphabet);
        }
    }


    private static byte getShiftMask(double v) {
        return (byte) (Math.pow(2, v) - 1);
    }


    /**
     * Is resposnbile for creating the alphabet table that is used when decoding a file.
     *
     * @param alphabet alphabet either provided explicitly, or implicitly (.base-32).
     * @return filled up alphabet table.
     */
    private int[] createAlphabetTable(String alphabet) {
        int[] alphabetTable = new int[256];

        for (int i = 0; i < alphabetTable.length; i++) {
            alphabetTable[i] = -1;
        }

        for (int i = 0; i < alphabet.length(); i++) {
            if (alphabetTable[alphabet.charAt(i)] == -1) {
                alphabetTable[alphabet.charAt(i)] = (byte) i;
            } else {
                System.err.println("Duplicate elements in alphabet. Please Check your inputfile");
                System.exit(123);
            }
        }
        return alphabetTable;
    }

    /**
     * Outputs an array filled with  decoded data once >BUFFER_CAPACITY data has been accumulated,
     * or the last bit of input data has been processed.
     *
     * @return Array of  decoded data.
     */
    public byte[] outputByteArrayForWritingToFile() {
        if (byteArrayOutputStream.size() >= ProjectConstants.BUFFER_CAPACITY || bytesProcessed == numberOfBytesToBeProcessed) {
            byte[] temp = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.reset();
            return temp;
        } else {
            return null;
        }
    }

    /**
     * Outputs an array filled with  encoded data once >BUFFER_CAPACITY data has been accumulated,
     * or the last bit of input data has been processed.
     *
     * @return Array of encoded data.
     */
    public String outputStringForWritingToFile() {
        if (stringBuilder.length() >= ProjectConstants.BUFFER_CAPACITY || bytesProcessed == numberOfBytesToBeProcessed) {
            String temp = stringBuilder.toString();
            stringBuilder.setLength(0);
            return temp;
        } else {
            return null;
        }
    }


    /**
     * Is responsible for stepwise decoding of a .base-32 or .base-n file up to 64 characters.
     * <p>
     * Writes completed characters into a ByteArrayOutputStream
     *
     * @param singleByte a single byte representing a character.
     */
    public void runDecode(byte singleByte) {
        bytesProcessed++;
        while (true) {
            temp = (byte) alphabetTable[singleByte & 0xff]; //decode the passed char.

            shift = indexInByte - encodeLength;

            //indexInByte refers to the index in the outputbyte
            indexInByte = indexInByte - encodeLength; // if it reaches into the next byte indexInByte will be NEGATIVE

            if (indexInByte > 0) { // The whole decoded char can be written into the outpuutByte.
                outputByte = (byte) (outputByte | (temp << shift)); //building the byte that will be written to file.
                break;
            } else {// finishing up the outputByte and preparing the next outputByte.
                outputByte = (byte) (outputByte | temp >> -shift);
                byteArrayOutputStream.write(outputByte);
                outputByte = 0;
                outputByte = (byte) (temp << 8 + shift);
                indexInByte = 8 + indexInByte; // the indexInByte on the left-hand side will be negative.
                break;
            }
        }

    }

    /**
     * Is responsible for the stepwise encoding of a binary file.
     *
     * Based on the alphabet that has been provided. (explicitly, or implicitly in the case of .base-32)
     *
     * @param singleByte
     */
    public void runEncode(byte singleByte) {
        bytesProcessed++;

        while (true) {


            // bitsInByte refers to the size of the block of bits that will be read from "singleByte" in this step.
            //example: if the encoding length is 5 and bitsInByteNeeded is 3 then that will either be the first 3 bytes, or the last 3 bytes
            //depending on the current position in the byte.
            maskSize = bitsInByteNeeded; // masksize 5 would refer to 0x1f etc.

            // shift can be positive and negative. The sign tells you in which direction the shift will happen.
            // positive -> right shift, negative -> left shift
            shift = indexInByte - bitsInByteNeeded;
            indexInByte = indexInByte - bitsInByteNeeded; // if indexByte is negative that means that the encoding goes across byte borders reaching into the next byte.
            // the 8th bit is the leftmost bit and 1 is the rightmost bit.
            if (indexInByte < 1) {
                bitsInByteNeeded = -shift;
                indexInByte = 8;
            } else {
                bitsInByteNeeded = encodeLength;
            }
            // temp holds the Character to be written.
            if (shift > 0) { // either the whole character can be found in the current byte, or we can now finish a partial char.
                temp = (byte) (temp | ((singleByte >> shift) & getShiftMask(maskSize)));
                stringBuilder.append(alphabet.charAt(temp & getShiftMask(encodeLength)));
                temp = 0;
            } else {// builds the partial char and puts the bits into the right position for the next round.
                temp = (byte) ((singleByte << (-shift)) & getShiftMask(encodeLength));
                if (bytesProcessed == numberOfBytesToBeProcessed) {// end of the source file has been reached.
                    stringBuilder.append(alphabet.charAt(temp & getShiftMask(encodeLength)));
                }
                break;
            }

        }


    }


}


