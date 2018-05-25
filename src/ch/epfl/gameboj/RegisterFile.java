package ch.epfl.gameboj;

import java.util.ArrayList;

import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

/**
 * 
 * @author David Gonzalez leon (270845)
 *
 */
public final class RegisterFile<E extends Register> {

    private final byte[] registers;

    public RegisterFile(E[] allRegs) {
        Preconditions.checkArgument(allRegs.length > 0);
        registers = new byte[allRegs.length];
    }

    /**
     * Returns the 8 bit value contained in the given register
     * 
     * @param reg,
     *            the given register
     * @return value, the value stored in the given register
     */
    public int get(E reg) {
        return Byte.toUnsignedInt(registers[reg.index()]);
    }

    /**
     * Sets the value of the given register to the new value
     * 
     * @param reg,
     *            the given register
     * @param newValue,
     *            the new value
     * @throws IllegalArgumentException
     *             if the new value is not an 8 bit value
     */
    public void set(E reg, int newValue) {
        registers[reg.index()] = (byte)Preconditions.checkBits8(newValue);
    }

    /**
     * Returns true if the bit of the given register is 1
     * 
     * @param reg,
     *            the given register
     * @param b,
     *            the number of the bit we want to test
     * @return 1 if the bit waas 1, false otherwise
     */
    public boolean testBit(E reg, Bit b) {
        return Bits.test(registers[reg.index()], b);
    }

    /**
     * Set the bit of the given register to the given new value
     * 
     * @param reg,
     *            the given register
     * @param bit,
     *            the given bit
     * @param newValue,
     *            the new value
     */
    public void setBit(E reg, Bit bit, boolean newValue) {
        set(reg, Bits.set(registers[reg.index()], bit.index(), newValue));
    }

   
}
