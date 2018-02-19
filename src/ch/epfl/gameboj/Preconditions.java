package ch.epfl.gameboj;

/**
 * 
 * 
 * @author David (270845)
 *
 */
public interface Preconditions {

    /**
     * checks if the param b is true
     * 
     * @param b
     * @throws IllegalArgumentException
     *             if b is false
     */
    public static void checkArgument(boolean b) {
        if (!b) {
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * Checks if an int is in the range of 0 to 255
     * 
     * @param v
     * @return v
     * @throws IllegalArgumentException
     *             if v is not in the range of 0 to 255
     */
    public static int checkBits8(int v) {
        if (0 <= v && v <= 0xff) {
            return v;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Checks if an int is in the range of 0 to 65535
     * 
     * @param v
     * @return v
     * @throws IllegalArgumentException
     *             if v is not in the range of 0 to 65535
     */
    public static int checkBits16(int v) {
        if (0 <= v && v <= 0xffff) {
            return v;
        } else {
            throw new IllegalArgumentException();
        }
    }
}
