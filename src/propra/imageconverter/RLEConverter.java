package src.propra.imageconverter;

import src.filetypes.FileTypeSuper;
import src.helperclasses.ConversionSpecs;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Diese Klasse enthält die Methoden für die Konvertierung unkomprimiert -> LRE und umgekehrt.
 */
public class RLEConverter {

    /**
     * Diese Methode ist die "Dachmethode", welche die zwischenschrrite enthält, um von
     * unkomprimiert zu einem LRE komprimierten ByteArray zu kommen.
     *
     * @param convspec Objekt, welches die Informationen über die durchzuführende Operation enthält.
     * @return LRE-komprimiertes ByteArray
     */
    public static byte[] LREconversionMethod(ConversionSpecs convspec) {
        // Initialisierung
        FileTypeSuper inputfile = convspec.getInputformat();
        FileTypeSuper outputfile = convspec.getOutputformat();
        int bytePerZeile = inputfile.getWidth() * 3; // bytes per Zeile
        int height = outputfile.getHeight();
        ByteArrayOutputStream bAOS = new ByteArrayOutputStream();

        byte[] reinesDatensegment = Arrays.copyOfRange(convspec.getInputDatasegments(), 0,
                bytePerZeile * height);
        //Die Bilddaten werden Zeile für Zeile abgearbeitet.
        for (int i = 0; i < height; i++) {
            byte[] zeile = Arrays.copyOfRange(reinesDatensegment, i * bytePerZeile,
                    (i + 1) * bytePerZeile);
            // Umwandlung in einfaches RLE
            byte[] RLEKonvertierteZeile = convertLineToRLE(zeile);
            try {// Umwandlung in das RLE-Format des TGA-Formats
                bAOS.write(convertToTGARLE(RLEKonvertierteZeile, convspec));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bAOS.toByteArray();
    }


    /**
     * Methode, welches ein ByteArray, welches das ByteArray in einfaches LRE umwandelt.
     *
     * @param datasegment das zu konvertierende Bild, bzw. vollständige Zeile(n)
     *
     * @return konvertiertes ByteArray
     */
    public static byte[] convertLineToRLE(byte[] datasegment) {
        byte[] byteArray = new byte[datasegment.length * 2];// das byteArray hat die doppelte Größe, da eine Zeile
        // in LRE größer sein könnte, als in eine "reglüäre" Zeile hat.
        int byteArrayIndex = 0;
        // Initialisieren
        if (datasegment.length > 1) {
            byteArray[byteArrayIndex] = 1;
        }
        else {// der Sonderfall, dass das Bild nur 1 Pixel breit ist.
            byteArray[byteArrayIndex] = 0;
        }
        byteArray[byteArrayIndex + 1] = datasegment[0];
        byteArray[byteArrayIndex + 2] = datasegment[1];
        byteArray[byteArrayIndex + 3] = datasegment[2];
        // Erstellen der RLE Zeile
        for (int i = 0; i < datasegment.length - 3; i += 3) {

            // Vergleiche das den x-ten Bildpunkt mit dem darauffolgenden
            if (datasegment[i] == datasegment[i + 3]
                    && datasegment[i + 1] == datasegment[i + 4]
                    && datasegment[i + 2] == datasegment[i + 5]
                    && (byteArray[byteArrayIndex] != (byte) 0x80) //
            ) {// LRE Segment
                byteArray[byteArrayIndex] += 1;
            } else {//Raw Segment
                byteArray[byteArrayIndex + 5] = datasegment[i + 3];
                byteArray[byteArrayIndex + 6] = datasegment[i + 4];
                byteArray[byteArrayIndex + 7] = datasegment[i + 5];
                byteArray[byteArrayIndex + 4] = 1;
                byteArrayIndex += 4;
            }
        }
        byte[] compactedArray = Arrays.copyOfRange(byteArray, 0,
                byteArrayIndex + 4);
        return compactedArray;

    }


    /**
     * Konvertierung eines LRE-komprimierten ByteArrays/Datensegments in ein unkomprimiertes ByteArray/Datensegment
     *
     * @param convspec Objekt, welches die Informationen über die durchzuführende Operation enthält
     * @return unkomprimiertes ByteArray.
     */
    public static byte[] convertRLEtoUncompressed(ConversionSpecs convspec) {
        // TODO Auto-generated method stub
        FileTypeSuper inputheader = convspec.getInputformat();
        byte[] datasegment = convspec.getInputDatasegments();

        int pixelanzahl = inputheader.getWidth() * inputheader.getHeight();
        int length = pixelanzahl * 3;
        int[] offsets = convspec.offsetCalculator(convspec);

        byte[] byteArray = new byte[length];
        ByteBuffer bb = ByteBuffer.wrap(byteArray);
        int laeufer;
        // konvertieren

        int i = 0;
        int n = 0;


        while (n < pixelanzahl) {
            laeufer = datasegment[i]; // das 8.Bit
            if ((laeufer & 0x80) == 0x80) {// LRE Segment
                laeufer &= 0x7f;
                // Pixel einzeln in ByteBuffer schreiben.
                for (int j = 0; j <= laeufer; j++) {
                    bb.put(datasegment[i + 1 + offsets[0]]);
                    bb.put(datasegment[i + 2 + offsets[1]]);
                    bb.put(datasegment[i + 3 + offsets[2]]);
                }
                i += 4;
            } else { // RAW Segment
                laeufer &= 0x7f;
                for (int j = 0; j <= laeufer; j++) {
                    bb.put(datasegment[i + 1 +offsets[0]]);
                    bb.put(datasegment[i + 2 + offsets[1]]);
                    bb.put(datasegment[i + 3 +offsets[2]]);
                    i += 3;

                }
                i++;
            }

            n += laeufer + 1;
        }

        return byteArray = bb.array();

    }

    /**
     * Konvertiert ein Datensegment/ByteArray, welches sich in einfacher LRE befindet in die, in der TGA-Spezifikation
     * angebenen Form.
     *(Siehe: http://www.dca.fee.unicamp.br/~martino/disciplinas/ea978/tgaffs.pdf)
     * Es können nur vielfache vollständiger Zeilen bearbeitet werden.
     *
     * @param bildZeilenBytes ByteArray von mindestens einer vollständigen Zeile
     * @param convspec Objekt, welches die Informationen über die durchzuführende Operation enthält
     * @return
     */
    private static byte[] convertToTGARLE(byte[] bildZeilenBytes,
                                          ConversionSpecs convspec) {
        //Initialisierung
        int[] offsets = convspec.offsetCalculator(convspec);
        byte[] arbeitsArray = new byte[bildZeilenBytes.length * 2];
        int byteArrayCounter = 0;
        int byteArrayIndex = 0;

        //Kovertierung der Zeile
        for (int i = 0; i < bildZeilenBytes.length; i += 4) {
            // neues LRE Segment
            if (bildZeilenBytes[i] >= 2 | bildZeilenBytes[i] == -128) {
                byteArrayCounter = byteArrayIndex;
                int temp = (Byte.toUnsignedInt(bildZeilenBytes[i]));
                arbeitsArray[byteArrayIndex] = (byte) (Byte
                        .toUnsignedInt(bildZeilenBytes[i]) - 1);
                arbeitsArray[byteArrayIndex + 1] = bildZeilenBytes[i + 1 + offsets[0]];
                arbeitsArray[byteArrayIndex + 2] = bildZeilenBytes[i + 2 + offsets[1]];
                arbeitsArray[byteArrayIndex + 3] = bildZeilenBytes[i + 3 + offsets[2]];
                arbeitsArray[byteArrayIndex] |= 0x80;
                byteArrayIndex += 4;
                continue;
            }
            // neues RAW Segment.
            if (bildZeilenBytes[i] == 1 && (arbeitsArray[byteArrayCounter] == 127
                    || arbeitsArray[byteArrayCounter] < 0) || i == 0) {

                byteArrayCounter = byteArrayIndex;
                arbeitsArray[byteArrayCounter] = 0;
                arbeitsArray[++byteArrayIndex] = bildZeilenBytes[i + 1 + offsets[0]];
                arbeitsArray[++byteArrayIndex] = bildZeilenBytes[i + 2 + offsets[1]];
                arbeitsArray[++byteArrayIndex] = bildZeilenBytes[i + 3 + offsets[2]];
                ++byteArrayIndex;
                continue;
            }
            // LRE Segment fortführen
            if (bildZeilenBytes[i] == 1 && (-1 < arbeitsArray[byteArrayCounter]
                    && arbeitsArray[byteArrayCounter] < 127)) {
                arbeitsArray[byteArrayIndex++] = bildZeilenBytes[i + 1 + offsets[0]];
                arbeitsArray[byteArrayIndex++] = bildZeilenBytes[i + 2 + offsets[1]];
                arbeitsArray[byteArrayIndex++] = bildZeilenBytes[i + 3 + offsets[2]];
                arbeitsArray[byteArrayCounter]++;
                continue;
            }
        }
        return Arrays.copyOfRange(arbeitsArray, 0, byteArrayIndex);
    }



}
