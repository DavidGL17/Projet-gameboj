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
	public static int mask(int index) throws IndexOutOfBoundsException{
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
	public static boolean test(int bits, int index) throws IndexOutOfBoundsException {
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
	 * (réponse) je penses que oui
	 */
	/**
	 * Computes the int corresponding to the (size) least significant bits
	 * @param size - an int : the number of bits
	 * @param bits - an int : the original code
	 * @return an int : the code corresponding to the (size) LSB
	 * @throw IllegalArgumentException
	 * 			if parameter size isn't within [0,32]
	 */
	public static int clip(int size, int bits) throws IllegalArgumentException {
		
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
	 * Extract the bits from index start to start+size
	 * @param bits - an int : the int
	 * @param start - an int : the index of the first replacement bit
	 * @param size - an int : the number of LSB to replace
	 * @return an int : the à compléter /!\ <-------------------------------------
	 * @throws IndexOutOfBoundsException
	 * 			if the start and size is invalid for a bit-sequence in an int
	 */
	public static int extract(int bits, int start, int size) throws IndexOutOfBoundsException {
		return  clip(size,bits>>>Objects.checkFromIndexSize(start,size,Integer.SIZE)) ;
	}
	
	
	/*
	 * Pas sur d'avoir compris l'objectif
	 * ---------------------------------------------------------------
	 */
	public static int rotate(int size, int bits, int distance) throws IllegalArgumentException, IndexOutOfBoundsException {
	    if ((size<=0)||(size>Integer.SIZE) ) {
	    		System.out.println("Par ici , bits vaut: " + bits + " size vaut: " + size + " distance vaut: " + distance );
            throw new IllegalArgumentException();
        }
	    if (size<31) {  		//Le cas size proche de 31 est tricky car après 0b1<<size est négatif
	    		if (bits >= (0b1<<size)) {
	    			System.out.println("Par là , bits vaut: " + bits + " size vaut: " + size + " distance vaut: " + distance );
	    			throw new IllegalArgumentException();
	    		}
	    } else if (size==31){
	    		if (bits >>>1 >= 0b1<<(size-1)) {
	    			System.out.println("Ouhou , bits vaut: " + bits + " size vaut: " + size + " distance vaut: " + distance );
	    			 throw new IllegalArgumentException();
	    		}
	    }
	    int res=0;
	    int decalage = distance%size; //permet de controler module de décalage
	    
	    /* Peu esthétique a%b renvoie c : [0;a] et c ≡ a[b]
	     * En faisant d=distance%size on obtient donc si a et positif d=décalage sinon on obtient d=décalage-size
	     * Pour palier à ce problème il faut dans le cas positif obtenir le même nombre et négatif obtenir le nombre+size
	     * Faire décalage = (d+size)%size fonctionne car dans le cas ou a est positif décalage vaut d, dans le cas contraire
	     * on obtient décalage = d+size
	     * 
	     * Enfin pour distance = 0, on obtient bien un décalage nul;
	     * 
	     * Les opérations de congruences sont rapides car elle revienne toujours à une complémentarité près faire un "clip". (On aurait pu 's'amuser' à le faire nous-même)
	     * 
	     *  Enfin 
	     */
	    for (int i=0 ; i<size ; i++) { //i indice du bit dans le nombre de sortie
	    		if (test(bits, ( (i+size-decalage)%size ) )) { //controle module decalage permet de controler signe de size-decalage
	    			res+=mask(i);
	    		}
	    }
	    
	    return res;
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
        if (test(b, 6)) {
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
	public static int make16(int highB, int lowB) throws IllegalArgumentException {
		return (Preconditions.checkBits8(highB)<<8) | Preconditions.checkBits8(lowB) ;
		
	}
	
	
}