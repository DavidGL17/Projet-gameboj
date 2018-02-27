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
	
	public static int maskZNHC(boolean z, boolean n, boolean h, boolean c) {
		
		int res=0;
		
		if (z) {
			res+=Flag.Z.mask();
		}
		if (n) {
			res+=Flag.N.mask();
		}
		if (h) {
			res+=Flag.H.mask();
		}
		if (c) {
			res+=Flag.C.mask();
		}

		return res;

	}
	
	public static int unpackValue(int valueFlags) {
		return -1;
	}
	
	public static int unpackFlags(int valueFlags) {
		return -1;
	}
	
	public static int add(int l, int r, boolean c0) {
		return -1;
	}
	
	public static int add(int l, int r) {
		return -1;
	}
	
	public static int add16L(int l, int r) {
		return -1;
	}
	
	public static int add16H (int l, int r) {
		return -1;
	}
	
	public static int sub(int l, int r, boolean b0) {
		return -1;
	}
	
	public static int sub(int l, int r) {
		return -1;
	}
	
	public static int bcdAdjust(int v, boolean n, boolean h, boolean c) {
		return 1;
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
	
	// Valeur valable : 0x00_**_**_*0 (16 bit) etoile 1-4 décrivent valeur 16 bits et 3-4 8 bits
	
	/**
	 * Checks if value is a correct packed Value where the result is an 8-bit int
	 * @param value - an int
	 * @throws IllegalArgumentException if value cannot be a packed value
	 */
	private static void check8BitPackedValue(int value) {
		final int mask=0x11_11_00_01;
		if((value & mask) != 0) {
			throw new IllegalArgumentException();
		}
		
	}
	

	/**
	 * Checks if value is a correct packed Value where the result is a 16-bit int
	 * @param value - an int
	 * @throws IllegalArgumentException if value cannot be a packed value
	 */
	private static void check16BitPackedValue(int value) {
		final int mask = 0x11_00_00_01;
		if ((value & mask) != 0) {
			throw new IllegalArgumentException();
		}
	}
	
	private static void checkPackedValue(int value) {
		check16BitPackedValue(value);
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
		return pack(maskZNHC(z,n,h,c),value);
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
