/**
 * 
 */
package ch.epfl.gameboj.bits;

import java.util.Arrays;

import ch.epfl.gameboj.Preconditions;

/**
 * @author David Gonzalez leon (270845)
 * @author Melvin Malonga-Matouba (288405)
 */
public final class BitVector {
    private int[] table;
    
    public BitVector(int size, boolean initialValue) {
        Preconditions.checkArgument(size>=0&&(size%32)==0);
        table = new int[size];
        Arrays.fill(table, initialValue?1:0);
    }
    
    public BitVector(int size) {
        this(size,false);
    }
    
    
}
