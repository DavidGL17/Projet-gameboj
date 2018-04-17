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
