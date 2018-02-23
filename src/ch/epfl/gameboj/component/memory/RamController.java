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

    /**
     * Constructs a new ramController
     * 
     * @param ram
     *            the ram this component will control
     * @param startAddress
     *            at which the memory of the ram starts
     * @param endAddress
     *            at which the memory of the ram ends
     * @throws NullPointerException
     *             if the ram given is null
     * @throws IllegalArgumentException
     *             if the startAddress or endAddress is not a 16 bits number or
     *             if the range the describe is bigger or smaller tha the actual
     *             size of the ram
     */
    public RamController(Ram ram, int startAddress, int endAddress) {
        if (ram == null) {
            throw new NullPointerException();
        }
        Preconditions.checkArgument(endAddress - startAddress == ram.size());
        this.startAddress = Preconditions.checkBits16(startAddress);
        this.endAddress = Preconditions.checkBits16(endAddress);
        this.ram = ram;
    }

    /**
     * Constructs a new ramController with only the startAddress given
     * 
     * @param ram
     *            the ram this component will control
     * @param startAddress
     *            at which the memory of the ram starts
     */
    public RamController(Ram ram, int startAddress) {
        this(ram, startAddress, ram.size() + startAddress);
    }

    /**
     * Checks if the address given is in the range of the startAddress
     * (included) and the endAddress (not included)
     * 
     * @param address
     *            the address we want to evaluate
     * @return true if the address is in the range or false otherwise
     */
    private boolean checkAdressIsBetweenBounds(int address) {
        return (address >= startAddress && address < endAddress);
    }

    /**
     * Returns the element located at the given index
     * 
     * @param address
     *            The address where the element is located
     * @return The element located at the given address or NO_DATA if the Ram
     *         does not have any element at that address
     * @throws IllegalArgumentException
     *             if the address is not a 16 bits value
     */
    @Override
    public int read(int address) {
        if (checkAdressIsBetweenBounds(Preconditions.checkBits16(address))) {
            return ram.read(address - startAddress);
        } else {
            return Component.NO_DATA;
        }
    }

    /**
     * Storages the given value at the given address or does nothing if the
     * RamController can't stock anything at that address
     * 
     * @param address
     *            the address where we want to locate our element
     * @param data
     *            the data we want to storage
     * @throws IllegalArgumentException
     *             if the address is not a 16 bits value or if the data is not a
     *             8 bits value
     */
    @Override
    public void write(int address, int data) {
        if (checkAdressIsBetweenBounds(Preconditions.checkBits16(address))) {
            ram.write(address - startAddress, Preconditions.checkBits8(data));
        }
    }

}
