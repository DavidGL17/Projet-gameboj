/**
 * 
 */
package ch.epfl.gameboj;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.component.cpu.Cpu;

/**
 * @author David Gonzalez leon (270845)
 *
 */
class GameBoyTest {
    @Disabled
    @Test
    void getCpuWorks() {
        GameBoy gb = new GameBoy(null);
        assertNotNull(gb.cpu());
    }
    @Disabled
    @Test 
    void simulationRunningWorks() {
        long cycle = 4;
        GameBoy gb = new GameBoy(null);
        gb.runUntil(cycle);
        assertEquals(cycle, gb.cycles());
        assertThrows(IllegalArgumentException.class, () ->{gb.runUntil(1);});
    }

}
