package ch.epfl.gameboj.bits;

import java.util.Objects;


public final class Bits{
	private Bits(){};
	
	/**
	 * 
	 * @param i
	 * @return
	 */
	public static int mask(int i){
		Objects.checkIndex(i, 32);
		return (1<<i);
		
		}
	/**
	 * 
	 * @param bits
	 * @param index
	 * @return
	 */
	public static boolean test( int bits, int index){
		Objects.checkIndex((1<<index-1),bits);
		bits = bits & mask(index);
		return (bits==1);
		}
	/**
	 * 
	 * @param bits
	 * @param bit
	 * @return
	 */
	public static boolean test ( int bits, Bit bit){
		return test(bits, bit.index());
	}
	/**
	 * 
	 * @param bits
	 * @param index
	 * @param newValue
	 * @return
	 */
	public static int set(int bits, int index, boolean newValue){ // Faut-il inverser l'ordre des tests pourlancer l'exception au plus tot ?
	/*
	 * Exception à ajouter
	 */
		
		
		if (newValue) { //Si le bit doit être activé
			if (test(bits,index)) { //Si le bit est déja activé
				return bits;
			} else { //Si le bit n'est pas encore activé
				return bits | mask(index);
			}		
		} else { //Si le bit doit être desactivé
			if (test(bits,index)) { //Si le bit est activé
				return (bits & ( bits | mask(index)));
			} else { //Si le bit est desactivé
				return bits;
			}	
		}
	}
	
	/**
	 * 
	 * @param bits
	 * @param start
	 * @param size
	 * @return
	 */
	public static int clip(int bits, int start, int size) {
		Objects.checkIndex(size,33);
		
		int mask=0;
		for (int i=0; i<size ; i++) {
			mask+=mask(i);
		}
		
		return (bits & mask);
	}
	
}