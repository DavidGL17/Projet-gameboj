

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
			assertEquals( Alu.maskZNHC(outputs[i], inputs[i][0],inputs[i][1],inputs[i][2],inputs[i][3]));
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
	
	
	//-----------------------------------Unpack lace-t-il des exceptions ? -----------------------------------------
	
	
	
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
	
	
	//----------------------------------------------------------REPRENDRE ICI-------------------------------------
	
	@Test
	void add1WorksOnRandomValuesAndFalse() {
		Random randomGenerator = new Random();
		int iterations=5;
		for ( int i=0; i<iterations ; i++) {
			int l = Bits.clip(8,randomGenerator.nextInt());
			int r = Bits.clip(8,randomGenerator.nextInt());
			
			assertEquals(l+r, Alu.add(l,r,false));
			}	
	
	@Test
	void add1WorksOnRandomValuesAndFalse() {
		Random randomGenerator = new Random();
		int iterations=5;
		for ( int i=0; i<iterations ; i++) {
			int l = Bits.clip(8,randomGenerator.nextInt());
			int r = Bits.clip(8,randomGenerator.nextInt());
			
			assertEquals(l+r, Alu.add(l,r,true));
			}	
	}
		
	
	
	//------------------------------------PROBABLEMENT BON---------------------------------------------------
	
	
	@Test
	void add2WorksOnRandomValues() {
		Random randomGenerator = new Random();
		int iterations=5;
		for ( int i=0; i<iterations ; i++) {
			int l = Bits.clip(8,randomGenerator.nextInt());
			int r = Bits.clip(8,randomGenerator.nextInt());
			
			assertEquals(l+r, Alu.add(l,r));
			}
	}
	
	

	@Test
	void AndFailsForInvalidIntegers() {
		
		assertThrows(IllegalArgumentException.class,
				() -> Alu.and(0b1<<8,0b0100_1000) );
		assertThrows(IllegalArgumentException.class,
				() -> Alu.and(0b01011_1100, 0b1<<8));
	}
	
	
	
	
	
	
	
	
	
	
	
}


