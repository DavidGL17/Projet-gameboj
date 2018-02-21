package ch.epfl.gameboj.bits;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;


public final class Bits{
	private Bits(){};
	
	/*
	 * Adapter le String du constructeur de l'exception ? l-98
	 * Que doit faire la méthode rotate ?
	 * 
	 */
	
	
	/**
	 * Computes the mask corresponding to a certain index
	 * @param index - an int : the index of the bit to activate
	 * @return an int : the corresponding mask
	 * @throws IndexOutOfBoundsException
	 * 			If parameter i doesn't belong to [0,31]
	 */
	public static int mask(int index) throws IndexOutOfBoundsException{
		Objects.checkIndex(index, Integer.SIZE);
		return (1<<index);
		
	}
	
	/**
	 * Determines if the bit of a given weight is activated in an int
	 * @param bits - an int : the int
	 * @param index - an int : the weight of the bit
	 * @return a boolean : wether the bit of weight index is activated in bits
	 * @throws IndexOutOfBoundsException
	 * 			If the index is invalid for a bit in a byte
	 */
	public static boolean test(int bits, int index) throws IndexOutOfBoundsException {
		Objects.checkIndex(index,Integer.SIZE);
		bits = bits & mask(index);
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
	public static boolean test ( int bits, Bit bit) throws IndexOutOfBoundsException {
		return test(bits, bit.index());
	}
	
	/*
	 * S'applique bien toujours aux int ?
	 */
	/**
	 * Computes a byte where the bit of weight index is set to 1 or 0
	 * @param bits - an int : the original byte
	 * @param index - an int : the weight of the bit
	 * @param newValue - a boolean : true iff the bit should be set to 1, false to 0
	 * @return an int : the byte with index-th bit's value set
	 * @throws IndexOutOfBoundsException
	 * 			If the index is invalid for a bit in an int
	 */
	public static int set(int bits, int index, boolean newValue) throws IndexOutOfBoundsException {
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
	
	/*
	 * Est-ce bien cette exception à lancer ? (String dans le constructeur)
	 */
	/**
	 * Computes the int corresponding to the (size) least significant bits
	 * @param bits - an int : the original code
	 * @param size - an int : the number of bits
	 * @return an int : the code corresponding to the (size) LSB
	 * @throw IllegalArgumentException
	 * 			if parameter size isn't within [0,32]
	 */
	public static int clip(int bits, int size) throws IllegalArgumentException {
		
		if ((size<0)||(size>32)){
			throw new IllegalArgumentException("!"); //Est-ce bien cette exception à lancer ? dans le sujet IllegalArgumentException(!) On utilise pas checkIndex car il lance OutOfBoundsExeption
		}
		
		int mask=0;
		for (int i=0; i<size ; i++) {
			mask+=mask(i);
		}
		
		return (bits & mask);
	}
	
	/* Est-ce bien ce qu'il faut faire ?
	 * à appliquer aux bytes ou aux int ? : byte - 114 "8", int - 114 "32"
	 */
	/**
	 * Computes an int where the (size) LSB are replaced by another sequence of bits from the int
	 * @param bits - an int : the int
	 * @param start - an int : the index of the first replacement bit
	 * @param size - an int : the number of LSB to replace
	 * @return an int : the à compléter /!\ <-------------------------------------
	 * @throws IndexOutOfBoundsException
	 * 			if the start and size is invalid for a bit-sequence in a byte
	 */
	public static int extract(int bits, int start, int size) throws IndexOutOfBoundsException {
		Objects.checkFromIndexSize(start,size,Integer.SIZE);
		
		return ( (bits & ( (~clip(bits,size)) % (Byte.MAX_VALUE+1) ) )| clip(bits>>>start,size) );
	}
	
	
	/*
	 * Pas sur d'avoir compris l'objectif
	 * ---------------------------------------------------------------
	 */
	public static int rotate(int size, int bits, int distance) {
		return 2;
	}
	
	
	/**
	 * Computes from a byte the int where the 8 LSB are the same and the rest of the sign of the MSB
	 * @param b - an int : the byte
	 * @return an int : the int code
	 * @throws IllegalArgumentException
	 * 			if b isn't a byte
	 */
	public static int signExtend8(int b) throws IllegalArgumentException {
		Preconditions.checkBits8(b);
			if (test(b,7)) {
				return b | (Integer.MAX_VALUE-Byte.MAX_VALUE);
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
	public static int reverse8(int b) throws IllegalArgumentException {
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
	public static int complement8(int b) throws IllegalArgumentException {
		Preconditions.checkBits8(b);
		
		return ((~b)%(Byte.MAX_VALUE+1));
	}
	
	/**
	 * Computes a 16 bits code given to 8-bits sequences.
	 * @param highB - an int : the 8 MSB code
	 * @param lowB - an int : the 8 LSB code
	 * @return the concatenated 16-bits value
	 * @throws IllegalArgumentException
	 * 			if either highB or lowB isn't a byte
	 */
	public static int make16(int highB, int lowB) throws IllegalArgumentException {
		Preconditions.checkBits8(highB);
		Preconditions.checkBits8(lowB);
		
		return highB<<8 | lowB ;
		
	}
	
	
}