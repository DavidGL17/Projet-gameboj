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

    private final Rom rom;
    private static final int romSize = 0x8000;
    
    
    public MBC0(Rom rom) {
        Objects.requireNonNull(rom);
        Preconditions.checkArgument(rom.size() == romSize);
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
        if (0 <= Preconditions.checkBits16(address) && address < romSize) {
            return rom.read(address);
        }
        return NO_DATA;
    }

    /*
     * Does nothing, as it is not possible to write on a memory bank of type 0
     */
    @Override
    public void write(int address, int data) {
    }

}
