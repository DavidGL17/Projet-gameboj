package ch.epfl.gameboj.bits;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;

/*
 * 
 * @author Melvin Malonga-Matouba (288405)
 * @author David González León (270845)
 * 
 */

public final class Bits{
	private Bits(){};
	/**
	 * Computes the mask corresponding to a certain index
	 * @param index - an int : the index of the bit to activate
	 * @return an int : the corresponding mask
	 * @throws IndexOutOfBoundsException
	 * 			If parameter i doesn't belong to [0,31]
	 */
	public static int mask(int index) {
		return (0b01<<Objects.checkIndex(index, Integer.SIZE));
		
	}
	
	/**
	 * Determines if the bit of a given weight is activated in an int
	 * @param bits - an int : the int
	 * @param index - an int : the weight of the bit
	 * @return a boolean : wether the bit of weight index is activated in bits
	 * @throws IndexOutOfBoundsException
	 * 			If the index is invalid for a bit in a int
	 */
	public static boolean test(int bits, int index)  {
		Objects.checkIndex(index,Integer.SIZE);
		bits = bits & mask(index);
		bits = bits>>>index;
		return (bits==1);
	}
	
	/**
	 * Determines wether a certain Bit is activated in an int
	 * @param bits - an int : the int
	 * @param bit - an Bit : the Bit
	 * @return a boolean : wether bit is activated in bits
	 * @throws IndexOutOfBoundsException
	 * 			If the return of bit.index() is out of [0,31] ---> Check the class of bit
	 */
	public static boolean test ( int bits, Bit bit)  {
		return test(bits, bit.index());
	}
	
	
	/**
	 * Computes a byte where the bit of weight index is set to 1 or 0
	 * @param bits - an int : the original int
	 * @param index - an int : the weight of the bit
	 * @param newValue - a boolean : true iff the bit should be set to 1, false to 0
	 * @return an int : the int with index-th bit's value set
	 * @throws IndexOutOfBoundsException
	 * 			If the index is invalid for a bit in an int
	 */
	public static int set(int bits, int index, boolean newValue)  {
		Objects.checkIndex(index,Integer.SIZE);
		if (test(bits,index)) {
			if (newValue) {
				return bits;
			} else {
				return bits & (~ mask(index)) ;
			}
		} else {
			if (newValue) {
				return bits | mask(index);
			} else {
				return bits;
			}
		}
	}
	
	/**
	 * Computes the int corresponding to the (size) least significant bits
	 * @param size - an int : the number of bits
	 * @param bits - an int : the original code
	 * @return an int : the code corresponding to the (size) LSB
	 * @throw IllegalArgumentException
	 * 			if parameter size isn't within [0,32]
	 */
	public static int clip(int size, int bits)  {
		
		if ((size<0)||(size>32)){
			throw new IllegalArgumentException("!"); //Est-ce bien cette exception à lancer ? dans le sujet IllegalArgumentException(!) On utilise pas checkIndex car il lance OutOfBoundsExeption
		}
		
		int mask=0;
		for (int i=0; i<size ; i++) {
			mask+=mask(i);
		}
		
		return (bits & mask);
	}
	
	
	/**
	 * Extract the bits from index start to start+size of an int
	 * @param bits - an int : the int
	 * @param start - an int : the index of the first extracted bit
	 * @param size - an int : the number of bits to extract
	 * @return an int : the extracted bit-sequence
	 * @throws IndexOutOfBoundsException
	 * 			if the start and size is invalid for a bit-sequence in an int
	 */
	public static int extract(int bits, int start, int size)  {
		return  clip(size,bits>>>Objects.checkFromIndexSize(start,size,Integer.SIZE)) ;
	}
	
	
	/**
	 * Computes the rotation of a number given the size of the number and distance of rotation
	 * @param size - an int : the size of the number
	 * @param bits - an int : the value of a number
	 * @param distance _ an int : the number by which the bits are rotated
	 * @return the rotated int sequence
	 * @throws IllegalArgumentException
	 * @throws IndexOutOfBoundsException
	 */
	public static int rotate(int size, int bits, int distance) {
	    if ((size<=0)||(size>Integer.SIZE) ) {
            throw new IllegalArgumentException();
        }
	    
	    if (size<31) {  		
	    		if (bits >= (0b1<<size)) {
	    			throw new IllegalArgumentException();
	    		}
	    } else if (size==31){
	    		if (bits >>>1 >= 0b1<<(size-1)) {
	    			throw new IllegalArgumentException();
	    		}
	    }
	    int res=0;
	    int decalage = distance%size; 
	    
	    for (int i=0 ; i<size ; i++) { 
	    		if (test(bits, ( (i+size-decalage)%size ) )) { 
	    			res+=mask(i);
	    		}
	    }
	    
	    return res;
	}
	
	/**
	 * Computes from a byte the int where the 8 LSB are the same and the rest is of the sign of the MSB
	 * @param b - an int : a byte
	 * @return an int : the int code
	 * @throws IllegalArgumentException
	 * 			if b isn't a byte
	 */
    public static int signExtend8(int b)  {
        if (test(Preconditions.checkBits8(b), 7)) {
            return b | (0xFFFFFF80);
        } else {
            return b;
        }
    }
	
	/**
	 * Computes a byte where the bits are reversed relative to center
	 * @param b - an int : the byte
	 * @return  an int : the byte
	 * @throws IllegalArgumentException
	 * 			if b isn't a byte
	 */
	public static int reverse8(int b)  {
		Preconditions.checkBits8(b);
		int res=0;
		for( int i=0 ; i<8 ; i++) {
			if (test(b,i)) {
				res+=mask(7-i);
			}
		}
		return res;
	}
	
	/**
	 * Computes the complement of a byte
	 * @param b - an int : the byte
	 * @return an int : the byte
	 * @throws IllegalArgumentException
	 * 			if b isn't a byte
	 */
	public static int complement8(int b)  {
		return (0xff^Preconditions.checkBits8(b));
	}
	
	/**
	 * Computes a 16 bits code given to 8-bits sequences.
	 * @param highB - an int : the 8 MSB code
	 * @param lowB - an int : the 8 LSB code
	 * @return the concatenated 16-bits value
	 * @throws IllegalArgumentException
	 * 			if either highB or lowB isn't a byte
	 */
	public static int make16(int highB, int lowB)  {
		return (Preconditions.checkBits8(highB)<<8) | Preconditions.checkBits8(lowB) ;
		
	}
	
	
}