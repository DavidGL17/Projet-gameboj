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
        this.size = size;
        Arrays.fill(table, initialValue ? -1 : 0);
    }

    public BitVector(int size) {
        this(size, false);
    }

    public BitVector(int[] table) {
        this.table = table; // Ne pas faire une copie conforme !
        size = table.length << 5;
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
        String res = "";
        final String LEADING_ZEROS = "0000000000000000000000000000000";
        for (int i = 0; i < table.length; i++) {
           String add=LEADING_ZEROS+Integer.toBinaryString(table[i]);
           add=add.substring(add.length()-32,add.length());
           res+=add;
        }
        return res;
    }

    public int size() {
        return size;
    }

    public boolean testBit(int index) {
        Preconditions.checkArgument(index < size && index >= 0);
        return Bits.test(table[index / 32], index % 32);
    }

    public BitVector not() {
        int[] notTable = new int[table.length];
        for (int i = 0; i < table.length; ++i) {
            notTable[i] = ~table[i];
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

        return new BitVector(extractTable(start, size, ExtensionType.BYZERO));

    }

    public BitVector extractWrapped(int start, int size) {

        return new BitVector(extractTable(start, size, ExtensionType.WRAPPED));
    }

    private int[] extractTable(int start, int size, ExtensionType ext) {
        int[] newTable = new int[size / 32];
        int internalShift = Math.floorMod(start, Integer.SIZE);
        int cellShift = Math.floorDiv(start, 32);
        for (int i = 0; i < newTable.length; i++) {
            int value = Bits.extract(
                    getIntAtIndexOfExtension(cellShift + i, ext), internalShift,
                    Integer.SIZE - internalShift)
                    | (Bits.clip(internalShift, getIntAtIndexOfExtension(
                            cellShift + i + 1, ext)) << (32 - internalShift));
            newTable[i] = value;
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
                return testBit(Math.floorMod(index, size)) ? 1 : 0;
            default:
                Objects.requireNonNull(ext);
                throw new IllegalArgumentException(" How ? ");
            }

        }
    }

    private int getIntAtIndexOfExtension(int index, ExtensionType ext) {
        switch (ext) {
        case BYZERO:
            if (index < 0 || index >= size() / 32) {
                return 0;
            } else {
                return table[index];
            }
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
