/**
 * 
 */
package ch.epfl.gameboj.bits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Random;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.bits.BitVector.Builder;

/**
 * @author David Gonzalez leon (270845)
 *
 */
class BitVectorTest {

    @Test
    void constructionThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new BitVector(0));
        for (int i = 1; i < 32; ++i) {
            int size = 32 + i;
            assertThrows(IllegalArgumentException.class,
                    () -> new BitVector(size));
        }
    }

    @Test
    void SizeTest() {
        Random rng = new Random();
        int cycles = rng.nextInt(100);
        for (int i = 0; i < cycles; ++i) {
            int x = rng.nextInt(50);
            x = x == 0 ? 1 : x;
            int size = (x < 0 ? -x : x) * 32;
            BitVector b = new BitVector(size);
            assertEquals(size, b.size());
        }
    }

    //marches pas trop, a corriger
    @Test
    void testBitWorks() {
        int[] table = new int[] { 0xAA, 0xAA, 0x55, 0x55};
        BitVector.Builder b = new Builder(32);
        b.setByte(0, (byte) table[0]);
        b.setByte(1, (byte) table[0]);
        b.setByte(2, (byte) table[1]);
        b.setByte(3, (byte) table[1]);
        BitVector vector = b.build();
        for (int i = 0; i < 4; ++i) {
            for (int j = 0;j<32;++j) {
                assertEquals(Bits.test(table[i], j), vector.testBit(32*i + j));
            }
        }
    }

}
