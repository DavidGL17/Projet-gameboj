/**
 * 
 */
package ch.epfl.gameboj.component.lcd;

import java.util.Map;
import java.util.TreeMap;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;

/**
 * @author David Gonzalez leon (270845)
 *
 */
public final class LcdImageLine {
    private BitVector msb;
    private BitVector lsb;
    private BitVector opacity;

    public LcdImageLine(BitVector msb, BitVector lsb, BitVector opacity) {
        Preconditions.checkArgument(
                msb.size() == lsb.size() && lsb.size() == opacity.size());
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return msb.hashCode() + lsb.hashCode() + opacity.hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LcdImageLine) {
            LcdImageLine that = (LcdImageLine) obj;
            return (lsb.equals(that.getLsb()) && msb.equals(that.getMsb())
                    && opacity.equals(that.getOpacity()));
        }
        return false;
    }

    public void shift(int shiftNumber) {
        msb.shift(shiftNumber);
        lsb.shift(shiftNumber);
        opacity.shift(shiftNumber);
    }

    public LcdImageLine extractWrapped(int start, int size) {
        BitVector newMSB = msb.extractWrapped(start, size);
        BitVector newLSB = lsb.extractWrapped(start, size);
        BitVector newOpacity = opacity.extractWrapped(start, size);
        return new LcdImageLine(newMSB, newLSB, newOpacity);
    }

    public void mapColors(int palette) {
        if (checkAllColorsSame(palette)) {
            return;
        }
        BitVector[] bitsAtColor = new BitVector[] {msb.not().and(lsb.not()), msb.not().and(lsb), msb.and(lsb.not()), msb.and(lsb)};
        int i = 0;
        BitVector newMsb = new BitVector(msb.size()), newLsb = new BitVector(lsb.size());
        for (BitVector colorVector : bitsAtColor) {
            if (Bits.test(palette, i)) {
                newLsb = newLsb.or(colorVector);
            }
            ++i;
            if (Bits.test(palette, i)) {
                newLsb = newMsb.or(colorVector);
            }
            ++i;
        }
        msb = newMsb;
        lsb = newLsb;
    }
    
    /**
     * @param palette
     * @return
     */
    private boolean checkAllColorsSame(int palette) {
        boolean result = false;
        for (int i = 0;i<4;++i) 
            result &= Bits.extract(palette, 2*i, 2)==i;
        return result;
    }

    //pas bon, peut être optimisé(utiliser or,and,...), mais aucune idée de comment le faire
    public LcdImageLine below (LcdImageLine that) {
        Builder b = new Builder(size());
        int msbByte = 0,lsbByte = 0; 
        for (int i = 0;i<size()/8;++i) {
            for (int j = 0;j<8;++j) {
                Bits.set(msbByte, j, that.getOpacity().testBit(i*8 +j)?that.getMsb().testBit(i*8 +j):msb.testBit(i*8 +j));
                Bits.set(lsbByte, j, that.getOpacity().testBit(i*8 +j)?that.getLsb().testBit(i*8 +j):lsb.testBit(i*8 +j));
            }
            b.setBytes(i, msbByte, lsbByte);
        }
        return b.build();
    }
    
    //idem
    public LcdImageLine below (LcdImageLine that, BitVector givenOpacity) {
        Builder b = new Builder(size());
        int msbByte = 0,lsbByte = 0; 
        for (int i = 0;i<size()/8;++i) {
            for (int j = 0;j<8;++j) {
                Bits.set(msbByte, j, givenOpacity.testBit(i*8 +j)?that.getMsb().testBit(i*8 +j):msb.testBit(i*8 +j));
                Bits.set(lsbByte, j, givenOpacity.testBit(i*8 +j)?that.getLsb().testBit(i*8 +j):lsb.testBit(i*8 +j));
            }
            b.setBytes(i, msbByte, lsbByte);
        }
        return b.build();
    }
    
    //Aucune idée de comment le faire pour le moment
    public LcdImageLine join(LcdImageLine that, int index) {
        Preconditions.checkArgument(that.size()==size()&&index>=0&&index<size());
        //à faire
        return null;
    }
    
    public static class Builder {
        private BitVector.Builder msbBuilder;
        private BitVector.Builder lsbBuilder;
        private BitVector.Builder opacityBuilder;

        public Builder(int size) {
            msbBuilder = new BitVector.Builder(size);
            lsbBuilder = new BitVector.Builder(size);
            opacityBuilder = new BitVector.Builder(size);
        }

        public Builder setBytes(int index, int msbBytes, int lsbBytes) {
            msbBuilder.setByte(index, msbBytes);
            lsbBuilder.setByte(index, lsbBytes);
            opacityBuilder.setByte(index, msbBytes | lsbBytes);
            return this;
        }

        public LcdImageLine build() {
            return new LcdImageLine(msbBuilder.build(), lsbBuilder.build(),
                    opacityBuilder.build());
        }
    }

}
