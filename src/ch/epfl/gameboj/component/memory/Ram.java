/**
 * 
 */
package ch.epfl.gameboj.component.memory;

import ch.epfl.gameboj.Preconditions;

/**
 * @author David (270845)
 *
 */
public final class Ram {
    
    private final byte[] data;
    
    public Ram(int size) {
        data = new byte[size];
    }
    public int size() {
        return data.length;
    }
    public int read(int index) {
        Preconditions.checkArgument(index<data.length);
        return Byte.toUnsignedInt(data[index]);
    }
    public void write(int index, int value) {
        Preconditions.checkArgument(index<data.length);
        Preconditions.checkBits8(value);
        data[index]=(byte) value;
    }
}
