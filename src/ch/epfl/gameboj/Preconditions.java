package ch.epfl.gameboj;

/**
 * 
 * @author David (270845)
 *
 */
public interface Preconditions {

    /**
     * Checks if the param b is true
     * 
     * @param b,
     *            the property to verify
     * @throws IllegalArgumentException
     *             if b is false
     */
    public static void checkArgument(boolean b) {
        if (!b) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Checks if an int is in the range of 0 to 0xff
     * 
     * @param v,
     *            the int
     * @return the argument v
     * @throws IllegalArgumentException
     *             if v is not in the range of 0 to 0xff
     */
    public static int checkBits8(int v) {
        checkArgument(0 <= v && v <= 0xff);
        return v;
    }

    /**
     * Checks if an int is in the range of 0 to 0xffff
     * 
     * @param v,
     *            the int
     * @return the argument v
     * @throws IllegalArgumentException
     *             if v is not in the range of 0 to 0xffff
     */
    public static int checkBits16(int v) {
        checkArgument(0 <= v && v <= 0xffff);
        return v;
    }
}
