/**
 * 
 */
package ch.epfl.gameboj.component.lcd;

import java.util.List;

import ch.epfl.gameboj.Preconditions;

/**
 * @author David Gonzalez leon (270845)
 *
 */
public final class LcdImage {
    private final List<LcdImageLine> lines;
    private final int width, height;

    public LcdImage(List<LcdImageLine> lines, int width, int height) {
        Preconditions.checkArgument(height == lines.size());
        this.lines = lines;
        this.width = width;
        this.height = height;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int res = 0;
        for (LcdImageLine i : lines) {
            res+=i.hashCode();
        }
        return res/lines.size();
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
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    public int get(int x, int y) {
        Preconditions.checkArgument(y <= lines.size() && x <= lines.get(y).size());
        return (lines.get(y).getMsb().testBit(x) ? 1 << 1 : 0)
                | (lines.get(y).getLsb().testBit(x) ? 1 : 0);
    }

}
