package src.Test;

import groovy.util.GroovyTestCase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import src.helperclasses.ProjectConstants;
import src.propra.conversionfacilitators.Decode;
import src.propra.conversionfacilitators.Encode;
import src.propra.conversionfacilitators.CommandLineInterpreter;
import src.propra.conversionfacilitators.Conversions;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;

class ConversionsTest extends GroovyTestCase {


    static String[] stringToStringArray(String str){
       str = str.trim().replaceAll(" +", " ");

        return str.split(" ");
    }

    static String stringToLowerCase (String str){
        return str.toLowerCase();
    }


    static Stream<Arguments> stringArrayProvider() {
        return Stream.of(
                Arguments.of((Object) stringToStringArray("--input=G:/ProPra/KE1_TestBilder/hRQJC9d.tga --output=../KE1_Konvertiert/TOO_LARGE.propra --compression=uncompressed"), stringToLowerCase("EF01BC63056E0B211F5ECCEB249A841E")),
                Arguments.of((Object) stringToStringArray("--input=../KE3_TestBilder/test_04_rle.propra --output=../KE3_Konvertiert/test_04.tga --compression=uncompressed"), "287952de21afe62de9d1b6427094db2a"),
                Arguments.of ((Object) stringToStringArray("--input=../KE3_TestBilder/test_05_huffman.propra      --output=../KE3_Konvertiert/test_05.tga  --compression=rle"), stringToLowerCase("eb8ebc447530dcec6a4980295fe33ba6")),
                Arguments.of((Object) new String[]{"--input=../KE3_Testbilder/test_05_huffman.propra", "--output=../KE3_Konvertiert/test_05_UNCOMPRESSED.tga", "--compression=uncompressed"}, stringToLowerCase("93BE04A09007873A72BDEE916009E833")),
                Arguments.of((Object) new String[]{"--input=../KE1_TestBilder/test_02_uncompressed.tga", "--output=../KE1_Konvertiert/test_02.propra"}, "7898c68cf3d07f2ed48cd0491bd8f3f2"),
                Arguments.of((Object) new String[]{"--input=../KE2_TestBilder_optional/test_base-2_a.propra.base-n", "--decode-base-n"}, stringToLowerCase("840F3D9944102038F5BFD2EFFAC2D3F8")),
                Arguments.of((Object) new String[]{"--input=../KE2_TestBilder/test_02_rle.tga", "--encode-base-32"}, stringToLowerCase("1BD51859E877425BF9D37B84B43649ED")),
                Arguments.of((Object) new String[]{"--input=../KE3_TestBilder/test_05.tga", "--output=../KE3_Konvertiert/test_05_huffman.propra", "--compression=huffman"}, "27a43d00a199f86d6c171864cb0cffc8"),
                Arguments.of((Object) new String[]{"--input=../KE2_TestBilder/test_04_rle.propra", "--encode-base-n=0987abc4321xyz56"}, stringToLowerCase("367009297E2A7E2529B6A9DE5D676634")),
                Arguments.of((Object) new String[]{"--input=../KE2_TestBilder/test_06_base32.propra.base-32", "--decode-base-32"}, "0"),
                Arguments.of((Object) new String[]{"--input=../KE2_TestBilder/test_04_rle.propra", "--encode-base-n=0987abc4321xyz56"}, "0"),
                Arguments.of((Object) new String[]{"--input=../KE2_TestBilder_optional/test_grosses_bild.propra", "--output=../KE2_Konvertiert/test_grosses_bild_compressed.tga", "--compression=rle"}, "d5ec22d8734fb8c04c09ca81966af1ff"),
                Arguments.of((Object) new String[]{"--input=../KE1_TestBilder/test_03_uncompressed.propra", "--output=../KE1_Konvertiert/test_03.tga", "--compression=uncompressed"}, "6a6e4599112ba08f8af926407e45109f"),
                Arguments.of((Object) new String[]{"--input=../KE3_TestBilder/test_05_huffman.propra", "--output=../KE3_Konvertiert/test_05.tga", "--compression=rle"}, "eb8ebc447530dcec6a4980295fe33ba6"),
                Arguments.of((Object) new String[]{"--input=../KE2_TestBilder/test_03_uncompressed.propra", "--output=../KE2_Konvertiert/test_03_AUTO.tga", "--compression=auto"}, "0"),
                Arguments.of((Object) new String[]{"--input=../KE2_TestBilder/test_03_uncompressed.propra", "--output=../KE2_Konvertiert/test_03_AUTO.propra", "--compression=auto"}, "0"),
                Arguments.of((Object) new String[]{"--input=../KE3_TestBilder/test_05.tga", "--output=../KE3_Konvertiert/test_05_AUTO.propra", "--compression=auto"}, "0"),
                Arguments.of((Object) new String[]{"--input=../KE3_Testbilder/test_05_huffman.propra", "--output=../KE3_Konvertiert/test_05_AUTO.propra", "--compression=auto"}, "0"),
                Arguments.of((Object) new String[]{"--input=../KE1_TestBilder/test_01_uncompressed.tga", "--output=../KE1_Konvertiert/test_01_AUTO.propra", "--compression=auto"}, "0"),
                Arguments.of((Object) new String[]{"--input=../KE1_TestBilder/test_02_uncompressed.tga", "--output=../KE1_Konvertiert/test_02_AUTO.propra", "--compression=auto"}, "0"),
                Arguments.of((Object) new String[]{"--input=../KE1_Konvertiert/test_02_AUTO.propra", "--output=../KE1_Konvertiert/test_02_huffmanTOUncompressed.propra", "--compression=uncompressed"}, "7898c68cf3d07f2ed48cd0491bd8f3f2"),
                Arguments.of((Object) new String[]{"--input=../KE2_TestBilder/test_04_rle.propra", "--output=../KE2_Konvertiert/test_04_rle.tga", "--compression=rle"}, stringToLowerCase("D84715FAFD3D1ADE7FB2466DB78D01DB")),

                Arguments.of((Object) stringToStringArray("--input=../KE2_TestBilder_optional/test_grosses_bild.propra --output=../KE2_Konvertiert/test_grosses_bild_uncompressed.propra --compression=uncompressed"), "ec7c42ae83c811410c3926ef7e036299"),
                Arguments.of((Object) new String[]{"--input=G:\\ProPra\\Gabi\\KE2_konvertiert\\test_grosses_bild_uncompressed.tga", "--output=../KE2_Konvertiert/test_grosses_unkomprimiert_zu_komprimiert.propra", "--compression=rle"}, "ce57d6c8336939f5f603ab27806c4018")





        );
    }

    @ParameterizedTest
    @MethodSource("stringArrayProvider")
    void initializeConversions(Object[] args, String md5) throws IOException, NoSuchAlgorithmException {

        CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.interpretInputs((String[]) args);
        String test = (String) args[1];

        if (test.startsWith(ProjectConstants.OUTPUT)) {
            Conversions conversions = new Conversions(commandLineInterpreter);
            conversions.initializeConversions();
            conversions.setPixels();
            conversions.executeConversion();

            assertEquals(conversions.getInputFile().getWidth() * conversions.getInputFile().getHeight(), conversions.getConversionSuper().getProcessedPixels());
        } else if (test.startsWith(ProjectConstants.ENCODE)) {
            src.propra.conversionfacilitators.Encode encode = new Encode(commandLineInterpreter);
            encode.exectueConversion();

        } else if (test.startsWith(ProjectConstants.DECODE)) {
            src.propra.conversionfacilitators.Decode decode = new Decode(commandLineInterpreter);
            decode.executeConversion();
        }



            assertEquals( getMD5Checksum(commandLineInterpreter.getOutputPath().toString()), md5);


    }

    private String getMD5Checksum(String fileNameAsString) throws NoSuchAlgorithmException, IOException {
        byte[] buffer= new byte[8192];
        int count;

        MessageDigest digest2 = MessageDigest.getInstance("MD5");
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileNameAsString));
        while ((count = bis.read(buffer)) > 0) {
            digest2.update(buffer, 0, count);
        }
        bis.close();
        byte[] hash = digest2.digest();
        // https://stackoverflow.com/questions/11665360/convert-md5-into-string-in-java
        StringBuffer sb2 = new StringBuffer();
        for (int i = 0; i < hash.length; i++)
            sb2.append(Integer.toString((hash[i] & 0xff) + 0x100, 16).substring(1));


        System.out.println("Digest(in hex format):: " + sb2.toString());

        return sb2.toString();
    }
}