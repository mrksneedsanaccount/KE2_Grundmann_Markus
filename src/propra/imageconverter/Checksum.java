package src.propra.imageconverter;

import java.util.ArrayList;

/**
 * beinhaltet die/den Algorithmus zur Berechnung von Prüfsummen.
 * 
 */

public class Checksum {

    public static final int X = 65513;
    long a;
    long b;

    /**
     * Berechnet die Propra-Prüfsumme eines eingelesenen ByteArrays,
     * welches dem Datensegment entsprechen sollte.
     *
     * @param b1 ByteArray, welches die Pixeldaten der Eingabedatei enthält.
     */
    public static int calculateChecksumPropra(byte[] b1) {
        long b = 1;
        long a = 0;
        ArrayList<Integer> aL = new ArrayList<>();
        // Berechnung der Summe, die in der Klammer steht.
        for (int j = 1; j < b1.length + 1; j++) {
            a = (a + j + Byte.toUnsignedInt(b1[j - 1])) % X;
            // Zum Array hinzufügen A_n = [...] mod X

            b = (b + a) % X;
        }
        return (int) (a * 65536 + b);
    }

    public static void main(String[] args) {
        //Tests
        byte[] b = new byte[2];
        b[0] = (byte) 255;
        b[1] = (byte) 128;
        String s = "test";
        byte[] b1 = s.getBytes();
        calculateChecksumPropra(b1);
        System.out.println(Integer.toHexString(calculateChecksumPropra(b1)));
    }

    

}
