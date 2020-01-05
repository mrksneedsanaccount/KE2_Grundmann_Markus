package src.propra.file_types;

import src.propra.conversion_facilitators.CommandLineInterpreter;
import src.propra.helpers.ImageFormats;
import src.propra.helpers.ProjectConstants;

import java.nio.ByteBuffer;

/**
 * Klasse, welche die Metadaten von Objekten im TGA-Format enthält.
 */
public class TGA extends FileTypeSuper {


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


    private final byte bildID = 0;
    private final byte farbPallete = 0;
    private final short palettenBeginn = 0;
    private final short palettenLaenge = 0;
    private final byte groessePalleteneintrag = 0;
    short ycoord;
    private byte bildTyp;
    private short xcoord = -1;
    private byte bildAttributByte = 0x20;


    /**
     * inputfile Konstruktor
     *
     * @param convspec Objekt, welches die Informationen über die durchzuführende Operation enthält.
     */
    public TGA(CommandLineInterpreter convspec) {
        super(convspec);
        imageFormats = ImageFormats.BGR;
        if (headerbb.getShort(8) != 0 && headerbb.getShort(10) == height) {
            System.err.println("The origin of TGA file is not in the top left corner.");
            System.exit(123);
        }
    }


    /**
     * Outpfile Konstruktor
     *
     * @param convspec    Objekt, welches die Informationen über die durchzuführende Operation enthält.
     * @param inputformat Das Objekt, der Eingabedatei. (Zur Unterscheidung der Eingabe- und Ausgabedateiheader
     */
    public TGA(CommandLineInterpreter convspec, FileTypeSuper inputformat) {
        // TODO Auto-generated constructor stub
        super(convspec, inputformat);
        imageFormats = ImageFormats.BGR;
        short ycoord = getWidth();
        bildAttributByte = 0x20;
        setCompressionOutputfile(convspec.getMode());

    }

    @Override
    public void ausgabeHeaderFertigInitialisieren(FileTypeSuper inputfile) {
        // TODO Auto-generated method stub
        headerbb.put(2, bildTyp);
        headerbb.putShort(10, height);

        headerbb.putShort(12, width);
        headerbb.putShort(14, height);
        headerbb.put(16, bitsprobildpunkt);
        headerbb.put(17, bildAttributByte);


    }

    @Override
    public byte[] buildHeader(FileTypeSuper inputfile) {
        // TODO Auto-generated method stub


        switch (getCompression()) {
            case ProjectConstants.UNCOMPRESSED:
                bildTyp = 2;
                break;
            case ProjectConstants.RLE:
                bildTyp = 10;
                break;
        }
        headerbb.put(2, bildTyp);
        headerbb.putShort(10, inputfile.height);
        headerbb.putShort(12, inputfile.getWidth());
        headerbb.putShort(14, inputfile.height);
        headerbb.put(16, bitsprobildpunkt);
        headerbb.put(17, (byte) 0x20);
        return headerbb.array();
    }

    @Override
    public void calculateChecksum(byte dataByte) {

    }

    @Override
    public void calculateChecksumOfArray(byte[] pixel) {

    }

    @Override
    public void calculateChecksumOfByteBuffer(ByteBuffer pixelBuffer, int limit) {

    }

    @Override
    public void fehlerausgabe() {
        // TODO Auto-generated method stub
        super.fehlerausgabe();
        // Bilddimensionen
        if ((getWidth() * height) == 0) {
            System.err.println("mindestens eine Bilddimension ist 0.");
            System.exit(123);
        }
        // Bildtyp
        if (!(header[2] == 2 || header[2] == 10)) {
            System.err.println("Bildtyp ist falsch");
            System.exit(123);
        }

        // Zu wenig Bilddaten
        if (bildTyp == 2) {
            if (getWidth() * height * 3 > (filepath.toFile().length() - TGA_HEADER_OFFSET)) {
                System.err.println("zu wenige Bilddaten");
                System.exit(123);
            }
        }

    }

    // Methoden

    public byte[] getHeader() {
        return header;
    }

    protected void setHeader(byte[] header) {
        this.header = header;
    }

    @Override
    int returnChecksum() {
        return 0;
    }

    @Override
    protected void setCompressionFromFile() {
        // TODO Auto-generated method stub
        bildTyp = headerbb.get(2);
        switch (bildTyp) {
            case 2:
                compression = ProjectConstants.UNCOMPRESSED;
                break;
            case 10:
                compression = ProjectConstants.RLE;
                break;
        }

    }

    public void setCompressionOutputfile(String mode) {

        switch (mode) {
            case ProjectConstants.UNCOMPRESSED:
                bildTyp = 2;
                break;
            case ProjectConstants.RLE:
                bildTyp = 10;
                break;
        }
    }

    @Override
    protected void setHeightandWidth() {
        // TODO Auto-generated method stub

        setWidth(headerbb.getShort(12));
        height = headerbb.getShort(14);

    }
}
