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
class CpuTest {

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
    
    
    ///Partie 4
    
    
    @Test
    void ADDWork() {
        Cpu c = new Cpu();
        Ram r = new Ram(30);
        Bus b = connect(c, r);
        Opcode a = Opcode.ADD_A_N8;
        b.write(0, a.encoding);
        b.write(1, 10);
        cycleCpu(c,getTotalCycles(new Opcode[] {a}));
        assertArrayEquals(new int[] {getTotalBits(new Opcode[] {a}),0,10,0,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        Opcode[] os = new Opcode[] {
                Opcode.ADD_A_A,
                Opcode.ADD_A_B,
                Opcode.ADD_A_C,
                Opcode.ADD_A_D,
                Opcode.ADD_A_E,
                Opcode.ADD_A_H,
                Opcode.ADD_A_L,
                };
        for (int i = 0;i<os.length;++i) {
            b.write(i, os[i].encoding);
        }
        cycleCpu(c, getTotalCycles(os));
        assertArrayEquals(new int[] {getTotalBits(os),0,10,0,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
    }
    @Test
    void ADCWork() {
        Cpu c = new Cpu();
        Ram r = new Ram(30);
        Bus b = connect(c, r);
        Opcode a = Opcode.ADD_A_N8;
        b.write(0, a.encoding);
        b.write(1, 10);
        cycleCpu(c,getTotalCycles(new Opcode[] {a}));
        assertArrayEquals(new int[] {getTotalBits(new Opcode[] {a}),0,10,0,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        Opcode[] os = new Opcode[] {
                Opcode.ADC_A_B,
                Opcode.ADC_A_C,
                Opcode.ADC_A_D,
                Opcode.ADC_A_E,
                Opcode.ADC_A_H,
                Opcode.ADC_A_L,
                };
        for (int i = 0;i<os.length;++i) {
            b.write(i+a.totalBytes, os[i].encoding);
        }
        cycleCpu(c, getTotalCycles(os)+a.cycles);
        assertArrayEquals(new int[] {getTotalBits(os)+a.totalBytes,0,10,0,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
    }
    
    @Test
    void ADDAHLRWorks() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        b.write(0, Opcode.LD_HL_N16.encoding);
        b.write(2, 0xFF);
        b.write(0xFF00, 20);
        b.write(3, Opcode.ADD_A_HLR.encoding);
        cycleCpu(c, Opcode.LD_HL_N16.cycles+Opcode.ADD_A_HLR.cycles);
        assertArrayEquals(new int[] {getTotalBits(new Opcode[] {Opcode.ADD_A_HLR,Opcode.LD_HL_N16}),0,20,Alu.unpackFlags(Alu.add(0, 20)),0,0,0,0,0xFF,0}, c._testGetPcSpAFBCDEHL());
    }
    
    @Test
    void INCr8Work() {
        Cpu c = new Cpu();
        Ram r = new Ram(30);
        Bus b = connect(c, r);
        Opcode[] os = new Opcode[] {
                Opcode.INC_A,
                Opcode.INC_B,
                Opcode.INC_C,
                Opcode.INC_D,
                Opcode.INC_E,
                Opcode.INC_H,
                Opcode.INC_L
                };
        for (int i = 0;i<os.length;++i) {
            b.write(i, os[i].encoding);
        }
        cycleCpu(c, getTotalCycles(os));
        assertArrayEquals(new int[] {getTotalBits(os),0,1,0,1,1,1,1,1,1}, c._testGetPcSpAFBCDEHL());
    }
    
    @Test
    void INCHLRWorks() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        b.write(0, Opcode.LD_HL_N16.encoding);
        b.write(2, 0xFF);
        b.write(0xFF00, 20);
        b.write(3, Opcode.INC_HLR.encoding);
        b.write(4, Opcode.ADD_A_HLR.encoding);
        cycleCpu(c, Opcode.LD_HL_N16.cycles+Opcode.ADD_A_HLR.cycles+Opcode.INC_HLR.cycles);
        assertArrayEquals(new int[] {getTotalBits(new Opcode[] {Opcode.ADD_A_HLR,Opcode.LD_HL_N16,Opcode.INC_HLR}),0,21,0,0,0,0,0,0xFF,0}, c._testGetPcSpAFBCDEHL());
    }
    
    @Test
    void ANDWorks() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        b.write(0, Opcode.LD_HL_N16.encoding);
        b.write(2, 0xFF);
        b.write(0xFF00, 0x0B);
        b.write(3, Opcode.ADD_A_HLR.encoding);
        Opcode[] os = new Opcode[] {
                Opcode.INC_C,
                Opcode.INC_D,
                Opcode.INC_E,
                };
        for (int i = 0;i<os.length;++i) {
            b.write(i+4, os[i].encoding);
        }
        Opcode[] os2 = new Opcode[] {
                Opcode.AND_A_C,
        };
        b.write(7, os2[0].encoding);
        cycleCpu(c, Opcode.LD_HL_N16.cycles+Opcode.ADD_A_HLR.cycles+getTotalCycles(os)+getTotalCycles(os2));
        assertArrayEquals(new int[] {getTotalBits(new Opcode[] {Opcode.ADD_A_HLR,Opcode.LD_HL_N16})+getTotalBits(os2)+getTotalBits(os),0,0x01,0x20,0,1,1,1,0xFF,0}, c._testGetPcSpAFBCDEHL());
    }
    
    
    private int getTotalCycles(Opcode[] os) {
        int cycles = 0;
        for (int i = 0;i<os.length;++i) {
            cycles += os[i].cycles;
        }
        return cycles;
    }
    private int getTotalBits(Opcode[] os) {
        int bits = 0;
        for (int i = 0;i<os.length;++i) {
            bits += os[i].totalBytes;
        }
        return bits;
    }
}
