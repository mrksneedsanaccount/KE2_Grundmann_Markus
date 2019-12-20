package src.filetypes;

import src.helperclasses.ConversionSpecs;
import src.propra.imageconverter.ImageConverter;

import java.util.Map;

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
    private String[] colourscheme = {"Blue", "Green", "Red"};
    private Map<String, Integer> colourSchemeMap = Map.of("Green", 1, "Red",
            2, "Blue", 0);
    private byte bildTyp;
    private short xcoord;
    private short ycoord;
    private byte bildAttributByte = 0x20;


    /**
     * inputfile Konstruktor
     *
     * @param convspec Objekt, welches die Informationen über die durchzuführende Operation enthält.
     */
    public TGA(ConversionSpecs convspec) {
        super(convspec);
        this.conversionspec = convspec;
        // TODO Auto-generated constructor stub
    }


    /**
     * Outpfile Konstruktor
     *
     * @param convspec    Objekt, welches die Informationen über die durchzuführende Operation enthält.
     * @param inputformat Das Objekt, der Eingabedatei. (Zur Unterscheidung der Eingabe- und Ausgabedateiheader
     */
    public TGA(ConversionSpecs convspec, FileTypeSuper inputformat) {
        // TODO Auto-generated constructor stub
        super(convspec, inputformat);

        ycoord = getWidth();
        bildAttributByte = 0x20;
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
            case ImageConverter.UNCOMPRESSED:
                bildTyp = 2;
                break;
            case ImageConverter.RLE:
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

    // Methoden

    @Override
    public byte[] buildHeader() {
        // TODO Auto-generated method stub
        return null;
    }

    protected void colourSchemeInfo() {
        // TODO Auto-generated method stub
        colourscheme = new String[]{"Blue", "Green", "Red"};
        colourSchemeMap = Map.of("Green", 1, "Red", 2, "Blue", 0);

    }

    @Override
    protected void compression() {
        // TODO Auto-generated method stub
        bildTyp = headerbb.get(2);
        switch (bildTyp) {
            case 2:
                compression = ImageConverter.UNCOMPRESSED;
                break;
            case 10:
                compression = ImageConverter.RLE;
                break;
        }

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
        if (!(header[2] != 2 || header[2] != 10)) {
            System.err.println("Bildtyp ist falsch");
            System.exit(123);
        }

        // Zu wenig Bilddaten
        if (bildTyp == 2) {
            if (getWidth() * height * 3 > (conversionspec.getInputPath().toFile().length() - TGA_HEADER_OFFSET)) {
                System.err.println("zu wenige Bilddaten");
                System.exit(123);
            }
        }

    }

    @Override
    public Map<String, Integer> getColourSchemeMap() {
        return colourSchemeMap;
    }

    @Override
    public void setColourSchemeMap(Map<String, Integer> colourSchemeMap) {
        this.colourSchemeMap = colourSchemeMap;
    }

    @Override
    public String[] getColourscheme() {
        return colourscheme;
    }

    @Override
    public void setColourscheme(String[] colourscheme) {
        this.colourscheme = colourscheme;
    }

    public byte[] getHeader() {
        return header;
    }

    protected void setHeader(byte[] header) {
        this.header = header;
    }

    @Override
    protected void heightandwidth() {
        // TODO Auto-generated method stub

        setWidth(headerbb.getShort(12));
        height = headerbb.getShort(14);

    }

    @Override
    public void setConversionspec() {
        // TODO Auto-generated method stub

    }

    /**
     * @param convspec
     */
    @Override
    public void setOperation(ConversionSpecs convspec) {
        super.setOperation(convspec);

        if (convspec.getOperation() != null) {//KE1 macht das notwendig
            switch (convspec.getOperation()) {

                case ImageConverter.UNCOMPRESSED:
                    bildTyp = 2;
                    break;
                case ImageConverter.RLE:
                    bildTyp = 10;
                    break;
            }
        }
    }


}
