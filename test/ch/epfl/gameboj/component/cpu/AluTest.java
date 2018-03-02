

package ch.epfl.gameboj.component.cpu;

import org.junit.jupiter.api.Test;

import com.sun.source.doctree.InlineTagTree;

import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import ch.epfl.gameboj.component.cpu.Alu;
import ch.epfl.gameboj.bits.Bits;


class AluTest {
	
	
	@Test
	void maskZNHCWorksOnIndividualKnownValues() {
		final boolean [][] inputs = { 
				{true,false,false,false},
				{false,true,false,false},
				{false,false,true,false},
				{false,false,false,true}	
		};
		final int [] outputs = { 
				Bits.set(0, 7, true),
				Bits.set(0, 6, true),
				Bits.set(0, 5, true),
				Bits.set(0, 4, true),
		};
				
		for (int i=0 ; i<inputs.length; i++) {
			assertEquals(outputs[i], Alu.maskZNHC( inputs[i][0],inputs[i][1],inputs[i][2],inputs[i][3]));
		}
	}
	
	@Test
	void maskZNHCWorksOnRandomValues() {
		int iterations = 50;
		Random randomGenerator = new Random();
		
		final int [] singularOutputs = { 
				Bits.set(0, 7, true),
				Bits.set(0, 6, true),
				Bits.set(0, 5, true),
				Bits.set(0, 4, true),
		};
		
		for (int i=0 ; i<iterations ; i++) {
			int res = 0 ;
			boolean [] inputs = new boolean [4];
			for (int j=0 ; j<4 ; j++) {
				boolean input = randomGenerator.nextBoolean();
				if (input)
					res+=singularOutputs[j];
				inputs[j]=input;
			}
			assertEquals(res, Alu.maskZNHC(inputs[0],inputs[1],inputs[2],inputs[3]) );
		}
	}
	
	
	
	
	@Test
	void unpackValueWorksOnKnown8BitValues() {  // Valeur valable : 0x00_**_**_*0 (16 bit) etoile 1-4 décrivent valeur 16 bits et 3-4 8 bits
		final int [] inputs = {		
				0x00_00_14_50, 
				0x00_00_2f_70, 
				0x00_00_51_30,
				0x00_00_14_50,
				0x00_00_35_20,
				0x00_00_32_10
		};
		final int[] outputs = {
				0x14,
				0x2f,
				0x51,
				0x14,
				0x35,
				0x32
		};
		for (int i=0 ; i<inputs.length; i++) {
			assertEquals( outputs[i], Alu.unpackValue(inputs[i]) );
		}
	}
	
	@Test
	void unpackValueWorksOnKnown16BitValues() {
		final int [] inputs = {		
				0x00_12_14_50, 
				0x00_34_2f_70, 
				0x00_a2_51_30,
				0x00_03_14_50,
				0x00_10_35_20,
				0x00_cb_32_10
		};
		final int[] outputs = {
				0x12_14,
				0x34_2f,
				0xa2_51,
				0x03_14,
				0x10_35,
				0xcb_32
		};
		
		for (int i=0 ; i<inputs.length; i++) {
			assertEquals( outputs[i], Alu.unpackValue(inputs[i]) );
		}
		
	}
	
	@Test
	void unpackValueWorksOnRandom8BitValues() {
		Random randomGenerator = new Random();
		int iterations=50;
		for (int i=0 ; i<iterations ; i++ ) {
			int value = Bits.clip(8,randomGenerator.nextInt());  //Bits 8-15
			boolean z = (value==0) ? true : false ;
			int input=(value<<8)+(Alu.maskZNHC(z,randomGenerator.nextBoolean(),randomGenerator.nextBoolean(),randomGenerator.nextBoolean()));
			
			
			assertEquals(value, Alu.unpackValue(input) );
		}
	}
	
	@Test 
	void unpackValueWorksOnRandom16BitValues() {
		Random randomGenerator = new Random();
		int iterations=50;
		for ( int i=0; i<iterations ; i++) {
			int value = Bits.clip(16,randomGenerator.nextInt());  //Bits 8-24

			boolean z = (value==0) ? true : false ;
			int input=(value<<8)+(Alu.maskZNHC(z,randomGenerator.nextBoolean(),randomGenerator.nextBoolean(),randomGenerator.nextBoolean()));
			int group1 = Bits.clip(4,randomGenerator.nextInt()); //Bits 4-7

			
			assertEquals(value, Alu.unpackValue(input) );
			
		}
	}
	
	// Valeur valable : 0x00_**_**_*0 (16 bit) etoile 1-4 décrivent valeur 16 bits et 3-4 8 bits
	@Test
	void unpackFlagsWorksOnKnownValues() {
		final int [] inputs = {		
				0x00_12_14_50, 
				0x00_34_2f_70, 
				0x00_a2_51_30,
				0x00_00_14_50, 
				0x00_00_2f_70, 
				0x00_00_51_30
				
		};
		final int[] outputs = {
				0x50,
				0x70,
				0x30,
				0x50,
				0x70,
				0x30
		};
		
		for (int i=0 ; i<inputs.length; i++) {
			assertEquals( outputs[i], Alu.unpackFlags(inputs[i]));
		}
	}
	
	@Test
	void unpackFlagsWorksOnRandomValues() {
		Random randomGenerator = new Random();
		int iterations=50;
		for ( int i=0; i<iterations ; i++) {
			int value = Bits.clip(16,randomGenerator.nextInt());  //Bits 8-24
			int flags = Bits.clip(4,randomGenerator.nextInt()); //Bits 4-7
			int input=(value<<8)+(flags<<4);
			
			assertEquals(flags<<4, Alu.unpackFlags(input) );
			}
	}
	
	
	// Valeur valable : 0x00_**_**_*0 (16 bit) etoile 1-4 décrivent valeur 16 bits et 3-4 8 bits
	@Test
	void unpacksFailWithInvalidArgument() {
		
		final int[] invalidArguments = {
				0x10_3f_d2_10,
				0x01_10_3d_40,
				0x00_23_f3_21
		};
		
		for (int argument : invalidArguments) {
			assertThrows(IllegalArgumentException.class,
					() -> Alu.unpackValue(argument));
			assertThrows(IllegalArgumentException.class,
					() -> Alu.unpackFlags(argument));
		}
	}
	
	@Test
	void addsAreTransitive() {
		Random randomGenerator = new Random();
		int iterations=50;
		for ( int i=0; i<iterations ; i++) {
			int l = Bits.clip(8,randomGenerator.nextInt());
			int r = Bits.clip(8,randomGenerator.nextInt());
			assertEquals(Alu.add(l,r,true),Alu.add(r,l,true));
			assertEquals(Alu.add(l,r,false),Alu.add(r,l,false));
			assertEquals(Alu.add(l,r),Alu.add(r,l));
		}
	}

	@Test
	void add1WorksOnRandomValuesAndTrue() {
		final int C = 1<<4;
		final int H = 1<<5;
		final int Z = 1<<7;
		Random randomGenerator = new Random();
		int iterations=50;
		for ( int i=0; i<iterations ; i++) {
			int l = Bits.clip(8,randomGenerator.nextInt());
			int r = Bits.clip(8,randomGenerator.nextInt());
			int c=0;
			int h=0;
			int z=0;
			
			int res = l+r;
			res+=1;
			
			if (Bits.test(res,8))
				c=C;
			res=Bits.clip(8,res);
			if (res==0)
				z=Z;
			if ( (Bits.test(l^r,4)!=Bits.test(l+r+1,4)))
				h=H;
			res = res << 8;
			
			res+=h+c+z;
			
			
			assertEquals(res, Alu.add(l,r,true));
			}	
	}

		
			
		
	@Test
	void add1WorksOnRandomValuesAndFalse() {
		final int C = 1<<4;
		final int H = 1<<5;
		final int Z = 1<<7;
		Random randomGenerator = new Random();
		int iterations=50;
		for ( int i=0; i<iterations ; i++) {
			int l = Bits.clip(8,randomGenerator.nextInt());
			int r = Bits.clip(8,randomGenerator.nextInt());
			int c=0;
			int h=0;
			int z=0;
			
			int res = l+r;
			
			
			if (Bits.test(res,8))
				c=C;
			res=Bits.clip(8,res);
			if (res==0)
				z=Z;
			if (Bits.test(l^r,4)!=Bits.test(l+r,4))
				h=H;
			res = res << 8;
			
			res+=h+c+z;
			
			
			assertEquals(res, Alu.add(l,r,false));
			}	
	}
	
		
	
//	@Test
//	void add1WorksOnTwoComplementRandomValues() {
//		final int C = 1<<4;
//		final int H = 1<<5;
//		final int Z = 1<<7;
//		final int res=C+Z+H;
//		
//		Random randomGenerator = new Random();
//		int iterations=5;
//		for ( int i=0; i<iterations ; i++) {
//			int l = Bits.clip(8,randomGenerator.nextInt());
//			int r = Bits.clip(8, Bits.complement8(l)+1);
//			
//			assertEquals(res, Alu.unpackFlags(Alu.add(l,r,false)));
//			}	
//	}
		
	@Test
	void addsFailWithInvalidArgument() {
			
			final int[] invalidArguments = {
					0xc_12_23,
					0x2_12_32,
					0x1_a3_56,
			};
			
			for (int i=0 ; i<invalidArguments.length ; i++) {
				
				for (int j=0 ; j<invalidArguments.length ; j++) {
					int k=i;
					int l=j;
					assertThrows(IllegalArgumentException.class,
					() -> Alu.add(invalidArguments[k],invalidArguments[l]));
					assertThrows(IllegalArgumentException.class,
					() -> Alu.add(invalidArguments[l],invalidArguments[k]));
				}
			}
			
		}
	
	
	@Test
	void add16HIsCompatibleWith8BitAdd() {
		Random randomGenerator = new Random();
		final int ITERATIONS=50;
		
		for ( int i=0; i<ITERATIONS ; i++) {
			int l = Bits.clip(16,randomGenerator.nextInt());
			int r = Bits.clip(16,randomGenerator.nextInt());
			int highR = Bits.extract(r,8,8);
			int highL=Bits.extract(l,8,8);
			int lowL=Bits.clip(8,l);
			int lowR=Bits.clip(8,r);
			int lowSum=Alu.add(lowR,lowL);
			boolean carry = (Bits.test(lowSum,4));
					
			int highSum=Alu.add(highR,highL,carry);
			int res=packNewValue(highSum,(Alu.unpackValue(highSum)<<8)+Alu.unpackValue(lowSum));
			
			
				
			assertEquals(res, Alu.add16H(l,r));
		}
		
	}
	
	@Test
	void add16LIsCompatibleWith8BitAdd() {
		Random randomGenerator = new Random();
		final int ITERATIONS=50;
		
		for ( int i=0; i<ITERATIONS ; i++) {
			int l = Bits.clip(16,randomGenerator.nextInt());
			int r = Bits.clip(16,randomGenerator.nextInt());
			int highR = Bits.extract(r,8,8);
			int highL=Bits.extract(l,8,8);
			int lowL=Bits.clip(8,l);
			int lowR=Bits.clip(8,r);
			int lowSum=Alu.add(lowR,lowL);
			boolean carry = (Bits.test(lowSum,4));
					
			int highSum=Alu.add(highR,highL,carry);
			int res=packNewValue(lowSum,(Alu.unpackValue(highSum)<<8)+Alu.unpackValue(lowSum));
			
			
				
			assertEquals(res, Alu.add16L(l,r));
		}
		
	}
	
	
	
	
	
	@Test
	void add16sFailWithInvalidArgument() {
		final int[] invalidArguments = {
				0x10_3f_d2_10,
				0x01_10_3d_40,
				0x00_23_f3_21
		};
		for (int i=0 ; i<invalidArguments.length ; i++) {
			for ( int j=0 ; j<invalidArguments.length ; j++) {
				int k=i;
				int l=j;
				assertThrows(IllegalArgumentException.class,
						() -> Alu.add16L(invalidArguments[k],invalidArguments[l]));
			}
		}
	}
	
	
	@Test
	void subFailsForInvalidArguments() {
		final int[] invalidArguments = {
				0x10_3f_d2_10,
				0x01_10_3d_40,
				0x00_23_f3_21,
				0xc_12_23,
				0x2_12_32,
				0x1_a3_56
		};
		
		for (int i : invalidArguments) {
			for (int j: invalidArguments) {
			assertThrows( IllegalArgumentException.class,
					() -> Alu.sub(i,j,false));
			assertThrows( IllegalArgumentException.class,
					() -> Alu.sub(i,j,true));
			}
		}
		
	}
	
	
	@Test
	void subsReturnCorrectValue() {
        assertEquals(0, Alu.unpackValue(Alu.sub(0x10, 0x10)));
        assertEquals(0x90, Alu.unpackValue(Alu.sub(0x10, 0x80)));
        assertEquals(0xFF, Alu.unpackValue(Alu.sub(0x01, 0x01, true)));
	};
	
	@Test
	void subsReturnCorrectFlags() {
	    assertEquals(0xC0, Alu.unpackFlags(Alu.sub(0x10, 0x10)));
        assertEquals(0x50, Alu.unpackFlags(Alu.sub(0x10, 0x80)));
        assertEquals(0x70, Alu.unpackFlags(Alu.sub(0x01, 0x01, true)));
	}
		
		
		
	
//	@Test
//	void subWorksOnKnownValuesAndFalse() {
//		final int Z=1<<8;
//		final int N=1<<7;
//		final int C=1<<5;
//		final int H = 1<<6;
//		final int inputs [][]= {
//				{0b1010_0001,0b1010_0001},
//				{0b0010_0010,0b0100_1011},
//				{0b0110_0110,0b0010_0010}
//				
//		};
//		final int expected[]= {
//				Z+N,
//				C+N+H+(0b1101_0111<<8),
//				N+(0b0100_0100<<8)
//				
//		};
//		
//		
//		for (int i=0 ; i<inputs.length ; i++) {
//			System.out.println("inputs " + inputs[i][0] + " - " + inputs[i][1] );
//			System.out.println("expected : " + expected[i] + " output : " + Alu.sub(inputs[i][0],inputs[i][1]));
//			System.out.println("distance = " + (expected[i]-Alu.sub(inputs[i][0],inputs[i][1])) + " ");
//			System.out.println();
//			assertEquals(expected[i], Alu.sub(inputs[i][0],inputs[i][1]));
//		}
//		
//		
//	}
	
	
	@Test
	void subWorksForRandomValues() {
		Random randomGenerator = new Random();
		boolean z;
		boolean c;
		boolean h;
		final int ITERATIONS = 50 ; 
		
		for (int i=0 ; i<ITERATIONS ; i++) {
			int l = Bits.clip(8,randomGenerator.nextInt());
			int r = Bits.clip(8,randomGenerator.nextInt());
			
			int lowL = Bits.clip(4,l);
			int lowR = Bits.clip(4,r);
			
			int res=Bits.clip(8,l-r);
			
			c=(l<r);
			h=(lowL<lowR);
			
			z=(res==0);
			
			res=pack(Alu.maskZNHC(z,true,h,c),res);
			
			System.out.println("inputs " + l + " - " + r );
			System.out.println("expected : " + res + " output : " + Alu.sub(l,r));
			System.out.println();
			assertEquals(res, Alu.sub(l,r));
			} 
			
				
	}
	
	
	@Test
	void bcdAdjustFailsForInvalidArgument() {
		final int[] VINPUTS={
			0x1_20,
			0xc0_01
		};
		Random randomGenerator = new Random();
			
		for( int i : VINPUTS) {
			assertThrows(IllegalArgumentException.class,
					() -> Alu.bcdAdjust(i,randomGenerator.nextBoolean(),randomGenerator.nextBoolean(),randomGenerator.nextBoolean()));
		}
	}
	
	@Test
	void bcdAdjustWorksValues() {
		assertEquals(0x73, Alu.unpackValue(Alu.bcdAdjust(0x6D, false, false, false)));
	    assertEquals(0x09, Alu.unpackValue(Alu.bcdAdjust(0x0F, true, true, false)));
	}
	@Test
    void bcdAdjustWorksFlags() {
        assertEquals(0x00, Alu.unpackFlags(Alu.bcdAdjust(0x6D, false, false, false)));
        assertEquals(0x40, Alu.unpackFlags(Alu.bcdAdjust(0x0F, true, true, false)));
    }
	

	
	
	@Test
	void AndFailsForInvalidIntegers() {
		
		assertThrows(IllegalArgumentException.class,
				() -> Alu.and(0b1<<8,0b0100_1000) );
		assertThrows(IllegalArgumentException.class,
				() -> Alu.and(0b01011_1100, 0b1<<8));
	}
	@Test
    void AndReturnsGoodFlags() {
	    assertEquals(0b1010_0000, Alu.unpackFlags(Alu.and(0, 0)));
	    assertEquals(0b0010_0000, Alu.unpackFlags(Alu.and(1, 1)));
	}
	@Test
    void AndReturnsGoodValue() {
	    Random randomGenerator = new Random();
	    final int ITERATIONS=5;
	    for (int i = 0;i<ITERATIONS;++i) {
	        int l = randomGenerator.nextInt(0b1_0000_0000);
	        int r = randomGenerator.nextInt(0b1_0000_0000);
	        assertEquals(l&r, Alu.unpackValue(Alu.and(l, r)));
	    }
	}
	@Test
    void OrFailsForInvalidIntegers() {
        
        assertThrows(IllegalArgumentException.class,
                () -> Alu.or(0b1<<8,0b0100_1000) );
        assertThrows(IllegalArgumentException.class,
                () -> Alu.or(0b01011_1100, 0b1<<8));
    }
    @Test
    void OrReturnsGoodFlags() {
        assertEquals(0b1000_0000, Alu.unpackFlags(Alu.or(0, 0)));
        assertEquals(0b0000_0000, Alu.unpackFlags(Alu.or(1, 1)));
    }
    @Test
    void orReturnsGoodValue() {
        Random randomGenerator = new Random();
        final int ITERATIONS=5;
        for (int i = 0;i<ITERATIONS;++i) {
            int l = randomGenerator.nextInt(0b1_0000_0000);
            int r = randomGenerator.nextInt(0b1_0000_0000);
            assertEquals(l|r, Alu.unpackValue(Alu.or(l, r)));
        }
    }
    @Test
    void XorFailsForInvalidIntegers() {
        
        assertThrows(IllegalArgumentException.class,
                () -> Alu.xor(0b1<<8,0b0100_1000) );
        assertThrows(IllegalArgumentException.class,
                () -> Alu.xor(0b01011_1100, 0b1<<8));
    }
    @Test
    void XorReturnsGoodFlags() {
        assertEquals(0b1000_0000, Alu.unpackFlags(Alu.xor(0, 0)));
        assertEquals(0b0000_0000, Alu.unpackFlags(Alu.xor(1, 0)));
    }
    @Test
    void XorReturnsGoodValue() {
        Random randomGenerator = new Random();
        final int ITERATIONS=5;
        for (int i = 0;i<ITERATIONS;++i) {
            int l = randomGenerator.nextInt(0b1_0000_0000);
            int r = randomGenerator.nextInt(0b1_0000_0000);
            assertEquals(l^r, Alu.unpackValue(Alu.xor(l, r)));
        }
    }
    
    @Test
    void shiftLeftFailsForInvalidIntegers() {
        assertThrows(IllegalArgumentException.class,
                () -> Alu.shiftLeft(0b1<<8) );
    }
    @Test
    void shiftLeftReturnsGoodFlags() {
        assertEquals(0b1000_0000, Alu.unpackFlags(Alu.shiftLeft(0)));
        assertEquals(0b1001_0000, Alu.unpackFlags(Alu.shiftLeft(0b1000_0000)));
        assertEquals(0b0000_0000, Alu.unpackFlags(Alu.shiftLeft(0b0100_0000)));
    }
    @Test
    void shiftLeftReturnsGoodValue() {
        Random randomGenerator = new Random();
        final int ITERATIONS=5;
        for (int i = 0;i<ITERATIONS;++i) {
            int l = randomGenerator.nextInt(0b1000_0000);
            assertEquals(Bits.clip(8, l<<1), Alu.unpackValue(Alu.shiftLeft(l)));
        }
    }
    @Test
    void shiftRightAFailsForInvalidIntegers() {
        assertThrows(IllegalArgumentException.class,
                () -> Alu.shiftRightA(0b1<<8) );
    }
    @Test
    void shiftRightAReturnsGoodFlags() {
        assertEquals(0b1000_0000, Alu.unpackFlags(Alu.shiftRightA(0)));
        assertEquals(0b1001_0000, Alu.unpackFlags(Alu.shiftRightA(0b0000_0001)));
        assertEquals(0b0000_0000, Alu.unpackFlags(Alu.shiftRightA(0b0100_0000)));
    }
    @Test
    void shiftRightAReturnsGoodValue() {
        Random randomGenerator = new Random();
        final int ITERATIONS=50;
        for (int i = 0;i<ITERATIONS;++i) {
            int l = randomGenerator.nextInt(0b1000_0000);
            assertEquals(Bits.clip(8, l>>1), Alu.unpackValue(Alu.shiftRightA(l)));
        }
    }
    @Test
    void shiftRightLFailsForInvalidIntegers() {
        assertThrows(IllegalArgumentException.class,
                () -> Alu.shiftRightL(0b1<<8) );
    }
    @Test
    void shiftRightLReturnsGoodFlags() {
        assertEquals(0b1000_0000, Alu.unpackFlags(Alu.shiftRightL(0)));
        assertEquals(0b1001_0000, Alu.unpackFlags(Alu.shiftRightL(0b0000_0001)));
        assertEquals(0b0000_0000, Alu.unpackFlags(Alu.shiftRightL(0b0100_0000)));
    }
    @Test
    void shiftRightLReturnsGoodValue() {
        Random randomGenerator = new Random();
        final int ITERATIONS=5;
        for (int i = 0;i<ITERATIONS;++i) {
            int l = randomGenerator.nextInt(0b1000_0000);
            assertEquals(Bits.clip(8, l>>>1), Alu.unpackValue(Alu.shiftRightL(l)));
        }
    }
    @Test
    void rotateFailsForInvalidInput() {
        assertThrows(IllegalArgumentException.class,
                () -> Alu.rotate(Alu.RotDir.LEFT, 0x100));
        assertThrows(IllegalArgumentException.class,
                () -> Alu.rotate(null, 0)); //---- Sure qu'il faut lancer IllegalArgumentException ????
    }
    @Test
    void rotateReturnsGoodFlags() {
        assertEquals(0x10, Alu.unpackFlags(Alu.rotate(Alu.RotDir.LEFT, 0x80)));
        assertEquals(0, Alu.unpackFlags(Alu.rotate(Alu.RotDir.RIGHT, 0x80)));
    }
    @Test
    void rotateReturnsGoodValue() {
        assertEquals(0x01, Alu.unpackValue(Alu.rotate(Alu.RotDir.LEFT, 0x80)));
        assertEquals(0x40, Alu.unpackValue(Alu.rotate(Alu.RotDir.RIGHT, 0x80)));
    }
    @Test
    void rotateCarryFailsForInvalidInput() {
        assertThrows(IllegalArgumentException.class,
                () -> Alu.rotate(Alu.RotDir.LEFT, 0x100, false));
        assertThrows(IllegalArgumentException.class,
                () -> Alu.rotate(null, 0, false));
    }
    @Test
    void rotateCarryReturnsGoodFlags() {
        assertEquals(0x90, Alu.unpackFlags(Alu.rotate(Alu.RotDir.LEFT, 0x80, false)));
        assertEquals(0, Alu.unpackFlags(Alu.rotate(Alu.RotDir.LEFT, 0x00, true)));
    }
    @Test
    void rotateCarryReturnsGoodValue() {
        assertEquals(0x00, Alu.unpackValue(Alu.rotate(Alu.RotDir.LEFT, 0x80, false)));
        assertEquals(0x01, Alu.unpackValue(Alu.rotate(Alu.RotDir.LEFT, 0x00, true)));
    }
    @Test
    void swapFailsForInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> Alu.swap(0x100));
    }
    @Test
    void swapReturnsGoodValue() {
        assertEquals(0x23, Alu.unpackValue(Alu.swap(0x32)));
        assertEquals(0xa1, Alu.unpackValue(Alu.swap(0x1a)));
        assertEquals(0x0, Alu.unpackValue(Alu.swap(0)));
    }
    
  
    @Test
    void swapReturnsGoodFlags() {
        assertEquals(0, Alu.unpackFlags(Alu.swap(0x32)));
        assertEquals(0x80, Alu.unpackFlags(Alu.swap(0)));
    }
    @Test
    void testBitFailsForInvalidInput() {
        assertThrows(IllegalArgumentException.class,
                () -> Alu.testBit(0x100, 1));
        assertThrows(IndexOutOfBoundsException.class,
                () -> Alu.testBit(0, 10));
        assertThrows(IndexOutOfBoundsException.class,
                () -> Alu.testBit(0, -1));
    }
    @Test
    void testBitReturnsGoodFlagAndNoValue() {
        assertEquals(0xA0, Alu.unpackFlags(Alu.testBit(0x20, 5)));
        assertEquals(0x20, Alu.unpackFlags(Alu.testBit(0x20, 6)));
        assertEquals(0, Alu.unpackValue(Alu.testBit(0x20, 5)));

    }
    
    
	private static int pack(int flags, int value) {
		return (flags+(value<<8));
		
		
	}
	
	
	private static int packNewValue(int packedValue , int newValue) {
		return pack(Alu.unpackFlags(packedValue), newValue);
	}
	
	private static int packValue(int value) {
		return pack(0,value);
	}
	
	private static int pack(boolean z, boolean n, boolean h, boolean c, int value ) {
		return pack(Alu.maskZNHC(z,n,h,c),value);
	}

	
	
	
	
	
	
	
	
	
}


