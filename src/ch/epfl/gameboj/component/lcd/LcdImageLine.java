/**
 * 
 */
package ch.epfl.gameboj.component.lcd;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.BitVector;

/**
 * @author David Gonzalez leon (270845)
 *
 */
public final class LcdImageLine {
    private final BitVector msb;
    private final BitVector lsb;
    private final BitVector opacity;

    public LcdImageLine(BitVector msb, BitVector lsb, BitVector opacity) {
        Preconditions.checkArgument(msb.size()==lsb.size()&&lsb.size()==opacity.size());
        this.msb = msb;
        this.lsb = lsb;
        this.opacity = opacity;
    }

    public int size() {
        return lsb.size();
    }
    
    /**
     * @return the msb
     */
    public BitVector getMsb() {
        return msb;
    }

    /**
     * @return the lsb
     */
    public BitVector getLsb() {
        return lsb;
    }

    /**
     * @return the opacity
     */
    public BitVector getOpacity() {
        return opacity;
    }

    public void shift(int shiftNumber) {
        msb.shift(shiftNumber);
    }
    
}
