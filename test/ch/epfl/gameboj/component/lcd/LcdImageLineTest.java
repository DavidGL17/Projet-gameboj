/**
 * 
 */
package ch.epfl.gameboj.component.lcd;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;

/**
 * @author David Gonzalez leon (270845)
 *
 */
class LcdImageLineTest {

    @Test
    void SizeWorks() {
        Random rng = new Random();
        for (int i = 0; i < rng.nextInt(10); ++i) {
            int size = (Math.abs(rng.nextInt(10)) + 1) * 32;
            LcdImageLine test = new LcdImageLine(new BitVector(size),
                    new BitVector(size), new BitVector(size));
            assertEquals(size, test.size());
        }
    }

    @Test
    void gettersWork() {
        LcdImageLine test = new LcdImageLine(
                new BitVector(new int[] { 0xAF00DEFF }),
                new BitVector(new int[] { 0xDFA37DEF }),
                new BitVector(new int[] { 0xFF123672 }));
        assertEquals(new BitVector(new int[] { 0xAF00DEFF }), test.getMsb());
        assertEquals(new BitVector(new int[] { 0xDFA37DEF }), test.getLsb());
        assertEquals(new BitVector(new int[] { 0xFF123672 }),
                test.getOpacity());
    }

    @Test
    void shiftTest() {
        // to do
    }

    @Test
    void extractWrappedWorks() {
        // toDo
    }

    @Test
    void MapColorsWorks() {
        // to do
    }

    @Test
    void Bellow1Works() {
        Random rng = new Random();
        LcdImageLine vector = new LcdImageLine(
                new BitVector(new int[] { rng.nextInt(), rng.nextInt(),
                        rng.nextInt(), rng.nextInt() }),
                new BitVector(new int[] { rng.nextInt(), rng.nextInt(),
                        rng.nextInt(), rng.nextInt() }),
                new BitVector(new int[] { rng.nextInt(), rng.nextInt(),
                        rng.nextInt(), rng.nextInt() }));
        LcdImageLine that = new LcdImageLine(
                new BitVector(new int[] { rng.nextInt(), rng.nextInt(),
                        rng.nextInt(), rng.nextInt() }),
                new BitVector(new int[] { rng.nextInt(), rng.nextInt(),
                        rng.nextInt(), rng.nextInt() }),
                new BitVector(new int[] { rng.nextInt(), rng.nextInt(),
                        rng.nextInt(), rng.nextInt() }));
        LcdImageLine.Builder b = new LcdImageLine.Builder(vector.size());
        for (int i = 0; i < vector.size() / 8; ++i) {
            int msbByte = 0, lsbByte = 0;
            for (int j = 0; j < 8; ++j) {
                Bits.set(msbByte, j,
                        that.getOpacity().testBit(i * 8 + j)
                                ? that.getMsb().testBit(i * 8 + j)
                                : vector.getMsb().testBit(i * 8 + j));
                Bits.set(lsbByte, j,
                        that.getOpacity().testBit(i * 8 + j)
                                ? that.getLsb().testBit(i * 8 + j)
                                : vector.getLsb().testBit(i * 8 + j));
            }
            b.setBytes(i, msbByte, lsbByte);
        }
        assertTrue(b.build().equals(vector.below(that)));

    }

}
