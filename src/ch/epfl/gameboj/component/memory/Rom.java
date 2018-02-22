/**
 * 
 */
package ch.epfl.gameboj.component.memory;

import java.util.Objects;

/**
 * 
 * @author David (270845)
 *
 */
public final class Rom {

    private final byte[] data;

    /**
     * Copies the elements of the parameter in the data array of the class
     * 
     * @param data,
     *            a byte array
     * @throws NullPointerException
     *             if the parameter is null
     */
    public Rom(byte[] data) {
        Objects.requireNonNull(data);
        this.data = new byte[data.length];
        for (int i = 0; i < data.length; ++i) {
            this.data[i] = data[i];
        }
    }

    /**
     * Returns the size of the rom's memory in octets
     * 
     * @return the size of the rom's memory
     */
    public int size() {
        return data.length;
    }

    /**
     * Returns the octet located at the index given in the rom's memory as a
     * value between 0 and 0xff
     * 
     * @param index
     *            the index were the octet is located
     * @return The element located at the specified index
     * @throws IndexOutOfBoundsException
     *             if the index is bigger or smaller than the size of the rom's memory
     */
    public int read(int index) {
        if (index >= data.length) {
            throw new IndexOutOfBoundsException();
        }
        return Byte.toUnsignedInt(data[index]);
    }
}
