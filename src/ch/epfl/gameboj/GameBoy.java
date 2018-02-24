/**
 * 
 */
package ch.epfl.gameboj;

import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

/**
 * 
 * @author David Gonzalez leon (270845)
 * @author Melvin Malonga-Matouba (288405)
 * 
 */
public class GameBoy {
    private Bus bus;
    private RamController workRam = new RamController(new Ram(AddressMap.WORK_RAM_SIZE), AddressMap.WORK_RAM_START, AddressMap.WORK_RAM_END);
    private RamController echoRam = new RamController(new Ram(AddressMap.ECHO_RAM_SIZE), AddressMap.ECHO_RAM_START, AddressMap.ECHO_RAM_END);
    
    public GameBoy(Object cartridge) {
        workRam.attachTo(bus);
        echoRam.attachTo(bus);
    }

    /**
     * Returns the gameBoy's Bus
     * 
     * @return bus The gameBoys bus
     */
    public Bus Bus() {
        return bus;
    }
}
