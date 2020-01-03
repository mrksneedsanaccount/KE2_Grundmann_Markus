package src.Test;


import src.filetypes.ProPra;
import src.filetypes.TGA;
import src.helperclasses.ProjectConstants;
import src.helperclasses.Huffman;
import src.propra.imageconverter.BaseNConverter;
import src.propra.imageconverter.RLEConverter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

/**
 * Hauptklasse der Konversion
 */
public class ImageConverterq {


    public static void main(String[] args) {
        // Initialisieren von Lokalen Variablen
        boolean inputbool = false;
        boolean outputbool = false;
        boolean compressionbool = false;
        boolean encodebool = false;
        boolean decodebool = false;
        String input;
        String output;
        //ConversionSpecs ist die Klasse, in welche die für die Konversion relelvanten Daten gespeichert werden
        // Output vor Input funktioniert nicht, aber mir ging die Zeit aus um es zu reparieren.
        ConversionSpecs convspec = new ConversionSpecs();
        // --input- usw.
        String prefix = "0";
        // Format der Eingabe für alle Betriebssysteme konform machen.
        for (String arg : args) {
            arg = arg.replaceAll("\\\\", "/");
            arg.toLowerCase();
        }

        // Einlesen der Argumente, Initialisieren des ConversionSpecs-Objekts und der daran gekoppelten Klassen.
        // Die Reihenfolge der Eingaben sollte egal sein.
        for (String arg : args) {
            String[] moeglichePrefixe = {ProjectConstants.INPUT, ProjectConstants.OUTPUT, ProjectConstants.COMPRESSION, ProjectConstants.DECODE,
                    ProjectConstants.ENCODE};
            for (String s : moeglichePrefixe) {
                if (arg.startsWith(s)) {
                    prefix = s;
                }
            }
            switch (prefix) {
                case ProjectConstants.INPUT:
                    // finde Input und überprüfe das Dateiformat;
                    if (!inputbool) {
                        inputbool = true;
                        String filetype;
                        input = arg.substring(prefix.length());
                        boolean temp = Files.exists(Paths.get(input));
                        if (!temp) {
                            System.err.println(
                                    "Dateipfad, oder Datei existieren nicht.");
                            System.exit(123);
                        }

                        convspec.setInputPath(Paths.get(input));
                        if (arg.contains(".")) {
                            filetype = arg
                                    .substring(arg.lastIndexOf("."));
                            if (!Arrays.stream(ProjectConstants.FILETYPES)
                                    .anyMatch(filetype::equals) // https://stackoverflow.com/questions/1128723/how-do-i-determine-whether-an-array-contains-a-particular-value-in-java
                            ) {
                                System.err.println("unsupported filetype");
                                System.exit(123);
                            } // ConversionSpecs Dateiformat festlegen
                            switch (filetype) {
                                case ProjectConstants.TGA:
                                    convspec.setInputFormatString(ProjectConstants.TGA);
                                    convspec.setInputformat(new TGA(convspec));
                                    break;
                                case ProjectConstants.PROPRA:
                                    convspec.setInputFormatString(ProjectConstants.PROPRA);
                                    convspec.setInputformat(new ProPra(convspec));
                                    break;
                                case ProjectConstants.BASE32:
                                    convspec.setInputFormatString(filetype);
                                    convspec.setInputformat(new BaseN(convspec));
                                    break;
                                case ProjectConstants.BASEN:
                                    convspec.setInputFormatString(filetype);
                                    convspec.setInputformat(new BaseN(convspec));
                                    break;
                                default:
                            }
                        }
                    } else {
                        System.err.println("Doppelter Input");
                        System.exit(123);
                    }

                    break;

                case ProjectConstants.OUTPUT:
                    if (!outputbool) {
                        outputbool = true;
                        String filetype;
                        output = arg.substring(prefix.length());
                        // Prüft, ob das Elternverzeichnis existiert.
                        boolean temp = new File(output).getParentFile().exists();
                        if (!temp) {
                            System.err.println(
                                    "Das Ausgabeverzeichnis existiert nicht.");
                            System.exit(123);
                        }
                        convspec.setOutputPath(Paths.get(output));
                        if (arg.contains(".")) {
                            filetype = arg
                                    .substring(arg.lastIndexOf("."));

                            if (!Arrays.stream(ProjectConstants.FILETYPES)
                                    .anyMatch(filetype::equals) // https://stackoverflow.com/questions/1128723/how-do-i-determine-whether-an-array-contains-a-particular-value-in-java
                            ) {
                                System.err.println("unsupported filetype");
                                System.exit(123);
                            } // ConversionSpecs Dateiformat festlegen
                            switch (filetype) {
                                case ProjectConstants.TGA:
                                    convspec.setOutputFormatString(ProjectConstants.TGA);
                                    convspec.setOutputformat(new TGA(convspec,
                                            convspec.getInputformat()));
                                    break;
                                case ProjectConstants.PROPRA:
                                    convspec.setOutputFormatString(ProjectConstants.PROPRA);
                                    convspec.setOutputformat(new ProPra(convspec,
                                            convspec.getInputformat()));
                                    break;
                                default:
                            }
                        }
                    } else {
                        System.err.println("Doppelter output");
                        System.exit(123);
                    }
                    break;
                case ProjectConstants.COMPRESSION:
                    if (!compressionbool)
                        compressionbool = true;
                    String compressiontype = arg.substring(prefix.length());
                    if (compressiontype.equals(ProjectConstants.RLE)) {
                        convspec.setOperation(ProjectConstants.RLE);
                    } else if (compressiontype.equals(ProjectConstants.UNCOMPRESSED)) {
                        convspec.setOperation(ProjectConstants.UNCOMPRESSED);
                    } else if (compressiontype.equals(ProjectConstants.HUFFMAN)) {
                        convspec.setOperation(ProjectConstants.HUFFMAN);
                    } else {
                        System.err.println("Unbekannte Kompression");
                        System.exit(123);
                    }
                    break;

                case ProjectConstants.DECODE:
                    if (!decodebool)
                        decodebool = true;

                    break;

                case ProjectConstants.ENCODE:
                    if (!encodebool) {
                        encodebool = true;
                        String encodebase = arg;
                        String base = arg.substring(arg.lastIndexOf("--encode-") + 9);

                        //Legt Ausgabepfad
                        if (base.equals(ProjectConstants.BASE32TYP)) {
                            convspec.setAlphabetBaseN(ProjectConstants.BASE32HEX);
                            convspec.setOutputformat(new BaseN(ProjectConstants.BASE32HEX));
                            convspec.setOperation(ProjectConstants.BASE32HEX);
                            convspec.setOutputFormatString(ProjectConstants.BASE32TYP);
                        } else {// falls es keine base-32-Datei ist.
                            convspec.setOutputFormatString(ProjectConstants.BASENTYP);
                            String basenAlphabet = arg.substring(arg.indexOf('=') + 1);
                            convspec.setAlphabetBaseN(basenAlphabet);
                            convspec.setOutputformat(new BaseN(basenAlphabet));
                            int basenAnzahl = basenAlphabet.length();
                            // prüft , ob es sich um eine legale Eingabe für die Kodierung handelt.
                            if (basenAnzahl <= basenAnzahl && basenAlphabet.length() == basenAnzahl && (Math.log(basenAnzahl) / Math.log(2)) % 1 == 0) {
                            } else {
                                System.err.println("Unbekannte, oder fehlerhafte Base-N Kodierungsanweisung");
                                System.exit(123);
                            }
                        }
                        String dateiname = convspec.getInputPath().toString() + "." + convspec.getOutputFormatString();

                        convspec.setOutputPath(Paths.get(dateiname));
                    }
                    break;

                default:
                    System.err.println("Unbekanntes Prefix");
                    System.exit(123);

            }

        }


        // Ab hier beginnen die Konvertierungen.
        // Ich hätte diesen Teil gerne in Methoden, bzw. ihre eigenen Klassen verpackt, aber ich hatte keine Zeit mehr.
        // Außerdem hätte ich es sehr gerne ordentlicher gestaltet, ich bin mir auch sicher, dass ich einige Dinge
        //unnötiger Weise mache, was verwirrend sein kann. Aber ich hatte keine Zeit mehr.

        //Beginn des Einlesen der Eingabedatei.
        File inputFile = convspec.getInputPath().toFile();
        try {



            // Falls es sich um eine Konvertierung handelt,
            if (inputbool && outputbool && !decodebool && !encodebool) {

                //Vorarbeit: Initialisieren/Laden des Datensegments und das bestimmen der genutzen Operationen.
                byte[] datasegments = new byte[(int) convspec.getInputPath().toFile()
                        .length() - convspec.getInputformat().getHeader().length];
//                convspec.setInputDatasegments(datasegments);
                convspec.getOutputformat().setOperation(convspec);
//                convspec.setByteArrayOutputStream(bAOS);
                convspec.setOffsetsInConversionSpec();

                //Um das KE1 Format nutzen zu können muss noch die Kompression festgelegt werden.
                if (convspec.getOutputformat().getCompression() == null) {
                    convspec.getOutputformat().setCompression(convspec.getInputformat().getCompression());
                    convspec.setOperation(convspec.getInputformat().getCompression());
                }

                // Einlesen des Datensegments
                try {
//                    inputStream.skip(convspec.getInputformat().getHeader().length);
//                    inputStream.read(datasegments);
//                    inputStream.close();



                    // in welches Format konvertiert werden soll.
                    switch (convspec.getInputformat().getCompression()) {
                        case ProjectConstants.UNCOMPRESSED:
                            if (convspec.getOutputformat()
                                    .getCompression() == ProjectConstants.UNCOMPRESSED) {
                                einfacheFarbkodierungsKonversion.convertColourscheme2(convspec);

                            } else if (convspec.getOutputformat()
                                    .getCompression() == ProjectConstants.RLE) {
                                RLEConverter.uncompressedToCompressed(convspec);
                            } else if (convspec.getOperation() == ProjectConstants.HUFFMAN) {
                                Huffman.toHuffmanCompression2(convspec);
                            }
                            break;
                        case ProjectConstants.RLE:
                            if (convspec.getOperation() == ProjectConstants.UNCOMPRESSED) {
//
                                RLEConverter.convertRLEtoUncompressed(convspec);
                            } else if (convspec.getOperation() == ProjectConstants.RLE) {
                                einfacheFarbkodierungsKonversion.convertColourscheme2(convspec);
                            } else if (convspec.getOperation() == ProjectConstants.HUFFMAN) {
                                Huffman.toHuffmanCompression2(convspec);
                            }
                            break;
                        case ProjectConstants.HUFFMAN:
                            //TODO
                            Huffman.ProPraHuffmanToUncompressed2(convspec);
                            break;
                    }

                    // Fehlerüberprüfung
                    convspec.getInputformat().fehlerausgabe();

                    //Header Nachbearbeiten (Hinzufügen der Prüfsumme usw.)
                    convspec.getOutputformat()
                            .ausgabeHeaderFertigInitialisieren(convspec.getInputformat());

                    //Header zur Ausgabedatei hinzufügen.
                    RandomAccessFile outputStream;
                    try {
                        byte[] outputheader = convspec.getOutputformat()
                                .buildHeader(convspec.getInputformat());
                        outputStream = new RandomAccessFile(convspec.getOutputPath().toFile(), "rw");
                        outputStream.write(outputheader);
                        outputStream.close();
                        System.out.println("Fertig");
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }













            BufferedInputStream inputStream = new BufferedInputStream(
                    new FileInputStream(inputFile));
            ByteArrayOutputStream bAOS = new ByteArrayOutputStream();

            // Dekodieren
            if (inputbool && !outputbool && !compressionbool && !encodebool
                    && decodebool) {
                try {
                    // conversionspec fertig schreiben (Ausgabepfad
                    String dateiname = convspec.getInputPath().toString();
                    dateiname = dateiname.substring(0, dateiname.lastIndexOf("."));
                    convspec.setOutputPath(Paths.get(dateiname));

                    // Ausgabedatei erstellen
                    BaseN inputObjekt = (BaseN) convspec.getInputformat();
                    OutputStream outputStream;
                    outputStream = new FileOutputStream(
                            convspec.getOutputPath().toFile());
                    outputStream.close();
                    // Die Kodierung bestimmen (erst für BaseN, dann für Base-32
                    if (convspec.getInputFormatString().equals(ProjectConstants.BASEN)) {
                        try {
                            BufferedReader bufferedReader = new BufferedReader(new FileReader(convspec.getInputPath().toFile()));
                            inputObjekt.setAlphabet(bufferedReader.readLine());
                            inputObjekt.calculateBasenGroesse();
                            bufferedReader.close();
                        } catch (FileNotFoundException ex) {
                            ex.printStackTrace();
                        }
                    }
                    //Datensegment erstellen
                    byte[] inputarray = new byte[12000]; //Ein Puffer, das durch 1,2,3,4,5 und 6 teilbar ist.
                    int length = 0;
                    //falls, Base-32
                    if (!convspec.getInputFormatString().equals(ProjectConstants.BASE32)) {
                        inputStream.skip(inputObjekt.getAlphabet().length() + 1);
                    }
                    // Das eigentliche dekodieren beginnt jetzt
                    while ((length = inputStream.read(inputarray)) != -1) {
                        bAOS.reset(); // bAOS ist der ByteArrayOutputStream, der genutzt wird um die kodierten Daten zu empfangen.
                        bAOS.write(BaseNConverter.decodeBaseN(new String(inputarray, 0, length),
                                inputObjekt.getAlphabet(), inputObjekt.getBasenGroesse()));
                        Files.write(convspec.getOutputPath(), bAOS.toByteArray(), StandardOpenOption.APPEND);
                    }
                    bAOS.reset();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // // Kodieren
            if (inputbool && !outputbool && !compressionbool && encodebool
                    && !decodebool) {


                try {
                    // Initialisieren
                    BaseN outputObjekt = (BaseN) convspec.getOutputformat();
                    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(convspec.
                            getOutputPath().toFile()));
                    if (!outputObjekt.getAlphabet().equals(ProjectConstants.BASE32HEX)) {
                        bufferedWriter.write(outputObjekt.getAlphabet());
                        bufferedWriter.newLine();
                        bufferedWriter.close();
                    }

                    byte[] inputarray = new byte[12000]; //
                    int length = 0;
                    // Der eigentliche Leseprozess, (der Inputstream wurde oben vor der Konversionsmethode geöffnet.
                    // Das Lesen
                    while ((length = inputStream.read(inputarray)) != -1) {
                        bAOS.reset();
                        if (length == 120) {
                            bAOS.write(BaseNConverter.convertToBaseN(inputarray, outputObjekt.getAlphabet(),
                                    outputObjekt.getBasenGroesse()).getBytes());
                        } else {
                            bAOS.write(BaseNConverter.convertToBaseN(Arrays.copyOf(inputarray, length),
                                    outputObjekt.getAlphabet(), outputObjekt.getBasenGroesse()).getBytes());
                        }
                        Files.write(convspec.getOutputPath(), bAOS.toByteArray(), StandardOpenOption.APPEND);
                    }
                    bAOS.reset();
                    inputStream.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}