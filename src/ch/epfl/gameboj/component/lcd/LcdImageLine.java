package ch.epfl.gameboj.component.lcd;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;

/**
 * @author David Gonzalez leon (270845)
 * @author Melvin Malonga-Matouba (288405)
 *
 */
public final class LcdImageLine {
    private BitVector msb;
    private BitVector lsb;
    private BitVector opacity;

    /**
     * Builds a LcdImageLine
     * 
     * @param msb,
     *            the msb vector
     * @param lsb, 
     *            the lsb vector
     * @param opacity,
     *            the opacity vector
     */
    public LcdImageLine(BitVector msb, BitVector lsb, BitVector opacity) {
        Preconditions.checkArgument(
                msb.size() == lsb.size() && lsb.size() == opacity.size());
        this.msb = msb;
        this.lsb = lsb;
        this.opacity = opacity;
    }

    /**
     * @return the length of the line
     */
    public int size() {
        return lsb.size();
    }

    /**
     * @return the msb vector
     */
    public BitVector getMsb() {
        return msb;
    }

    /**
     * @return the lsb vector
     */
    public BitVector getLsb() {
        return lsb;
    }

    /**
     * @return the opacity vector
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
        return Objects.hash(msb,lsb,opacity);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LcdImageLine) {
            return (lsb.equals(((LcdImageLine)obj).getLsb()) && msb.equals(((LcdImageLine)obj).getMsb())
                    && opacity.equals(((LcdImageLine)obj).getOpacity()));
        }
        return false;
    }

    /**
     * shifts the line by a specified number of pixels
     * 
     * @param shiftNumber
     */
    public LcdImageLine shift(int shiftNumber) {
        return new LcdImageLine(msb.shift(shiftNumber),lsb.shift(shiftNumber),opacity.shift(shiftNumber));
    }

    /**
     * Extracts a wrapped extension of the line
     * 
     * @param start
     *            the start of the new line
     * @param size
     *            the size of the extension
     * @return the wrapped extension of the line
     */
    public LcdImageLine extractWrapped(int start, int size) {
        BitVector newMSB = msb.extractWrapped(start, size);
        BitVector newLSB = lsb.extractWrapped(start, size);
        BitVector newOpacity = opacity.extractWrapped(start, size);
        return new LcdImageLine(newMSB, newLSB, newOpacity);
    }

    /**
     * Changes the color of each bit of the line according ti the given palette
     * 
     * @param palette
     */
    public LcdImageLine mapColors(int palette) {
        if (checkAllColorsSame(palette)) {
            return new LcdImageLine(msb, lsb, opacity);
        }
        BitVector[] bitsAtColor = new BitVector[] { msb.not().and(lsb.not()),
                msb.not().and(lsb), msb.and(lsb.not()), msb.and(lsb) };
        int i = 0;
        BitVector newMsb = new BitVector(msb.size()),
                newLsb = new BitVector(lsb.size());
        for (BitVector colorVector : bitsAtColor) {
            if (Bits.test(palette, i)) {
                newLsb = newLsb.or(colorVector);
            }
            ++i;
            if (Bits.test(palette, i)) {
                newMsb = newMsb.or(colorVector);
            }
            ++i;
        }
        return new LcdImageLine(newMsb, newLsb, opacity);
    }

    /**
     * Checks if the given palette changes the colors or not
     * 
     * @param palette
     * @return true if it doesn't, false otherwise
     */
    private boolean checkAllColorsSame(int palette) {
        boolean result = true;
        for (int i = 0; i < 4; ++i)
            result &= Bits.extract(palette, 2 * i, 2) == i;
        return result;
    }

    /**
     * Composes the line with the given line, placed above, using the opacity of
     * the given line to do the composition
     * 
     * @param that,
     *            the given line
     * @return
     */
    public LcdImageLine below(LcdImageLine that) {
        return below(that,that.opacity);
        // Builder b = new Builder(size());
        // int msbByte = 0,lsbByte = 0;
        // for (int i = 0;i<size()/8;++i) {
        // for (int j = 0;j<8;++j) {
        // Bits.set(msbByte, j, that.getOpacity().testBit(i*8
        // +j)?that.getMsb().testBit(i*8 +j):msb.testBit(i*8 +j));
        // Bits.set(lsbByte, j, that.getOpacity().testBit(i*8
        // +j)?that.getLsb().testBit(i*8 +j):lsb.testBit(i*8 +j));
        // }
        // b.setBytes(i, msbByte, lsbByte);
        // }
        // return b.build();
    }

    /**
     * Composes the line with the given line, placed above, using the given
     * opacity vector to do the composition
     * 
     * @param that,
     *            the given line
     * @return
     */
    public LcdImageLine below(LcdImageLine that, BitVector givenOpacity) {
        BitVector finalMSB = givenOpacity.and(that.msb)
                .or(givenOpacity.not().and(msb));
        BitVector finalLSB = givenOpacity.and(that.lsb)
                .or(givenOpacity.not().and(lsb));
        BitVector finalOpacity = givenOpacity.or(getOpacity());
        
        return new LcdImageLine(finalMSB, finalLSB, finalOpacity);
        //
        // Builder b = new Builder(size());
        // int msbByte = 0,lsbByte = 0;
        // for (int i = 0;i<size()/8;++i) {
        // for (int j = 0;j<8;++j) {
        // Bits.set(msbByte, j, givenOpacity.testBit(i*8
        // +j)?that.getMsb().testBit(i*8 +j):msb.testBit(i*8 +j));
        // Bits.set(lsbByte, j, givenOpacity.testBit(i*8
        // +j)?that.getLsb().testBit(i*8 +j):lsb.testBit(i*8 +j));
        // }
        // b.setBytes(i, msbByte, lsbByte);
        // }
        // return b.build();
    }

    // Aucune idée de comment le faire pour le moment
    /**
     * Computes the jointure of the first LcdImageLine up to index and thw
     * second LcdImageline
     * @param that - the second LcdImageLine
     * @param index - the index
     * @return the jointure
     */
    public LcdImageLine join(LcdImageLine that, int index) {
        Preconditions.checkArgument(
                that.size() == size() && index >= 0 && index < size());
        BitVector.Builder builder = new BitVector.Builder(size());
        int i=0;
        while (index>8) {
        		builder.setByte(i,0xFF);
        		index-=8;
         	i++;
        }
        builder.setByte(i,Bits.clip(index,-1))	;
        BitVector mask=builder.build();
        
        BitVector newMsb = ( mask.and(this.msb) ).or( 
        		(mask.not()).and(that.msb) );
        BitVector newLsb = ( mask.and(this.lsb) ).or( 
        		(mask.not()).and(that.lsb) );
        BitVector newOpacity = ( mask.and(this.opacity) ).or( 
        		(mask.not()).and(that.opacity) );
        
        return new LcdImageLine(newMsb,newLsb,newOpacity);
    }

    /**
     * Allows the build per byte of a LcdImageLine
     *
     */
    public static class Builder {
        private BitVector.Builder msbBuilder;
        private BitVector.Builder lsbBuilder;

        /**
         * Builds a LcdImageLine builder
         * 
         * @param size,
         *            the length of the desired line
         */
        public Builder(int size) {
            msbBuilder = new BitVector.Builder(size);
            lsbBuilder = new BitVector.Builder(size);
        }

        /**
         * sets a byte at the given index in the msb and lsb vector and adapts
         * the opacity accordingly
         * 
         * @param index
         * @param msbBytes
         * @param lsbBytes
         * @return this
         */
        public Builder setBytes(int index, int msbBytes, int lsbBytes) {
            msbBuilder.setByte(index, msbBytes);
            lsbBuilder.setByte(index, lsbBytes);
            return this;
        }

        /**
         * builds a LcdImageLine
         * 
         * @return a new LcdImageLine
         */
        public LcdImageLine build() {
        		BitVector msb=msbBuilder.build();
        		BitVector lsb=lsbBuilder.build();
            return new LcdImageLine(msb, lsb, msb.or(lsb)
                    );
        }
    }

}
