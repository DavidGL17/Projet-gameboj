/**
 * 
 */
package ch.epfl.gameboj;

/**
 * @author David Gonzalez leon (270845)
 *
 */
public interface Register {
    public default int index() {
        return this.ordinal();
    }

    abstract int ordinal();
}
