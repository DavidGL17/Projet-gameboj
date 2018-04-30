/**
 * 
 */
package ch.epfl.gameboj.component;

import ch.epfl.gameboj.component.cpu.Cpu;

/**
 * @author David Gonzalez leon (270845)
 *
 */
public final class Joypad implements Component {

    private final Cpu cpu;
    private int P1;
    
    public Joypad(Cpu cpu) {
        this.cpu = cpu;
    }
    

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        
        return 0;
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        // TODO Auto-generated method stub

    }

}
