package ch.epfl.gameboj.component.cpu;

import java.util.Objects;

import static ch.epfl.gameboj.Preconditions.checkBits8;
import static ch.epfl.gameboj.Preconditions.checkBits16;
import static ch.epfl.gameboj.Preconditions.checkArgument;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

/**
 * 
 * @author David Gonzalez leon (270845)
 * @author Melvin Malonga-Matouba (288405)
 * 
 */

public final class Alu {

    /*
     * Represents the bits of Register F as well as the flags that can be raised
     * while performing arithmetic operations
     */
    public enum Flag implements Bit {
        UNUSED_0, UNUSED_1, UNUSED_2, UNUSED_3, C, H, N, Z;
    }

    /*
     * Represents a Direction of rotation
     */
    public static enum RotDir {
        LEFT, RIGHT;
    }

    private Alu() {
    };

    /**
     * Returns a value where the bits corresponding to each fanion are 1 if the
     * corresponding fanion is true
     * 
     * @param z,
     *            the flag z
     * @param n,
     *            the flag n
     * @param h,
     *            the flag h
     * @param c,
     *            the flag c
     * @return an 8 bits int with the flags encoded in their respective bit
     */
    public static int maskZNHC(boolean z, boolean n, boolean h, boolean c) {
       return ( (z ? Flag.Z.mask() : 0) +
    		   (n ? Flag.N.mask() : 0) +
    		   (h ? Flag.H.mask() : 0) +
    		   (c ? Flag.C.mask() : 0)
    		   );
    }

    /**
     * Returns an 8 bits bigger value with the flags in the 8 LSB and the value
     * in the rest of the bits
     * 
     * @param value,
     *            the value
     * @param z,
     *            the flag z
     * @param n,
     *            the flag n
     * @param h,
     *            the flag h
     * @param c,
     *            the flag c
     * @return The value and the flags in the same int
     */
    private static int packValueFlags(int value, boolean z, boolean n,
            boolean h, boolean c) {
        return (value << 8) | maskZNHC(z, n, h, c);
    }

    /**
     * Extracts the value from an int, supressing the flags
     * 
     * @param valueFlags,
     *            the int from which we want to extract the value
     * @return the value
     * @throws IllegalArgumentException
     *             if valueFlags isn't valid
     */
    public static int unpackValue(int valueFlags) {
        return Bits.extract(checkFlagValueIsValid(valueFlags),8,16);
    }

    /**
     * Extracts the flags from an int, supressing the value
     * 
     * @param valueFlags,
     *            the int from which we want to extract the flags
     * @return the flags in a 8 bit int
     * @throws IllegalArgumentException
     *             if valueFlags isn't valid
     */
    public static int unpackFlags(int valueFlags) {
        return Bits.clip(8, checkFlagValueIsValid(valueFlags));
    }

    /**
     * Checks if the input composed of a value and the flags is within the
     * possible range
     * 
     * @param valueFlags,
     *            the value and the flags
     * @return valueFlags, if it is within the range
     * @throws IllegalArgumentException
     *             if valueFlags isn't valid
     */
    private static int checkFlagValueIsValid(int valueFlags) {
        checkArgument(
                !((valueFlags & 0x0F) != 0 || valueFlags > 0xFFFFF0));
        return valueFlags;
    }

    
    /**
     * Returns the sum of the two 8 bits ints and of the initial carry bit and
     * the flags Z0HC
     * 
     * @param l,
     *            the first 8 bit int
     * @param r,
     *            the second 8 bit int
     * @param c0,
     *            the initial carry bit
     * @return the result and the flags in the same int
     * @throws IllegalArgumentException
     *             if one of the ints is not an 8 bit value
     */
    public static int add(int l, int r, boolean c0) {
    	int sum = checkBits8(l)+checkBits8(r)+(c0?1:0);
    	int clippedSum=Bits.clip(8,sum);
    	return packValueFlags(
    			clippedSum,
    			(clippedSum==0),
    			false,
    			(Bits.test(sum,4)!=(Bits.test(l,4)^Bits.test(r,4))),
    			Bits.test(sum,8)
    			);
//        int sum[] = addition(Preconditions.checkBits8(l),
//                Preconditions.checkBits8(r), 8, 3, 7, c0);
//        return packValueFlags(sum[0], (sum[0] == 0), false, sum[1] == 1,
//                sum[2] == 1);
    }

    /**
     * Returns the sum of the two 8 bits ints and the flags Z0HC
     * 
     * @param l,
     *            the first 8 bit int
     * @param r,
     *            the second 8 bit int
     * @return the result and the flags in the same int
     * @throws IllegalArgumentException
     *             if one of the ints is not an 8 bit value
     */
    public static int add(int l, int r) {
        return add(l, r, false);
    }

    /**
     * Returns the sum of the two 16 bits ints and the flags 00HC, where H and C
     * correspond to the addition of the 8 LSB
     * 
     * @param l,
     *            the first 16 bit int
     * @param r,
     *            the second 16 bit int
     * @return the result and the flags in the same int
     * @throws IllegalArgumentException
     *             if one of the ints is not an 16 bit value
     */
    public static int add16L(int l, int r) {
    	int sum = checkBits16(l)+checkBits16(r);
    	int clippedSum=Bits.clip(16,sum);
    	return packValueFlags(
    			clippedSum,
    			false,
    			false,
    			(Bits.test(sum,4)!=(Bits.test(l,4)^Bits.test(r,4))),
    			Bits.test(sum,8)!=(Bits.test(l,8)^Bits.test(r,8))
    			);
//        int sum[] = addition(Preconditions.checkBits16(l),
//                Preconditions.checkBits16(r), 16, 3, 7, false);
//        return packValueFlags(sum[0], false, false, sum[1] == 1, sum[2] == 1);
    }

    /**
     * Returns the sum of the two 16 bits ints and the flags 00HC, where H and C
     * correspond to the addition of the 8 MSB
     * 
     * @param l,
     *            the first 16 bit int
     * @param r,
     *            the second 16 bit int
     * @return the result and the flags in the same int
     * @throws IllegalArgumentException
     *             if one of the ints is not an 16 bit value
     */
    public static int add16H(int l, int r) {
    	int sum = checkBits16(l)+checkBits16(r);
    	int clippedSum=Bits.clip(16,sum);
    	
    	return packValueFlags(
    			clippedSum,
    			false,
    			false,
    			(Bits.test(sum,12)!=(Bits.test(l,12)^Bits.test(r,12))),
    			Bits.test(sum,16)
    			);
//        int sum[] = addition(Preconditions.checkBits16(l),
//                Preconditions.checkBits16(r), 16, 11, 15, false);
//        return packValueFlags(sum[0], false, false, sum[1] == 1, sum[2] == 1);
    }

    /**
     * Computes the substraction of two 8 bits int and of the initial carry bit,
     * returns the result and the flags Z1HC
     * 
     * @param l,
     *            the first 8 bit int
     * @param r,
     *            the second 8 bit int
     * @param b0,
     *            the initial carry bit
     * @return the result and the flags in the same int
     * @throws IllegalArgumentException
     *             if one of the ints is not an 8 bit value
     */
    public static int sub(int l, int r, boolean b0) {
    	int value = checkBits8(l)-checkBits8(r)-(b0?1:0);
    	int clippedValue = Bits.clip(8,((value>=0)?value:value+0x100));
    	
    	return packValueFlags(
    			clippedValue,
    			clippedValue==0,
    			true,
    			(Bits.test(clippedValue,4)!=(Bits.test(l,4)^Bits.test(r,4))),
    			(value<0)
    			);
    	
    	
//        if (Preconditions.checkBits8(l) == Preconditions.checkBits8(r) && !b0) {
//            return packValueFlags(0, true, true, false, false);
//        }
//        int difference = 0;
//        boolean carry = b0;
//        boolean fanionH = false;
//        for (int i = 0; i < 8; ++i) {
//            if (!Bits.test(l, i) && Bits.test(r, i) && carry) {
//                carry = true;
//            } else {
//                if ((Bits.test(r, i) && !Bits.test(l, i) && !carry)
//                        || (carry && !Bits.test(r, i) && !Bits.test(l, i))
//                        || (carry && Bits.test(r, i) && Bits.test(l, i))) {
//                    carry = true;
//                    difference += Bits.mask(i);
//                } else {
//                    if (!Bits.test(r, i) && Bits.test(l, i) && !carry) {
//                        carry = false;
//                        difference += Bits.mask(i);
//                    } else {
//                        carry = false;
//                    }
//                }
//            }
//            if (i == 3) {
//                fanionH = carry;
//            }
//        }
//        return packValueFlags(difference, (difference == 0), true, fanionH,
//                carry);
    }

    /**
     * Computes the substraction l-r, of two 8 bits int, returns the result and
     * the flags Z1HC
     * 
     * @param l,
     *            the first 8 bit int
     * @param r,
     *            the second 8 bit int
     * @return the result and the flags in the same int
     * @throws IllegalArgumentException
     *             if one of the ints is not an 8 bit value
     */
    public static int sub(int l, int r) {
        return sub(l, r, false);
    }

    /**
     * Adjusts an 8 bit value so it is in the DCB format
     * 
     * @param v,
     *            the value we want to adjust
     * @param n,
     *            the flag n
     * @param h,
     *            the flag h
     * @param c,
     *            the flag c
     * @return the value, adjusted to the DCB format
     * @throws IllegalArgumentException
     *             if the value is not an 8 bit int
     */
    public static int bcdAdjust(int v, boolean n, boolean h, boolean c) {
        boolean fixL = h
                | (!n & ((Bits.clip(4, checkBits8(v))) > 9));
        boolean fixH = c | (!n & (v > 0x99));
        int fix = 0x60 * (fixH ? 1 : 0) + 0x06 * (fixL ? 1 : 0);
        int Va = Bits.clip(8, n ? v - fix : v + fix);
        return packValueFlags(Va, Va == 0, n, false, fixH);
    }

    /**
     * Computes the AND operation and corresponding flags
     * 
     * @param l,
     *            the first term
     * @param r,
     *            the second term
     * @return the packed valueFlags
     * @throws IllegalArgumentException
     *             if one of the int is not an 8 bit int
     */
    public static int and(int l, int r) {

        int result = checkBits8(l) & checkBits8(r);

        return packValueFlags(result, (result == 0), false, true, false);
    }

    /**
     * Computes the OR operation and corresponding flags
     * 
     * @param l,
     *            first term
     * @param r,
     *            second term
     * @return the packed valueFlags
     * @throws IllegalArgumentException
     *             if one of the int is not an 8 bit int
     */
    public static int or(int l, int r) {

        int result = checkBits8(l) | checkBits8(r);
        return packValueFlags(result, (result == 0), false, false, false);
    }

    /**
     * Computes the XOR operation and corresponding flags
     * 
     * @param l,
     *            the first term
     * @param r,
     *            the second term
     * @return the packed valueFlags
     * @throws IllegalArgumentException
     *             if one of the int is not an 8 bit int
     */
    public static int xor(int l, int r) {

        checkBits8(l);
        checkBits8(r);

        int result = l ^ r;

        return packValueFlags(result, (result == 0), false, false, false);
    }

    /**
     * Computes the left shift and corresponding flags
     * 
     * @param v,
     *            the value to shift
     * @return the packed valueFlags
     * @throws IllegalArgumentException
     *             if v is not an 8 bit int
     * 
     */
    public static int shiftLeft(int v) {

        boolean c = false;

        int result = checkBits8(v) << 1;

        if (Bits.test(result, 8))
            c = true;

        result = Bits.clip(8, result);

        return packValueFlags(result, (result == 0), false, false, c);

    }

    /**
     * Computes the arithmetic rigth shift and corresponding flags
     * 
     * @param v,
     *            the value to shift
     * @return the packed valueFlags
     * @throws IllegalArgumentException
     *             if v is not an 8 bit int
     */
    public static int shiftRightA(int v) {

        boolean c = false;

        if (Bits.test(checkBits8(v), 0))
            c = true;
        int result = v;

        if (Bits.test(v, 7)) {
            result += Bits.mask(8);
        }

        result = result >>> 1;

        result = Bits.clip(8, result);

        return packValueFlags(result, (result == 0), false, false, c);
    }

    /**
     * Computes the logical rigth shift and corresponding flags
     * 
     * @param v,
     *            the value to shift
     * @return the packed valueFlags
     * @throws IllegalArgumentException
     *             if v is not an 8 bit int
     */
    public static int shiftRightL(int v) {

        boolean c = false;

        if (Bits.test(checkBits8(v), 0))
            c = true;

        int result = v >>> 1;

        result = Bits.clip(8, result);

        return packValueFlags(result, (result == 0), false, false, c);
    }
    

    /**
     * Computes the rotation in a direction and corresponding flags
     * 
     * @param d,
     *            the direction of the rotation
     * @param v,
     *            the value to rotate
     * @return the packed valueFlags
     * @throws IllegalArgumentException
     *             if v is not an 8 bit int or if d is null
     */
    public static int rotate(RotDir d, int v) {

        Objects.requireNonNull(d);

        boolean c = false;

        c = Bits.test(checkBits8(v), (7 + d.ordinal()) % 8);
        int result = Bits.rotate(8, v, (d == RotDir.LEFT) ? 1 : -1);

        return packValueFlags(result, (result == 0), false, false, c);

    }

    /**
     * Computes the rotation of a value and its carry, and corresponding flags
     * 
     * @param d,
     *            a RotDir, the direction of the rotation
     * @param v,
     *            an int, the value
     * @param c,
     *            a boolean, wether the carry is 1
     * @return the packed valueFlags
     * @throws IllegalArgumentException
     *             if v is not an 8 bit int or d is null
     */
    public static int rotate(RotDir d, int v, boolean c) {

        Objects.requireNonNull(d);
        
        int result = checkBits8(v);
        
        if (c)
            result += Bits.mask(8);

        result = Bits.rotate(9, result, (d == RotDir.LEFT) ? 1 : -1);
        int clippedResult=Bits.clip(8,result);

        return packValueFlags(clippedResult, clippedResult==0, false, false, Bits.test(result, 8));

    }

    /**
     * Computes the byte where the 4 LSB and 4 MSB swap position
     * 
     * @param v,
     *            the value
     * @return the packed valueFlags
     * @throws IllegalArgumentException
     *             if v is not an 8 bit int
     */
    public static int swap(int v) {


        final boolean N = false;
        final boolean H = false;
        final boolean C = false;

        return packValueFlags(Bits.rotate(8,checkBits8(v),4), (v == 0), N, H, C);
    }

    /**
     * Computes wether a bit is activated in a 8-bit int
     * 
     * @param v,
     *            the 8-bit int
     * @param bitIndex,
     *            the index
     * @return 0b_Z010_0000 where Z=1 iff the bit is not activated
     * @throws IllegalArgumentException
     *             if v is not an 8 bit int
     * @throws IndexOutOfBoundsException
     *             if bitIndex isn't between 0 and 8
     * 
     */
    public static int testBit(int v, int bitIndex) {
        Objects.checkIndex(bitIndex, 8);
        return packValueFlags(0,
                !Bits.test(checkBits8(v), bitIndex), false, true,
                false);
    }

}