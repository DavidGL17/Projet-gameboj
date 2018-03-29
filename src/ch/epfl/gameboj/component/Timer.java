/**
 * @author Melvin Malonga-Matouba (288405)
 */
package ch.epfl.gameboj.component;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;

public final class Timer implements Component, Clocked {

    private Cpu cpu;
    private int principalCounter = 0;
    private int secondaryCounter = 0;
    private int TMA = 0;
    private int TAC = 0;

    public Timer(Cpu cpu) {
        if (cpu == null)
            throw new NullPointerException();
        this.cpu = cpu;
    }

    @Override
    public void cycle(long cycle) {
        for (int i = 0; i < cycle; ++i) {
            boolean s0 = state();
            principalCounter = Bits.clip(16, principalCounter + 4);
            incIFChange(s0);
        }
    }

    @Override
    public int read(int address) {

        if (address == AddressMap.REG_DIV) {
            return Bits.extract(principalCounter, 8, 8);
        } else if (address == AddressMap.REG_TIMA) {
            return secondaryCounter;
        } else if (address == AddressMap.REG_TMA) {
            return TMA;
        } else if (address == AddressMap.REG_TAC) {
            return TAC;
        }
        return NO_DATA;
    }

    @Override
    public void write(int address, int data) {

        if (address == AddressMap.REG_DIV) {
            boolean s0 = state();
            principalCounter = Bits.make16(Preconditions.checkBits8(data),
                    Bits.clip(8, principalCounter));
            incIFChange(s0);
        } else if (address == AddressMap.REG_TIMA) {
            secondaryCounter = Preconditions.checkBits8(data);
        } else if (address == AddressMap.REG_TMA) {
            TMA = Preconditions.checkBits8(data);
        } else if (address == AddressMap.REG_TAC) {
            boolean s0 = state();
            TAC = Preconditions.checkBits8(data);
            incIFChange(s0);
        }
    }

    private boolean state() {
        switch (Bits.extract(TAC, 0, 3)) {
        case 0b100: 
            return checkBitsActivated(9);
        case 0b101: 
            return checkBitsActivated(3);
        case 0b110: 
            return checkBitsActivated(5);
        case 0b111: 
            return checkBitsActivated(7);
        default: 
            return false;
        }
        //Version test. (Sans le &&principalCounter il augmente bien Tima, par contre l'exception n'est kamais lancée)
//	    switch (Bits.extract(TAC, 0, 3)) {
//	    case 0b100: {
//	        return ((principalCounter & principalCounterMask(9)) == 0)&&principalCounter!=0;
//	    }
//	    case 0b101: {
//	        return ((principalCounter & principalCounterMask(3)) == 0)&&principalCounter!=0;
//	    }
//	    case 0b110: {
//	        return ((principalCounter & principalCounterMask(5)) == 0)&&principalCounter!=0;
//	    }
//	    case 0b111: {
//	        return ((principalCounter & principalCounterMask(7)) == 0)&&principalCounter!=0;
//	    }
//	    default: {
//	        return false;
//	    }
//	    }
	}

    private boolean checkBitsActivated(int msb) {
        boolean bitsActivated= false;
        for (int i = 0; i <= msb; ++i) {
           bitsActivated |= Bits.test(principalCounter, i);
        }
        return bitsActivated;
    }

    private void incIFChange(boolean previous) {
        if (!state() && previous) {
            if (secondaryCounter == 0xFF) {
                cpu.requestInterrupt(Interrupt.TIMER);
                secondaryCounter = TMA;
            } else {
                ++secondaryCounter;
            }
        }
    }

}