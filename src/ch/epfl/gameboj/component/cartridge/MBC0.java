/**
 * 
 */
package ch.epfl.gameboj.component.cartridge;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

/**
 * @author David Gonzalez leon (270845)
 *
 */
public final class MBC0 implements Component {

    private Rom rom;

    public MBC0(Rom rom) {
        Objects.requireNonNull(rom);
        Preconditions.checkArgument(rom.size() == 0x8000);
        this.rom = rom;
    }

    /**
     * Returns the element located at the given index
     * 
     * @param address
     *            The address where the element is located
     * @return The element located at the given address or NO_DATA if the
     *         component does not have any element at that address
     * @throws IllegalArgumentException
     *             if the address is not a 16 bits value
     */
    @Override
    public int read(int address) {
        if (0 <= Preconditions.checkBits16(address) && address < 0x8000) {
            return rom.read(address);
        }
        return NO_DATA;
    }

    /*
     * Does nothing, as it is not possible to write on a rom
     */
    @Override
    public void write(int address, int data) {
    }

}
