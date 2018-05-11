package ch.epfl.gameboj.bits;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;

/*
 * 
 * @author Melvin Malonga-Matouba (288405)
 * @author David González León (270845)
 * 
 */

public final class Bits {

    private Bits() {
    };

    private final static int[] REVERSE_TABLE = new int[] { 0x00, 0x80, 0x40,
            0xC0, 0x20, 0xA0, 0x60, 0xE0, 0x10, 0x90, 0x50, 0xD0, 0x30, 0xB0,
            0x70, 0xF0, 0x08, 0x88, 0x48, 0xC8, 0x28, 0xA8, 0x68, 0xE8, 0x18,
            0x98, 0x58, 0xD8, 0x38, 0xB8, 0x78, 0xF8, 0x04, 0x84, 0x44, 0xC4,
            0x24, 0xA4, 0x64, 0xE4, 0x14, 0x94, 0x54, 0xD4, 0x34, 0xB4, 0x74,
            0xF4, 0x0C, 0x8C, 0x4C, 0xCC, 0x2C, 0xAC, 0x6C, 0xEC, 0x1C, 0x9C,
            0x5C, 0xDC, 0x3C, 0xBC, 0x7C, 0xFC, 0x02, 0x82, 0x42, 0xC2, 0x22,
            0xA2, 0x62, 0xE2, 0x12, 0x92, 0x52, 0xD2, 0x32, 0xB2, 0x72, 0xF2,
            0x0A, 0x8A, 0x4A, 0xCA, 0x2A, 0xAA, 0x6A, 0xEA, 0x1A, 0x9A, 0x5A,
            0xDA, 0x3A, 0xBA, 0x7A, 0xFA, 0x06, 0x86, 0x46, 0xC6, 0x26, 0xA6,
            0x66, 0xE6, 0x16, 0x96, 0x56, 0xD6, 0x36, 0xB6, 0x76, 0xF6, 0x0E,
            0x8E, 0x4E, 0xCE, 0x2E, 0xAE, 0x6E, 0xEE, 0x1E, 0x9E, 0x5E, 0xDE,
            0x3E, 0xBE, 0x7E, 0xFE, 0x01, 0x81, 0x41, 0xC1, 0x21, 0xA1, 0x61,
            0xE1, 0x11, 0x91, 0x51, 0xD1, 0x31, 0xB1, 0x71, 0xF1, 0x09, 0x89,
            0x49, 0xC9, 0x29, 0xA9, 0x69, 0xE9, 0x19, 0x99, 0x59, 0xD9, 0x39,
            0xB9, 0x79, 0xF9, 0x05, 0x85, 0x45, 0xC5, 0x25, 0xA5, 0x65, 0xE5,
            0x15, 0x95, 0x55, 0xD5, 0x35, 0xB5, 0x75, 0xF5, 0x0D, 0x8D, 0x4D,
            0xCD, 0x2D, 0xAD, 0x6D, 0xED, 0x1D, 0x9D, 0x5D, 0xDD, 0x3D, 0xBD,
            0x7D, 0xFD, 0x03, 0x83, 0x43, 0xC3, 0x23, 0xA3, 0x63, 0xE3, 0x13,
            0x93, 0x53, 0xD3, 0x33, 0xB3, 0x73, 0xF3, 0x0B, 0x8B, 0x4B, 0xCB,
            0x2B, 0xAB, 0x6B, 0xEB, 0x1B, 0x9B, 0x5B, 0xDB, 0x3B, 0xBB, 0x7B,
            0xFB, 0x07, 0x87, 0x47, 0xC7, 0x27, 0xA7, 0x67, 0xE7, 0x17, 0x97,
            0x57, 0xD7, 0x37, 0xB7, 0x77, 0xF7, 0x0F, 0x8F, 0x4F, 0xCF, 0x2F,
            0xAF, 0x6F, 0xEF, 0x1F, 0x9F, 0x5F, 0xDF, 0x3F, 0xBF, 0x7F, 0xFF, };

    /**
     * Computes the mask corresponding to a certain index
     * 
     * @param index,
     *            the index of the bit to activate
     * @return the corresponding mask
     * @throws IndexOutOfBoundsException
     *             If parameter i doesn't belong to [0,31]
     */
    public static int mask(int index) {
        return (0b01 << Objects.checkIndex(index, Integer.SIZE));

    }

    /**
     * Determines if the bit of a given weight is activated in an int
     * 
     * @param bits,
     *            the int
     * @param index,
     *            the weight of the bit
     * @return wether the bit of weight index is activated in bits
     * @throws IndexOutOfBoundsException
     *             If the index is invalid for a bit in a int
     */
    public static boolean test(int bits, int index) {
        Objects.checkIndex(index, Integer.SIZE);
        bits = bits & mask(index);
        bits = bits >>> index;
        return (bits == 1);
    }

    /**
     * Determines wether a certain Bit is activated in an int
     * 
     * @param bits,
     *            the int
     * @param bit,
     *            the Bit
     * @return a boolean : wether bit is activated in bits
     * @throws IndexOutOfBoundsException
     *             If the return of bit.index() is out of [0,31] ---> Check the
     *             class of bit
     */
    public static boolean test(int bits, Bit bit) {
        return test(bits, bit.index());
    }

    /**
     * Computes a byte where the bit of weight index is set to 1 or 0
     * 
     * @param bits,
     *            the original int
     * @param index,
     *            the weight of the bit
     * @param newValue,
     *            true if the bit should be set to 1, false to 0
     * @return bits but with index-th bit's value set
     * @throws IndexOutOfBoundsException
     *             If the index is invalid for a bit in an int
     */
    public static int set(int bits, int index, boolean newValue) {
        Objects.checkIndex(index, Integer.SIZE);
        if (test(bits, index)) {
            if (newValue) {
                return bits;
            } else {
                return bits & (~mask(index));
            }
        } else {
            if (newValue) {
                return bits | mask(index);
            } else {
                return bits;
            }
        }
    }

    /**
     * Computes the int corresponding to the (size) least significant bits
     * 
     * @param size,
     *            the number of bits
     * @param bits,
     *            the original int
     * @return the int corresponding to the (size) LSB
     * @throw IllegalArgumentException if parameter size isn't within [0,32]
     */
    public static int clip(int size, int bits) {

        if ((size < 0) || (size > 32)) {
            System.out.print(size);
            throw new IllegalArgumentException();
        }

        int mask = 0;
        for (int i = 0; i < size; i++) {
            mask += mask(i);
        }

        return (bits & mask);
    }

    /**
     * Extract the bits from index start to start+size of an int
     * 
     * @param bits,
     *            the int
     * @param start,
     *            the index of the first extracted bit
     * @param size,
     *            the number of bits to extract
     * @return the extracted bit-sequence
     * @throws IndexOutOfBoundsException
     *             if the start and size is invalid for a bit-sequence in an int
     */
    public static int extract(int bits, int start, int size) {
        return clip(size,
                bits >>> Objects.checkFromIndexSize(start, size, Integer.SIZE));
    }

    /**
     * Computes the rotation of a number given the size of the number and
     * distance of rotation
     * 
     * @param size,
     *            the size of the number
     * @param bits,
     *            the value of a number
     * @param distance,
     *            the number of bits by which the bits are rotated positive if
     *            the direction of the rotation is left, negative otherwise
     * @return the rotated int sequence
     * @throws IllegalArgumentException
     *             if size isn't within [0,32]
     */
    public static int rotate(int size, int bits, int distance) {
        if ((size <= 0) || (size > Integer.SIZE)) {
            throw new IllegalArgumentException();
        }

        if (size < 31) {
            if (bits >= (0b1 << size)) {
                throw new IllegalArgumentException();
            }
        } else if (size == 31) {
            if (bits >>> 1 >= 0b1 << (size - 1)) {
                throw new IllegalArgumentException();
            }
        }
        int res = 0;
        int decalage = distance % size;

        for (int i = 0; i < size; i++) {
            if (test(bits, ((i + size - decalage) % size))) {
                res += mask(i);
            }
        }

        return res;
    }

    /**
     * Computes from a byte the int where the 8 LSB are the same and the rest is
     * of the sign of the MSB
     * 
     * @param b,
     *            the byte
     * @return the int
     * @throws IllegalArgumentException
     *             if b isn't a byte
     */
    public static int signExtend8(int b) {
        if (test(Preconditions.checkBits8(b), 7)) {
            return b | (0xFFFFFF80);
        } else {
            return b;
        }
    }

    /**
     * Computes a byte where the bits are reversed relative to center
     * 
     * @param b,
     *            the byte
     * @return the byte
     * @throws IllegalArgumentException
     *             if b isn't a byte
     */
    public static int reverse8(int b) {
        Preconditions.checkBits8(b);
        return REVERSE_TABLE[b];
    }

    /**
     * Computes the complement of a byte
     * 
     * @param b,
     *            the byte
     * @return the byte
     * @throws IllegalArgumentException
     *             if b isn't a byte
     */
    public static int complement8(int b) {
        return (0xff ^ Preconditions.checkBits8(b));
    }

    /**
     * Computes a 16 bits code given two 8-bits sequences.
     * 
     * @param highB,
     *            the 8 MSB code
     * @param lowB,
     *            the 8 LSB code
     * @return the concatenated 16-bits value
     * @throws IllegalArgumentException
     *             if either highB or lowB isn't a byte
     */
    public static int make16(int highB, int lowB) {
        return (Preconditions.checkBits8(highB) << 8)
                | Preconditions.checkBits8(lowB);

    }

}