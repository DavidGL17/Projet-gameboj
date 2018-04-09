/**
 * 
 */
package ch.epfl.gameboj.component;

/**
 * @author David Gonzalez leon (270845)
 *
 */
public interface Clocked {
    /**
     * Simulates the behaviour of a component during a single cycle
     * 
     * @param cycle
     */
    abstract void cycle(long cycle);
}
