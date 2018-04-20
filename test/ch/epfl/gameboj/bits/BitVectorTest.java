package ch.epfl.gameboj.bits;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import javax.swing.event.ListSelectionEvent;

import org.junit.Ignore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.bits.BitVector.Builder;
import ch.epfl.gameboj.bits.BitVector.ExtensionType;


/**
 * 
 * @author David Gonzales Leon (270845)
 * @author Melvin Malonga-Matouba (288405)
 *
 */

public class BitVectorTest {

    private static class BitVectorTableExample {
        final int[] table;
        BitVector vector;
        final String correspondingString;

        BitVectorTableExample(int[] table, String correspondingString) {
            this.table = table;
            vector = new BitVector(table);
            this.correspondingString = correspondingString;
        }

        BitVector build() {
            vector = new BitVector(table);
            return vector;
        }

    }

    private static class BitVectorBuildingExample {
        final int size;
        final int[][] bytes;
        final String correspondingString;
        BitVector.Builder builder = null;
        BitVector vector = null;

        BitVectorBuildingExample(int size, int[][] bytes, String str) {
            this.size = size;
            this.bytes = bytes;
            correspondingString = str;
        }

        BitVector.Builder setAll() {
            builder = new BitVector.Builder(size);
            for (int[] line : bytes) {
                builder.setByte(line[0], line[1]);
            }
            return builder;
        }

    }

    private static final BitVectorBuildingExample[] ValidExampleBuilders = new BitVectorBuildingExample[] {
            new BitVectorBuildingExample(32,
                    new int[][] { { 0, 0xAF }, { 3, 0xAF } },
                    "11110101000000000000000011110101"),

    };

   // private static final BitVectorBuildingExample[] InvalidExampleBuilders = new BitVectorBuildingExample[] {
     //       new BitVectorBuildingExample(0, new byte[][] {}, ""),
       //     new BitVectorBuildingExample(32,
         //           new byte[][] { { 0, (byte) 0xAF }, { 3, (byte) 0xAF } },
           //         ""),

 //   };

    private static final BitVectorTableExample[] ValidExamples = new BitVectorTableExample[] {
            new BitVectorTableExample(new int[] { 0xAF_0000_AF },
                    "11110101000000000000000011110101") };

    @Test
    void BitVectorBuilderBuildsCorrectly() {
        for (BitVectorBuildingExample example : ValidExampleBuilders) {
            example.setAll();
            System.out
                    .println(((BitVector.Builder) example.builder).toString());
            BitVector vector = example.builder.build();
            assertEquals(example.correspondingString, vector.toString());
        }
    }

    @Test
    void BitVectorPrivatesConstructorWorks() {
        for (BitVectorTableExample example : ValidExamples) {
            assertEquals(example.correspondingString,
                    example.build().toString());
        }

    }

    @Test
    void BitVectorBuildingCanBeDoneOnlyOnce() {
        for (BitVectorBuildingExample example : ValidExampleBuilders) {
            example.setAll();
            example.builder.build();
            assertThrows(NullPointerException.class,
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

    
    @Test
    void testBitWorks() {
        int table = 0xAAAA5555;
        // BitVector.Builder b = new Builder(32);
        // b.setByte(0, (byte) table[0]);
        // b.setByte(1, (byte) table[1]);
        // b.setByte(2, (byte) table[2]);
        // b.setByte(3, (byte) table[3]);
        // BitVector vector = b.build();
        BitVector vector = new BitVector(new int[] { table });
        for (int j = 0; j < 32; ++j) {
            assertEquals(Bits.test(table, j), vector.testBit(j));
        }
    }

    @Test
    void AndOrThrowException() {
        BitVector v1 = new BitVector(new int[] { 0xAF0000AF });
        BitVector v2 = new BitVector(new int[] { 0xAFFFFF50, 0xAFFFFF50 });
        assertThrows(IllegalArgumentException.class, () -> v1.and(v2));
        assertThrows(IllegalArgumentException.class, () -> v1.or(v2));
    }

    @Test
    void AndWorks() {
        BitVector v1 = new BitVector(new int[] { 0xAF0000AF });
        BitVector v2 = new BitVector(new int[] { 0xAFFFFF50 });
        BitVector res = v1.and(v2);
        assertEquals(new BitVector(new int[] { 0xAF000000 }).toString(),
                res.toString());
    }

    @Test
    void OrWorks() {
        BitVector v1 = new BitVector(new int[] { 0xAF0000AF });
        BitVector v2 = new BitVector(new int[] { 0x00F00F50 });
        BitVector res = v1.or(v2);
        assertEquals(new BitVector(new int[] { 0xAFF00FFF }).toString(),
                res.toString());
    }

    @Test
    void notWorks() {
        BitVector v1 = new BitVector(new int[] { 0xAF50AA00 });
        v1 = v1.not();
        assertEquals(new BitVector(new int[] { 0x50AF55FF }).toString(),
                v1.toString());
    }
    
    @Test
    void equalsWorksWithHashCode() {
        Random rng = new Random();
        int[] table = new int[rng.nextInt(10)];
        for (int i = 0;i<table.length;++i) {
            table[i] = rng.nextInt();
        }
        
        List<BitVector> vectors = new ArrayList<>();
        for (int i = 0;i<table.length;++i) {
            vectors.add(new BitVector(new int[] {table[i]}));
        }
        
        for (BitVector b : vectors ) {
            for (BitVector b2 : vectors) {
                if (b.equals(b2)) {
                    assertTrue(b.hashCode()==b2.hashCode());
                }
            }
        }
    }

    @Test
    void extractZeroExtendedWorksBasic() {
    	
        int[] table = new int[] { 0xAF0000FA, 0xFA0000AF };
        int[] result = new int[] { 0, 0xAF0000FA };
        BitVector vector = new BitVector(table);
        BitVector res = vector.extractZeroExtended(-32, 64);
        assertEquals(new BitVector(result).toString(), res.toString());
        
        table = new int[] { 0xAF0000FA, 0xFA0000AF };
        result = new int[] {0xAF0000FA, 0xFA0000AF };
        vector = new BitVector(table);
        res = vector.extractZeroExtended(0, 64);
        assertEquals(new BitVector(result).toString(), res.toString());
        

        table = new int[] { 0xAF0EC0FA, 0x10150FEA };
        result = new int[] {0xC0FA0000, 0x0FEAAF0E };
        vector = new BitVector(table);
        res = vector.extractZeroExtended(-16, 64);
        assertEquals(new BitVector(result).toString(), res.toString());

        table = new int[] { 0xAF0EC0FA, 0x10150FEA };
        result = new int[] {0xC0FA0000, 0x0FEAAF0E, 0x00001015 };
        vector = new BitVector(table);
        res = vector.extractZeroExtended(-16, 96);
        assertEquals(new BitVector(result).toString(), res.toString());

        table = new int[]  {0xAF0EC0FA, 0x10150FEA };
        result = new int[] {0x607D0000, 0x87F55787 };
        vector = new BitVector(table);
        res = vector.extractZeroExtended(-15, 64);
        assertEquals(new BitVector(result).toString(), res.toString());

      
    }

    @Test
    void extractWrappedWorksBasic() {
        // Multiple de 32
        int[] table = new int[] { 0xAF0000FA, 0xFA0000AF };
        int[] result = new int[] { 0xFA0000AF, 0xAF0000FA };
        BitVector vector = new BitVector(table);
        BitVector res = vector.extractWrapped(-32, 64);
        assertEquals(new BitVector(result).toString(), res.toString());

       
    }

    @Test
    void BuilderThrowsExceptionAtCreation() {
        assertThrows(IllegalArgumentException.class, ()->new Builder(0));
        assertThrows(IllegalArgumentException.class, ()->new Builder(-1));
        assertThrows(IllegalArgumentException.class, ()->new Builder(21));
    }
    
    @Test
    void BitVectorBuilderBuildWorks2() {
        int[] res = new int[] {0xAF00AF00, 0xFF00FF00};
        BitVector.Builder b = new Builder(64);
        b.setByte(0, 0).setByte(1,0xAF).setByte(2, 0).setByte(3, 0xAF).setByte(4, 0).setByte(5, 0xFF).setByte(6, 0).setByte(7, 0xFF);
        assertEquals(new BitVector(res).toString(), b.build().toString());
    }
    
    @Test
    void extractZeroExtendedWorks() {
    		Random random = new Random();
    		for (int i=0 ; i<50 ; i++) {
    			int size=32*random.nextInt(10);
    			size = size>0?size:32;
    			BitVector.Builder builder = new BitVector.Builder(size);
    			for (int j=0 ; j<size%8 ; j++) {
    				builder.setByte(j,random.nextInt(8));
    			}
    			BitVector vector = builder.build();
    			int randomStart = (random.nextBoolean()?-1:1)*random.nextInt(10);
    			int randomSize = 32*(random.nextInt(9));
    			randomSize = randomSize>0?randomSize : 32;
    			BitVector extracted= vector.extractZeroExtended(randomStart,randomSize);
    			
    			for (int k = 0 ; k<randomSize ; k++) {
    				assertEquals((bitAtIndexOfExtensiontest(randomStart+k,ExtensionType.BYZERO,vector)==1), extracted.testBit(k));
    			}
    		}
    }
    
    @Test
    void extractWrappedWorks() {
    		Random random = new Random();
    		for (int i=0 ; i<50 ; i++) {
    			int size=32*random.nextInt(10);
    			size = size>0?size:32;
    			BitVector.Builder builder = new BitVector.Builder(size);
    			for (int j=0 ; j<size%8 ; j++) {
    				builder.setByte(j,random.nextInt(8));
    			}
    			BitVector vector = builder.build();
    			int randomStart = (random.nextBoolean()?-1:1)*random.nextInt(10);
    			int randomSize = 32*(random.nextInt(9));
    			randomSize = randomSize>0?randomSize : 32;
    			BitVector extracted= vector.extractWrapped(randomStart,randomSize);
    			
    			for (int k = 0 ; k<randomSize ; k++) {
    				assertEquals((bitAtIndexOfExtensiontest(randomStart+k,ExtensionType.WRAPPED,vector)==1), extracted.testBit(k));
    			}
    		}
    }
    
    
    
    
//    @Test
//    @Disabled 
//    void betterToStringWorks() {
//    	 Random rng = new Random();
//         int size = rng.nextInt(10);
//         int[] table = new int[size == 0?1:size];
//         for (int i = 0;i<table.length;++i) {
//             table[i] = Bits.clip(32, rng.nextInt());
//         }
//         BitVector vector = new BitVector(table.clone());
//         assertEquals(vector.toString(), vector.betterToString());    	
//    }
//    
//    
//    @Test
//    @Disabled 
//    void betterToStringWorksBasic() {
//    		BitVector vector = new BitVector(new int[] {-1});
//    		assertEquals(vector.toString(), vector.betterToString());
//    		
//    		vector = new BitVector(new int[] {0x0000FFFF});
//    		assertEquals(vector.toString(), vector.betterToString());
//    		
//    		vector = new BitVector(new int[] {0x0000FFFF,0xFFFF0000});
//    		assertEquals(vector.toString(), vector.betterToString());
//    }
    
    
    private int bitAtIndexOfExtensiontest(int index, ExtensionType ext, BitVector vector) {
        if (index >= 0 && index < vector.size()) {
            return vector.testBit(index) ? 1 : 0;
        } else {
            switch (ext) {
            case BYZERO:
                return 0;
            case WRAPPED:
                return vector.testBit(Math.floorMod(index,vector.size())) ? 1 : 0;
            default:
                Objects.requireNonNull(ext);
                throw new IllegalArgumentException(" How ? ");
            }
        
       }
   }
}
