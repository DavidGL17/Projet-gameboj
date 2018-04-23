/**
 * 
 */
package ch.epfl.gameboj.component.lcd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.memory.Ram;

/**
 * @author David Gonzalez leon (270845)
 *
 */
public final class LcdController implements Clocked, Component {

    public final static int LCD_WIDTH = 160;
    public final static int LCD_HEIGHT = 140;
    
    private final Cpu cpu;
    private LcdImage defaultImage;
    private final Ram videoRam;

    
    
    public LcdController(Cpu cpu) {
        this.cpu = cpu;
        videoRam = new Ram(AddressMap.VIDEO_RAM_SIZE);
        List<LcdImageLine> lines = new ArrayList<>();
        Collections.fill(lines, new LcdImageLine(new BitVector(LCD_WIDTH), new BitVector(LCD_WIDTH), new BitVector(LCD_WIDTH)));
        defaultImage = new LcdImage(lines, LCD_WIDTH, LCD_HEIGHT);
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Clocked#cycle(long)
     */
    @Override
    public void cycle(long cycle) {
        // TODO Auto-generated method stub

    }
    
    //todo
    public LcdImage currentImage() {
        return defaultImage;
    }

}
