package ch.epfl.gameboj.component;

import ch.epfl.gameboj.Bus;

/**
 * @author David (270845)
 *
 */
public interface Component {
    public static final int NO_DATA = 0x100;

    /**
     * Returns the element located at the given index
     * 
     * @param address
     *            The address where the element is located
     * @return The element located at the given address or NO_DATA if the
     *         component does not have any element at that address
     * @throws IllegalArgumentException
     *             if the address is not a 16 bits value
     */
    public abstract int read(int address);

    /**
     * Storages the given value at the given address or does nothing if the
     * component can't stock anything at that address
     * 
     * @param address
     *            the address where we want to locate our element
     * @param data
     *            the data we want to storage
     * @throws IllegalArgumentException
     *             if the address is not a 16 bits value or if the data is not a
     *             8 bits value
     */
    public abstract void write(int address, int data);

    /**
     * Attaches the component to the given bus
     * 
     * @param bus
     *            the bus we want to attach our component to
     */
    public default void attachTo(Bus bus) {
        bus.attach(this);
    }
}