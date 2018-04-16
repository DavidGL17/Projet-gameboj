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
    private final int size;

    public BitVector(int size, boolean initialValue) {
        Preconditions.checkArgument(size >= 0 && (size % 32) == 0);
        table = new int[size / 32]; 
        this.size=size;
        Arrays.fill(table, initialValue ? -1 : 0);
    }

    public BitVector(int size) {
        this(size, false);
    }

    public BitVector(int[] table) {
        this.table = table; // Ne pas faire une copie conforme !
        size=table.length<<5;
    }

    private BitVector(BitVector bv) {
        this(bv.table);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object arg0) {
        if (arg0 instanceof int[]) {
            return Arrays.equals(table, (int[]) arg0);
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
    @Override
    public String toString() {
    		String res = new String();
    			for (int i=0 ; i<table.length ; i++) {
    	            for (int j = 0 ; j<32 ; ++j) {
    	                res = res + (Bits.test(table[i], j) ? '1' : '0');
    	            }

			}
		return res;
    }
    

    public int size() {
        return size;
    }

    public boolean testBit(int index) {
        Preconditions.checkArgument(index <size && index >= 0);
        return Bits.test(table[index / 32],index % 32);
    }

    
    public BitVector not() {
        int[] notTable = new int[table.length];
        for (int i = 0; i < table.length; ++i) {
        		notTable[i] = ~ table[i];
        }
        return new BitVector(notTable);
    }
    
   

    public BitVector and(BitVector that) {
        Preconditions.checkArgument(that.size() == size);
        int[] andTable = new int[table.length];
        for (int i = 0; i < table.length; ++i) {
            andTable[i] = table[i] & that.table[i];
        }
        return new BitVector(andTable);
    }
    

    public BitVector or(BitVector that) {
        Preconditions.checkArgument(that.size() == size);
        int[] orTable = new int[table.length];
        for (int i = 0; i < table.length; ++i) {
            orTable[i] = table[i] | that.table[i];
        }
        return new BitVector(orTable);
    }
    
    
    protected enum ExtensionType {
    		BYZERO, WRAPPED
    }
    
    public BitVector extractZeroExtended(int start, int size) {
		
			return new BitVector(extractTable(start,size,ExtensionType.BYZERO));
		
	}

	public BitVector extractWrapped(int start, int size) {
		
			return new BitVector(extractTable(start,size,ExtensionType.WRAPPED));
	}

	private int[] extractTable(int start, int size, ExtensionType ext) {
		int[] newTable = new int[size / 32 + 1];
		if (Math.floorMod(start, 32) == 0) { // Optionnal
			switch (ext) {
			case BYZERO:
				int i = 0;
				while (i < newTable.length) {
					while (i + start < 0) {
						newTable[i] = 0;
						i++;
					}
					while (i + start < this.size) {
						newTable[i] = table[start + i];
						i++;
					}
					newTable[i] = table[i + start];
				}
				break;
			case WRAPPED:
				for (int j = 0; j < newTable.length; j++) {
					newTable[j] = table[Math.floorMod(start + j, this.size)];
				}
				break;
			}
		} else {
			for (int i=0; i<newTable.length ; i++) {
				int value=0;
				for (int j=0 ; j<Integer.SIZE ; j++) {
					value+=(testBit(i*Integer.SIZE+j)?1 :0) << j;
				}
				newTable[i]= value;
			}
    		}
		return newTable;
    }
    
	private int bitAtIndexOfExtension(int index, ExtensionType ext) {
		if (index >= 0 && index < size) {
			return testBit(index) ? 1 : 0;
		} else {
			switch (ext) {
			case BYZERO:
				return 0;
			case WRAPPED:
				return testBit(Math.floorMod(index,size)) ? 1 : 0;
			default:
				Objects.requireNonNull(ext);
				throw new IllegalArgumentException(" How ? ");
			}
		
	   }
   }
   
   
    public final static class Builder {

        private byte[] table=null;

        /**
         * Builds a Builder
         * 
         * @param size
         *            the number of Bit of the desired BitVector
         */
        public Builder(int size) {
            Preconditions.checkArgument(size > 0 && size % 32 == 0);
            table = new byte[Math.floorDiv(size, 8)];
            Arrays.fill(table,(byte)0);
        }

        /**
         * Sets the byte of weight index to a value
         * 
         * @param index
         * @param value
         * @return the updated Builder
         */
        public Builder setByte(int index, byte value) {
            if (table == null) {
                throw new IllegalStateException();
            }
            Objects.checkIndex(index, table.length);
            table[index] = value;

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
            Arrays.fill(res,0);
            for (int rank = 0; rank < res.length; rank++) {
                res[rank] = table[rank * 4]
                        + (table[rank * 4 + 1] + (table[rank * 4 + 2]
                                + (table[rank * 4 + 3] << 8) << 8) << 8);
            }
            return res;
        }
        
        public String toString() {
        		return Arrays.toString(table);
        }
    }
}
