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

    private final Cpu cpu;
    private int principalCounter = 0;
    private int TIMA = 0;
    private int TMA = 0;
    private int TAC = 0;

    public Timer(Cpu cpu) {
        if (cpu == null)
            throw new NullPointerException();
        this.cpu = cpu;
    }

    @Override
    public void cycle(long cycle) {
        boolean s0 = state();
        principalCounter = Bits.clip(16, principalCounter + 4);
        incIFChange(s0);
    }

    @Override
    public int read(int address) {
    		Preconditions.checkBits16(address);
        if (address == AddressMap.REG_DIV) {
            return Bits.extract(principalCounter, 8, 8);
        } else if (address == AddressMap.REG_TIMA) {
            return TIMA;
        } else if (address == AddressMap.REG_TMA) {
            return TMA;
        } else if (address == AddressMap.REG_TAC) {
            return TAC;
        }
        return NO_DATA;
    }

    @Override
    public void write(int address, int data) {
    		Preconditions.checkBits8(data);
        if (Preconditions.checkBits16(address) == AddressMap.REG_DIV) {
            principalCounter=0;
        } else if (address == AddressMap.REG_TIMA) {
            TIMA = data;
        } else if (address == AddressMap.REG_TMA) {
            TMA = data;
        } else if (address == AddressMap.REG_TAC) {
            boolean s0 = state();
            TAC = data;
            incIFChange(s0);
        }
    }

    private boolean state() {
        switch (Bits.extract(TAC, 0, 3)) {
        case 0b100: {
        return ((principalCounter & Bits.mask(9)) != 0);
        }
        case 0b101: {
        return ((principalCounter & Bits.mask(3)) != 0);
        }
        case 0b110: {
        return ((principalCounter & Bits.mask(5)) != 0);
        }
        case 0b111: {
        return ((principalCounter & Bits.mask(7)) != 0);
        }
        default: {
        return false;
    			}
         }
    }


    private void incIFChange(boolean previous) {
        if (!state() && previous) {
            if (TIMA == 0xFF) {
                cpu.requestInterrupt(Interrupt.TIMER);
                TIMA = TMA;
            } else {
                ++TIMA;
            }
        }
    }

}
