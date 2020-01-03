package src.propra.compressionoperations;

import java.io.IOException;

public interface AutoInterface {

    public int getTotalSizeOfRLEDatasegment();
    void run(byte singleByte) throws IOException;


}
