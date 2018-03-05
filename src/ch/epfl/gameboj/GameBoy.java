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
    private Bus bus = new Bus();
    private RamController workRam;
    private RamController echoRam;
    
    public GameBoy(Object cartridge) {
        Ram ram = new Ram(AddressMap.WORK_RAM_SIZE);
        workRam = new RamController(ram, AddressMap.WORK_RAM_START);
        echoRam = new RamController(ram, AddressMap.ECHO_RAM_START, AddressMap.ECHO_RAM_END);
        workRam.attachTo(bus);
        echoRam.attachTo(bus);
    }

    /**
     * Returns the gameBoy's Bus
     * 
     * @return bus The gameBoys bus
     */
    public Bus bus() {
        return bus;
    }
}
