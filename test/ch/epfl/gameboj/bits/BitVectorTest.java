package ch.epfl.gameboj.bits;

import org.junit.jupiter.api.Test;
import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.BitVector.Builder;

/**
 * 
 * @author David Gonzales Leon (270845)
 * @author Melvin Malonga-Matouba (288405)
 *
 */

public class BitVectorTest {
	
	private static class BitVectorExample{
		final int size;
		final byte[][] bytes;
		final String correspondingString;
		BitVector.Builder builder=null;
		BitVector vector=null;
		
		BitVectorExample(int size, byte[][] bytes, String str){
			this.size=size;
			this.bytes=bytes;
			correspondingString=str;
		}
		
		BitVector.Builder setAll(){
			builder=new BitVector.Builder(size);
			for (int i=0 ; i<bytes.length ; i++) {
				builder.setByte(bytes[i][0],bytes[i][1]);
			}
			return builder;
		}
		
		BitVector build() {
			if (builder==null) {
				setAll();
				return build();
			} else {
				return builder.build();
			}
		}
		
		
	}
	
	
	@Test
	void bitVectorBuilderBuildsCorrectly() {
		
	}
	
	@Test
	void buildingCanBeDoneOnlyOnce(){
		for (BitVectorExample example : ExampleBuilders) {
			example.build();
			assertThrows(NullPointerException.class ,
					() -> example.build());
		}
	}
	
	


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
        b.setByte(1, (byte) table[1]);
        b.setByte(2, (byte) table[2]);
        b.setByte(3, (byte) table[3]);
        BitVector vector = b.build();
        for (int i = 0; i < 4; ++i) {
            for (int j = 0;j<8;++j) {
                assertEquals(Bits.test(table[i], j), vector.testBit(8*i + j));
            }
        }
    }

}
