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
    		Preconditions.checkBits16(address);
        if (address == AddressMap.REG_DIV) {
            principalCounter=0;
        } else if (address == AddressMap.REG_TIMA) {
            TIMA = Preconditions.checkBits8(data);
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

    
    //Problème : impose un passage par 11...1 (i+1 bits ou i est la valeur désignée par TAC)
    	// Or notre Timer simulé avance de 4 en 4 donc le bit de poids 0 vaut toujours 0
    // Ainsi si on veut mesurer tous les états d'activations on aura toujours 0/faux.
    // La disjonction n'est pas ce que l'on souhaite faire :
    // si le bit de poids i-1 uniquement est activé alors on considérera que tous jusqu'à i le sont, ce qui est faux
    //
    // private boolean checkBitsActivated(int msb) {
        // boolean bitsActivated = true;
        // for (int i = 0; i <= msb; ++i) {
            // bitsActivated |= Bits.test(principalCounter, i);
        // }
        // return bitsActivated;
    // }

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
