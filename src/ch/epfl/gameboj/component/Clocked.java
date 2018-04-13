/**
 * 
 */
package ch.epfl.gameboj.component;

/**
 * @author David Gonzalez leon (270845)
 * @author Melvin Malonga-Matouba (288405)
 *
 */
public interface Clocked {
    /**
     * Simulates the behaviour of a component during a single cycle
     * 
     * @param cycle, the cycle to simulate
     */
    abstract void cycle(long cycle);
}
