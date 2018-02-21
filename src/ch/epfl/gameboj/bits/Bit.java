package ch.epfl.gameboj.bits;

import java.lang.Enum;

/*
 * 
 * @author Melvin (288405)
 * 
 * 
 */

interface Bit {
	
	/**
	 * Computes the index
	 * @return an int : the index
	 */
	default int index(){
		return ((Enum<?>)this).ordinal();
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