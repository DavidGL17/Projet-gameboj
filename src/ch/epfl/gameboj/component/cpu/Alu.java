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
		UNUSED_0(0b1),
		UNUSED_1(0b10),
		UNUSED_2(0b100),
		UNUSED_3(0b1000),
		C(0b10000),
		H(0b100000),
		N(0b1000000),
		Z(0b10000000);	
		
	    private int mask = 0;
	    Flag(int mask){
	        this.mask = mask;
	    }
	    int getMask(){
	        return mask;
	    }
	}
	
	
	public enum RotDir {
		LEFT,
		RIGHT;
	}
	
    public static int maskZNHC(boolean z, boolean n, boolean h, boolean c) {
        int value = 0;
        boolean[] fanion = { c, h, n, z };
        for (int i = 0; i < 4; ++i) {
            if (fanion[i]) {
                value += Bits.mask(i);
            }
        }
        return value << 4;
    }

    // return une valeur de 8 bits plus grande avec les flags dans les 8 LSB et la valeur dans le reste
    private static int packValueFlags(int value, boolean z, boolean n, boolean h,
            boolean c) {
        return (value << 8) | maskZNHC(z, n, h, c);
    }

    public static int unpackValue(int valueFlags) {
        return (valueFlags & 0xffff00) >>> 8;
    }

    public static int unpackFlags(int valueFlags) {
        return (valueFlags & 0xff);
    }

//    private static int addition(int size, int carryH, int carryC) {
//        
//    }
    
    public static int add(int l, int r, boolean c0) {
        Preconditions.checkBits8(r);
        Preconditions.checkBits8(l);
        int sum = 0;
        boolean carry = c0;
        boolean fanionH = false;
        for (int i = 0; i < 8; ++i) {
            int count = 0;
            boolean[] bits = { Bits.test(l, i), Bits.test(r, i), carry };
            for (int j = 0; j < 3; ++j) {
                if (bits[j]) {
                    ++count;
                }
            }
            switch (count) {
            case 0:
                carry = false;
                break;
            case 1:
                sum += Bits.mask(i);
                carry = false;
                break;
            case 2:
                carry = true;
                break;
            case 3:
                carry = true;
                sum += Bits.mask(i);
                break;
            }
            if (i == 3) {
                fanionH = carry;
            }
        }
        return packValueFlags(sum, (sum == 0), false, fanionH, carry);
    }

    public static int add(int l, int r) {
        return add(l, r, false);
    }
	
	public static int add16L(int l, int r) {
	    Preconditions.checkBits16(r);
        Preconditions.checkBits16(l);
        int sum = 0;
        boolean carry = false;
        boolean fanionH = false;
        boolean fanionC = false;
        for (int i = 0; i < 16; ++i) {
            int count = 0;
            boolean[] bits = { Bits.test(l, i), Bits.test(r, i), carry };
            for (int j = 0; j < 3; ++j) {
                if (bits[j]) {
                    ++count;
                }
            }
            switch (count) {
            case 0:
                carry = false;
                break;
            case 1:
                sum += Bits.mask(i);
                carry = false;
                break;
            case 2:
                carry = true;
                break;
            case 3:
                carry = true;
                sum += Bits.mask(i);
                break;
            }
            if (i == 3) {
                fanionH = carry;
            }
            if (i == 7) {
                fanionC = carry;
            }
        }
        return packValueFlags(sum, false, false, fanionH, fanionC);
	}
	
	public static int add16H (int l, int r) {
	    Preconditions.checkBits16(r);
        Preconditions.checkBits16(l);
        int sum = 0;
        boolean carry = false;
        boolean fanionH = false;
        boolean fanionC = false;
        for (int i = 0; i < 16; ++i) {
            int count = 0;
            boolean[] bits = { Bits.test(l, i), Bits.test(r, i), carry };
            for (int j = 0; j < 3; ++j) {
                if (bits[j]) {
                    ++count;
                }
            }
            switch (count) {
            case 0:
                carry = false;
                break;
            case 1:
                sum += Bits.mask(i);
                carry = false;
                break;
            case 2:
                carry = true;
                break;
            case 3:
                carry = true;
                sum += Bits.mask(i);
                break;
            }
            if (i == 11) {
                fanionH = carry;
            }
            if (i == 15) {
                fanionC = carry;
            }
        }
        return packValueFlags(sum, false, false, fanionH, fanionC);
	}
	
	public static int sub(int l, int r, boolean b0) {
	    Preconditions.checkBits8(r);
        Preconditions.checkBits8(l);
        int difference = 0;
        boolean carry = b0;
        boolean fanionH = false;
        for (int i = 0; i < 8; ++i) {
            if (!Bits.test(l, i)&&Bits.test(r, i)&&!carry) {
                carry = true;
            } else {
                if (Bits.test(l, i)||carry|| (Bits.test(l, i)&&Bits.test(r, i)&&carry)) {
                    difference += Bits.mask(i);
                    carry = false;
                } else {
                    carry = false;
                }
            }
            if (i == 3) {
                fanionH = carry;
            }
        }
        return packValueFlags(difference, (difference == 0), false, fanionH, carry);
	}
	
	public static int sub(int l, int r) {
		return sub(l, r, false);
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
		
		int result=v<<1;
		
		if (Bits.test(v,8))
			c=true;
		
		result=Bits.clip(8,result);
				
				
		return pack(n,h,c,result);
		
	}
	
	/**
	 * Computes the arithmetic rigth shift and corresponding flags
	 * @param v - an int, the value to shift
	 * @return the packed valueFlags
	*/
	public static int shiftRightA(int v) {
		
		Preconditions.checkBits8(v);

		final boolean n = false;
		final boolean h = false;
		boolean c = false;
		
		if (Bits.test(v,0))
			c=true;
		
		int result = v>>1;
		
		result=Bits.clip(8,result);
		
		return pack(n,h,c,result);
	}
	
	/**
	 * Computes the logical rigth shift and corresponding flags
	 * @param v - an int, the value to shift
	 * @return the packed valueFlags
	*/
	public static int shiftRightL(int v) {
		
		Preconditions.checkBits8(v);

		final boolean n = false;
		final boolean h = false;
		boolean c = false;
		
		if (Bits.test(v,0))
			c=true;
		
		int result = v>>>1;
		
		result=Bits.clip(8,result);
		
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
	
	/**
	 * AUXILIARY
	 * 
	 * @param packedValue
	 * @param newValue
	 */
	private static int packNewValue(int packedValue , int newValue) {
		return pack(unpackFlags(packedValue), newValue);
	}
	
	private static int packValue(int value) {
		return pack(0,value);
	}
	
}
