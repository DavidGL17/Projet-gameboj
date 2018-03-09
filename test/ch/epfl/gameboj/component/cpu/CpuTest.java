/**
 * 
 */
package ch.epfl.gameboj.component.cpu;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

/**
 * @author David Gonzalez leon (270845)
 *
 */
public class CpuTest {

    private Bus connect(Cpu cpu, Ram ram) {
        RamController rc = new RamController(ram, 0);
        Bus b = new Bus();
        cpu.attachTo(b);
        rc.attachTo(b);
        return b;
    }

    private void cycleCpu(Cpu cpu, long cycles) {
        for (int c = 0; c < cycles; ++c) {
            cpu.cycle(c);
        }
    }

    @Test
    void nopDoesNothing() {
        Cpu c = new Cpu();
        Ram r = new Ram(30);
        Bus b = connect(c, r);
        b.write(0, Opcode.NOP.encoding);
        cycleCpu(c, Opcode.NOP.cycles);
        assertArrayEquals(new int[] {1,0,0,0,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
    }
    
    @Test
    void storeHLandLoadATest() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        b.write(0, Opcode.LD_HL_N16.encoding);
        b.write(2, 0xFF);
        b.write(0xFF00, 20);
        b.write(4, Opcode.LD_A_HLR.encoding);
        cycleCpu(c, Opcode.LD_HL_N16.cycles+Opcode.LD_A_HLR.cycles);
        assertArrayEquals(new int[] {5,0,20,0,0,0,0,0,0xFF,0}, c._testGetPcSpAFBCDEHL());
    }
}
