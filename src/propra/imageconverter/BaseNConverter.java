package src.propra.imageconverter;

import java.util.HashMap;
import java.util.Map;

/**
 * Beinhaltet die Methoden für Kodierung einer ByteArrays
 * bzw. Dekodierung eines Strings in ein ByteArray
 *
 *
 */
public class BaseNConverter {


    private static final int BITSINBYTE = 8;


    /**
     * Verarbeitet, das eingehende ByteArray in den String der entsprechenden Kodierung.
     *
     *
     * @param ba  ByteArray für die Eingabe
     * @param alphabet Kodierungsalphabet
     * @param offset Anzahl der Bits der Kodierung
     * @return der kodierte String
     */

    public static String convertToBaseN(byte[] ba, String alphabet,
                                        int offset) {
        byte temp = 0;
        StringBuilder outputString = new StringBuilder();
        // conversion part
        for (int i = 0; i < ba.length * BITSINBYTE; i += offset) {
            int indexImArray = i % BITSINBYTE;
            int indexOfArray = i / BITSINBYTE;
            int abstandZuArryEnde = BITSINBYTE - indexImArray;
            byte b = ba[i / 8];
            if (8 - (indexImArray % BITSINBYTE) >= offset) {
                // Falls der Offset nicht kleiner als das Byte ist.

                temp = (byte) ((b >> (abstandZuArryEnde - offset))
                        & ((byte) (Math.pow(2, offset) - 1))); //

                outputString.append(alphabet.charAt(temp));
            } else { // nur ein Teil des Wortest befindet sich im
                // Ausgangsindex
                temp = (byte) (b
                        & ((byte) (Math.pow(2, abstandZuArryEnde) - 1)));

                if (indexOfArray < ba.length - 1) {// nicht letzes Byte im Array
                    temp = (byte) (temp << (offset - (abstandZuArryEnde)));

                    //ba[indexOfArray + 1]&0xff nötig wegen   https://stackoverflow.com/questions/14501233/unsigned-right-shift-operator-in-java
                    byte temp2 = (byte) ((ba[indexOfArray + 1] & 0xff) >>> (byte) (2 * BITSINBYTE
                            - offset - indexImArray)); // fügt die
                    // beiden
                    // Teilstrings
                    // zusammen.
                    temp |= temp2;
                } else { //letztes byte im Array
                    temp = (byte) (temp << (offset - (abstandZuArryEnde)));
                    temp &= ((byte) (Math.pow(2, offset) - 1));
                }
                outputString.append(alphabet.charAt((temp & ((byte) (Math.pow(2, offset) - 1)))));// Nur die ersten x Byte sollen gelesen werden
            }
        }
        return outputString.toString();
    }

    /**
     * dekodiert einen eingehenden String und gibt das ByteArray aus.
     *
     * @param input Eingabestring
     * @param alphabet benutztes Alphabet
     * @param offset Anzahl der Bits der Kodierung
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
                inputBits = (byte) map.get(input.charAt(indexinString));
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
                ba[arrayIndex] += (byte) (inputBits >> verschiebefenster);
                {
                    if (arrayIndex + 1 < ba.length) {
                        ba[arrayIndex
                                + 1] += (byte) (inputBits << (8 - verschiebefenster));
                    }
                }
            }
            indexinByte++; // da wir falsche Zeichen ignorieren;
        }
        return ba;
    }


}


