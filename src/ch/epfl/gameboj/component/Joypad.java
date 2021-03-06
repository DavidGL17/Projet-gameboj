package ch.epfl.gameboj.component;

import static ch.epfl.gameboj.Preconditions.checkBits16;
import static ch.epfl.gameboj.Preconditions.checkBits8;

import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;

import static ch.epfl.gameboj.bits.Bits.test;
import static ch.epfl.gameboj.bits.Bits.set;
import static ch.epfl.gameboj.bits.Bits.complement8;
import static ch.epfl.gameboj.AddressMap.REG_P1;

/**
 * @author David Gonzalez leon (270845)
 *
 */
public final class Joypad implements Component {

    /**
     * Represents the different keys of the gameboy. The attributes line and
     * index represent the position of each key in the "lines" of P1
     */
    public enum Key {
        RIGHT(0, 1), LEFT(1, 1), UP(2, 1), DOWN(3, 1), A(0, 2), B(1,
                2), SELECT(2, 2), START(3, 2);
        private final int index;
        private final int line;

        private Key(int index, int line) {
            this.index = index;
            this.line = line;
        }
    }

    private final Cpu cpu;
    private int line1 = 0;
    private int line2 = 0;

    public Joypad(Cpu cpu) {
        this.cpu = cpu;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        if (REG_P1 == checkBits16(address)) {
            return getP1();
        }
        return NO_DATA;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        if (REG_P1 == checkBits16(address)) {
            line1 = set(line1, 4,
                    !test(checkBits8(data), 4));
            line2 = set(line2, 5, !test(data, 5));
            ;
        }
    }

    /**
     * Presses the given key, updates P1 accordingly, and requests the JOYPAD
     * interrupt in the cpu if necessary
     * 
     * @param key,
     *            the key to press
     */
    public void keyPressed(Key key) {
        if (test(getP1(), key.index)
                && test((key.line == 1 ? line1 : line2), key.line + 3)) {
            cpu.requestInterrupt(Interrupt.JOYPAD);
        }
        switch (key.line) {
        case 1:
            line1 = set(line1, key.index, true);
            break;
        case 2:
            line2 = set(line2, key.index, true);
            break;
        }
    }

    /**
     * Releases the given key, and updates P1 accordingly
     * 
     * @param key,
     *            the key to release
     */
    public void keyReleased(Key key) {
        switch (key.line) {
        case 1:
            line1 = set(line1, key.index, false);
            break;
        case 2:
            line2 = set(line2, key.index, false);
            break;
        }
    }

    /**
     * Computes P1 according to which line is active, and which key is pressed
     * 
     * @return P1
     */
    private int getP1() {
        if (test(line1, 4) && test(line2, 5)) {
            return complement8(line1 | line2);
        }
        if (test(line1, 4)) {
            return complement8(line1);
        }
        if (test(line2, 5)) {
            return complement8(line2);
        }
        return 0xFF;
    }
}
