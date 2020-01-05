package propra.helpers;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

public class HelperMethods {

    public static void exitProgramAfterError() {
        System.exit(123);
    }

    public static FileChannel initialiseInputChannel(File file, int offset) throws IOException {
        FileChannel fileChannel = new FileInputStream(file).getChannel();
        fileChannel.position(offset);
        return fileChannel;
    }

    public static void initialiseOutputFile(File outputfile1, int length) throws IOException {
        File outputfile = outputfile1;
        if (outputfile.exists()) {
            outputfile.delete();
        }
        Set<StandardOpenOption> options = new HashSet<>();
        options.add(StandardOpenOption.CREATE);
        options.add(StandardOpenOption.APPEND);
        Path path = Paths.get(outputfile.getAbsolutePath());
        FileChannel outputChannel = FileChannel.open(path, options);
        outputChannel.write(ByteBuffer.allocate(length));
        outputChannel.close();
    }

















}
