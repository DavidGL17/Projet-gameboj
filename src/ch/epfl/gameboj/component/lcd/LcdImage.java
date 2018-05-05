package ch.epfl.gameboj.component.lcd;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.BitVector;

/**
 * @author David Gonzalez leon (270845)
 *
 */
public final class LcdImage {
    private final List<LcdImageLine> lines;
    private final int width, height;

    /**
     * Builds a LcdImage
     * 
     * @param lines,
     *            a list of lines
     * @param width,
     *            the width of the image
     * @param height,
     *            the height of the image
     */
    public LcdImage(List<LcdImageLine> lines, int width, int height) {
        Preconditions
                .checkArgument(height == Objects.requireNonNull(lines).size());
        this.lines = lines;
        this.width = width;
        this.height = height;
    }

    /**
     * @return the width of the image
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the height of the image
     */
    public int getHeight() {
        return height;
    }

    /**
     * Gets the pixel at the given index(x,y)
     * 
     * @param x
     * @param y
     * @return the pixel at that index
     */
    public int get(int x, int y) {
        Preconditions
                .checkArgument(y <= lines.size() && x <= lines.get(0).size());
        return (lines.get(y).getMsb().testBit(x) ? 1 << 1 : 0)
                | (lines.get(y).getLsb().testBit(x) ? 1 : 0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(lines, height, width);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LcdImage) {
            return lines.equals(((LcdImage) obj).lines);
        }
        return false;
    }

    /**
     * Enables the build line per line of a LcdImage
     *
     */
    public static class Builder {
        private final LcdImageLine[] lines;
        private final int width;

        /**
         * Creates a LcdImage builder
         * 
         * @param height,
         *            the height of the builder
         * @param width,
         *            the width of the builder
         */
        public Builder(int width, int height) {
            lines = new LcdImageLine[height];
            Arrays.fill(lines, new LcdImageLine(new BitVector(width),
                    new BitVector(width), new BitVector(width)));
            this.width = width;
        }

        /**
         * Sets a line in the builder
         * 
         * @param line
         * @param index
         * @return the updated Builder
         * @throws IllegalArgumentException
         *             if the index is not within the bounds or if the size of
         *             the given line is not equal to the width of the image
         * @throws NullPointerException
         *             if the given line is null
         * @throws IllegalStateException
         *             if the builder has already built the lcdImage
         */
        public Builder setLine(LcdImageLine line, int index) {
            Preconditions.checkArgument(index < lines.length && index >= 0
                    && Objects.requireNonNull(line).size() == width);
            lines[index] = line;
            return this;
        }

        /**
         * Builds a new LcdImage containing the lines previously added
         * 
         * The builder is then unusable
         * 
         * @return a new LcdImage
         * @throws IllegalStateException
         *             if the builder has already built the lcdImage
         */
        public LcdImage build() {
            return new LcdImage(Arrays.asList(lines.clone()), width,
                    lines.length);
        }
    }

}
