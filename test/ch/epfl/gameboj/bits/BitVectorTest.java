package ch.epfl.gameboj.bits;

import org.junit.jupiter.api.Test;
import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import ch.epfl.gameboj.bits.BitVector;

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
	
	final BitVectorExample[] ExampleBuilders = {
		new BitVectorExample(0, {{}}, "")
	};
	
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
	
	
}
