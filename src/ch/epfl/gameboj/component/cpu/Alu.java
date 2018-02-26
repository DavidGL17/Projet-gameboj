/**
 * 
 */
package ch.epfl.gameboj.component.cpu;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

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
	
	/**
	 * Computes the AND operation and corresponding flags
	 * @param l - an int, first term
	 * @param r - an int, second term
	 * @return the packed valueFlags
	 */
	public static int and(int l,int r ) {
		
		Preconditions.checkBits8(l);
		Preconditions.checkBits8(r);

		final boolean n=false;
		final boolean h=true;
		final boolean c=false;
		
		int result = l & r;
		
		return pack(n,h,c,result);
	}
	
	/**
	 * Computes the OR operation and corresponding flags
	 * @param l - an int, first term
	 * @param r - an int, second term
	 * @return the packed valueFlags
	*/
	public static int or(int l, int r) {
		
		Preconditions.checkBits8(l);
		Preconditions.checkBits8(r);
		
		final boolean n = false;
		final boolean h = false;
		final boolean c = false;
		
		int result = l | r;
		return pack(n,h,c,result);
	}
	
	/**
	 * Computes the XOR operation and corresponding flags
	 * @param l - an int, first term
	 * @param r - an int, second term
	 * @return the packed valueFlags
	*/
	public static int xor(int l,int r) {
	
		Preconditions.checkBits8(l);
		Preconditions.checkBits8(r);
		
		final boolean n = false;
		final boolean h = false;
		final boolean c = false;
		
		int result = l ^ r;
		
		return pack(n,h,c,result);
	}
	
	
	/**
	 * Computes the left shift and corresponding flags
	 * @param v - an int, the value to shift
	 * @return the packed valueFlags
	*/
	public static int shiftLeft(int v) {
		
		Preconditions.checkBits8(v);
		
		final boolean n = false;
		final boolean h = false;
		boolean c = false;
		
		if (Bits.test(v,7))
			c=true;
		
		int result=v<<1;
		
		return pack(n,h,c,result);
		
	}
	
	/**
	 * Computes the rigth shift and corresponding flags
	 * @param v - an int, the value to shift
	 * @return the packed valueFlags
	*/
	public static int shiftRight(int v) {
		
		Preconditions.checkBits8(v);

		final boolean n = false;
		final boolean h = false;
		boolean c = false;
		
		if (Bits.test(v,7))
			c=true;
		
		int result = v>>1;
		
		return pack(n,h,c,result);
	}
	
	/**
	 * Computes the rotation in a direction and corresponding flags
	 * @param d - a RotDir, the direction of the rotation
	 * @param v - an int, the value to rotate
	 * @return the packed valueFlags
	*/
	public static int rotate(RotDir d, int v) {
		
		Preconditions.checkBits8(v);

		final boolean n=false;
		final boolean h=false;
		boolean c=false;
		
		int result=0;
		
		c=Bits.test(v,(7+d.ordinal())%8);
		result=Bits.rotate(8,v,rotate(d));
		
		return pack(n,h,c,result);
		
	}
	
	/**
	 * Computes the rotation of a value and its carry, and corresponding flags
	 * @param d - a RotDir, the direction of the rotation
	 * @param v - an int, the value
	 * @param c - a boolean, wether the carry is 1
	 * @return the packed valueFlags
	 */
	public static int rotate(RotDir d, int v, boolean c) {
		
		Preconditions.checkBits8(v);

		final boolean n=false;
		final boolean h=false;
		
		int result=v;
		if (c)
			result += Bits.mask(8);
		
		
		result = Bits.rotate(9,result,rotate(d));
		if (result >= 1<<8)
			c=true;
		
		result = Bits.set(result,8,false);
		
		return pack(n,h,c,result);
		
	}
	
	/**
	 * Computes the byte where the 4 LSB and 4 MSB swap position
	 * @param v - an int, the value
	 * @return the packed valueFlags
	 */
	public static int swap(int v) {
		
		Preconditions.checkBits8(v);

		final boolean n = false;
		final boolean h = false;
		final boolean c = false;
		
		int lsb=Bits.extract(v,0,4);
		int msb=Bits.extract(v,4,4);
		
		int result=lsb<<4 + msb;
		
		return pack(n,h,c,result);
	}
	
	
	/**
	 * Computes wether a bit is activated in a 8-bit int
	 * @param v - an int, the 8-bit int 
	 * @param bitIndex - an int, the index
	 * @return 0b_Z010_0000 where Z=1 iff the bit is activated
	 */
	public static int testBit(int v, int bitIndex) {
		
		Preconditions.checkBits8(v);
		Objects.checkIndex(bitIndex,8);
		
		final boolean z = Bits.test(v,bitIndex);
		final boolean n = false;
		final boolean h = true;
		final boolean c = false;
		
		return pack(z,n,h,c);
		
	}
	
	
	/**
	 * AUXILIARY
	 * 
	 * @param d
	 * @return
	 */
	private static int rotate(RotDir d) {
		return (d.ordinal()*2-1);
	}
	
	/**
	 * AUXILIARY
	 * 
	 * @param n
	 * @param h
	 * @param c
	 * @param value
	 * @return
	 */
	private static int pack(boolean n, boolean h, boolean c, int value ) {
		boolean z = false;
		if (value==0)
			z=true;
		return pack(z,n,h,c,value);
	}
	
	/**
	 * AUXILIARY
	 * 
	 * @param z
	 * @param n
	 * @param h
	 * @param c
	 * @param value
	 * @return
	 */
	private static int pack(boolean z, boolean n, boolean h, boolean c, int value ) {
		return pack(maskZNHC(z,n,h,c,value),value);
	}
	
	/**
	 * AUXILIARY
	 * 
	 * @param z
	 * @param n
	 * @param h
	 * @param c
	 * @return
	 */
	private static int pack(boolean z, boolean n, boolean h, boolean c) {
		return pack(z,n,h,c,0);
	}
	
	/**
	 * AUXILIARY
	 * 
	 * @param flags
	 * @param value
	 * @return
	 */
	private static int pack(int flags, int value) {
		return (flags+(value<<8));
	}
	
}
