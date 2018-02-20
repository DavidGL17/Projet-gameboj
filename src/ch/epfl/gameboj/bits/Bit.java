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
	 * 
	 * @return
	 */
	default int index(){
		return ((Enum)this).ordinal();
	}
	
	default int mask(){
		
		int j = this.index();
		return Bits.mask(j);
		
	}
}