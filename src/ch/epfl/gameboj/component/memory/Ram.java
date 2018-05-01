/**
 * 
 */
package ch.epfl.gameboj.component.memory;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;

/**
 * 
 * @author David (270845)
 *
 */
public final class Ram {

    private final byte[] data;

    /**
     * Creates a new ram memory of the size given
     * 
     * @param size,
     *            the size we want the memory to be
     * @throws IllegalArgumentException
     *             if the size is strictly smaller than 0
     */ 
    public Ram(int size) {
        Preconditions.checkArgument(size >= 0);
        data = new byte[size];
    }

    /**
     * Returns the size of the ram's memory in octets
     * 
     * @return the size of the ram's memory
     */
    public int size() {
        return data.length;
    }

    /**
     * Returns the octet located at the index given in the ram's memory as a
     * value between 0 and 0xff
     * 
     * @param index
     *            the index were the octet is located
     * @return The element located at the specified index
     * @throws IndexOutOfBoundsException
     *             if the index is bigger or smaller than the size of the ram's
     *             memory
     */
    public int read(int index) {
        return Byte.toUnsignedInt(data[Objects.checkIndex(index, data.length)]);
    }

    /**
     * Modifies the content of the ram's memory at the given index
     * 
     * @param index
     *            where we want to modify the ram's memory
     * @param value
     *            The value that we want to put at the given index in the ram's
     *            memory
     * @throws IndexOutOfBoundsException
     *             if the index is bigger or smaller than the size of the ram's
     *             memory
     * @throws IllegalArgumentException
     *             if the value is not an 8 bits value
     */
    public void write(int index, int value) {
        data[Objects.checkIndex(index, data.length)] = (byte) Preconditions
                .checkBits8(value);
    }
}
