package propra.Test;

import groovy.util.GroovyTestCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import propra.conversion_facilitators.CommandLineInterpreter;
import propra.exceptions.IllegalCommandLineInputException;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;

class ConversionsTest extends GroovyTestCase {


    static String[] convertUniStringToStringArray(String str) {


        return str.split(" ");
    }

    static Stream<Arguments> stringArrayProvider() {
        return Stream.of(

//fehler


//                Arguments.of ((Object) stringToStringArray("--input=G:/ProPra/Referenzen/Testbilder/test_05_huffman_invalid_encoded_tree.propra" +
//                        " --output=../KE3_Konvertiert/_129AllBlack_rle.tga --compression=rle"), stringToLowerCase("0")),
//                Arguments.of ((Object) stringToStringArray("--input=G:/ProPra/Referenzen/Testbilder/test_05_huffman_invalid_huffman_datasegment.propra" +
//                        " --output=../KE3_Konvertiert/_129AllBlack_rle.tga --compression=rle"), stringToLowerCase("0")),


// Sonderfälle
                Arguments.of(stringToStringArray("--input=G:/ProPra/Referenzen/Testbilder/128.pixel-test._uncompressed.tga --output=../KE3_Konvertiert/128.pixel-test._uncomToRLE.tga --compression=rle"), stringToLowerCase("00DB016F404A8A9C12C99CE5C2A89B5F")),
                Arguments.of(stringToStringArray("--input=G:/ProPra/Referenzen/Testbilder/128.pixel-test._rle.tga --output=../KE3_Konvertiert/128.pixel-test._rle_uncompressed.tga --compression=uncompressed"), stringToLowerCase("9455A195D44B429A690EB976016C2B8D")),

                Arguments.of(stringToStringArray("--input=G:/ProPra/Referenzen/Testbilder/129AllBlack_uncompressed.tga --output=../KE3_Konvertiert/_129AllBlack_rle.tga --compression=rle"), stringToLowerCase("CB19C85D4D17C94662464C8F2873CA70")),
                Arguments.of(stringToStringArray("--input=G:/ProPra/Referenzen/Testbilder/1Pixel_uncompressed.tga --output=../KE3_Konvertiert/1Pixel_rle.tga --compression=rle"), stringToLowerCase("DEE48D73361BE14F321EEE7E4A1B65F3")),
                Arguments.of(stringToStringArray("--input=G:/ProPra/Referenzen/Testbilder/1Pixel_uncompressed.tga --output=../KE3_Konvertiert/1Pixel_huffman.propra --compression=huffman"), stringToLowerCase("955d464c4533a9ca455f83996fb3f243")),

                Arguments.of(stringToStringArray("--input=../KE3_Konvertiert/1Pixel_huffman.propra  --output=../KE3_Konvertiert/1Pixel_huffman_to_rle.tga --compression=rle"), stringToLowerCase("DEE48D73361BE14F321EEE7E4A1B65F3")),

                Arguments.of(stringToStringArray("--input=G:/ProPra/Referenzen/Testbilder/129AllBlack_RLE_GIMP.tga --output=../KE3_Konvertiert/_129AllBlack_rle.tga --compression=uncompressed"), stringToLowerCase("0655FB2048CD55A9BAEC3A4BDC703380")),

                Arguments.of(stringToStringArray("--input=G:/ProPra/KE1_TestBilder/hRQJC9d.tga --output=../KE1_Konvertiert/TOO_LARGE.propra --compression=uncompressed"), stringToLowerCase("EF01BC63056E0B211F5ECCEB249A841E")),
                Arguments.of(stringToStringArray("--input=../KE3_TestBilder/test_04_rle.propra --output=../KE3_Konvertiert/test_04.tga --compression=uncompressed"), "287952de21afe62de9d1b6427094db2a"),
                Arguments.of(new String[]{"--input=../KE1_TestBilder/test_02_uncompressed.tga", "--output=../KE1_Konvertiert/test_02.propra"}, "7898c68cf3d07f2ed48cd0491bd8f3f2"),

                //to rle to rle


                // decodes
                Arguments.of(stringToStringArray("--input=../KE2_TestBilder/test_05_base32.tga.base-32 --decode-base-32"), stringToLowerCase("ba08f2b984211e710b2dd5b14841ef1b")),
                Arguments.of(new String[]{"--input=../KE2_TestBilder/test_06_base32.propra.base-32", "--decode-base-32"}, stringToLowerCase("40C9DBC2EA45629CB56B15E2CBCAD027")),
                Arguments.of(new String[]{"--input=../KE2_TestBilder_optional/test_base-2_a.propra.base-n", "--decode-base-n"}, stringToLowerCase("F0C889DCB6A820A345E1BB87D3299565")),
                Arguments.of(stringToStringArray("--input=../KE2_TestBilder_optional/test_base-2_b.propra.base-n --decode-base-n"), stringToLowerCase("36d6be6b555418822550e2fab05bee19")),
                Arguments.of(stringToStringArray("--input=../KE2_TestBilder_optional/test_base-4.propra.base-n --decode-base-n"), stringToLowerCase("cdc71e5cd7e4bcc050c6ca5f81c3fa58")),
                Arguments.of(stringToStringArray("--input=../KE2_TestBilder_optional/test_base-8.propra.base-n --decode-base-n"), stringToLowerCase("aa105056b4693c0db53752d44d70a895")),
                Arguments.of(stringToStringArray("--input=../KE2_TestBilder_optional/test_base-64.propra.base-n --decode-base-n"), stringToLowerCase("10d2def7f609bf223a1cc5d9635bb824")),


                // encode
                Arguments.of(new String[]{"--input=../KE2_TestBilder/test_04_rle.propra", "--encode-base-n=0987abc4321xyz56"}, stringToLowerCase("5cafb88b8ba967468d0857740c4d5f13")),
                Arguments.of(new String[]{"--input=../KE2_TestBilder/test_04_rle.propra.base-n", "--decode-base-n"}, stringToLowerCase("88154851C0B8AF7DF81DEBC3CF105594")),

                Arguments.of(new String[]{"--input=../KE2_TestBilder/test_02_rle.tga", "--encode-base-32"}, stringToLowerCase("1BD51859E877425BF9D37B84B43649ED")),
                Arguments.of(stringToStringArray("--input=../KE2_TestBilder/test_04_rle.propra --encode-base-32"), stringToLowerCase("d9645dc4bd6a28c853c5f00f50c721cd")),


                Arguments.of(new String[]{"--input=../KE2_TestBilder_optional/test_base-2_a.propra", "--encode-base-n=10"}, stringToLowerCase("840F3D9944102038F5BFD2EFFAC2D3F8")),
                Arguments.of(new String[]{"--input=../KE2_TestBilder_optional/test_base-2_b.propra", "--encode-base-n=01"}, stringToLowerCase("FF246F69EEEC97BBD30446459075DF05")),
                Arguments.of(new String[]{"--input=../KE2_TestBilder_optional/test_base-4.propra", "--encode-base-n=04DY"}, stringToLowerCase("94DAA6398344E6B7F1AACEA127CBDAF5")),
                Arguments.of(new String[]{"--input=../KE2_TestBilder_optional/test_base-8.propra", "--encode-base-n=rKs1t_W0"}, stringToLowerCase("D725FC14D557A750F21A6CADA9198387")),
                Arguments.of(new String[]{"--input=../KE2_TestBilder_optional/test_base-64.propra", "--encode-base-n=ctzrna:C-6SQI1?0jF.XNiVGm3JoWM;#+9R7g8fKP2dv<'uT>^l@%/Z4!5(Yk*)h"}, stringToLowerCase("4138621C46E6BADF0EB7104178358DE1")),


                //huffman
                Arguments.of(new String[]{"--input=../KE3_Testbilder/test_05_huffman.propra", "--output=../KE3_Konvertiert/test_05_UNCOMPRESSED.tga", "--compression=uncompressed"}, stringToLowerCase("93BE04A09007873A72BDEE916009E833")),
                Arguments.of(stringToStringArray("--input=../KE3_TestBilder/test_05_huffman.propra      --output=../KE3_Konvertiert/test_05.tga  --compression=rle"), stringToLowerCase("eb8ebc447530dcec6a4980295fe33ba6")),

                Arguments.of(new String[]{"--input=../KE3_TestBilder/test_05.tga", "--output=../KE3_Konvertiert/test_05_huffman.propra", "--compression=huffman"}, "27a43d00a199f86d6c171864cb0cffc8"),


                Arguments.of(new String[]{"--input=../KE2_TestBilder_optional/test_grosses_bild.propra", "--output=../KE2_Konvertiert/test_grosses_bild_compressed.tga", "--compression=rle"}, "d5ec22d8734fb8c04c09ca81966af1ff"),
                Arguments.of(new String[]{"--input=../KE1_TestBilder/test_03_uncompressed.propra", "--output=../KE1_Konvertiert/test_03.tga", "--compression=uncompressed"}, "6a6e4599112ba08f8af926407e45109f"),
                Arguments.of(new String[]{"--input=../KE3_TestBilder/test_05_huffman.propra", "--output=../KE3_Konvertiert/test_05.tga", "--compression=rle"}, "eb8ebc447530dcec6a4980295fe33ba6"),
                Arguments.of(new String[]{"--input=../KE2_TestBilder/test_03_uncompressed.propra", "--output=../KE2_Konvertiert/test_03_AUTO.propra", "--compression=auto"}, "a4cba2baf9b5b827b6f17e09b8cdb77b"),
                Arguments.of(stringToStringArray("--input=../KE2_Konvertiert/test_03_AUTO.propra --output=../KE2_Konvertiert/test_03_AUTO_uncompressed.propra --compression=uncompressed"), stringToLowerCase("DA0A09F3CCA4C622E2A23502412C79C2")),

                Arguments.of(new String[]{"--input=../KE1_TestBilder/test_01_uncompressed.tga", "--output=../KE1_Konvertiert/test_01_AUTO.propra", "--compression=auto"}, "a9f7b21810bbf2ce7aa7daedfd018ce0"),
                Arguments.of(stringToStringArray("--input=../KE1_Konvertiert/test_01_AUTO.propra --output=../KE1_Konvertiert/test_01_AUTO_huffman_to_uncompressed.tga --compression=uncompressed"), stringToLowerCase("938FC4EE83B2981243FAB48B9E3B779A")),

                Arguments.of(new String[]{"--input=../KE1_TestBilder/test_02_uncompressed.tga", "--output=../KE1_Konvertiert/test_02_AUTO.propra", "--compression=auto"}, "2814e7990918ddfc3da8f430874e4fa3"),
                Arguments.of(new String[]{"--input=../KE1_Konvertiert/test_02_AUTO.propra", "--output=../KE1_Konvertiert/test_02_AUTO_touncomoressed.tga", "--compression=uncompressed"}, "9d70fb42cc397cab493afe531c61da3f"),

                Arguments.of(new String[]{"--input=../KE2_TestBilder/test_03_uncompressed.propra", "--output=../KE2_Konvertiert/test_03_AUTO.tga", "--compression=auto"}, stringToLowerCase("65C15E7473D3774DA5C57F404392AFA1")),

                Arguments.of(new String[]{"--input=../KE3_Testbilder/test_05_huffman.propra", "--output=../KE3_Konvertiert/test_05_AUTO.propra", "--compression=auto"}, "27a43d00a199f86d6c171864cb0cffc8"),
                Arguments.of(stringToStringArray("--input=../KE3_Konvertiert/test_05_AUTO.propra --output=../KE3_Theo_Konvertiert/test_05_AUTO_rle.tga --compression=rle"), stringToLowerCase("EB8EBC447530DCEC6A4980295FE33BA6")),

                Arguments.of(new String[]{"--input=../KE3_TestBilder/test_05.tga", "--output=../KE3_Konvertiert/test_05_AUTO.propra", "--compression=auto"}, "27a43d00a199f86d6c171864cb0cffc8"),


                Arguments.of(new String[]{"--input=../KE1_Konvertiert/test_02_AUTO.propra", "--output=../KE1_Konvertiert/test_02_huffmanTOUncompressed.propra", "--compression=uncompressed"}, "7898c68cf3d07f2ed48cd0491bd8f3f2"),
                Arguments.of(new String[]{"--input=../KE2_TestBilder/test_04_rle.propra", "--output=../KE2_Konvertiert/test_04_rle.tga", "--compression=rle"}, stringToLowerCase("D84715FAFD3D1ADE7FB2466DB78D01DB")),


                //theo Tests
                Arguments.of(stringToStringArray("--input=../KE3_Theo/fullhuffmantree.tga --output=../KE3_Theo_Konvertiert/x1.propra --compression=huffman"), stringToLowerCase("0b16440d2d0085e12f53950e185ce110")),
                Arguments.of(stringToStringArray("--input=../KE3_Theo_Konvertiert/x1.propra --output=../KE3_Theo_Konvertiert/x1_uncompressed.tga --compression=uncompressed"), stringToLowerCase("D86AA190FB403689D1A8041F9AA8DD14")),
                Arguments.of(stringToStringArray("--input=../KE3_Theo/fullhuffmantree.tga --output=../KE3_Theo_Konvertiert/x1_uncompressed.propra --compression=uncompressed"), stringToLowerCase("5F6718DAC165AE8965F44C9BE91BBE9C")),
                Arguments.of(stringToStringArray("--input=../KE3_Theo_Konvertiert/x1_uncompressed.propra --output=../KE3_Theo_Konvertiert/x1_uncomp_tohuffman.propra --compression=huffman"), stringToLowerCase("0b16440d2d0085e12f53950e185ce110")),
                Arguments.of(stringToStringArray("       --input=../KE3_Theo/huffman_is_optimal.tga --output=../KE3_Theo_Konvertiert/x2.propra --compression=auto"), "b8dff8e598c1150c3b07364861a57701"),
                Arguments.of(stringToStringArray("       --input=../KE3_Theo_Konvertiert/x2.propra --output=../KE3_Theo_Konvertiert/x2_tohuffman_is_optimal.tga --compression=rle"), stringToLowerCase("EBF3A5D277975CA7C34DAEBE5E57F585")),

                Arguments.of(stringToStringArray("--input=../KE3_Theo/uncompressed_is_optimal.tga --output=../KE3_Theo_Konvertiert/x4.propra --compression=auto"), stringToLowerCase("18a13b0f5637fbfc25e9ed8c7d472f1e")),
                Arguments.of(stringToStringArray("  --input=../KE3_Theo/rle_is_optimal.tga --output=../KE3_Theo_Konvertiert/x5.tga --compression=auto"), stringToLowerCase("05425d67dae5edc4ead6c21cd9ace25c")),
                Arguments.of(stringToStringArray("--input=../KE3_Theo/uncompressed_is_optimal.tga --output=../KE3_Theo_Konvertiert/x6.tga --compression=auto"), "3f821d0674972de8d1d498399fd96220"),


// Gabi Tests
                Arguments.of(stringToStringArray("--input=../KE1_TestBilder/test_01_uncompressed.tga  --output=../KE3_Konvertiert/x1_huff.propra --compression=huffman"), stringToLowerCase("0ef40e461734e9f4d6a6bd8a800571af")),
                Arguments.of(stringToStringArray("--input=../KE3_Konvertiert/x1_huff.propra  --output=../KE3_Konvertiert/x1_huff_to_uncompressed.tga --compression=uncompressed"), stringToLowerCase("938FC4EE83B2981243FAB48B9E3B779A")),


                Arguments.of(stringToStringArray("--input=../KE3_TestBilder/test_02_rle.tga --output=../KE3_Konvertiert/x2_huff.propra --compression=huffman"), stringToLowerCase("b7875f2c61540afdd54cb2b68c4318ea  ")),
                Arguments.of(stringToStringArray("--input=../KE3_Konvertiert/x2_huff.propra --output=../KE3_Konvertiert/x2_huff_rle.tga --compression=rle"), stringToLowerCase("AA1F6F8D4B72A1277C95C9496BD2EAD7  ")),

                Arguments.of(stringToStringArray("--input=../KE3_TestBilder/test_05_huffman.propra --output=../KE3_Konvertiert/x3_uncompr.tga --compression=uncompressed"), stringToLowerCase("93be04a09007873a72bdee916009e833")),


                Arguments.of(stringToStringArray("--input=../KE3_TestBilder/test_03_uncompressed.propra --output=../KE3_Konvertiert/x4_rle.propra  --compression=rle"), stringToLowerCase("DA166C00421782C8177F39C1C66F1245")),
                Arguments.of(stringToStringArray("--input=../KE3_TestBilder/test_04_rle.propra --output=../KE3_Konvertiert/x5_rle.propra  --compression=rle"), stringToLowerCase("c8eaaa1cd99858e662604669998ef353")),
                Arguments.of(stringToStringArray("--input=../KE3_TestBilder/test_05_huffman.propra --output=../KE3_Konvertiert/x6_rle.propra --compression=rle"), stringToLowerCase("71CB24141B8C942905C57A586DC7F1ED")),
                Arguments.of(stringToStringArray("--input=../KE3_Konvertiert/x6_rle.propra --output=../KE3_Konvertiert/x6_rle_TGA.tga --compression=rle"), stringToLowerCase("eb8ebc447530dcec6a4980295fe33ba6")),

                Arguments.of(stringToStringArray("--input=../KE3_TestBilder/test_03_uncompressed.propra --output=../KE3_Konvertiert/x7_uncompr.propra  --compression=uncompressed"), stringToLowerCase("38f263597523d92e3a4c6299483d14e4")),
                Arguments.of(stringToStringArray("--input=../KE2_TestBilder/test_04_rle.propra --output=../KE3_Konvertiert/x8_uncompr.propra --compression=uncompressed"), stringToLowerCase("5599cc1cb3502cff9dff0ac9e1ddcf13")),
                Arguments.of(stringToStringArray("--input=../KE3_TestBilder/test_05_huffman.propra --output=../KE3_Konvertiert/x9_uncompr.propra --compression=uncompressed"), stringToLowerCase("b2d77e109f787753132853267c765963")),
                Arguments.of(stringToStringArray(" --input=../KE3_TestBilder/test_02_rle.tga --output=../KE3_Konvertiert/x10_rle.propra --compression=rle"), stringToLowerCase("ff50dee1f71b15ceb72d89c6fe23d6ba")),
                Arguments.of(stringToStringArray("--input=../KE3_TestBilder/test_03_uncompressed.propra --output=../KE3_Konvertiert/x11_huffman.propra  --compression=huffman"), stringToLowerCase("848b25b62f92945c3502c31256fb8338")),
                Arguments.of(stringToStringArray("--input=../KE3_Konvertiert/x11_huffman.propra  --output=../KE3_Konvertiert/x11_huffman_to_uncompressed.propra  --compression=uncompressed"), stringToLowerCase("38F263597523D92E3A4C6299483D14E4")),

                Arguments.of(stringToStringArray("--input=../KE3_TestBilder/test_04_rle.propra --output=../KE3_Konvertiert/x12_huffman.propra  --compression=huffman"), stringToLowerCase("dda165588c14d4d2184b8baa4c379cea  ")),
                Arguments.of(stringToStringArray("--input=../KE3_Konvertiert/x12_huffman.propra --output=../KE3_Konvertiert/x12_huffman_toUncompressed.tga  --compression=uncompressed"), stringToLowerCase("287952DE21AFE62DE9D1B6427094DB2A")),


                Arguments.of(stringToStringArray("--input=../KE3_TestBilder/test_05_huffman.propra --output=../KE3_Konvertiert/x13_huffman.propra --compression=huffman"), stringToLowerCase("27A43D00A199F86D6C171864CB0CFFC8  ")),
                Arguments.of(stringToStringArray("--input=../KE3_TestBilder/test_02_rle.tga --output=../KE3_Konvertiert/x14_rle.tga --compression=rle"), stringToLowerCase("aa1f6f8d4b72a1277c95c9496bd2ead7  ")),
                Arguments.of(stringToStringArray("--input=../KE1_TestBilder/test_01_uncompressed.tga  --output=../KE3_Konvertiert/x15_rle.tga --compression=rle"), stringToLowerCase("cb54b4f224485451befdc38798f0fce0  ")),
                Arguments.of(stringToStringArray("--input=../KE1_TestBilder/test_01_uncompressed.tga  --output=../KE3_Konvertiert/x16_uncompr.tga --compression=uncompressed"), stringToLowerCase("938fc4ee83b2981243fab48b9e3b779a  ")),
                Arguments.of(stringToStringArray("--input=../KE3_TestBilder/test_02_rle.tga --output=../KE3_Konvertiert/x17_uncompr.tga --compression=uncompressed"), stringToLowerCase("811486f29ab9a8011281492e3ec3b099    ")),
                Arguments.of(stringToStringArray("--input=../KE2_TestBilder/test_04_rle.propra --output=../KE3_Konvertiert/x18_rle.tga --compression=rle"), stringToLowerCase("d84715fafd3d1ade7fb2466db78d01db    ")),

//Gabi extra:
                Arguments.of(stringToStringArray("--input=../KE3_TestBilder/test_09_lauterVerschiedeneFarben_uncompressed.tga --output=../KE3_Konvertiert/test_09_lauterVerschiedeneFarben_rle.tga --compression=rle"), stringToLowerCase("01D916447436A354008461C9C79E8416")),
                Arguments.of(stringToStringArray("--input=../KE3_TestBilder/test_09_lauterVerschiedeneFarben_uncompressed.tga --output=../KE3_Konvertiert/test_09_lauterVerschiedeneFarben_huffman.propra --compression=huffman"), stringToLowerCase("bd408daed518d3dbcb0f2e55b85b82c5")),
                Arguments.of(stringToStringArray("--input=../KE3_Konvertiert/test_09_lauterVerschiedeneFarben_huffman.propra --output=../KE3_Konvertiert/test_09_lauterVerschiedeneFarben_uncompressed.tga --compression=uncompressed"), stringToLowerCase("6911174F5818E55A9BC86579DD6BBB44")),
//                Arguments.of ((Object) stringToStringArray(""), stringToLowerCase("0")),

//                Arguments.of ((Object) stringToStringArray(""), stringToLowerCase("0")),
//                Arguments.of ((Object) stringToStringArray(""), stringToLowerCase("0")),

// Large files:

                Arguments.of(stringToStringArray("--input=../KE3_TestBilder/test_05_huffman.propra      --output=../KE3_Konvertiert/test_05_auto.tga  --compression=auto"), stringToLowerCase("EB8EBC447530DCEC6A4980295FE33BA6")),
                Arguments.of(stringToStringArray("--input=../KE2_TestBilder_optional/test_grosses_bild.propra --output=../KE2_Konvertiert/test_grosses_bild_rle_to_auto.propra --compression=auto"), "ce57d6c8336939f5f603ab27806c4018"),
                Arguments.of(new String[]{"--input=G:\\ProPra\\Gabi\\KE2_konvertiert\\test_grosses_bild_uncompressed.tga", "--output=../KE2_Konvertiert/test_grosses_unkomprimiert_zu_auto.propra", "--compression=auto"}, "ce57d6c8336939f5f603ab27806c4018"),

                Arguments.of(stringToStringArray("--input=../KE3_TestBilder/test_05_huffman.propra      --output=../KE3_Konvertiert/test_05.tga  --compression=rle"), stringToLowerCase("eb8ebc447530dcec6a4980295fe33ba6")),

                Arguments.of(stringToStringArray("--input=../KE2_TestBilder_optional/test_grosses_bild.propra --output=../KE2_Konvertiert/test_grosses_bild_uncompressed.propra --compression=uncompressed"), "ec7c42ae83c811410c3926ef7e036299"),
                Arguments.of(new String[]{"--input=G:\\ProPra\\Gabi\\KE2_konvertiert\\test_grosses_bild_uncompressed.tga", "--output=../KE2_Konvertiert/test_grosses_unkomprimiert_zu_komprimiert.propra", "--compression=rle"}, "ce57d6c8336939f5f603ab27806c4018"),


//                Arguments.of ((Object) stringToStringArray(""), stringToLowerCase("0")),


                // MINIMUM REQUIREMENTS


                Arguments.of(stringToStringArray("--input=../KE1_TestBilder/test_01_uncompressed.tga --output=../KE1_Konvertiert/test_01.propra"), stringToLowerCase("aa031715c6890385c649b43f396c0662")),

                Arguments.of(stringToStringArray("--input=../KE1_TestBilder/test_02_uncompressed.tga --output=../KE1_Konvertiert/test_02.propra"), stringToLowerCase("7898c68cf3d07f2ed48cd0491bd8f3f2")),

                Arguments.of(stringToStringArray(" --input=../KE1_TestBilder/test_03_uncompressed.propra --output=../KE1_Konvertiert/test_03.tga"), stringToLowerCase("6a6e4599112ba08f8af926407e45109f")),

                Arguments.of(stringToStringArray(" --input=../KE1_TestBilder/test_04_uncompressed.propra --output=../KE1_Konvertiert/test_04.tga"), stringToLowerCase("78954c29cd021dc7fea50e58c04017ec")),

                Arguments.of(stringToStringArray(" --input=../KE2_TestBilder/test_01_uncompressed.tga --output=../KE2_Konvertiert/test_01.propra --compression=rle"), stringToLowerCase("78b4a2f629e81c30b34c8d9ac1ec7428")),

                Arguments.of(stringToStringArray(" --input=../KE2_TestBilder/test_02_rle.tga --output=../KE2_Konvertiert/test_02.propra --compression=uncompressed"), stringToLowerCase("b9dc047bff52605c7a0319cfbced93e6")),

                Arguments.of(stringToStringArray(" --input=../KE2_TestBilder/test_03_uncompressed.propra --output=../KE2_Konvertiert/test_03.tga --compression=rle"), stringToLowerCase("65c15e7473d3774da5c57f404392afa1")),

                Arguments.of(stringToStringArray(" --input=../KE2_TestBilder/test_04_rle.propra --output=../KE2_Konvertiert/test_04.tga --compression=uncompressed"), stringToLowerCase("8d723848b152bb9ee8ca319725df17c1")),

                Arguments.of(stringToStringArray(" --input=../KE2_TestBilder/test_05_base32.tga.base-32 --decode-base-32"), stringToLowerCase("ba08f2b984211e710b2dd5b14841ef1b")),

                Arguments.of(stringToStringArray(" --input=../KE2_TestBilder/test_06_base32.propra.base-32 --decode-base-32"), stringToLowerCase("40c9dbc2ea45629cb56b15e2cbcad027")),

                Arguments.of(stringToStringArray(" --input=../KE2_TestBilder/test_02_rle.tga --encode-base-32"), stringToLowerCase("1bd51859e877425bf9d37b84b43649ed")),

                Arguments.of(stringToStringArray(" --input=../KE2_TestBilder/test_04_rle.propra --encode-base-32"), stringToLowerCase("d9645dc4bd6a28c853c5f00f50c721cd")),
                Arguments.of(stringToStringArray(" --input=../KE2_TestBilder_optional/test_base-2_a.propra.base-n --decode-base-n"), stringToLowerCase("f0c889dcb6a820a345e1bb87d3299565")),


                Arguments.of(stringToStringArray(" --input=../KE2_TestBilder_optional/test_base-2_b.propra.base-n --decode-base-n"), stringToLowerCase("36d6be6b555418822550e2fab05bee19")),

                Arguments.of(stringToStringArray(" --input=../KE2_TestBilder_optional/test_base-4.propra.base-n --decode-base-n"), stringToLowerCase("cdc71e5cd7e4bcc050c6ca5f81c3fa58")),

                Arguments.of(stringToStringArray(" --input=../KE2_TestBilder_optional/test_base-8.propra.base-n --decode-base-n"), stringToLowerCase("aa105056b4693c0db53752d44d70a895")),

                Arguments.of(stringToStringArray(" --input=../KE2_TestBilder_optional/test_base-64.propra.base-n --decode-base-n"), stringToLowerCase("10d2def7f609bf223a1cc5d9635bb824")),

                Arguments.of(stringToStringArray(" --input=../KE2_TestBilder_optional/test_grosses_bild.propra --output=../KE2_Konvertiert/test_grosses_bild_compressed.tga --compression=rle"), stringToLowerCase("d5ec22d8734fb8c04c09ca81966af1ff")),

                Arguments.of(stringToStringArray(" --input=../KE2_TestBilder_optional/test_grosses_bild.propra --output=../KE2_Konvertiert/test_grosses_bild_compressed.propra --compression=rle"), stringToLowerCase("ce57d6c8336939f5f603ab27806c4018")),


                Arguments.of(stringToStringArray(" --input=../KE3_TestBilder/test_01_uncompressed.tga --output=../KE3_Konvertiert/test_01.propra --compression=rle"), stringToLowerCase("9343d04d5f9e7bcfee90cdaacb70b525")),

                Arguments.of(stringToStringArray(" --input=../KE3_TestBilder/test_02_rle.tga --output=../KE3_Konvertiert/test_02.propra --compression=uncompressed"), stringToLowerCase("bd87158f38a2a03dea2aa818495ccc38")),

                Arguments.of(stringToStringArray(" --input=../KE3_TestBilder/test_03_uncompressed.propra --output=../KE3_Konvertiert/test_03.tga --compression=rle"), stringToLowerCase("ee73c656fb3b0459612e2c4cdcf46ef5")),

                Arguments.of(stringToStringArray(" --input=../KE3_TestBilder/test_04_rle.propra --output=../KE3_Konvertiert/test_04.tga --compression=uncompressed"), stringToLowerCase("287952de21afe62de9d1b6427094db2a")),

                Arguments.of(stringToStringArray(" --input=../KE3_TestBilder/test_05_huffman.propra --output=../KE3_Konvertiert/test_05.tga --compression=rle"), stringToLowerCase("eb8ebc447530dcec6a4980295fe33ba6")),


// AUTO TEST

                Arguments.of(stringToStringArray(" --input=../KE1_TestBilder/test_01_uncompressed.tga --output=../KE1_autoTest/test_01.propra --compression=auto"), stringToLowerCase("a9f7b21810bbf2ce7aa7daedfd018ce0  ")),

                Arguments.of(stringToStringArray(" --input=../KE1_TestBilder/test_02_uncompressed.tga --output=../KE1_autoTest/test_02.propra --compression=auto"), stringToLowerCase("2814e7990918ddfc3da8f430874e4fa3  ")),

                Arguments.of(stringToStringArray(" --input=../KE1_TestBilder/test_03_uncompressed.propra --output=../KE1_autoTest/test_03.propra --compression=auto"), stringToLowerCase("d1d8ae030a0796109140319aa9888eb3  ")),

                Arguments.of(stringToStringArray(" --input=../KE1_TestBilder/test_04_uncompressed.propra --output=../KE1_autoTest/test_04.propra --compression=auto"), stringToLowerCase("23c5ba509f84e794c4a365ff75304ffb  ")),


                Arguments.of(stringToStringArray(" --input=../KE2_TestBilder/test_01_uncompressed.tga --output=../KE2_autoTest/test_01.propra --compression=auto"), stringToLowerCase("78b4a2f629e81c30b34c8d9ac1ec7428  ")),

                Arguments.of(stringToStringArray(" --input=../KE2_TestBilder/test_02_rle.tga --output=../KE2_autoTest/test_02.propra --compression=auto"), stringToLowerCase("1eb0003242e675636a8f6683ae8d4ea8  ")),

                Arguments.of(stringToStringArray(" --input=../KE2_TestBilder/test_03_uncompressed.propra --output=../KE2_autoTest/test_03.propra --compression=auto"), stringToLowerCase("a4cba2baf9b5b827b6f17e09b8cdb77b  ")),

                Arguments.of(stringToStringArray(" --input=../KE2_TestBilder/test_04_rle.propra --output=../KE2_autoTest/test_04.propra --compression=auto"), stringToLowerCase("88154851c0b8af7df81debc3cf105594  ")),

                Arguments.of(stringToStringArray(" --input=../KE2_TestBilder/test_05_base32.tga --output=../KE2_autoTest/test_05.propra --compression=auto"), stringToLowerCase("f64e71e286ea128f19e5b4e867ad83e7")),

                Arguments.of(stringToStringArray(" --input=../KE2_TestBilder/test_06_base32.propra  --output=../KE2_autoTest/test_06.propra --compression=auto"), stringToLowerCase("40c9dbc2ea45629cb56b15e2cbcad027  ")),


                Arguments.of(stringToStringArray(" --input=../KE2_TestBilder_optional/test_base-2_a.propra --output=../KE2_autoTest/test_base-2_a.propra  --compression=auto"), stringToLowerCase("14f0ff988ef5dc2d530d0682ae00eab7  ")),
                Arguments.of(stringToStringArray(" --input=../KE2_TestBilder_optional/test_base-2_b.propra --output=../KE2_autoTest/test_base-2_b.propra --compression=auto"), stringToLowerCase("c8c8dfad5600c0819aed15b137b3d977  ")),
                Arguments.of(stringToStringArray(" --input=../KE2_TestBilder_optional/test_base-4.propra --output=../KE2_autoTest/test_base-4.propra --compression=auto"), stringToLowerCase("4c3197fdee86242900e476f40e5d098c  ")),
                Arguments.of(stringToStringArray(" --input=../KE2_TestBilder_optional/test_base-8.propra --output=../KE2_autoTest/test_base-8.propra  --compression=auto"), stringToLowerCase("a8616e0f31e3335f3137b86990c198d2  ")),
                Arguments.of(stringToStringArray(" --input=../KE2_TestBilder_optional/test_base-64.propra --output=../KE2_autoTest/test_base-64.propra  --compression=auto"), stringToLowerCase("db5c28105436993cd2fa4e43fcb356ad  ")),
                Arguments.of(stringToStringArray(" --input=../KE2_TestBilder_optional/test_grosses_bild.propra  --output=../KE2_autoTest/test_grosses.propra  --compression=auto"), stringToLowerCase("ce57d6c8336939f5f603ab27806c4018    ")),


                Arguments.of(stringToStringArray(" --input=../KE3_TestBilder/test_01_uncompressed.tga  --output=../KE3_autoTest/test_01.propra  --compression=auto"), stringToLowerCase("027b0c460473a15fdb6d5ac9a50ba6c7  ")),

                Arguments.of(stringToStringArray(" --input=../KE3_TestBilder/test_02_rle.tga --output=../KE3_autoTest/test_02.propra  --compression=auto"), stringToLowerCase("ff50dee1f71b15ceb72d89c6fe23d6ba  ")),

                Arguments.of(stringToStringArray(" --input=../KE3_TestBilder/test_03_uncompressed.propra --output=../KE3_autoTest/test_03.propra  --compression=auto"), stringToLowerCase("da166c00421782c8177f39c1c66f1245  ")),

                Arguments.of(stringToStringArray(" --input=../KE3_TestBilder/test_04_rle.propra --output=../KE3_autoTest/test_04.propra  --compression=auto"), stringToLowerCase("C8EAAA1CD99858E662604669998EF353  ")),

                Arguments.of(stringToStringArray(" --input=../KE3_TestBilder/test_05_huffman.propra --output=../KE3_autoTest/test_05.propra  --compression=auto"), stringToLowerCase("27A43D00A199F86D6C171864CB0CFFC8  ")),


// fehlersuche
                Arguments.of(stringToStringArray(" --input=../KE3_Konvertiert/test_03.tga --output=../KE3_Konvertiert/test_03_rle_to_uncompressed.propra --compression=uncompressed"), stringToLowerCase("38F263597523D92E3A4C6299483D14E4"))


        );
    }

    static String stringToLowerCase(String str) {

        str = str.trim().replaceAll(" +", " ");
        return str.toLowerCase();
    }

    static String[] stringToStringArray(String str) {
        str = str.trim().replaceAll(" +", " ");
        return str.split(" ");
    }

    private String getMD5Checksum(String fileNameAsString) throws NoSuchAlgorithmException, IOException {
        byte[] buffer = new byte[8192];
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


    @ParameterizedTest
    @MethodSource("stringArrayProvider")
    void initializeConversions(Object[] args, String md5) throws IOException, NoSuchAlgorithmException, IllegalCommandLineInputException {


        propra.imageconverter.ImageConverter.main((String[]) args);
//
        CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.interpretInputs((String[]) args);


        System.out.println("Filesize: " + commandLineInterpreter.getOutputPath().toFile().length());
        assertEquals(md5, getMD5Checksum(commandLineInterpreter.getOutputPath().toString()));


    }


    @Test
    public void test() {

        propra.imageconverter.ImageConverter.main(stringToStringArray("--input=G:\\ProPra\\Gabi\\KE2_konvertiert\\test_grosses_bild_uncompressed.tga --output=../KE2_Konvertiert/test_grosses_unkomprimiert_zu_komprimiert.propra --compression=rle"));
        for (int i = 0; i < 0; i++) {
            propra.imageconverter.ImageConverter.main(stringToStringArray("--input=G:\\ProPra\\Gabi\\KE2_konvertiert\\test_grosses_bild_uncompressed.tga --output=../KE2_Konvertiert/test_grosses_unkomprimiert_zu_komprimiert.propra --compression=rle"));
            propra.imageconverter.ImageConverter.main(stringToStringArray("--input=G:\\ProPra\\Gabi\\KE2_konvertiert\\test_grosses_bild_uncompressed.tga --output=../KE2_Konvertiert/test_grosses_unkomprimiert_zu_komprimiert.propra --compression=rle"));
            propra.imageconverter.ImageConverter.main(stringToStringArray("--input=G:\\ProPra\\Gabi\\KE2_konvertiert\\test_grosses_bild_uncompressed.tga --output=../KE2_Konvertiert/test_grosses_unkomprimiert_zu_komprimiert.propra --compression=rle"));
            propra.imageconverter.ImageConverter.main(stringToStringArray("--input=G:\\ProPra\\Gabi\\KE2_konvertiert\\test_grosses_bild_uncompressed.tga --output=../KE2_Konvertiert/test_grosses_unkomprimiert_zu_komprimiert.propra --compression=rle"));
            propra.imageconverter.ImageConverter.main(stringToStringArray("--input=G:\\ProPra\\Gabi\\KE2_konvertiert\\test_grosses_bild_uncompressed.tga --output=../KE2_Konvertiert/test_grosses_unkomprimiert_zu_komprimiert.propra --compression=rle"));
            propra.imageconverter.ImageConverter.main(stringToStringArray("--input=G:\\ProPra\\Gabi\\KE2_konvertiert\\test_grosses_bild_uncompressed.tga --output=../KE2_Konvertiert/test_grosses_unkomprimiert_zu_komprimiert.propra --compression=rle"));
            propra.imageconverter.ImageConverter.main(stringToStringArray("--input=G:\\ProPra\\Gabi\\KE2_konvertiert\\test_grosses_bild_uncompressed.tga --output=../KE2_Konvertiert/test_grosses_unkomprimiert_zu_komprimiert.propra --compression=rle"));
            propra.imageconverter.ImageConverter.main(stringToStringArray("--input=G:\\ProPra\\Gabi\\KE2_konvertiert\\test_grosses_bild_uncompressed.tga --output=../KE2_Konvertiert/test_grosses_unkomprimiert_zu_komprimiert.propra --compression=rle"));
        }


        assertEquals("test", "test");
    }


}