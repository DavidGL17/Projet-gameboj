/**
 * 
 */
package ch.epfl.gameboj;

import java.util.Objects;

import ch.epfl.gameboj.component.Timer;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;
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
    private Cpu cpu;
    private Timer timer;
    
    private long currentCycle = 0;
    
    public GameBoy(Cartridge cartridge) {
        Objects.requireNonNull(cartridge);
        Ram ram = new Ram(AddressMap.WORK_RAM_SIZE);
        workRam = new RamController(ram, AddressMap.WORK_RAM_START);
        echoRam = new RamController(ram, AddressMap.ECHO_RAM_START, AddressMap.ECHO_RAM_END);
        workRam.attachTo(bus);
        echoRam.attachTo(bus);
        cpu = new Cpu();
        cpu.attachTo(bus);
        timer = new Timer(cpu);
        timer.attachTo(bus);
    }

    /**
     * Returns the gameBoy's Bus
     * 
     * @return bus the gameBoys bus
     */
    public Bus bus() {
        return bus;
    }
    
    /**
     * Returns the gameBoy's Cpu
     * 
     * @return bus the gameBoys Cpu
     */
    public Cpu cpu() {
        return cpu;
    }
    
    public Timer timer() {
        return timer;
    }
    
    /**
     * Runs all the gameboy's elements implementing the interface clocked cycle-1 times
     * 
     * @param cycle, the number of cycles + 1 the components will run
     * @throws IllegalArgumentException if the param cycle si strictly inferior than the number of cycles already simulated
     */
    public void runUntil(long cycle) {
        Preconditions.checkArgument(cycle>=currentCycle);
        for (int i = 0;i<cycle;++i) {
            timer.cycle(currentCycle+i);
            cpu.cycle(currentCycle+i);
        }
        currentCycle += cycle;
    }
    
    /**
     * Returns the number of cycles the gameboy has already simulated
     * 
     * @return currentCycle, the number of cycles already simulated
     */
    public long cycles() {
        return currentCycle;
    }
}
