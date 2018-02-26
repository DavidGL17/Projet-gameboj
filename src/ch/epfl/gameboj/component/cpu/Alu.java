/**
 * 
 */
package ch.epfl.gameboj.component.cpu;

import ch.epfl.gameboj.bits.Bit;

/**
 * 
 * @author David Gonzalez leon (270845)
 * @author Melvin Malonga-Matouba (288405)
 * 
 */
public final class Alu {

	private Alu() {};
	
	public enum Flag implements Bit{
		UNUSED_0,
		UNUSED_1,
		UNUSED_2,
		UNUSED_3,
		C,
		H,
		N,
		Z;	
		
	}
	
	public enum RotDir {
		LEFT,
		RIGHT;
	}
	
	public static int and(int l,int r ) {
		boolean z=false;
		final boolean n=false;
		final boolean h=true;
		final boolean c=false;
		
		int result = l & r;
		
		if (result==0)
			z=true;
		
		return pack(z,n,h,c,result);
	}
	
	
	private static int pack(boolean z, boolean n, boolean h, boolean c, int value ) {
		return pack(maskZNHC(z,n,h,c,value),value);
	}
	
	private static int pack (int flags, int value) {
		return (flags+(value<<8));
	}
	
}
