package src.filetypes;

import src.helperclasses.ConversionSpecs;


/**
 * Klasse ubn der das Alphabet und die Kodierungskänge gespeichert werden
 *
 */
public class BaseN extends FileTypeSuper{
    private String alphabet;
    private int kodierungslaenge;

    private int basenGroesse;
    final public static String BASE32HEX = "0123456789ABCDEFGHIJKLMNOPQRSTUV";

    public BaseN(String alphabet) {
        this.alphabet = alphabet;
        calculateBasenGroesse();
    }

    // input ist im Base-N Kodiert
    public BaseN(ConversionSpecs convspec) {
        String inputformat = convspec.getInputFormatString();
        switch (inputformat){
            case ".base-32":
                this.alphabet = BASE32HEX;
                calculateBasenGroesse();
                break;

        }


    }



    public int getKodierungslaenge() {
        return kodierungslaenge;
    }

    public void setKodierungslaenge(int kodierungslaenge) {
        this.kodierungslaenge = kodierungslaenge;
    }

    public void setAlphabet(String alphabet) {
        this.alphabet = alphabet;
    }

    public int getBasenGroesse() {
        return basenGroesse;
    }

    public void setBasenGroesse(int basenGroesse) {
        this.basenGroesse = basenGroesse;
    }

    public String getAlphabet() {
        return alphabet;
    }

    /**
     * berechnet die Anzahl, der Bits welche für das Alphabet benötigt werden.
     */
    public void calculateBasenGroesse() {
        this.basenGroesse = (int) (Math.log(alphabet.length()) / Math.log(2));;
    }





















    @Override
    public byte[] buildHeader() {
        return new byte[0];
    }

    @Override
    public byte[] buildHeader(FileTypeSuper inputfile) {
        return new byte[0];
    }

    @Override
    protected void colourSchemeInfo() {

    }

    @Override
    protected void compression() {

    }

    @Override
    public byte[] getHeader() {
        return new byte[0];
    }

    @Override
    public void ausgabeHeaderFertigInitialisieren(FileTypeSuper inputfile) {

    }

    @Override
    public void calculateChecksum(byte dataByte) {

    }

    @Override
    long returnChecksum() {
        return 0;
    }

    @Override
    public void calculateChecksumOfPixel(byte[] pixel) {

    }

    @Override
    protected void heightandwidth() {

    }

    @Override
    public void setConversionspec() {

    }
}
