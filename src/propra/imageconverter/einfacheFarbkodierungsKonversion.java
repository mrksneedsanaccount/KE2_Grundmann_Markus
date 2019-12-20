package src.propra.imageconverter;


import src.helperclasses.ConversionSpecs;

public class einfacheFarbkodierungsKonversion {


    /**
     * Diese Klasse enthält, die Algorithmen für die "einfache" Konvertiertung
     * zwischen Datensegmenten, welche die gleiche Konviertierung benutzen, aber
     * unterschiedliche Farbkodierungen.
      */
    public einfacheFarbkodierungsKonversion() {


    }

    /**
     * Methode zur Konvertierung von Datensegmenten, die den gleichen Konvertierungsalgorithmus benuten,
     * aber unterschiedliche Farbkodierungen (bei gleicher Anzahl von Bytes pro Pixel und gleicher Farbmenge (RBG))
     *
     * @param convspec Hilfsobjekt, welches die für die Operation relevanten Daten enthält.
     * @return konvertiertes ByteArray
     */
    public static byte[] convertColourscheme(ConversionSpecs convspec) {
        //Initialisierung
        int[] offsets = convspec.offsetCalculator(convspec);
        byte[] datasegment = convspec.getInputDatasegments();
        byte[] outputarray = new byte[datasegment.length];

        if (convspec.getOperation() == ImageConverter.UNCOMPRESSED) {
            for (int i = 0; i < (outputarray.length); i += 3) {
                outputarray[i] = datasegment[i + offsets[0]];
                outputarray[i + 1] = datasegment[i + offsets[1] + 1];
                outputarray[i + 2] = datasegment[i + offsets[2] + 2];
            }
        }
        if (convspec.getOperation() == ImageConverter.RLE) {
            int i = 0;
            while (i < (datasegment.length - 3)) {
                if (datasegment[i] < 0) {
                    outputarray[i] = datasegment[i];
                    i++;
                    outputarray[i] = datasegment[i + offsets[0]];
                    outputarray[i + 1] = datasegment[i + offsets[1] + 1];
                    outputarray[i + 2] = datasegment[i + offsets[2] + 2];
                    i += 3;
                    continue;
                }
                if (datasegment[i] > -1) {
                    outputarray[i] = datasegment[i];
                    int laeufer = (datasegment[i] + 1);
                    i++;
                    // jedes Pixel 3 byte lang und +1 (siehe TARGA-Spezifikation)
                    for (int j = 0; j < laeufer; j++) {
                        outputarray[i] = datasegment[i + offsets[0]];
                        outputarray[i + 1] = datasegment[i + offsets[1] + 1];
                        outputarray[i + 2] = datasegment[i + offsets[2] + 2];
                        i += 3;
                    }
                }

            }

        }

        return outputarray;

    }

}
