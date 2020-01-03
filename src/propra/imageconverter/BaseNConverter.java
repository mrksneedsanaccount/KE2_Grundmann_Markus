package src.propra.imageconverter;

import src.helperclasses.ProjectConstants;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Beinhaltet die Methoden für Kodierung einer ByteArrays
 * bzw. Dekodierung eines Strings in ein ByteArray
 */
public class BaseNConverter {


    private static final int BITSINBYTE = 8;
    final int numberOfBytesToBeProcessed;
    int encodeLength;
    int bitsInByteNeeded;
    int indexInByte = 8;
    int shift;
    byte temp;
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    int maskSize;
    String alphabet;
    StringBuilder stringBuilder = new StringBuilder();
    int bytesProcessed;
    byte outputByte = 0;

    private byte[] alphabetTable;


    public BaseNConverter(int numberOfBytesToBeProcessed, String alphabet, int encodeLength, String operation) {
        this.numberOfBytesToBeProcessed = numberOfBytesToBeProcessed;
        this.alphabet = alphabet;
        this.encodeLength = encodeLength;
        this.bitsInByteNeeded = encodeLength;
        if (operation.equals(ProjectConstants.DECODE)) {
            this.alphabetTable = createAlphabetTable(alphabet);
        }
    }


    /**
     * Verarbeitet, das eingehende ByteArray in den String der entsprechenden Kodierung.
     *
     * @param ba       ByteArray für die Eingabe
     * @param alphabet Kodierungsalphabet
     * @param offset   Anzahl der Bits der Kodierung
     * @return der kodierte String
     */

    public static String convertToBaseN(byte[] ba, String alphabet,
                                        int offset) {
        byte temp = 0;
        StringBuilder outputString = new StringBuilder();
        // conversion part
        for (int i = 0; i < ba.length * BITSINBYTE; i += 00) {
            int indexImArray = i % BITSINBYTE;
            int indexOfArray = i / BITSINBYTE;
            int abstandZuArryEnde = BITSINBYTE - indexImArray;
            byte b = ba[i / 8];

            if (8 - (indexImArray % BITSINBYTE) >= offset) {
                // Falls der Offset nicht kleiner als das Byte ist.

                temp = (byte) ((b >> (abstandZuArryEnde - offset))
                        & getShiftMask(Math.pow(2, offset) - 1)); //

                outputString.append(alphabet.charAt(temp));
            } else { // nur ein Teil des Wortest befindet sich im
                // Ausgangsindex
                temp = (byte) (b
                        & (getShiftMask(Math.pow(2, abstandZuArryEnde) - 1)));

                if (indexOfArray < ba.length - 1) {// nicht letzes Byte im Array
                    temp = getShiftMask(temp << (offset - (abstandZuArryEnde)));

                    //ba[indexOfArray + 1]&0xff nötig wegen   https://stackoverflow.com/questions/14501233/unsigned-right-shift-operator-in-java
                    byte temp2 = (byte) ((ba[indexOfArray + 1] & 0xff) >>> (byte) (2 * BITSINBYTE
                            - offset - indexImArray)); // fügt die
                    // beiden
                    // Teilstrings
                    // zusammen.
                    temp |= temp2;
                } else { //letztes byte im Array
                    temp = getShiftMask(temp << (offset - (abstandZuArryEnde)));
                    temp &= (getShiftMask(Math.pow(2, offset) - 1));
                }
                outputString.append(alphabet.charAt((temp & (getShiftMask(Math.pow(2, offset) - 1)))));// Nur die ersten x Byte sollen gelesen werden
            }
        }
        return outputString.toString();
    }

    /**
     * dekodiert einen eingehenden String und gibt das ByteArray aus.
     *
     * @param input    Eingabestring
     * @param alphabet benutztes Alphabet
     * @param offset   Anzahl der Bits der Kodierung
     * @return dekodiertes ByteArray
     */
    public static byte[] decodeBaseN(String input, String alphabet,
                                     int offset) {
        // Ausgabearrays
        int dateiLaenge = input.length() * offset / 8;

        byte[] ba = new byte[dateiLaenge];
        Map map = new HashMap<String, Byte>();
        byte inputBits;
        int indexinString = 0;
        int indexImArray;
        int arrayIndex;
        int indexinByte = 0;

        // Alphabet inititalisieren
        for (byte i = 0; i < alphabet.length(); i++) {
            map.put(alphabet.charAt(i), i);

        }
        // dekodieren
        for (indexinString = 0; indexinString < input
                .length(); indexinString++) {

            indexImArray = (indexinByte * offset) % 8;
            arrayIndex = indexinByte * offset / BITSINBYTE;
            try {
                inputBits = getShiftMask((double) map.get(input.charAt(indexinString)));
            } catch (NullPointerException e) {// falls ein Symbol nicht Teil des Alphabets ist.
                System.out.println(
                        "unbekanntes Symbol an Stelle: " + indexinByte);
                System.exit(123);
                continue;
            }
            if ((indexImArray + offset) <= 8) {
                ba[arrayIndex] += (byte) (inputBits << (8 - indexImArray
                        - offset));
            } else {
                int verschiebefenster = offset - (BITSINBYTE - indexImArray);
                ba[arrayIndex] += getShiftMask(inputBits >> verschiebefenster);
                {
                    if (arrayIndex + 1 < ba.length) {
                        ba[arrayIndex
                                + 1] += getShiftMask(inputBits << (8 - verschiebefenster));
                    }
                }
            }
            indexinByte++; // da wir falsche Zeichen ignorieren;
        }
        return ba;
    }

    public static byte getShiftMask(double v) {
        return (byte) (Math.pow(2, v) - 1);
    }

    private HashMap buildHasmap() {

        Map map = new HashMap<String, Byte>();

        for (byte i = 0; i < alphabet.length(); i++) {
            map.put(alphabet.charAt(i), i);

        }


        return null;
    }

    private byte[] createAlphabetTable(String alphabet) {
        byte[] alphabetTable = new byte[256];

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

    public void runDecode(byte singleByte) {
        bytesProcessed++;
        while (true) {
            temp = (byte) alphabetTable[singleByte & 0xff];
            shift = indexInByte - encodeLength;

            indexInByte = indexInByte - encodeLength;

            if (indexInByte > 0) {
                outputByte = (byte) (outputByte | (temp << shift));
                break;
            } else {
                outputByte = (byte) (outputByte | temp >> -shift);
                byteArrayOutputStream.write(outputByte);
                outputByte = 0;
                outputByte = (byte) (temp << 8 + shift);
                indexInByte = 8 + indexInByte;
                break;

            }


        }


    }

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

    public byte[] returnByteArray() {
        if (byteArrayOutputStream.size() > 0) {
            byte[] temp = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.reset();
            return temp;
        } else {
            return null;
        }
    }


    public byte[] outputByteArrayForWritingToFile() {
        if (byteArrayOutputStream.size() >= ProjectConstants.BUFFER_CAPACITY || bytesProcessed == numberOfBytesToBeProcessed) {
            byte[] temp = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.reset();
            return temp;
        }
        else {
            return  null;
        }
    }

    public String outputStringForWritingToFile() {
        if (stringBuilder.length() >= ProjectConstants.BUFFER_CAPACITY || bytesProcessed == numberOfBytesToBeProcessed) {
            String temp = stringBuilder.toString();
            stringBuilder.setLength(0);
            return temp;
        }
        else {
            return  null;
        }
    }



}


