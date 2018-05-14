package ch.epfl.gameboj.component.lcd;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.lcd.LcdImageLine.Builder;
import ch.epfl.gameboj.bits.BitVectorTest;

/**
 * @author David Gonzalez leon (270845)
 * @author Melvin Malonga-Matouba (288405)
 *
 */
class LcdImageLineTest {
	
	
	@Test
	void ConstructorThrowsException() {
		Random random=new Random();
		for (int i=0; i<50 ; i++) {
			int size1=Bits.clip(31,random.nextInt());
			int size2=Bits.clip(31,random.nextInt());
			int size3=Bits.clip(31,random.nextInt());
			if (size1!=size2 || size2!=size3) {
				assertThrows(IllegalArgumentException.class, 
					() -> new LcdImageLine(
							BitVectorTest.randomBitVectorInstantiation(size1),
							BitVectorTest.randomBitVectorInstantiation(size2),
							BitVectorTest.randomBitVectorInstantiation(size3)));
				
			}
		}
		
	}
	
	

    @Test
    void SizeWorks() {
        Random rng = new Random();
        for (int i = 0; i < rng.nextInt(10); ++i) {
            int size = (Bits.clip(16,rng.nextInt())) * 32; 
            size=(size>0) ? size : 32; // Simplement pour ne pas exclure de valeur
            LcdImageLine test = new LcdImageLine(BitVectorTest.randomBitVectorInstantiation(size),
            		BitVectorTest.randomBitVectorInstantiation(size), 
            		BitVectorTest.randomBitVectorInstantiation(size)); //Génère un bitVector aléatoire d'une taille donnée
            assertEquals(size, test.size());
        }
    }

    @Test
    /*
     * requirement : BitVector.equals(BitVector that) works
     */
    void gettersWorkBasic() {
        LcdImageLine test = new LcdImageLine(
                BitVectorTest.BitVectorInstantiation(new int[] { 0xAF00DEFF }),
                BitVectorTest.BitVectorInstantiation(new int[] { 0xDFA37DEF }),
                BitVectorTest.BitVectorInstantiation(new int[] { 0xFF123672 }));
        assertEquals(BitVectorTest.BitVectorInstantiation(new int[] { 0xAF00DEFF }), test.getMsb());
        assertEquals(BitVectorTest.BitVectorInstantiation(new int[] { 0xDFA37DEF }), test.getLsb());
        assertEquals(BitVectorTest.BitVectorInstantiation(new int[] { 0xFF123672 }),
                test.getOpacity());
    }
    
    @Test
    /*
     * requirement : BitVector.equals(BitVector that) works
     */
    void gettersWork() {
	    	for (int i=0 ; i<10 ; i++) {
	    	
	    		BitVector vector1 = BitVectorTest.randomBitVectorInstantiation();
	    		BitVector vector2 = BitVectorTest.randomBitVectorInstantiation(vector1.size());
	    		BitVector vector3 = BitVectorTest.randomBitVectorInstantiation(vector1.size());
	    		LcdImageLine test2 = new LcdImageLine(
	    				vector1,
	    				vector2,
	    				vector3);
	    		assertEquals(vector1, test2.getMsb());
	    		assertEquals(vector2, test2.getLsb());
	    		assertEquals(vector3, test2.getOpacity());
	    		
//	    		int [] table1 = BitVectorTest.randomBitTable();
//	    		int [] table2 = BitVectorTest.randomBitTable(table1.length*32);
//	    		int [] table3 = BitVectorTest.randomBitTable(table1.length*32);
//	    		LcdImageLine test1 = new LcdImageLine(
//	    				BitVectorTest.BitVectorInstantiation(table1),
//	    				BitVectorTest.BitVectorInstantiation(table2),
//	    				BitVectorTest.BitVectorInstantiation(table3));
//	    		assertEquals(BitVectorTest.BitVectorInstantiation(table1),test1.getMsb());
//	    		assertEquals(BitVectorTest.BitVectorInstantiation(table2),test1.getLsb());
//	    		assertEquals(BitVectorTest.BitVectorInstantiation(table3),test1.getOpacity());
	    		
	    	}
	    	
    }

    @Test
    void EqualsAndHashCodeWork() {
        Random rng = new Random();
        int[] table = new int[rng.nextInt(10) * 3];
        for (int i = 0; i < table.length; ++i) {
            table[i] = rng.nextInt();
        }

        List<LcdImageLine> lines = new ArrayList<>();
        for (int i = 0; i < table.length / 3; ++i) {
            lines.add(
                    new LcdImageLine(BitVectorTest.BitVectorInstantiation(new int[] { table[3 * i] }),
                            BitVectorTest.BitVectorInstantiation(new int[] { table[3 * i + 1] }),
                            BitVectorTest.BitVectorInstantiation(new int[] { table[3 * i + 2] })));
        }

        for (LcdImageLine l : lines) {
            for (LcdImageLine l2 : lines) {
                if (l.equals(l2)) {
                    assertEquals(l.hashCode(), l2.hashCode());
                }
            }
        }
    }

    @Test
    /*
     * assumption : BitVector shift method works
     */
    void shiftTest() {
    		Random random= new Random();
 
        BitVector msb=BitVectorTest.randomBitVectorInstantiation();
        BitVector lsb=BitVectorTest.randomBitVectorInstantiation(msb.size());
        BitVector opacity=BitVectorTest.randomBitVectorInstantiation(msb.size());
        
        LcdImageLine test = new LcdImageLine(msb,lsb,opacity);
        
        int shift=random.nextInt();
        LcdImageLine shifted = test.shift(shift);
        assertEquals(msb.shift(shift),shifted.getMsb());
        assertEquals(lsb.shift(shift),shifted.getLsb());
        assertEquals(opacity.shift(shift),shifted.getOpacity());
    }

    @Disabled
    @Test
    void extractWrappedWorks() {
    	for(int i=0 ; i<40 ; i++) {
	    	Random random= new Random();
	    	 
	        BitVector msb=BitVectorTest.randomBitVectorInstantiation();
	        BitVector lsb=BitVectorTest.randomBitVectorInstantiation(msb.size());
	        BitVector opacity=BitVectorTest.randomBitVectorInstantiation(msb.size());
	        
	        LcdImageLine test = new LcdImageLine(msb,lsb,opacity);
	        
	        int start= (random.nextBoolean() ? 1: -1 )*random.nextInt(15);
	        int size=32*random.nextInt(11);
	        size = size>0 ? size : Math.abs(size);
	        LcdImageLine extracted = test.extractWrapped(start,size);
	        assertEquals(msb.extractWrapped(start,size),extracted.getMsb());
	        assertEquals(lsb.extractWrapped(start,size),extracted.getLsb());
	        assertEquals(opacity.extractWrapped(start,size),extracted.getOpacity());
	
	    }
    }

    @Test
    void MapColorsWorksBasic() {
        LcdImageLine line = new LcdImageLine(
                BitVectorTest.BitVectorInstantiation(new int[] { 0xFFFF0000 }),
                BitVectorTest.BitVectorInstantiation(new int[] { 0xFF00FF00 }),
                BitVectorTest.BitVectorInstantiation(new int[] { 0x00000000 }));
        int palette = 0x39;
        LcdImageLine changedLine = line.mapColors(palette);
        assertEquals(new LcdImageLine(BitVectorTest.BitVectorInstantiation(new int[] { 0x00FFFF00 }),
                BitVectorTest.BitVectorInstantiation(new int[] { 0x00FF00FF }),
                BitVectorTest.BitVectorInstantiation(new int[] { 0x00000000 })), changedLine);
    }
    
    @Test
    void MapColorsWorks() {
    	for (int j=0 ; j<50 ; j++) {
	    	Random random = new Random();
	    	BitVector msb = BitVectorTest.randomBitVectorInstantiation();
	    	BitVector lsb = BitVectorTest.randomBitVectorInstantiation(msb.size());
	    	BitVector opacity = BitVectorTest.randomBitVectorInstantiation(msb.size());
	    	LcdImageLine line = new LcdImageLine( msb, lsb, opacity);
	    	int palette = random.nextInt(0xFF);
	    	LcdImageLine changedLine = line.mapColors(palette);		
	    	
	    	for (int i = 0 ; i<line.size(); i++ ) {
	    		int initialColor = ((msb.testBit(i)?0b10:0b0) + (lsb.testBit(i)?0b1:0b0));
	    		int actualNewColor =  ((changedLine.getMsb().testBit(i)?0b10:0b0) + (changedLine.getLsb().testBit(i)?0b1:0b0));
	    		int expectedColor = Bits.extract(palette,initialColor*2,2);
	    		assertEquals(expectedColor,actualNewColor);
	    	}
	    }
    }

    @Test
    void MapColorsDoesNothingIfColorsSame() {
        LcdImageLine line = new LcdImageLine(
                BitVectorTest.BitVectorInstantiation(new int[] { 0xFFFF0000 }),
                BitVectorTest.BitVectorInstantiation(new int[] { 0xFF00FF00 }),
                BitVectorTest.BitVectorInstantiation(new int[] { 0x00000000 }));
        int palette = 0xE4;
        LcdImageLine changedLine = line.mapColors(palette);
        assertEquals(line, changedLine);
    }

//    @Test
//    void Below1Works() {
//        Random rng = new Random();
//        LcdImageLine vector = new LcdImageLine(
//                BitVectorTest.BitVectorInstantiation(new int[] { rng.nextInt(), rng.nextInt(),
//                        rng.nextInt(), rng.nextInt() }),
//                BitVectorTest.BitVectorInstantiation(new int[] { rng.nextInt(), rng.nextInt(),
//                        rng.nextInt(), rng.nextInt() }),
//                BitVectorTest.BitVectorInstantiation(new int[] { rng.nextInt(), rng.nextInt(),
//                        rng.nextInt(), rng.nextInt() }));
//        LcdImageLine that = new LcdImageLine(
//                BitVectorTest.BitVectorInstantiation(new int[] { rng.nextInt(), rng.nextInt(),
//                        rng.nextInt(), rng.nextInt() }),
//                BitVectorTest.BitVectorInstantiation(new int[] { rng.nextInt(), rng.nextInt(),
//                        rng.nextInt(), rng.nextInt() }),
//                BitVectorTest.BitVectorInstantiation(new int[] { rng.nextInt(), rng.nextInt(),
//                        rng.nextInt(), rng.nextInt() }));
//        LcdImageLine.Builder b = new LcdImageLine.Builder(vector.size());
//        for (int i = 0; i < vector.size() / 8; ++i) {
//            int msbByte = 0, lsbByte = 0;
//            for (int j = 0; j < 8; ++j) {
//                Bits.set(msbByte, j,
//                        that.getOpacity().testBit(i * 8 + j)
//                                ? that.getMsb().testBit(i * 8 + j)
//                                : vector.getMsb().testBit(i * 8 + j));
//                Bits.set(lsbByte, j,
//                        that.getOpacity().testBit(i * 8 + j)
//                                ? that.getLsb().testBit(i * 8 + j)
//                                : vector.getLsb().testBit(i * 8 + j));
//            }
//            b.setBytes(i, msbByte, lsbByte);
//        }
//        assertTrue(b.build().equals(vector.below(that)));
//    }
//    
    @Test
    void belowWorks() {
    		for (int i=0 ; i<20 ; i++) {
	    		BitVector msb1 =  BitVectorTest.randomBitVectorInstantiation();
	    		BitVector lsb1 =  BitVectorTest.randomBitVectorInstantiation(msb1.size());
	    		
	    		BitVector msb2 =  BitVectorTest.randomBitVectorInstantiation(msb1.size());
	    		BitVector lsb2 =  BitVectorTest.randomBitVectorInstantiation(msb1.size());
	    		
	    		BitVector opacity2 =  BitVectorTest.randomBitVectorInstantiation(msb1.size());
	    		
	    		LcdImageLine below = new LcdImageLine(msb1,lsb1,
	    				BitVectorTest.randomBitVectorInstantiation(msb1.size()));
	    		LcdImageLine that = new LcdImageLine(msb2,lsb2,opacity2);
	    		
	    		LcdImageLine computed = below.below(that);
	    		
	    		for (int j=0 ; j<computed.size() ; j++) {
	    			if (opacity2.testBit(j)) {
	    				assertEquals(msb2.testBit(j),computed.getMsb().testBit(j));
	    			} else {
	    				assertEquals(msb1.testBit(j),computed.getMsb().testBit(j));
	    			}
    				
	    		}
    		}
    		
    }

    @Test
    void joinWorks( ) {
    		for (int i=0 ; i<100 ; i++) {
	    		Random random = new Random();
	    	
		    BitVector msb1 =  BitVectorTest.randomBitVectorInstantiation();
			BitVector lsb1 =  BitVectorTest.randomBitVectorInstantiation(msb1.size());
			BitVector opacity1 =  BitVectorTest.randomBitVectorInstantiation(msb1.size());
			
			BitVector msb2 =  BitVectorTest.randomBitVectorInstantiation(msb1.size());
			BitVector lsb2 =  BitVectorTest.randomBitVectorInstantiation(msb1.size());
			BitVector opacity2 =  BitVectorTest.randomBitVectorInstantiation(msb1.size());
			
			int index = random.nextInt(msb1.size());
			
			LcdImageLine test= (new LcdImageLine(msb1,lsb1,opacity1)).join(
					new LcdImageLine(msb2,lsb2,opacity2),index);
			
			int check=0;
			while (check<msb1.size()) {
				while(check<index) {
					assertEquals(msb1.testBit(check),test.getMsb().testBit(check));
					assertEquals(lsb1.testBit(check),test.getLsb().testBit(check));
					assertEquals(opacity1.testBit(check),test.getOpacity().testBit(check));
					check++;
				}
				assertEquals(msb2.testBit(check),test.getMsb().testBit(check));
				assertEquals(lsb2.testBit(check),test.getLsb().testBit(check));
				assertEquals(opacity2.testBit(check),test.getOpacity().testBit(check));
				check++;
			}
    		}
			
    }
    
    @Test
    void BuilderTest() {
        int[] vectorsInt = new int[] { 0xffaadd22, 0xffaaaa00 };
        LcdImageLine.Builder b = new Builder(32);
        for (int i = 0; i < 4; ++i) {
            b.setBytes(i, Bits.extract(vectorsInt[0], 8 * i, 8),
                    Bits.extract(vectorsInt[1], 8 * i, 8));
        }
        BitVector msb = BitVectorTest.BitVectorInstantiation(new int[] { vectorsInt[0] });
        BitVector lsb = BitVectorTest.BitVectorInstantiation(new int[] { vectorsInt[1] });
        assertEquals(new LcdImageLine(msb, lsb, msb.or(lsb)), b.build());
    }
    
    

}
