/**
 * 
 */
package ch.epfl.gameboj.component.memory;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;

/**
 * @author David (270845)
 *
 */
public final class RamController implements Component {
    
    private final int startAddress;
    private int endAddress;
    private Ram ram;

    public RamController(Ram ram, int startAddress, int endAddress) {
        if (ram == null) {
            throw new NullPointerException();
        }
        Preconditions.checkBits16(endAddress);
        Preconditions.checkBits16(startAddress);
        Preconditions.checkArgument(endAddress-startAddress == ram.size());
        this.startAddress = startAddress; 
        this.endAddress = endAddress;
        this.ram = ram;
    }
    
    public RamController(Ram ram, int startAddress) {
        this(ram, startAddress, ram.size()+startAddress);
    }
    
    private boolean checkAdressIsBetweenBounds(int address) {
        return !(address<startAddress || address>=endAddress);
    }
    
    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if(checkAdressIsBetweenBounds(address)) {
            return ram.read(address-startAddress);
        } else {
            return Component.NO_DATA;
        }    
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        if(checkAdressIsBetweenBounds(address)) {
            ram.write(address-startAddress, data);
        }
    }

}
