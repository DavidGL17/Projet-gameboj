/**
 * 
 */
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

    public BitVector(int size, boolean initialValue) {
        Preconditions.checkArgument(size >= 0 && (size % 32) == 0);
        table = new int[size / 32];
        Arrays.fill(table, initialValue ? -1 : 0);
    }

    public BitVector(int size) {
        this(size, false);
    }

    private BitVector(int[] table) {
        this.table = table.clone();
    }

    private BitVector(BitVector bv) {
        this(bv.table);
    }

    public int size() {
        return table.length * 32;
    }

    public boolean testBit(int index) {
        Preconditions.checkArgument(index < table.length * 32 && index >= 0);
        return Bits.test(index % 32, table[index / 32]);
    }

    public int[] not() {
        int[] notTable = new int[table.length];
        for (int i = 0; i < table.length; ++i) {
            notTable[i] = -1 & table[i];
        }
        return notTable;
    }

    public int[] and(BitVector that) {
        Preconditions.checkArgument(that.size() == this.size());
        int[] andTable = new int[table.length];
        for (int i = 0; i < table.length; ++i) {
            andTable[i] = table[i] & that.table[i];
        }
        return andTable;
    }

    public int[] or(BitVector that) {
        Preconditions.checkArgument(that.size() == this.size());
        int[] orTable = new int[table.length];
        for (int i = 0; i < table.length; ++i) {
            orTable[i] = table[i] | that.table[i];
        }
        return orTable;
    }

    public static class Builder {

        private byte[] table;

        /**
         * Builds a Builder
         * 
         * @param size
         *            the number of Bit of the desired BitVector
         */
        public Builder(int size) {
            Preconditions.checkArgument(size >= 0 && size % 32 == 0);
            table = new byte[Math.floorDiv(size, 8)];
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
            // If the Builder hasn't been constructed throw NullPointerException
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
            for (int rank = 0; rank < res.length; rank++) {
                res[rank] = table[rank * 4]
                        + (table[rank * 4 + 1] + (table[rank * 4 + 2]
                                + (table[rank * 4 + 3] << 8) << 8) << 8);
            }
            return res;
        }
    }
}
