package propra.compression_operations;

import java.io.IOException;

public interface AutoInterface {

    int getTotalSizeOfRLEDatasegment();

    void run(byte singleByte) throws IOException;

    void runIteratingOverArray(int limit, byte[] byteArray) throws IOException;


}
