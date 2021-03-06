package ch.epfl.gameboj;

import java.util.ArrayList;
import java.util.Objects;

import ch.epfl.gameboj.component.Component;

/**
 * @author David (270845)
 *
 */
public final class Bus {
    private final ArrayList<Component> composants = new ArrayList<Component>();

    private final int defaultReturnValue = 0xff;

    /**
     * Attaches the given component to the bus
     * 
     * @param component
     *            the component that we want to attach to the bus
     * @throws NullPointerException
     *             if the component is null
     */
    public void attach(Component component) {
        composants.add(Objects.requireNonNull(component));
    }

    /**
     * Returns the value located at the given adress if at least one of the
     * components of the bus has a value at that adress, else returns 0xff
     * 
     * @param address
     *            where the value should be located
     * @return the value if one of the components has one at the given adress or
     *         0xff
     * @throws IllegalArgumentException
     *             if the adress is not a 16 bit value
     */
    public int read(int address) {
        Preconditions.checkBits16(address);
        for (Component c : composants) {
            int value = c.read(address);
            if (value != Component.NO_DATA) {
                return value;
            }
        }
        return defaultReturnValue;
    }

    /**
     * Writes the value at the given adress given in all the components attached
     * to the bus
     * 
     * @param address
     *            where we want to write the value
     * @param data
     *            that we want to write
     * @throws IllegalArgumentException
     *             if the adress is not a 16 bits value or if the data is not a
     *             8 bits value
     */
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        for (Component c : composants) {
            c.write(address, data);
        }
    }
}
