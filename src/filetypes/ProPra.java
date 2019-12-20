package src.filetypes;

import src.helperclasses.ConversionSpecs;
import src.propra.imageconverter.Checksum;
import src.propra.imageconverter.ImageConverter;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

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










//    protected byte[] header = new byte[PROPRA_HEADER_OFFSET];
    protected String[] colourscheme = {"Green", "Blue", "Red"};
    protected Map<String, Integer> colourSchemeMap = Map.of("Green", 0, "Red",
            2, "Blue", 1);
    private String formatkennung = "ProPraWS19";
    private long datensegmentgroesse;
    private int checksum;
    private byte kompressionstyp;


    // Konstruktor für die Eingabedatei
    public ProPra(ConversionSpecs convspec) {
        super(convspec);
        // TODO Auto-generated constructor stub
        datensegmentgroesse = headerbb.getLong(16);
        checksum = headerbb.getInt(24);
    }

    //Konstruktor für die Ausgabedatei
    public ProPra(ConversionSpecs convspec, FileTypeSuper inputformat) {
        // TODO Auto-generated constructor stub
        super(convspec, inputformat);
    }


    @Override
    public byte[] buildHeader() {
        headerbb.put("ProPraWS19".getBytes());
        headerbb.putShort(10, getWidth());
        headerbb.putShort(12, height);
        headerbb.put(14, (byte) 24);
        switch (compression) {
            case ImageConverter.UNCOMPRESSED:
                headerbb.put(15, (byte) 0);
                break;
            case ImageConverter.RLE:
                headerbb.put(15, (byte) 1);
        }
        headerbb.putLong(conversionspec.getInputPath().toFile().length());
        headerbb.putInt(Checksum.calculateChecksumPropra(conversionspec.getbOAS().toByteArray()));

        return header;
    }

    @Override
    public byte[] buildHeader(FileTypeSuper inputfile) {
        // TODO Auto-generated method stub
        headerbb.put(formatkennung.getBytes());
        headerbb.putShort(10, width);
        headerbb.putShort(12, height);
        headerbb.put(14, bitsprobildpunkt);
        headerbb.put(15, kompressionstyp);
        headerbb.putLong(16, datensegmentgroesse);
        headerbb.putInt(24, checksum);
        return headerbb.array();
    }

    protected void colourSchemeInfo() {
        // TODO Auto-generated method stub
        colourscheme = new String[]{"Green", "Blue", "Red"};
        colourSchemeMap = Map.of("Green", 0, "Red",
                2, "Blue", 1);


    }

    @Override
    protected void compression() {
        // TODO Auto-generated method stub
        kompressionstyp = headerbb.get(15);
        switch (kompressionstyp) {
            case 0:
                compression = ImageConverter.UNCOMPRESSED;
                break;
            case 1:
                compression = ImageConverter.RLE;
                break;
            case 2:
                compression = ImageConverter.HUFFMAN;
                break;
        }

    }

    @Override
    public void fehlerausgabe() {
        // TODO Auto-generated method stub
        super.fehlerausgabe();
        // Dateigröße im Header überprüfen (Prüft, ob Daten fehlen, oder zu
        // viel vorhanden sind.
        if(kompressionstyp == 0) {
            if (height * width * 3 != conversionspec.getInputPath().toFile().length() - PROPRA_HEADER_OFFSET) {
                System.err.println(
                        "Zu wenige, oder zu viele Datensegmente vorhanden.");
                System.exit(123);
            }
        }
        // Datensegmentangabe mit Menge der Bilddaten in der Datei abgleichen

        if (headerbb.getLong(16) != conversionspec.getInputPath().toFile().length()-PROPRA_HEADER_OFFSET) {
            System.err.println(
                    "Datensegmentanzahl aus dem Header stimmt nicht mit den Anzahl an Datensegmenten in der Datei überein");
            System.exit(123);
        }

        // Prüfsumme zum Test berechnen
        int cs1 = Checksum.calculateChecksumPropra(conversionspec.getInputDatasegments());
        System.out.println("berechnete Prüfsumme: " + cs1);
        // Prüfsumme des Datei extrahieren.
        System.out.println("Prüfsumme: " + checksum);
        if (!(checksum == cs1)) {
            System.err.println("Angegebene Prüfsumme ist inkorrekt.");
            System.exit(123);
        }
        // Kompressionstyp
        if (!(header[15] == 0 || header[15] == 1 || header[15] == 2)) {
            System.err.println("falscher Kompressionstyp");
            System.exit(123);
        }
        // Testen, ob ProPraWS19 im Header steht
        String s = "ProPraWS19";
        if (!(Arrays.equals(s.getBytes(StandardCharsets.UTF_8),
                extractDatasegments(10, 0, header)))) {
            System.err.println("Datei beginnt nicht mit ProPraWS19");
            System.exit(123);
        }
    }



    public Map<String, Integer> getColourSchemeMap() {
        return colourSchemeMap;
    }

    public void setColourSchemeMap(Map<String, Integer> colourSchemeMap) {
        this.colourSchemeMap = colourSchemeMap;
    }

    public String[] getColourscheme() {
        return colourscheme;
    }

    public void setColourscheme(String[] colourscheme) {
        this.colourscheme = colourscheme;
    }

    public long getDatensegmentgroesse() {
        return datensegmentgroesse;
    }

    public void setDatensegmentgroesse(long datensegmentgroesse) {
        this.datensegmentgroesse = datensegmentgroesse;
    }

    public String getFormatkennung() {
        return formatkennung;
    }

    public void setFormatkennung(String formatkennung) {
        this.formatkennung = formatkennung;
    }

    public byte[] getHeader() {
        return header;
    }

    public void setHeader(byte[] header) {
        this.header = header;
    }

    public byte getKompressionstyp() {
        return kompressionstyp;
    }

    public void setKompressionstyp(byte kompressionstyp) {
        this.kompressionstyp = kompressionstyp;
    }

    @Override
    public void ausgabeHeaderFertigInitialisieren(FileTypeSuper inputfile) {
        // TODO Auto-generated method stub
        datensegmentgroesse = conversionspec.getbOAS().size();
        checksum = Checksum.calculateChecksumPropra(conversionspec.getbOAS().toByteArray());


    }

    @Override

    protected void heightandwidth() {
        // TODO Auto-generated method stub

        setWidth(headerbb.getShort(10));
        height = headerbb.getShort(12);

    }

    @Override
    public void setConversionspec() {
        // TODO Auto-generated method stub

    }

    @Override
    public void setOperation(ConversionSpecs convspec) {
        super.setOperation(convspec);
        if (convspec.getOperation() != null) {
            switch (convspec.getOperation()) {
                case ImageConverter.UNCOMPRESSED:
                    kompressionstyp = 0;
                    break;
                case ImageConverter.RLE:
                    kompressionstyp = 1;
                    break;
            }
        }


    }

}
