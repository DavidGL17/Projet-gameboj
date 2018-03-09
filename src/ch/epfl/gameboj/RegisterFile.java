/**
 * 
 */
package ch.epfl.gameboj;

import java.util.ArrayList;

import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

/**
 * @author David Gonzalez leon (270845)
 *
 */
public final class RegisterFile<E extends Register> {
    private ArrayList<E> allRegs = new ArrayList<>();
    private int[] registers;
    
    public RegisterFile(E[] allRegs) {
        Preconditions.checkArgument(allRegs.length>0);
        for(int i = 0; i<allRegs.length;++i) {
            this.allRegs.add(allRegs[i]);
        }
        registers = new int[allRegs.length];
    }
    
    /**
     * Checks at which index is the given register stocked
     * 
     * @param reg
     * @return
     */
    private int getIndex(E reg) {
        for(int i=0;i<allRegs.size();++i) {
            if (allRegs.get(i)==reg) {
                return i;
            }
        }
        throw new IllegalArgumentException();
    }
    public int get(E reg) {
        return registers[getIndex(reg)];
    }
    public void set(E reg, int newValue) {
        registers[getIndex(reg)] = Preconditions.checkBits8(newValue);
    }
    public boolean testBit(E reg, Bit b) {
        return Bits.test(registers[getIndex(reg)], b);
    }
    public void setBit(E reg, Bit bit, boolean newValue) {
        registers[getIndex(reg)] = Bits.set(registers[getIndex(reg)], bit.index(), newValue);
    }
}
