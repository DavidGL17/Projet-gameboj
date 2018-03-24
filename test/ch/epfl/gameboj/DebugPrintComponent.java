/**
 * 
 */
package ch.epfl.gameboj;

import ch.epfl.gameboj.component.Component;

/**
 * @author David Gonzalez leon (270845)
 *
 */
public final class DebugPrintComponent implements Component {
    @Override
    public int read(int address) {
        return NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        if (address == 0xFF01)
            System.out.print((char) data);
    }
}
