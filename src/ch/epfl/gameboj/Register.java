package ch.epfl.gameboj;

/**
 * @author David Gonzalez leon (270845)
 *
 */
public interface Register {
	
    /**
     * @return ordinal
     */
    public default int index() {
        return this.ordinal();
    }

    int ordinal();
}
