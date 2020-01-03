package src.Test;

import src.filetypes.FileTypeSuper;

import java.nio.ByteBuffer;


/**
 * Klasse ubn der das Alphabet und die Kodierungskänge gespeichert werden
 *
 */
public class BaseN extends FileTypeSuper {
    private String alphabet;
    private int kodierungslaenge;

    private int basenGroesse;


    public BaseN(String alphabet) {
        this.alphabet = alphabet;
        calculateBasenGroesse();
    }

    // input ist im Base-N Kodiert
    public BaseN(String suffix, String alphabetstring) {
        this.alphabet = alphabetstring;
        calculateBasenGroesse();


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
    public byte[] buildHeader(FileTypeSuper inputfile) {
        return new byte[0];
    }



    @Override
    protected void setCompressionFromFile() {

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
    int returnChecksum() {
        return 0;
    }

    @Override
    public void calculateChecksumOfArray(byte[] pixel) {

    }

    @Override
    public void calculateChecksumOfByteBuffer(ByteBuffer pixelBuffer, int limit) {

    }

    @Override
    protected void setHeightandWidth() {

    }

}
