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

    public static void deleteFileAtLocationIfNecessary(File file) {
        if (file.exists()) {
            file.delete();
            System.out.println("File at output destination has been replaced.");
        }
    }

    /**
     * @param file   input file File Object.
     * @param offset length of the inputfiles header.
     * @return initialised FileCHannel
     * @throws IOException
     */
    public static FileChannel initialiseInputChannel(File file, int offset) throws IOException {
        FileChannel fileChannel = new FileInputStream(file).getChannel();
        fileChannel.position(offset);
        return fileChannel;
    }

    /**
     * This method is responsible for creating the target file and writing a dummy header.
     *
     * @param outputfile file - File Object of the output file.
     * @param length     Size of the target file's header.
     * @throws IOException
     */
    public static void initialiseOutputFile(File outputfile, int length) throws IOException {
        deleteFileAtLocationIfNecessary(outputfile);
        Set<StandardOpenOption> options = new HashSet<>();
        options.add(StandardOpenOption.CREATE);
        options.add(StandardOpenOption.APPEND);
        Path path = Paths.get(outputfile.getAbsolutePath());
        FileChannel outputChannel = FileChannel.open(path, options);
        outputChannel.write(ByteBuffer.allocate(length));
        outputChannel.close();
    }

















}
