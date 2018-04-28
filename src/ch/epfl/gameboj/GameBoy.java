package ch.epfl.gameboj;

import java.util.Objects;

import ch.epfl.gameboj.component.Timer;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.memory.BootRomController;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

/**
 * 
 * @author David Gonzalez leon (270845)
 * @author Melvin Malonga-Matouba (288405)
 * 
 */
public final class GameBoy {
    private final Bus bus = new Bus();
    private final RamController workRam;
    private final RamController echoRam;
    private final Cpu cpu;
    private final Timer timer;
    private final BootRomController bootRomController;
    private final LcdController lcdController;

    private long currentCycle = 0;

    /**
     * Builds a GameBoy
     * @param cartridge, the cartridge the GameBoy will run
     */
    public GameBoy(Cartridge cartridge) {
        Ram ram = new Ram(AddressMap.WORK_RAM_SIZE);
        workRam = new RamController(ram, AddressMap.WORK_RAM_START);
        echoRam = new RamController(ram, AddressMap.ECHO_RAM_START,
                AddressMap.ECHO_RAM_END);
        cpu = new Cpu();
        timer = new Timer(cpu);
        bootRomController = new BootRomController(
                Objects.requireNonNull(cartridge));
        lcdController = new LcdController(cpu);

        workRam.attachTo(bus);
        echoRam.attachTo(bus);
        cpu.attachTo(bus);
        timer.attachTo(bus);
        bootRomController.attachTo(bus);
        lcdController.attachTo(bus);
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

    /**
     * Returns the gameBoy's Timer
     * 
     * @return bus the gameBoys Timer
     */
    public Timer timer() {
        return timer;
    }
    
    /**
     * Returns the gameBoy's LcdController
     * 
     * @return lcdController the gameBoy's LcdController
     */
    public LcdController lcdController() {
        return lcdController;
    }

    /**
     * Runs all the gameboy's elements implementing the interface clocked
     * cycle-1 times
     * 
     * @param cycle,
     *            the number of cycles + 1 the components will run
     * @throws IllegalArgumentException
     *             if the param cycle is strictly inferior than the number of
     *             cycles already simulated
     */
    public void runUntil(long cycle) {
        Preconditions.checkArgument(cycle >= currentCycle);
        while (currentCycle < cycle) {
            timer.cycle(currentCycle);
            lcdController.cycle(currentCycle);
            cpu.cycle(currentCycle);
            ++currentCycle;
        }
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
