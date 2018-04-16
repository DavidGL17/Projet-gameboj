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
	
	private static class BitVectorTableExample{
		final int[] table ;
		BitVector vector;
		final String correspondingString;
		
		BitVectorTableExample(int[] table, String correspondingString){
			this.table=table;
			vector=new BitVector(table);
			this.correspondingString=correspondingString;
		}
		
		BitVector build() {
			vector= new BitVector(table);
			return vector;
		}
		
	}
	
	private static class BitVectorBuildingExample{
		final int size;
		final byte[][] bytes;
		final String correspondingString;
		BitVector.Builder builder=null;
		BitVector vector=null;
		
		BitVectorBuildingExample(int size, byte[][] bytes, String str){
			this.size=size;
			this.bytes=bytes;
			correspondingString=str;
		}
		
		BitVector.Builder setAll(){
			builder=new BitVector.Builder(size);
			for (byte[] line : bytes) {
					builder.setByte(line[0],line[1]);
				}
			return builder;
		}
		
		
		
	}

	private static final BitVectorBuildingExample[] ValidExampleBuilders = 
			new BitVectorBuildingExample [] {
			new BitVectorBuildingExample(32, new byte [][] {{0,(byte)0xAF},{3,(byte)0xAF}} , "11110101000000000000000011110101"),
			
					
			
	};
	
	private static final BitVectorBuildingExample[] InvalidExampleBuilders = 
			new BitVectorBuildingExample [] {
			new BitVectorBuildingExample(0, new byte[][] {},""),
			new BitVectorBuildingExample(32, new byte [][] {{0,(byte)0xAF},{3,(byte)0xAF}} , ""),
			
					
			
	};
	
	private static final BitVectorTableExample [] ValidExamples =
			new BitVectorTableExample [] {
			new BitVectorTableExample( new int[] {0xAF_0000_AF}, "11110101000000000000000011110101")
	};
	
	@Test
	void BitVectorBuilderBuildsCorrectly() {
		for (BitVectorBuildingExample example : ValidExampleBuilders) {
			example.setAll();
			System.out.println(( (BitVector.Builder)example.builder).toString());
			BitVector vector=example.builder.build();
			assertEquals(example.correspondingString, vector.toString());
		}
	}
	
	@Test
	void BitVectorPrivatesConstructorWorks(){
		for (BitVectorTableExample example : ValidExamples) {
			assertEquals(example.correspondingString, example.build().toString());
		}
		
	}
	@Test
	void BitVectorBuildingCanBeDoneOnlyOnce(){
		for (BitVectorBuildingExample example : ValidExampleBuilders) {
			example.setAll();
			example.builder.build();
			assertThrows(NullPointerException.class ,
					() -> example.builder.build());
		}
	}
	
	


    @Test
    void BitVectorConstructionThrowsException() {
        for (int i = 1; i < 32; ++i) {
            int size = 32 + i;
            assertThrows(IllegalArgumentException.class,
                    () -> new BitVector(size));
        }
    }

    @Test
    void BitVectorSizeTest() {
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
