package src.propra.imageconverter;

import src.filetypes.FileTypeSuper;
import src.helperclasses.Pixel;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
//    public static byte[] LREconversionMethod(ConversionSpecs convspec) {
//        // Initialisierung
//        FileTypeSuper inputfile = convspec.getInputFile();
//        FileTypeSuper outputfile = convspec.getOutputfile();
//        int bytePerZeile = inputfile.getWidth() * 3; // bytes per Zeile
//        int height = outputfile.getHeight();
//        ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
//
//        byte[] reinesDatensegment = Arrays.copyOfRange(convspec.getInputDatasegments(), 0,
//                bytePerZeile * height);
//        //Die Bilddaten werden Zeile für Zeile abgearbeitet.
//        for (int i = 0; i < height; i++) {
//            byte[] zeile = Arrays.copyOfRange(reinesDatensegment, i * bytePerZeile,
//                    (i + 1) * bytePerZeile);
//            // Umwandlung in einfaches RLE
//            byte[] RLEKonvertierteZeile = convertLineToRLE(zeile);
//            try {// Umwandlung in das RLE-Format des TGA-Formats
//                bAOS.write(convertToTGARLE(RLEKonvertierteZeile, convspec));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return bAOS.toByteArray();
//    }


    /**
     * Methode, welches ein ByteArray, welches das ByteArray in einfaches LRE umwandelt.
     *
     * @param datasegment das zu konvertierende Bild, bzw. vollständige Zeile(n)
     * @return konvertiertes ByteArray
     */
    public static byte[] convertLineToRLE(byte[] datasegment) {
        byte[] byteArray = new byte[datasegment.length * 2];// das byteArray hat die doppelte Größe, da eine Zeile
        // in LRE größer sein könnte, als in eine "reglüäre" Zeile hat.
        int byteArrayIndex = 0;
        // Initialisieren
        if (datasegment.length > 1) {
            byteArray[byteArrayIndex] = 1;
        } else {// der Sonderfall, dass das Bild nur 1 Pixel breit ist.
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


    static byte[] convertLineToRLEV2(byte[] pixels) throws IOException {


        byte[] pixelOneArray = new byte[3];
        byte[] pixelTwoArray = new byte[3];
        Mode mode = Mode.START;
        int rleCounter = 0;
        int rawCounter = 0;
        ByteArrayOutputStream rleBufferBAOS = new ByteArrayOutputStream();
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        ByteBuffer pixelBufferByteBuffer = ByteBuffer.wrap(pixels);

        int i = 0;
        while (pixelBufferByteBuffer.hasRemaining()) {

            // TODO 1 pixel sonderfall

            if (mode == Mode.START) {
                pixelBufferByteBuffer.get(pixelOneArray);
                pixelBufferByteBuffer.get(pixelTwoArray);
                mode = null;
            } else {
                System.arraycopy(pixelTwoArray, 0, pixelOneArray, 0, 3);
                pixelBufferByteBuffer.get(pixelTwoArray);
            }
            // Ein Neues Segment beginnt.
            if (Arrays.equals(pixelOneArray, pixelTwoArray) & mode == null) {
                mode = Mode.RLE;
                rleBufferBAOS.write(pixelOneArray);
                rleCounter++;
            } else if (!(Arrays.equals(pixelOneArray, pixelTwoArray)) & mode == null) {
                mode = Mode.RAW;
                rleBufferBAOS.write(pixelOneArray);
            }

            else if (Arrays.equals(pixelOneArray, pixelTwoArray) & mode == Mode.RLE) {
                rleCounter++;
                if (rleCounter == 127) {
                    arrayOutputStream.write(rleCounter | 0x80);
                    rleBufferBAOS.writeTo(arrayOutputStream);
                    rleBufferBAOS.reset();
                    rleCounter = -1;
                    mode = null;
                }
            } else if (!(Arrays.equals(pixelOneArray, pixelTwoArray)) & mode == Mode.RAW) {
                rawCounter++;
                rleBufferBAOS.write(pixelOneArray);
                if (127 == rawCounter) {
                    arrayOutputStream.write(rawCounter);
                    rleBufferBAOS.writeTo(arrayOutputStream);
                    rleBufferBAOS.reset();
                    rawCounter = 0;
                    mode = null;
                }
            } else if (!(Arrays.equals(pixelOneArray, pixelTwoArray)) & mode == Mode.RLE) {//Veränderung Entweder neues
                arrayOutputStream.write(rleCounter | 0x80);
                rleBufferBAOS.writeTo(arrayOutputStream);
                rleBufferBAOS.reset();
                rleCounter = 0;
                mode = null;
            } else if ((Arrays.equals(pixelOneArray, pixelTwoArray)) & mode == Mode.RAW) {//Ver#nderung
                arrayOutputStream.write(rawCounter);
                rleBufferBAOS.writeTo(arrayOutputStream);
                rleBufferBAOS.reset();
                rawCounter = 0;
//                mode = null;
                mode = Mode.RLE;
                rleBufferBAOS.write(pixelOneArray);
                rleCounter++;

            }
        }
        //letztes pixel
        if ((Arrays.equals(pixelOneArray, pixelTwoArray))) {//Veränderung
            arrayOutputStream.write(rleCounter | 0x80);
            rleBufferBAOS.writeTo(arrayOutputStream);
            rleBufferBAOS.reset();
        } else if (!(Arrays.equals(pixelOneArray, pixelTwoArray)) & mode == Mode.RAW) {//Ver#nderung
            rawCounter++;
            arrayOutputStream.write(rawCounter);
            rleBufferBAOS.writeTo(arrayOutputStream);
            arrayOutputStream.write(pixelTwoArray);
            System.out.println("Boop");
        }else if(!(Arrays.equals(pixelOneArray, pixelTwoArray)) & (mode == Mode.RLE | mode == null)){
            arrayOutputStream.write(rawCounter);
            arrayOutputStream.write(pixelTwoArray);
        }

        else {
            System.out.println("??");
        }

        return arrayOutputStream.toByteArray();
    }

//    public static void convertRLEtoUncompressed(Conversions convspec) throws IOException {
//
//        FileChannel fileChannel = new FileInputStream(convspec.getInputFile().getFilepath().toFile()).getChannel();
//        fileChannel.position(convspec.getInputFile().getHeader().length);
//        int width = convspec.getInputFile().getWidth();
//        int height = convspec.getInputFile().getHeight();
//        File outputfile = convspec.getOutputFile().getFilepath().toFile();
//        if (outputfile.exists()) {
//            outputfile.delete();
//        }
//        Set<StandardOpenOption> options = new HashSet<>();
//        options.add(StandardOpenOption.CREATE);
//        options.add(StandardOpenOption.APPEND);
//        Path path = Paths.get(outputfile.getAbsolutePath());
//        FileChannel outputChannel = FileChannel.open(path, options);
//        outputChannel.write(ByteBuffer.allocate(convspec.getOutputFile().getHeader().length));
//
//
//        ByteBuffer byteBuffer = ByteBuffer.allocate(64 * 1024);
//
//        int limit;
//        Mode mode = null;
//        byte singleByteBuffer = 0;
//        int counter = 0;
//
//
//        byte[] pixel = new byte[3];
//        ByteArrayOutputStream inputPixelStream = new ByteArrayOutputStream();
//        byte[] pixels;
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//
//        while ((limit = fileChannel.read(byteBuffer)) > -1) {
//            byteBuffer.flip();
//
//
//
//
//            while (true) { //da damit garanitiwer wird byteBuffer.position() < limit
//                if (mode == null) {
//                    singleByteBuffer = byteBuffer.get();
//                    convspec.getInputFile().calculateChecksum(singleByteBuffer);
//                    if (singleByteBuffer < 0) {
//                        counter = (singleByteBuffer & 0x7f) + 1;
//                        mode = Mode.RLE;
//                    } else {
//                        counter = singleByteBuffer + 1;
//                        mode = Mode.RAW;
//                    }
//                }
//                if (byteBuffer.remaining() >= counter*3 && mode == Mode.RAW){
//                    for (int i = 0; i < counter; i++) {
//                        byteBuffer.get(pixel);
//                        convspec.getInputFile().calculateChecksumOfArray(pixel);
//                        pixel = Pixel.changePixelOrder(pixel, offsets);
//                        inputPixelStream.write(pixel);
//                    }
//
//                } else if (byteBuffer.remaining() >= 3 && mode == Mode.RLE) {
//                    byteBuffer.get(pixel);
//                    convspec.getInputFile().calculateChecksumOfArray(pixel);
//                    pixel = Pixel.changePixelOrder(pixel, offsets);
//                    inputPixelStream.write(pixel);
//                }else{
//
//                    break;
//                }
//                pixels = getRLEPacket(inputPixelStream.toByteArray(), mode, counter, convspec.getInputFile());
//                inputPixelStream.reset();
//                convspec.getOutputFile().calculateChecksumOfArray(pixels);
//                byteArrayOutputStream.write(pixels);
//                mode = null;
//
//                if (byteBuffer.remaining() < 1) {
//                    break;
//                }
//
//            }
//            Files.write(convspec.getOutputFile().getFilepath(), byteArrayOutputStream.toByteArray(), StandardOpenOption.APPEND);
//            byteArrayOutputStream.reset();
//            byteBuffer.compact();
//
//        }
//        System.out.println("TEST");
//        }
//
//
//    private static byte[] getRLEPacket(byte[] bytes, Mode mode, int counter, FileTypeSuper outpufFile) throws IOException {
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        byte[] pixel = new byte[3];
//
//        if (mode == Mode.RLE) {
//
//            for (int i = 0; i < counter; i++) {
//                byteArrayOutputStream.write(bytes);
//            }
//        } else if (mode == Mode.RAW) {
//
//            byteArrayOutputStream.write(bytes);
//        }
//        return byteArrayOutputStream.toByteArray();
//    }





    /**
     * Konvertierung eines LRE-komprimierten ByteArrays/Datensegments in ein unkomprimiertes ByteArray/Datensegment
     *
     * @param convspec Objekt, welches die Informationen über die durchzuführende Operation enthält
     * @return unkomprimiertes ByteArray.
     */
//    public static byte[] convertRLEtoUncompressed2(ConversionSpecs convspec) {
//        // TODO Auto-generated method stub
//        FileTypeSuper inputheader = convspec.getInputFile();
//        byte[] datasegment = convspec.getInputDatasegments();
//
//        int pixelanzahl = inputheader.getWidth() * inputheader.getHeight();
//        int length = pixelanzahl * 3;
//        int[] offsets = convspec.offsetCalculator(convspec);
//
//        byte[] byteArray = new byte[length];
//        ByteBuffer bb = ByteBuffer.wrap(byteArray);
//        int laeufer;
//        // konvertieren
//
//        int i = 0;
//        int n = 0;
//
//
//        while (n < pixelanzahl) {
//            laeufer = datasegment[i]; // das 8.Bit
//            if ((laeufer & 0x80) == 0x80) {// LRE Segment
//                laeufer &= 0x7f;
//                // Pixel einzeln in ByteBuffer schreiben.
//                for (int j = 0; j <= laeufer; j++) {
//                    bb.put(datasegment[i + 1 + offsets[0]]);
//                    bb.put(datasegment[i + 2 + offsets[1]]);
//                    bb.put(datasegment[i + 3 + offsets[2]]);
//                }
//                i += 4;
//            } else { // RAW Segment
//                laeufer &= 0x7f;
//                for (int j = 0; j <= laeufer; j++) {
//                    bb.put(datasegment[i + 1 + offsets[0]]);
//                    bb.put(datasegment[i + 2 + offsets[1]]);
//                    bb.put(datasegment[i + 3 + offsets[2]]);
//                    i += 3;
//
//                }
//                i++;
//            }
//
//            n += laeufer + 1;
//        }
//
//        return byteArray = bb.array();
//
//    }

//    /**
//     * Konvertiert ein Datensegment/ByteArray, welches sich in einfacher LRE befindet in die, in der TGA-Spezifikation
//     * angebenen Form.
//     * (Siehe: http://www.dca.fee.unicamp.br/~martino/disciplinas/ea978/tgaffs.pdf)
//     * Es können nur vielfache vollständiger Zeilen bearbeitet werden.
//     *
//     * @param bildZeilenBytes ByteArray von mindestens einer vollständigen Zeile
//     * @param convspec        Objekt, welches die Informationen über die durchzuführende Operation enthält
//     * @return
//     */
//    private static byte[] convertToTGARLE(byte[] bildZeilenBytes,
//                                          ConversionSpecs convspec) {
//        //Initialisierung
//        int[] offsets = convspec.offsetCalculator(convspec);
//        byte[] arbeitsArray = new byte[bildZeilenBytes.length * 2];
//        int byteArrayCounter = 0;
//        int byteArrayIndex = 0;
//
//        //Kovertierung der Zeile
//        for (int i = 0; i < bildZeilenBytes.length; i += 4) {
//            // neues LRE Segment
//            if (bildZeilenBytes[i] >= 2 | bildZeilenBytes[i] == -128) {
//                byteArrayCounter = byteArrayIndex;
//                int temp = (Byte.toUnsignedInt(bildZeilenBytes[i]));
//                arbeitsArray[byteArrayIndex] = (byte) (Byte
//                        .toUnsignedInt(bildZeilenBytes[i]) - 1);
//                arbeitsArray[byteArrayIndex + 1] = bildZeilenBytes[i + 1 + offsets[0]];
//                arbeitsArray[byteArrayIndex + 2] = bildZeilenBytes[i + 2 + offsets[1]];
//                arbeitsArray[byteArrayIndex + 3] = bildZeilenBytes[i + 3 + offsets[2]];
//                arbeitsArray[byteArrayIndex] |= 0x80;
//                byteArrayIndex += 4;
//                continue;
//            }
//            // neues RAW Segment.
//            if (bildZeilenBytes[i] == 1 && (arbeitsArray[byteArrayCounter] == 127
//                    || arbeitsArray[byteArrayCounter] < 0) || i == 0) {
//
//                byteArrayCounter = byteArrayIndex;
//                arbeitsArray[byteArrayCounter] = 0;
//                arbeitsArray[++byteArrayIndex] = bildZeilenBytes[i + 1 + offsets[0]];
//                arbeitsArray[++byteArrayIndex] = bildZeilenBytes[i + 2 + offsets[1]];
//                arbeitsArray[++byteArrayIndex] = bildZeilenBytes[i + 3 + offsets[2]];
//                ++byteArrayIndex;
//                continue;
//            }
//            // LRE Segment fortführen
//            if (bildZeilenBytes[i] == 1 && (-1 < arbeitsArray[byteArrayCounter]
//                    && arbeitsArray[byteArrayCounter] < 127)) {
//                arbeitsArray[byteArrayIndex++] = bildZeilenBytes[i + 1 + offsets[0]];
//                arbeitsArray[byteArrayIndex++] = bildZeilenBytes[i + 2 + offsets[1]];
//                arbeitsArray[byteArrayIndex++] = bildZeilenBytes[i + 3 + offsets[2]];
//                arbeitsArray[byteArrayCounter]++;
//                continue;
//            }
//        }
//        return Arrays.copyOfRange(arbeitsArray, 0, byteArrayIndex);
//    }
//
//    public static void uncompressedToCompressed(ConversionSpecs convspec) throws IOException {
//
//
//        FileChannel fileChannel = new FileInputStream(convspec.getInputPath().toFile()).getChannel();
//        fileChannel.position(convspec.getInputFile().getHeader().length);
//        int width = convspec.getInputFile().getWidth();
//        int height = convspec.getInputFile().getHeight();
//        File outputfile = convspec.getOutputPath().toFile();
//        if (outputfile.exists()) {
//            outputfile.delete();
//        }
//        Set<StandardOpenOption> options = new HashSet<>();
//        options.add(StandardOpenOption.CREATE);
//        options.add(StandardOpenOption.APPEND);
//        Path path = Paths.get(outputfile.getAbsolutePath());
//        FileChannel outputChannel = FileChannel.open(path, options);
//        outputChannel.write(ByteBuffer.allocate(convspec.getOutputfile().getHeader().length));
//
//
//        int limit;
//        int counter = 0;
//        byte[] pixel = new byte[3];
//        byte[] outputPixel = new byte[3];
//        byte[][] pixels = new byte[width][3];
//        int[] offsets = convspec.getOffsets();
//
//        ByteBuffer byteBuffer = ByteBuffer.allocate(64 * 1024*16);
//        ByteBuffer lineBuffer = ByteBuffer.allocate(width * 3);
//
//        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteBuffer.array());
//        ReadableByteChannel readableByteChannel = Channels.newChannel(byteArrayInputStream);
//
//        ByteArrayOutputStream rleByteArrayOutputStream = new ByteArrayOutputStream();
//
//        while ((limit = fileChannel.read(byteBuffer)) > -1) {
//
//            byteBuffer.rewind();
//            byteArrayInputStream.reset();
//
//            int limit2 = 0;
//            while ((limit2 = readableByteChannel.read(lineBuffer)) > -1) {
//                if (limit2 != width * 3) {
//                    byteBuffer.rewind();
//                    fileChannel.read(byteBuffer);
//                    byteBuffer.rewind();
//                    byteArrayInputStream.reset();
//                    readableByteChannel.read(lineBuffer);
//                }
//                byte[] lineArray = lineBuffer.array();
//                lineBuffer.rewind();
//                for (int i = 0; i < width; i++) {
//                    //Inoutpixel
//                    Pixel.getPixelFromBuffer(lineBuffer, pixels[i]);
//                    convspec.getInputFile().calculateChecksumOfArray( pixels[i]);
//                    //Outputpixel
//                    pixels[i] = Pixel.changePixelOrder( pixels[i], offsets);
//
//                }
//                rleByteArrayOutputStream.write(convertLineToRLE(pixels));
//                convspec.getOutputfile().calculateChecksumOfArray(rleByteArrayOutputStream.toByteArray());
//                Files.write(convspec.getOutputPath(), rleByteArrayOutputStream.toByteArray(), StandardOpenOption.APPEND);
//                rleByteArrayOutputStream.reset();
//
//                lineBuffer.clear();
//                counter++;
//                if (counter == height) {
//                    break;
//                }
//
//
//            }
//
//
//        }
//
//    }
//
//
//    private static byte[] convertLineToRLE(byte[][] pixels) throws IOException {
//
//        byte[] pixelOneArray = new byte[3];
//        byte[] pixelTwoArray = new byte[3];
//        Mode mode = Mode.START;
//        int rleCounter = 0;
//        int rawCounter = 0;
//        ByteArrayOutputStream rleBufferBAOS = new ByteArrayOutputStream();
//        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
//
//        int i = 0;
//        while (i < pixels.length) {
//            // TODO 1 pixel sonderfall
//            if (mode == Mode.START) {
//                pixelOneArray = pixels[i++];
//                pixelTwoArray = pixels[i++];
//                mode = null;
//            } else {
//                pixelOneArray = pixelTwoArray;
//                pixelTwoArray = pixels[i++];
//            }
//            // Ein Neues Segment beginnt.
//            if (Arrays.equals(pixelOneArray, pixelTwoArray) & mode == null) {
//                mode = Mode.RLE;
//                rleBufferBAOS.write(pixelOneArray);
//                rleCounter++;
//            } else if (!(Arrays.equals(pixelOneArray, pixelTwoArray)) & mode == null) {
//                mode = Mode.RAW;
//                rleBufferBAOS.write(pixelOneArray);
//            } else if (Arrays.equals(pixelOneArray, pixelTwoArray) & mode == Mode.RLE) {
//                rleCounter++;
//                if (rleCounter == 127) {
//                    arrayOutputStream.write(rleCounter | 0x80);
//                    rleBufferBAOS.writeTo(arrayOutputStream);
//                    rleBufferBAOS.reset();
//                    rleCounter = -1;
//                    mode = null;
//                }
//            } else if (!(Arrays.equals(pixelOneArray, pixelTwoArray)) & mode == Mode.RAW) {
//                rawCounter++;
//                rleBufferBAOS.write(pixelOneArray);
//                if (127 == rawCounter) {
//                    arrayOutputStream.write(rawCounter);
//                    rleBufferBAOS.writeTo(arrayOutputStream);
//                    rleBufferBAOS.reset();
//                    rawCounter = 0;
//                    mode = null;
//                }
//            } else if (!(Arrays.equals(pixelOneArray, pixelTwoArray)) & mode == Mode.RLE) {//Veränderung Entweder neues
//                arrayOutputStream.write(rleCounter | 0x80);
//                rleBufferBAOS.writeTo(arrayOutputStream);
//                rleBufferBAOS.reset();
//                rleCounter = 0;
//                mode = null;
//            } else if ((Arrays.equals(pixelOneArray, pixelTwoArray)) & mode == Mode.RAW) {//Ver#nderung
//                arrayOutputStream.write(rawCounter);
//                rleBufferBAOS.writeTo(arrayOutputStream);
//                rleBufferBAOS.reset();
//                rawCounter = 0;
//                mode = Mode.RLE;
//                rleBufferBAOS.write(pixelOneArray);
//                rleCounter++;
//            }
//        }
//        //letztes pixel
//        if ((Arrays.equals(pixelOneArray, pixelTwoArray))) {//Veränderung
//            arrayOutputStream.write(rleCounter | 0x80);
//            rleBufferBAOS.writeTo(arrayOutputStream);
//            rleBufferBAOS.reset();
//        } else if (!(Arrays.equals(pixelOneArray, pixelTwoArray)) & mode == Mode.RAW) {//Ver#nderung
//            rawCounter++;
//            arrayOutputStream.write(rawCounter);
//            rleBufferBAOS.writeTo(arrayOutputStream);
//            arrayOutputStream.write(pixelTwoArray);
//
//        } else if (!(Arrays.equals(pixelOneArray, pixelTwoArray)) & (mode == Mode.RLE | mode == null)) {
//            arrayOutputStream.write(rawCounter);
//            arrayOutputStream.write(pixelTwoArray);
//        } else {
//            System.out.println("??");
//        }
//
//        return arrayOutputStream.toByteArray();
//    }

    enum Mode {
        RAW, RLE, START

    }
}