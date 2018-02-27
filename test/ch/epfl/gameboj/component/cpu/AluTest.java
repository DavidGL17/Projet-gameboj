

package ch.epfl.gameboj.component.cpu;

import org.junit.jupiter.api.Test;
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
		int iterations = 5;
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
		};
		final int[] outputs = {
				0x14,
				0x2f,
				0x51
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
		};
		final int[] outputs = {
				0x12_14,
				0x34_2f,
				0xa2_51
		};
		
		for (int i=0 ; i<inputs.length; i++) {
			assertEquals( outputs[i], Alu.unpackValue(inputs[i]) );
		}
		
	}
	
	@Test
	void unpackValueWorksOnRandom8BitValues() {
		Random randomGenerator = new Random();
		int iterations=5;
		for (int i=0 ; i<iterations ; i++ ) {
			int value = Bits.clip(8,randomGenerator.nextInt());  //Bits 8-15
			int group1 = Bits.clip(4,randomGenerator.nextInt()); //Bits 4-7
			int input=value<<8+group1<<4;
			
			assertEquals(value, Alu.unpackValue(input) );
		}
	}
	
	@Test 
	void unpackValueWorksOnRandom16BitValues() {
		Random randomGenerator = new Random();
		int iterations=5;
		for ( int i=0; i<iterations ; i++) {
			int value = Bits.clip(16,randomGenerator.nextInt());  //Bits 8-24
			int group1 = Bits.clip(4,randomGenerator.nextInt()); //Bits 4-7
			int input=value<<8+group1<<4;
			
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
		int iterations=5;
		for ( int i=0; i<iterations ; i++) {
			int value = Bits.clip(16,randomGenerator.nextInt());  //Bits 8-24
			int flags = Bits.clip(4,randomGenerator.nextInt()); //Bits 4-7
			int input=value<<8+flags<<4;
			
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
		int iterations=5;
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
		int iterations=5;
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
			res=res<<8;
			if (Bits.test(l^r,5)!=Bits.test(l+r,5))
				h=H;
			
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
		int iterations=5;
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
			res=res<<8;
			if (Bits.test(l^r,5)!=Bits.test(l+r,5))
				h=H;
			if (res==0)
					z=Z;
			res+=h+c+z;
			
			assertEquals(res, Alu.add(l,r,true));
			}	
	}
	
	@Test
	void add1WorksOnTwoComplementRandomValues() {
		final int C = 1<<4;
		final int H = 1<<5;
		final int Z = 1<<7;
		final int res=C+Z+H;
		
		Random randomGenerator = new Random();
		int iterations=5;
		for ( int i=0; i<iterations ; i++) {
			int l = Bits.clip(8,randomGenerator.nextInt());
			int r = Bits.complement8(l)+1;
			
			assertEquals(res, Alu.add(l,r,false));
			assertEquals(res, Alu.add(l,r-1,true));
			}	
	}
		
	@Test
	void addsFailWithInvalidArgument() {
			
			final int[] invalidArguments = {
					0x10_3f_d2_10,
					0x01_10_3d_40,
					0x00_23_f3_21
			};
			
			for (int i=0 ; i<invalidArguments.length ; i++) {
				
				for (int j=0 ; j<invalidArguments.length ; j++) {
					int k=i;
					int l=j;
					assertThrows(IllegalArgumentException.class,
					() -> Alu.unpackValue(invalidArguments[k]));
					assertThrows(IllegalArgumentException.class,
					() -> Alu.unpackFlags(invalidArguments[l]));
				}
			}
			
		}
	
	
	@Test
	void add16HIsCompatibleWith8BitAdd() {
		Random randomGenerator = new Random();
		final int ITERATIONS=5;
		final int C=1 << 4;
		for ( int i=0; i<ITERATIONS ; i++) {
			int l = Bits.clip(16,randomGenerator.nextInt());
			int r = Bits.clip(16,randomGenerator.nextInt());
			int highR = Bits.extract(r,8,8)<<8;
			int highL=Bits.extract(l,8,8)<<8;
			int lowL=Bits.clip(8,l);
			int lowR=Bits.clip(8,l);
			
			int res = Alu.add(highL,highR+( (Alu.unpackFlags(Alu.add(lowL,lowR,false)) & C)/C) )+ pack(0,Alu.unpackValue(Alu.add(lowL,lowR)));	;	
			assertEquals(res, Alu.add16H(l,r));
		}
		
	}
	
	@Test
	void add16LIsCompatibleWith8BitAdd() {
		Random randomGenerator = new Random();
		final int ITERATIONS=5;
		final int C=1 << 4;
		for ( int i=0; i<ITERATIONS ; i++) {
			int l = Bits.clip(16,randomGenerator.nextInt());
			int r = Bits.clip(16,randomGenerator.nextInt());
			int highR = Bits.extract(r,8,8)<<8;
			int highL=Bits.extract(l,8,8)<<8;
			int lowL=Bits.clip(8,l);
			int lowR=Bits.clip(8,l);
			
			int res = Alu.add(lowL,lowR) + pack(0,Alu.unpackValue(Alu.add(highL,highR+( (Alu.unpackFlags(Alu.add(lowL,lowR,false)) & C)/C) )));	;	
			assertEquals(res, Alu.add16H(l,r));
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
	void AndFailsForInvalidIntegers() {
		
		assertThrows(IllegalArgumentException.class,
				() -> Alu.and(0b1<<8,0b0100_1000) );
		assertThrows(IllegalArgumentException.class,
				() -> Alu.and(0b01011_1100, 0b1<<8));
	}
	
	
	
	
	private static int pack(int flags, int value) {
		return (flags+(value<<8));
	}
	
	
	
	
	
	
	
	
	
}


