package propra.Test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import propra.conversion_facilitators.BaseNConverter;
import propra.helpers.ProjectConstants;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

class BaseNConverterTest {


    static Stream<Arguments> decodingTestArray() {

        return Stream.of(
                Arguments.of("01", "011001100110111101101111011000100110000101110010"),
                Arguments.of("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/", "Zm9vYmFy"),
                Arguments.of("0123456789ABCDEF", "666F6F626172"),
                Arguments.of("0123156789ABCDEF", "666F6F626172")
        );

    }

    static Stream<Arguments> integerArrayProvider() {

        return Stream.of(
                Arguments.of((Object) new Byte[]{0x76, 0x15, (byte) 0xf3}),
                Arguments.of((Object) toObjects("foobar".getBytes()))
        );
    }

    static Byte[] toObjects(byte[] bytesPrim) {
        Byte[] bytes = new Byte[bytesPrim.length];
        Arrays.setAll(bytes, n -> bytesPrim[n]);
        return bytes;
    }

    @ParameterizedTest
    @MethodSource("decodingTestArray")
    void decodingOperation(String alphabet, String code) {


        BaseNConverter baseNConverter = new BaseNConverter(3, alphabet, (int) (Math.log(alphabet.length()) / Math.log(2)), ProjectConstants.DECODE);


        byte[] test = code.getBytes();


        for (byte b1 : test) {
            baseNConverter.runDecode(b1);
        }


//        assertEquals("foobar", baseNConverter.byteArrayOutputStream.toString());


    }

    @ParameterizedTest
    @MethodSource("integerArrayProvider")
    void integerArrayInput(Object[] array) {


        Byte[] B = (Byte[]) array;
        byte[] b2 = new byte[B.length];
        for (int i = 0; i < B.length; i++) {
            b2[i] = (byte) (B[i] & 0xff);
        }
        BaseNConverter baseNConverter = new BaseNConverter(6,
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/", 6,
                ProjectConstants.ENCODE);
        ByteBuffer byteBuffer = ByteBuffer.wrap(b2);

        while (byteBuffer.hasRemaining()) {
            baseNConverter.runEncode(byteBuffer.get());
        }

        System.out.println("test");

    }

    @ParameterizedTest
    @ValueSource(ints = {0x76, 0x15, 0xf3})
    void whyWontItWork(int a) {

        BaseNConverter baseNConverter = new BaseNConverter(6, ProjectConstants.BASE32HEX, 5, ProjectConstants.ENCODE);


        baseNConverter.runEncode((byte) a);

        assertEquals(3, a);


    }


}











