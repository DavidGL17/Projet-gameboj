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
	
	
	public static enum RotDir {
		LEFT,
		RIGHT;
	}
	
    /**
     * Returns a value where the bits corresponding to each fanion are 1 if the corresponding fanion is true
     * 
     * @param z, the flag z
     * @param n, the flag n
     * @param h, the flag h
     * @param c, the flag c
     * @return an 8 bits int with the flags encoded in their respective bit
     */
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

    /**
     * Returns an 8 bits bigger value with the flags in the 8 LSB and the value in the rest of the bits
     * 
     * @param value, the value
     * @param z, the flag z
     * @param n, the flag n
     * @param h, the flag h
     * @param c, the flag c
     * @return The value and the flags in the same int
     */
    private static int packValueFlags(int value, boolean z, boolean n, boolean h,
            boolean c) {
        return (value << 8) | maskZNHC(z, n, h, c);
    }

    /**
     * Extracts the value from an int, supressing the flags
     * 
     * @param valueFlags, the int we want to extract the value from
     * @return the value
     */
    public static int unpackValue(int valueFlags) {
        return (checkFlagValueIsWithinRange(valueFlags) & 0xffff00) >>> 8;
    }

    /**
     * Extracts the flags from an int, supressing the value
     * 
     * @param valueFlags, the int we want to extract the flags from
     * @return the flags in a 8 bit int
     */
    public static int unpackFlags(int valueFlags) {
        return (checkFlagValueIsWithinRange(valueFlags) & 0xff);
    }
    
    /**
     * Checks if the input composed of a value and the flags is within the possible range
     * 
     * @param valueFlags, the value and the flags
     * @return valueFlags, if it is within the range
     * @throws IllegalArgumentException if the value is not within range
     */
    private static int checkFlagValueIsWithinRange(int valueFlags) {
        if ((valueFlags&0x0F) != 0 || valueFlags>= 0x1000000) {
            throw new IllegalArgumentException();
        } else {
            return valueFlags;
        }
    }
    
    /**
     * Ads two ints bit by bit up to the size bit, and also adds the initial carry bit. 
     * 
     * @param l, an int 
     * @param r, an int 
     * @param size, the number of bit by bit addition it makes
     * @param carryH, the index at the point in which the method has to register the flag H
     * @param carryC, the index at the point in which the method has to register the flag C
     * @param firstCarry, the initial carry bit
     * @return an array, containing the result, and the value of the flags H and C as 1 if they are true
     */
    private static int[] addition(int l, int r,int size, int carryH, int carryC, boolean firstCarry) {
        int[] results = new int[3];
        boolean carry = firstCarry;
        for (int i = 0; i < size; ++i) {
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
                results[0] += Bits.mask(i);
                carry = false;
                break;
            case 2:
                carry = true;
                break;
            case 3:
                carry = true;
                results[0] += Bits.mask(i);
                break;
            }
            if (i == carryH) {
                if (carry) {
                    results[1] = 1;
                }
            }
            if (i == carryC) {
                if (carry) {
                    results[2] = 1;
                }
            }
        }
        return results;
    }
    /**
     * Ads two ints bit by bit up to the size bit
     * 
     * @param l, an int 
     * @param r, an int 
     * @param size, the number of bit by bit addition it makes
     * @param carryH, the index at the point in which the method has to register the flag H
     * @param carryC, the index at the point in which the method has to register the flag C
     * @return an array, containing the result, and the value of the flags H and C as 1 if they are true
     */
    private static int[] addition(int l, int r,int size, int carryH, int carryC) {
        return addition(l, r, size, carryH, carryC, false);
    }
    
    /**
     * Returns the sum of the two 8 bits ints and of the initial carry bit and the flags Z0HC 
     * 
     * @param l, the first 8 bit int
     * @param r, the second 8 bit int
     * @param c0, the initial carry bit
     * @return a 16 bit int with the value and the flags
     * @throws IllegalArgumentException if one of the ints is not an 8 bit value
     */
    public static int add(int l, int r, boolean c0) {
        Preconditions.checkBits8(r);
        Preconditions.checkBits8(l);
        int sum[] = addition(l, r, 8, 3, 7,c0);
        return packValueFlags(sum[0], (sum[0] == 0), false, sum[1]==1, sum[2]==1);
    }

    /**
     * Returns the sum of the two 8 bits ints and the flags Z0HC 
     * 
     * @param l, the first 8 bit int
     * @param r, the second 8 bit int
     * @return a 16 bit int with the value and the flags
     * @throws IllegalArgumentException if one of the ints is not an 8 bit value
     */
    public static int add(int l, int r) {
        return add(l, r, false);
    }
	
    /**
     * Returns the sum of the two 16 bits ints and the flags 00HC, where H and C correspond to the addition of the 8 LSB
     * 
     * @param l, the first 16 bit int
     * @param r, the second 16 bit int
     * @return a 24 bit int with the value and the flags
     * @throws IllegalArgumentException if one of the ints is not an 16 bit value
     */
	public static int add16L(int l, int r) {
	    Preconditions.checkBits16(r);
        Preconditions.checkBits16(l);
        int sum[] = addition(l, r, 16, 3, 7);
        return packValueFlags(sum[0], false, false, sum[1]==1, sum[2]==1);
	}
	
	/**
     * Returns the sum of the two 16 bits ints and the flags 00HC, where H and C correspond to the addition of the 8 MSB
     * 
     * @param l, the first 16 bit int
     * @param r, the second 16 bit int
     * @return a 24 bit int with the value and the flags
     * @throws IllegalArgumentException if one of the ints is not an 16 bit value
     */
	public static int add16H (int l, int r) {
	    Preconditions.checkBits16(r);
        Preconditions.checkBits16(l);
        int sum[] = addition(l, r, 16, 11, 15);
        return packValueFlags(sum[0], false, false, sum[1]==1, sum[2]==1);
	}
	
	/**
	 * Computes the substraction of two 8 bits int and of the initial carry bit, returns the result and the flags Z1HC
	 * 
	 * @param l, a 8 bit int
	 * @param r, a 8 bit int
	 * @param b0, the initial carry bit
	 * @return the result and the flags in a same int
	 * @throws IllegalArgumentException if one of the ints is not an 8 bit value
	 */
	public static int sub(int l, int r, boolean b0) {
	    Preconditions.checkBits8(r);
        Preconditions.checkBits8(l);
        if(l==r && !b0) {
            return packValueFlags(0, true, true, false, false);
        }
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
        return packValueFlags(difference, (difference == 0), true, fanionH, carry);
	}
	
	/**
     * Computes the substraction of two 8 bits int, returns the result and the flags Z1HC
     * 
     * @param l, a 8 bit int
     * @param r, a 8 bit int
     * @return the result and the flags in a same int
     * @throws IllegalArgumentException if one of the ints is not an 8 bit value
     */
	public static int sub(int l, int r) {
		return sub(l, r, false);
	}
	
	/**
	 * Adjusts an 8 bit value so it is in the DCB format
	 * 
	 * @param v, the value we want to adjust
	 * @param n, the flag n
	 * @param h, the flag h
	 * @param c, the flag c
	 * @return the value, adjusted to the DCB format
	 * @throws IllegalArgumentException if the value is not an 8 bit int
	 */
	public static int bcdAdjust(int v, boolean n, boolean h, boolean c) {
		boolean fixL = h|(!n & ((Bits.clip(4, Preconditions.checkBits8(v)))>9));
		boolean fixH = c | (!n & (v>0x99));
		int fix = 0x60 * (fixH?1:0) + 0x06*(fixL?1:0);
		int Va = n?v-fix:v+fix;
	    return packValueFlags(Va, Va==0, n, false, fixH);
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
		
		final boolean N = false;
		final boolean H = false;
		boolean c = false;
		
		int result=v<<1;
		
		if (Bits.test(result,8))
			c=true;
		
		result=Bits.clip(8,result);
				
				
		return pack(N,H,c,result);
		
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

		final boolean N=false;
		final boolean H=false;
		boolean flagC=false;
		
		int result=v;
		if (c)
			result += Bits.mask(8);
		
		
		result = Bits.rotate(9,result,rotate(d));
		if (Bits.test(result,8))
			flagC=true;
		
		result = Bits.clip(8,result);
		
		return pack(N,H,flagC,result);
		
	}
	
	/**
	 * Computes the byte where the 4 LSB and 4 MSB swap position
	 * @param v - an int, the value
	 * @return the packed valueFlags
	 */
	public static int swap(int v) {
		
		Preconditions.checkBits8(v);

		final boolean N = false;
		final boolean H = false;
		final boolean C = false;
		
		int lsb=Bits.clip(4,v);
		int msb=Bits.extract(v,4,4);
		
		int result= Bits.clip(8, (lsb<<4) + msb);
		
		return pack(N,H,C,result);
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
		return (d==RotDir.LEFT) ? 1 : -1;
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
		return pack((value==0),n,h,c,value);
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
