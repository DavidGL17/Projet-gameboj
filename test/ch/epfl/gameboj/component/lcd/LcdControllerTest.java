/**
 * 
 */
package ch.epfl.gameboj.component.lcd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cartridge.CartridgeTestProf;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

/**
 * @author David Gonzalez leon (270845)
 *
 */
public class LcdControllerTest {

    @Test
    void readWriteTest() {
        LcdController c = new LcdController(new Cpu());
        Random rng = new Random();
        int i = 0;
        while (i < AddressMap.VIDEO_RAM_SIZE) {
            int n = Bits.clip(8, rng.nextInt());
            c.write(AddressMap.VIDEO_RAM_START + i, n);
            assertEquals(n, c.read(AddressMap.VIDEO_RAM_START + i));
            ++i;
        }
    }

    @Test
    void OAMIsWrittenCorrectly() throws IOException {
        final int startAddress = 0;
        Bus bus = new Bus();
        LcdController lcd = new LcdController(new Cpu());
        RamController ram = new RamController(new Ram(160), startAddress);
        lcd.attachTo(bus);
        ram.attachTo(bus);
        for (int i = 0; i < 160; ++i) {
            ram.write(i, i);
        }
        bus.write(0xFF46, 0);
        int cycle = 0;
        while (cycle < 160) {
            lcd.cycle(cycle);
            assertEquals(cycle, bus.read(AddressMap.OAM_START + cycle));
            ++cycle;
        }
    }
}
