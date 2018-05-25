package ch.epfl.gameboj.bits;

/*
 * @author Melvin Malonga-Matouba (288405)
 * 
 */

public interface Bit {
	
	
	/**
	 * Uses automatically inherited method from type enum
	 * @return the ordinal of this enumeration instance
	 */
	abstract int ordinal();
	
	
	/**
	 * Computes the index
	 * @return the index
	 */
	default int index(){
		return ordinal();
	}
	
	/**
	 * Computes the mask
	 * @return the mask
	 */
	default int mask(){
		
		int j = this.index();
		return Bits.mask(j);
		
	}
}