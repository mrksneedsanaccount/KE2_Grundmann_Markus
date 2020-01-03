package src.Test;

import groovy.util.GroovyTestCase;
import org.junit.Rule;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import src.propra.conversionfacilitators.CommandLineInterpreter;


import java.nio.file.Files;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CommandLineInterpreterTest extends GroovyTestCase {

    @Test
    void interpretInputs() {
    }

    @Test
    void testGetFileExtensionFromString() {
    }

    @Rule


    @ParameterizedTest
    @MethodSource("stringArrayProvider")
    void interpretInputs(Object[] args) throws IllegalAccessException {


        CommandLineInterpreter test = new CommandLineInterpreter();
        assert(args.length == 2 || args.length == 3);

        System.out.println("Args length is "+ args.length);

        test.interpretInputs((String[]) args);


        System.out.println(test.getInputSuffix());
        System.out.println(test.getInputPath());
        System.out.println(test.getOutputPath());
        System.out.println(test.getOutputSuffix());
        System.out.println(test.getMode());
        System.out.println(test.getAlphabet());

//        assertEquals(CommandLineInterpreter.UNCOMPRESSED, test.getMode());

        assertTrue(
                CommandLineInterpreter.UNCOMPRESSED == test.getMode() ||
                CommandLineInterpreter.KE1CONVERSION == test.getMode() ||
                CommandLineInterpreter.ENCODE == test.getMode() ||
                CommandLineInterpreter.DECODE == test.getMode()
        );

        @Nested
        class Test22
        {
            @Test
            @DisplayName("is empty")
            void inputpathExists ()
            {
                assert(test.getInputPath() != null);
                assertTrue(Files.exists(test.getInputPath()));
            }
        }

        assert(test.getOutputPath() != null);

        assert(test.getInputSuffix() != null);
    }


    static Stream<Arguments> stringArrayProvider() {
        return Stream.of(
                Arguments.of((Object) new String[]{"--input=../KE3_Konvertiert/test_05_huffman.propra", "--output=../KE3_Konvertiert/test_05_UNCOMPRESSED.propra", "--compression=uncompressed"}),
                Arguments.of((Object) new String[]{"--input=../KE1_TestBilder/test_02_uncompressed.tga", "--output=../KE1_Konvertiert/test_02.propra"}),
                Arguments.of((Object) new String[]{"--input=../KE2_TestBilder_optional/test_base-2_a.propra.base-n", "--decode-base-n"}),
                Arguments.of((Object) new String[]{"--input=../KE2_TestBilder/test_02_rle.tga", "--encode-base-32"}),
                Arguments.of((Object) new String[]{"--input=../KE3_Konvertiert/test_05_huffman.propra", "--output=../KE3_Konvertiert/test_05_UNCOMPRESSED.propra", "--compression=uncompressed"}),
                Arguments.of((Object) new String[]{"--input=../KE2_TestBilder/test_04_rle.propra","--encode-base-n=0987abc4321xyz56"})
        );
    }


}