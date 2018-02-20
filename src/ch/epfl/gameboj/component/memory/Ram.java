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
        Preconditions.checkArgument(size>=0);
        data = new byte[size];
    }
    public int size() {
        return data.length;
    }
    public int read(int index) {
        if (index>=data.length || index<0){
            throw new IndexOutOfBoundsException();
        }
        return Byte.toUnsignedInt(data[index]);
    }
    public void write(int index, int value) {
        if (index>=data.length || index<0){
            throw new IndexOutOfBoundsException();
        }
        Preconditions.checkBits8(value);
        data[index]=(byte) value;
    }
}
