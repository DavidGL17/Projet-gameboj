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
    public static final LcdImageLine ZERO_OF_SIZE_256 = new LcdImageLine(BitVector.ZERO_OF_SIZE_256,
    		BitVector.ZERO_OF_SIZE_256, BitVector.ZERO_OF_SIZE_256);
    public static final LcdImageLine ZERO_OF_SIZE_160 = new LcdImageLine(BitVector.ZERO_OF_SIZE_160,
    		BitVector.ZERO_OF_SIZE_160, BitVector.ZERO_OF_SIZE_160);
    
    
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
        return Objects.hash(msb, lsb, opacity);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LcdImageLine) {
            return (lsb.equals(((LcdImageLine) obj).getLsb())
                    && msb.equals(((LcdImageLine) obj).getMsb())
                    && opacity.equals(((LcdImageLine) obj).getOpacity()));
        }
        return false;
    }

    /**
     * shifts the line by a specified number of pixels
     * 
     * @param shiftNumber
     */
    public LcdImageLine shift(int shiftNumber) {
        return new LcdImageLine(msb.shift(shiftNumber), lsb.shift(shiftNumber),
                opacity.shift(shiftNumber));
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


//    jusqu'à 16 parcours de BitVector de taille size
//    public LcdImageLine mapColors(int palette) {
//        if (checkAllColorsSame(palette)) {
//            return new LcdImageLine(msb, lsb, opacity);
//        }
//        BitVector[] bitsAtColor = new BitVector[] { msb.not().and(lsb.not()),
//                msb.not().and(lsb), msb.and(lsb.not()), msb.and(lsb) };
//        int i = 0;
//        
//        BitVector newMsb ,newLsb;
//        if (msb.size()==256) {
//        	newMsb=BitVector.ZERO_OF_SIZE_256;
//        } else {
//        	newMsb = new BitVector(msb.size());
//        }
//        if (lsb.size()==256) {
//        	newLsb=BitVector.ZERO_OF_SIZE_256;
//        } else {
//        	newLsb = new BitVector(msb.size());
//        }
//        
//        for (BitVector colorVector : bitsAtColor) {
//            if (Bits.test(palette, i)) {
//                newLsb = newLsb.or(colorVector);
//            }
//            ++i;
//            if (Bits.test(palette, i)) {
//                newMsb = newMsb.or(colorVector);
//            }
//            ++i;
//        }
//        return new LcdImageLine(newMsb, newLsb, opacity);
//    }
    
    //Max 4 parcours de LcdImageLine !En utilisant xor! Autrement 10
    
    /**
     * Changes the color of each bit of the line according to the given palette
     * 
     * @param palette
     *            an int containing the new set of colors
     * @return a new LcdImageLine with the colors changed to correspond to the
     *         given palette
     */
    public LcdImageLine mapColors(int palette) {
    	
    	if (checkAllColorsSame(palette)) {
    		return this;
    	}
    	int size = this.size();
    	BitVector newMsb=BitVector.ZERO_OF_SIZE_160;
    	BitVector newLsb=BitVector.ZERO_OF_SIZE_160;
    	int msbReplacement = Preconditions.checkBits8(palette) & 0b10101010;
    	int lsbReplacement = palette & 0b01010101;
    	
    	switch (msbReplacement) {
    	case 0b10101010:
    			newMsb=new BitVector(this.size(),true);
    		break;
    	case 0b00000000:
    		if (size==256) {
    			newMsb= BitVector.ZERO_OF_SIZE_256;
    		} else if (size == 160) {
    			;
    		} else {
    			newMsb=new BitVector(this.size(),false);
    		}
    		break;
    	case 0b10:
    		newMsb=(msb.or(lsb)).not();
    		break;
    	case 0b1000:
    		newMsb=msb.not().and(lsb);
    		break;
    	case 0b1010:
    		newMsb=msb.not();
    		break;
    	case 0b100000:
    		newMsb=msb.and(lsb.not());
    		break;
    	case 0b100010:
    		newMsb=lsb.not();
    		break;
    	case 0b101000:
//    		newMsb=msb.xor(lsb);
    		newMsb=msb.and(lsb.not()).or(msb.not().and(lsb));
//    		newMsb=((msb.and(lsb).or(msb.or(lsb).not())).not());
    		break;
    	case 0b101010:
    		newMsb=msb.and(lsb).not();
    		break;
    	case 0b10000000:
    		newMsb=msb.and(lsb);
    		break;
    	case 0b10000010:
//    		newMsb=msb.xor(lsb.not());
    		newMsb=msb.and(lsb).or((msb.or(lsb)).not());
    		break;
    	case 0b10001000:
    		newMsb=lsb;
    		break;
    	case 0b10001010:
    		newMsb=msb.not().or(lsb);
    		break;
    	case 0b10100000:
    		newMsb=msb;
    		break;
    	case 0b10100010:
    		newMsb=msb.or(lsb.not());
    		break;
    	case 0b10101000:
    		newMsb=msb.or(lsb);
    		break;
    	default:
    			System.out.println("Failed");
    	}
    	
    	switch (lsbReplacement) {
    	case 0b1010101:
    			newLsb=new BitVector(this.size(),true);
    		break;
    	case 0b0000000:
    		if (size==256) {
    			newLsb= BitVector.ZERO_OF_SIZE_256;
    		} else if (size == 160) {
    			;
    		} else {
    			newLsb=new BitVector(this.size(),false);
    		}
    		break;
    	case 0b1:
    		newLsb=msb.or(lsb).not();
    		break;
    	case 0b100:
    		newLsb=msb.not().and(lsb);;
    		break;
    	case 0b101:
    		newLsb=msb.not();
    		break;
    	case 0b10000:
    		newLsb=msb.and(lsb.not());
    		break;
    	case 0b10001:
    		newLsb=lsb.not();
    		break;
    	case 0b10100:
//    		newLsb=msb.xor(lsb);
    		newLsb=msb.and(lsb.not()).or((msb.not()).and(lsb));
    		break;
    	case 0b10101:
    		newLsb=msb.and(lsb).not();
    		break;
    	case 0b1000000:
    		newLsb=msb.and(lsb);
    		break;
    	case 0b1000001:
//    		newLsb=msb.xor(lsb.not());
    		newLsb=msb.and(lsb).or(msb.or(lsb).not());
    		break;
    	case 0b1000100:
    		newLsb=lsb;
    		break;
    	case 0b1000101:
    		newLsb=msb.not().or(lsb);
    		break;
    	case 0b1010000:
    		newLsb=msb;
    		break;
    	case 0b1010001:
    		newLsb=msb.or(lsb.not());
    		break;
    	case 0b1010100:
    		newLsb=msb.or(lsb);
    		break;
    	default:
			System.out.println("Failed" + palette);
    	}
    	
    	try {
    	return new LcdImageLine(newMsb, newLsb, opacity);
    	} catch(IllegalArgumentException e) {
    		System.out.println("newMsb == ZERO_OF_SIZE_160 : " + (newMsb.equals(BitVector.ZERO_OF_SIZE_160)));
    		System.out.println("newMsb size = " + newMsb.size());
    		System.out.println("newLsb == ZERO_OF_SIZE_160 : " + (newLsb.equals(BitVector.ZERO_OF_SIZE_160)));
    		System.out.println("newLsb size = " + newLsb.size());
    		System.out.println("opacity size = " + opacity.size());
    		System.out.println("palette is : " + Integer.toBinaryString(palette));
    		throw new IllegalArgumentException();
    	}
    	
    	
    }

    /**
     * Checks if the given palette changes the colors or not
     * 
     * @param palette
     * @return true if it doesn't, false otherwise
     */
    private boolean checkAllColorsSame(int palette) {
        return (palette==0b11100100);
    }

    /**
     * Composes the line with the given line, placed above, using the opacity of
     * the given line to do the composition
     * 
     * @param that,
     *            the given line
     * @return the composition of the two lines
     */
    public LcdImageLine below(LcdImageLine that) {
        return below(that, that.opacity);

    }

    /**
     * Composes the line with the given line, placed above, using the given
     * opacity vector to do the composition
     * 
     * @param that,
     *            the given line
     * @return the composition of the two lines
     */
    public LcdImageLine below(LcdImageLine that, BitVector givenOpacity) {
        BitVector finalMSB = givenOpacity.and(that.msb)
                .or(givenOpacity.not().and(msb));
        BitVector finalLSB = givenOpacity.and(that.lsb)
                .or(givenOpacity.not().and(lsb));
        BitVector finalOpacity = givenOpacity.or(getOpacity());

        return new LcdImageLine(finalMSB, finalLSB, finalOpacity);
    }

    /**
     * Computes the junction of the first LcdImageLine up to index and the
     * second LcdImageline from the index to the end of the second LcdImageLine
     * 
     * @param that
     *            - the second LcdImageLine
     * @param index
     *            - the index at which we start putting the second LcdImageLine
     * @return the junction of the two LcdImageLine
     * @throws IllegalArgumentException
     *             if both images don't have the same size, or if the index is
     *             not within the bounds
     */
    public LcdImageLine join(LcdImageLine that, int index) {
        Preconditions.checkArgument(
                that.size() == size() && index >= 0 && index <= size());
        if (index==size()) {
        	return this;
        } else if (index ==0) {
        	return that;
        } else {
        	if (index<size()/2) {
		        BitVector.Builder builder = new BitVector.Builder(size());
		        int i = 0;
		        while (index > 8) {
		            builder.setByte(i, 0xFF);
		            index -= 8;
		            i++;
		        }
		        builder.setByte(i, Bits.clip(index, -1));
		        BitVector mask = builder.build();
		
		        BitVector newMsb = (mask.and(this.msb)).or((mask.not()).and(that.msb));
		        BitVector newLsb = (mask.and(this.lsb)).or((mask.not()).and(that.lsb));
		        BitVector newOpacity = (mask.and(this.opacity))
		                .or((mask.not()).and(that.opacity));
		
		        return new LcdImageLine(newMsb, newLsb, newOpacity);
        	} else {
        		BitVector.Builder builder = new BitVector.Builder(size());
        		index = size()-index;
        		int i = 0;
		        while (index > 8) {
		            builder.setByte(size()/8-1-i, 0xFF);
		            index -= 8;
		            i++;
		        }
		        builder.setByte(size()/8-1-i, Bits.extract(-1, 31-index ,index));
		        
		        BitVector mask = builder.build();
				
		        BitVector newMsb = (mask.not().and(this.msb)).or((mask).and(that.msb));
		        BitVector newLsb = (mask.not().and(this.lsb)).or((mask).and(that.lsb));
		        BitVector newOpacity = (mask.not().and(this.opacity))
		                .or((mask).and(that.opacity));
		
		        return new LcdImageLine(newMsb, newLsb, newOpacity);
        	}
        }
    }

    /**
     * Enables the build byte per byte of a LcdImageLine
     *
     */
    public static class Builder {
        private BitVector.Builder msbBuilder;
        private BitVector.Builder lsbBuilder;

        /**
         * Creates a LcdImageLine builder
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
         * @param index,
         *            the index of the byte
         * @param msbBytes
         * @param lsbBytes
         * @return the updated Builder
         * @throws IndexOutOfBoundsException
         *             if the index is not valid
         * @throws IllegalArgumentException
         *             if the value is not an 8 bit value
         * @throws IllegalStateException
         *             if the builder has already built the lcdImageLine
         */
        public Builder setBytes(int index, int msbBytes, int lsbBytes) {
            msbBuilder.setByte(index, msbBytes);
            lsbBuilder.setByte(index, lsbBytes);
            return this;
        }

        /**
         * builds a LcdImageLine corresponding to the current state of the
         * Builder
         * 
         * The builder is then unusable
         * 
         * @return a new LcdImageLine
         * @throws IllegalStateException
         *             if the builder has already built the lcdImageLine
         */
        public LcdImageLine build() {
            BitVector msb = msbBuilder.build();
            BitVector lsb = lsbBuilder.build();
            return new LcdImageLine(msb, lsb, msb.or(lsb));
        }
    }

}
