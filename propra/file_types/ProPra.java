package propra.file_types;

import propra.conversion_facilitators.CommandLineInterpreter;
import propra.helpers.HelperMethods;
import propra.helpers.ImageFormats;
import propra.helpers.ProjectConstants;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ProPra extends FileTypeSuper {
    // .propra
    // 0-9 Formatkennung
    // 10-11 Bildbreite
    // 12-13 Bildhöhe
    // 14 Bits pro Bildpunkt 24
    // 15 Kompressionstyp = 0
    // 16- 23 Datensegmentgröße (3*Breite*Höhe)
    // 24-27 Prüfsumme
    // 28-x Datensegmente
    // .tga
    // 0 Bild-ID
    // 1 Farbpalettentyp
    // 2 Bildtyp (hier 2)
    // 3-4 Palettenbeginn
    // 5-6 Palettenlänge
    // 7 Größe eines Paletteneintrags
    // 8-9 X-Koordinate
    // 10-11 Y-Koordinate
    // 12-13 Bildbreite
    // 14-15 Bildhöhe
    // 16 Bits pro Bildpunkt
    // 17 Bild-Attribut-Byte
    // 18 - x Bilddaten
    // (x+1)-y Rest


    private long a = 0;
    private long b = 1;
    private long i = 1;
    private String formatkennung = "ProPraWS19";
    private long datensegmentgroesse;
    private int checksum;
    private byte kompressionstyp;


    // Konstruktor für die Eingabedatei
    public ProPra(CommandLineInterpreter convspec) {
        super(convspec);
        imageFormats = ImageFormats.GBR;
        // TODO Auto-generated constructor stub
        datensegmentgroesse = headerbb.getLong(16);
        checksum = headerbb.getInt(24);
    }

    //Konstruktor für die Ausgabedatei
    public ProPra(CommandLineInterpreter convspec, FileTypeSuper inputformat) {
        // TODO Auto-generated constructor stub
        super(convspec, inputformat);
        imageFormats = ImageFormats.GBR;
        setCompressionOutputfile(convspec.getMode());


    }

    @Override
    public void ausgabeHeaderFertigInitialisieren(FileTypeSuper inputfile) {
        setCompressionOutputfile(super.compression);

    }

    @Override
    public byte[] buildHeader(FileTypeSuper inputfile) {
        // TODO Auto-generated method stub
        headerbb.put(formatkennung.getBytes());
        headerbb.putShort(10, width);
        headerbb.putShort(12, height);
        headerbb.put(14, bitsprobildpunkt);
        headerbb.put(15, kompressionstyp);
        headerbb.putLong(16, filepath.toFile().length() - getHeader().length);
        headerbb.putInt(24, returnChecksum());
        return headerbb.array();
    }

    public void calculateChecksum(byte dataByte) {
        a = (a + (i + (dataByte & 0xff))) % 65513;
        b = (b + a) % 65513;
        i++;

    }

    public void calculateChecksumOfArray(byte[] byteArray) {
        for (byte singleByte : byteArray) {
            calculateChecksum(singleByte);

        }

    }

    public void calculateChecksumOfByteBuffer(ByteBuffer byteBuffer, int limit) {
        int startingPos = byteBuffer.limit() - limit;
        int finalPos = byteBuffer.limit();
        byte[] pixelArray = byteBuffer.array();
        for (int j = startingPos; j < finalPos; j++) {
            calculateChecksum(pixelArray[j]);
        }

    }

    @Override
    public void fehlerausgabe() {
        // TODO Auto-generated method stub
        super.fehlerausgabe();
        // Dateigröße im Header überprüfen (Prüft, ob Daten fehlen, oder zu
        // viel vorhanden sind.
        if (kompressionstyp == 0) {
            if (height * width * 3 != filepath.toFile().length() - PROPRA_HEADER_OFFSET) {
                System.err.println(
                        "Zu wenige, oder zu viele Datensegmente vorhanden.");
                HelperMethods.exitProgramAfterError();
            }
        }
        // Datensegmentangabe mit Menge der Bilddaten in der Datei abgleichen

        if (headerbb.getLong(16) != filepath.toFile().length() - PROPRA_HEADER_OFFSET) {
            System.err.println(
                    "Datensegmentanzahl aus dem Header stimmt nicht mit den Anzahl an Datensegmenten in der Datei überein");
            HelperMethods.exitProgramAfterError();
        }

        // Prüfsumme zum Test berechnen
//        int cs1 = Checksum.calculateChecksumPropra(conversionspec.getInputDatasegments());
        int cs1 = returnChecksum();
        System.out.println("berechnete Prüfsumme: " + cs1);
        // Prüfsumme des Datei extrahieren.
        System.out.println("Prüfsumme: " + checksum);
        if (!(checksum == cs1)) {
            System.err.println("Angegebene Prüfsumme ist inkorrekt.");
            HelperMethods.exitProgramAfterError();
        }
        // Kompressionstyp
        if (!(header[15] == 0 || header[15] == 1 || header[15] == 2)) {
            System.err.println("falscher Kompressionstyp");
            HelperMethods.exitProgramAfterError();
        }
        // Testen, ob ProPraWS19 im Header steht
        String s = "ProPraWS19";
        byte[] header = new byte[10];
        System.arraycopy(headerbb.array(), 0, header, 0, 10);
        if (!(Arrays.equals(s.getBytes(StandardCharsets.UTF_8),
                header))) {
            System.err.println("Datei beginnt nicht mit ProPraWS19");
            HelperMethods.exitProgramAfterError();
        }
    }

    public byte[] getHeader() {
        return header;
    }

    int returnChecksum() {
        int temp = (int) Math.pow(2, 16);
        return (int) (a * temp + b);
    }

    @Override
    protected void setCompressionFromFile() {
        // TODO Auto-generated method stub
        kompressionstyp = headerbb.get(15);
        switch (kompressionstyp) {
            case 0:
                compression = ProjectConstants.UNCOMPRESSED;
                break;
            case 1:
                compression = ProjectConstants.RLE;
                break;
            case 2:
                compression = ProjectConstants.HUFFMAN;
                break;
        }

    }

    @Override
    protected void setHeightandWidth() {
        setWidth(headerbb.getShort(10));
        height = headerbb.getShort(12);

    }


    public void setCompressionOutputfile(String mode) {

        switch (mode) {
                case ProjectConstants.UNCOMPRESSED:
                    kompressionstyp = 0;
                    break;
                case ProjectConstants.RLE:
                    kompressionstyp = 1;
                    break;
                case ProjectConstants.HUFFMAN:
                    kompressionstyp = 2;
                    break;
            }
        }


}


