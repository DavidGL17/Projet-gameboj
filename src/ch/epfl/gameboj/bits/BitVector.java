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
    private final int[] table;
    
    public BitVector(int size, boolean initialValue) {
        Preconditions.checkArgument(size>=0&&(size%32)==0);
        table = new int[size/32];
        Arrays.fill(table, initialValue?-1:0);
    }
    
    public BitVector(int size) {
        this(size,false);
    }
    
    private BitVector(int[] table) {
        this.table = table.clone();
    }
    
    private BitVector(BitVector bv) {
        this(bv.table);
    }
    
    
    public int size() {
        return table.length*32;
    }
    
    public boolean testBit(int index) {
        Preconditions.checkArgument(index<table.length*32&&index>=0);
        return Bits.test(index%32, table[index/32]);
    }
    
    public int[] not() {
        int[] notTable = new int[table.length];
        for (int i = 0;i<table.length;++i) {
            notTable[i] = -1&table[i];
        }
        return notTable;
    }
    
    public int[] and(BitVector that) {
        Preconditions.checkArgument(that.size() == this.size());
        int[] andTable = new int[table.length];
        for (int i = 0;i<table.length;++i) {
            andTable[i] = table[i]&that.table[i];
        }
        return andTable;
    }
    
    public int[] or(BitVector that) {
        Preconditions.checkArgument(that.size() == this.size());
        int[] orTable = new int[table.length];
        for (int i = 0;i<table.length;++i) {
            orTable[i] = table[i]|that.table[i];
        }
        return orTable;
    }
}
