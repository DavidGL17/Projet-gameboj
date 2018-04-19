package ch.epfl.gameboj.bits;

import java.util.Arrays;
import java.util.Objects;

import ch.epfl.gameboj.Preconditions;

/**
 * @author David Gonzalez leon (270845)
 * @author Melvin Malonga-Matouba (288405)
 */

// Juste pour être au clair, poids d'un bit croissant de l'index de l'entier
// dans lequel il se trouve
public final class BitVector {

    private final int[] table;
    
    /**
     * Builds a BitVector
     * @param size - the number of bits in the vector		
     * @param initialValue - wether the bits' value is one
     */
    public BitVector(int size, boolean initialValue) {
        Preconditions.checkArgument(size > 0 && (size % 32) == 0);
        table = new int[size / 32];
        Arrays.fill(table, initialValue ? -1 : 0);
    }

    /**
     * Builds a BitVector made of 0
     * @param size - the number of bits in the vector
     */
    public BitVector(int size) {
        this(size, false);
    }

    
    public BitVector(int[] table) {
        this.table = table; // Ne pas faire une copie conforme !
    }

    

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object arg0) {
        if (arg0 instanceof BitVector) {
            BitVector arg1 = (BitVector) arg0;
            return Arrays.equals(table, arg1.table);
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(table);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
   // @Override
   // public String toString() {
    //    String res = "";
      //  for (int value : table) {
        //    for (int i = 0; i < Integer.SIZE; i++) {
          //      res = res
          //              + ((Integer) (Bits.test(value, i) ? 1 : 0)).toString();
          //  }
        // }
        // return res;
   // }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
    String res = "";
    for (int i = 0; i < table.length; i++) {
	    StringBuilder add= new StringBuilder("00000000000000000000000000000000");
	    add=add.append(Integer.toBinaryString(table[i]));
	    add.reverse();
	    res=res.concat(add.substring(0,32));
    }
    return res;
    }

    /**
     * 
     * @return the number of bits in the BitVector
     */
    public int size() {
        return table.length*Integer.SIZE;
    }
    
    //Peut être intéressante ? Pas demandée pour le moment
    // public boolean testBit(Bit bit) {
    	// 		return testBit(bit.index());
    //		}

    /**
     * Checks if a bit is activated
     * @param index - the index of the bit
     * @return wether the bit is activated
     */
    public boolean testBit(int index) {
        Objects.checkIndex(index,size());
        return Bits.test(table[Math.floorDiv(index,32)], Math.floorMod(index,32));
    }

    /**
     * Computes the complementary BitVector
     * @return complementary BitVector
     */
    public BitVector not() {
        int[] notTable = new int[table.length];
        for (int i = 0; i < table.length; ++i) {
            notTable[i] = ~table[i];
        }
        return new BitVector(notTable);
    }

    /**
     * Computes the conjunction of two BitVectors
     * @param that - the BitVector with which we want to compute the 
     * conjuction
     * @return conjunction BitVector
     */
    public BitVector and(BitVector that) {
        Preconditions.checkArgument(that.size() == size());
        int[] andTable = new int[table.length];
        for (int i = 0; i < table.length; ++i) {
            andTable[i] = table[i] & that.table[i];
        }
        return new BitVector(andTable);
    }

    /**
     * Computes the disjunction of two BitVectors
     * @param that - the BitVector with which we want to compute the 
     * disjunction
     * @return disjunction BitVector
     */
    public BitVector or(BitVector that) {
        Preconditions.checkArgument(that.size() == size());
        int[] orTable = new int[table.length];
        for (int i = 0; i < table.length; ++i) {
            orTable[i] = table[i] | that.table[i];
        }
        return new BitVector(orTable);
    }

    protected enum ExtensionType {
        BYZERO, WRAPPED
    }

    /**
     * Computes the extraction of the instance using extension by zero
     * @param start - the index of the first bit in extraction
     * @param size - the size of the extracted BitVector
     * @return the extracted BitVector
     */
    public BitVector extractZeroExtended(int start, int size) {
        return extract(start, size, ExtensionType.BYZERO);
    }

    /**
     * Computes the extraction of the instance using wrapped extension
     * @param start - the index of the first bit in extraction
     * @param size - the size of the extracted BitVector
     * @return the extracted BitVector
     */
    public BitVector extractWrapped(int start, int size) {
    		return extract(start,size,ExtensionType.WRAPPED);
    }
    
    /**
     * Computes the shift of the instance
     * @param start - the index of the first bit in extraction
     * @return the shifted BitVector
     */
    public BitVector shift(int start) {
        return extractWrapped(-start,size());
    }

    private BitVector extract(int start, int size, ExtensionType ext) {
    		Preconditions.checkArgument((size > 0) && (size%32==0));
        int[] newTable = new int[size / 32];
        int internalShift = Math.floorMod(start, Integer.SIZE);
        int cellShift = Math.floorDiv(start, 32);
        int value;
        for (int i = 0; i < newTable.length; i++) {
            value = Bits.extract(
                    getIntAtIndexOfExtension(cellShift + i, ext), internalShift,
                    Integer.SIZE - internalShift)
                    | (Bits.clip(internalShift, getIntAtIndexOfExtension(
                            cellShift + i + 1, ext)) << (32 - internalShift));
            newTable[i] = value;
        }
        return new BitVector(newTable);
    }

    private int getIntAtIndexOfExtension(int index, ExtensionType ext) {
        switch (ext) {
        case BYZERO:
            return (index < 0 || index >= size() / 32)? 0 : table[index];
        case WRAPPED:
            return table[Math.floorMod(index, size() / 32)];
        }

        throw new IllegalStateException("how");
    }

  
    public final static class Builder {

        private byte[] table = null;

        /**
         * Builds a Builder
         * 
         * @param size
         *            the number of Bit of the desired BitVector
         */
        public Builder(int size) {
            Preconditions.checkArgument(size > 0 && size % 32 == 0);
            table = new byte[Math.floorDiv(size, 8)];
            Arrays.fill(table, (byte) 0);
        }

        /**
         * Sets the byte of weight index to a value
         * 
         * @param index
         * @param value
         * @return the updated Builder
         */
        public Builder setByte(int index, int value) {
            Preconditions.checkBits8(value);
            if (table == null) {
                throw new IllegalStateException();
            }
            Objects.checkIndex(index, table.length);
            table[index] = (byte) value;

            return this;
        }

        /**
         * Builds the BitVector corresponding to the Builder
         * 
         * @return the BitVector
         */
        public BitVector build() {
            Objects.requireNonNull(table);
            int[] argument = buildIntTable();
            table = null;
            return new BitVector(argument);
        }

        /**
         * Computes the int Array containing the same bits as table;
         * 
         * @return the int array
         */
        private int[] buildIntTable() {
            int[] res = new int[table.length / 4];
            Arrays.fill(res, 0);
            for (int rank = 0; rank < res.length; rank++) {
                res[rank] = (Byte.toUnsignedInt(table[(rank * 4)]))
                        + ((Byte.toUnsignedInt(table[(rank * 4) + 1]))
                                + ((Byte.toUnsignedInt(table[(rank * 4) + 2]))
                                        + ((Byte.toUnsignedInt(table[(rank * 4)
                                                + 3])) << 8) << 8) << 8);
            }
            return res;
        }

        public String toString() {
            return Arrays.toString(table);
        }
    }
}
