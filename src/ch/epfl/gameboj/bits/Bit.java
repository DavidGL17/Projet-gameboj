package ch.epfl.gameboj.bits;


/*
 * 
 * @author Melvin (288405)
 * 
 * 
 */

public interface Bit {
	
	
	/**
	 * Uses automatically inherited method from type enum
	 * @return the ordinal of this enumeration constant
	 */
	abstract int ordinal();
	
	
	/**
	 * Computes the index
	 * @return an int : the index
	 */
	default int index(){
		return ordinal();
	}
	
	/**
	 * Computes the mask
	 * @return an int : the mask
	 */
	default int mask(){
		
		int j = this.index();
		return Bits.mask(j);
		
	}
}