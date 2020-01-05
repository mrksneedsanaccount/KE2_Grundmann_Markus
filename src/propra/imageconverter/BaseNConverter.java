package src.propra.imageconverter;

import src.propra.helpers.ProjectConstants;

import java.io.ByteArrayOutputStream;

/**
 * Contains the stepwise encode and decode methods.
 */
public class BaseNConverter {


    private static final int BITSINBYTE = 8;
    private final int numberOfBytesToBeProcessed;
    private int encodeLength;
    private int bitsInByteNeeded;
    private int indexInByte = 8;
    private int shift;
    private byte temp;
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
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



    public static byte getShiftMask(double v) {
        return (byte) (Math.pow(2, v) - 1);
    }


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

    public void runDecode(byte singleByte) {
        bytesProcessed++;
        while (true) {
            temp = (byte) alphabetTable[singleByte & 0xff];
            shift = indexInByte - encodeLength;

            // if it reaches into the next byte indexInByte will be NEGATIVE
            indexInByte = indexInByte - encodeLength;

            if (indexInByte > 0) {
                outputByte = (byte) (outputByte | (temp << shift));
                break;
            } else {
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
     * The stepwise encoding method.
     *
     * @param singleByte
     */
    public void runEncode(byte singleByte) {
        bytesProcessed++;

        while (true) {

            shift = indexInByte - bitsInByteNeeded;
            maskSize = bitsInByteNeeded;

            indexInByte = indexInByte - bitsInByteNeeded;
            if (indexInByte < 1) {
                bitsInByteNeeded = -shift;
                indexInByte = 8;
            } else {
                bitsInByteNeeded = encodeLength;
            }

            if (shift > 0) {
                temp = (byte) (temp | ((singleByte >> shift) & getShiftMask(maskSize)));
                stringBuilder.append(alphabet.charAt(temp & getShiftMask(encodeLength)));
                temp = 0;
            } else {
                temp = (byte) ((singleByte << (-shift)) & getShiftMask(encodeLength));
                if (bytesProcessed == numberOfBytesToBeProcessed) {
                    stringBuilder.append(alphabet.charAt(temp & getShiftMask(encodeLength)));
                }
                break;
            }

            //TODO letztes Byte in der Datei herausschreiben?

        }


    }



}


