/**
 * 
 */
package ch.epfl.gameboj.component.cpu;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Assembler.Program;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;



/**
 * @author David Gonzalez leon (270845)
 * @author Melvin Malonga-Matouba (288405)
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
    void CPSetsFlagsCorrectly() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        b.write(0, Opcode.LD_HL_N16.encoding);
        b.write(2, 0xFF);
        b.write(0xFF00, 0x0B);
        b.write(3, Opcode.ADD_A_HLR.encoding);
        b.write(4,Opcode.CP_A_N8.encoding);
        b.write(5, 0x0B);
        cycleCpu(c, Opcode.LD_HL_N16.cycles+Opcode.ADD_A_HLR.cycles+Opcode.CP_A_N8.cycles);
        assertArrayEquals(new int[] {getTotalBits(new Opcode[] {Opcode.ADD_A_HLR,Opcode.LD_HL_N16,Opcode.CP_A_N8}),0,0x0B,0xC0,0,0,0,0,0xFF,0}, c._testGetPcSpAFBCDEHL());
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
    
    @Disabled
    @Test
    void XORWorks() {
    		//XOR_A_HLR
    	/*
    		Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        int initialHL = 0xFF;
        int valueA=0x08;
        b.write(0, Opcode.LD_HL_N16.encoding);
        b.write(2, initialHL);
        b.write(initialHL, valueA);
        b.write(3, Opcode.ADD_A_HLR.encoding);
        */
        // HL is loaded with initialHL; A is loaded with valueA
        int [][] inputs = { //initialHL, intialA, ValueAtHL
        		{0xFF,0x54,0x25},
        		{0x52,0x45,0x37},
        		{0x72,0xAC,0x47},
        		{0x73,0xB3,0x24},
        		{0x15,0x15,0x15}
        };
        int [][] outputs = {
        		{0b0111_0001,0},
        		{0b0111_0010,0},
        		{0b1110_1011,0},
        		{0b1001_0111,0},
        		{0,1<<7}
        		
        };
        for (int i=0 ;i<inputs.length ; i++) {
        		Cpu c = new Cpu();
            Ram r = new Ram(0xFFFF);
            Bus b = connect(c, r);
            int initialHL = inputs[i][0];
            int initialA=inputs[i][1];
            int valueAtHL=inputs[i][2];
            b.write(0, Opcode.LD_HL_N16.encoding);
            b.write(2, initialHL);
            b.write(initialHL, initialA);
            b.write(3, Opcode.ADD_A_HLR.encoding);
            cycleCpu(c,Opcode.LD_HL_N16.cycles+Opcode.ADD_A_HLR.cycles);
            b.write(initialHL,valueAtHL);
            b.write(4, Opcode.XOR_A_HLR.encoding);
            c.cycle(5+1);
            c.cycle(5+2);
            assertArrayEquals( new int [] {5,0,outputs[i][0],outputs[i][1],0,0,0,0,0,0},c._testGetPcSpAFBCDEHL());
        }            
    }
        
    
    	


    
//    @Test
////    void XORSetsCorrectFlags() {
//    		final int ITERATIONS=100;
//        final Opcode N8 = Opcode.XOR_A_N8;
//        final Opcode RB = Opcode.XOR_A_B;
//        final Opcode RC = Opcode.XOR_A_C;
//        final Opcode RD = Opcode.XOR_A_D;
//        final Opcode RE = Opcode.XOR_A_E;
//        final Opcode RH = Opcode.XOR_A_H;
//        final Opcode RL = Opcode.XOR_A_L;
//        final Opcode HLR = Opcode.XOR_A_HLR;
//        final Opcode[] R8 = {	
//        		RB,RC,RD,RE,RH,RL
//        };
//        Random randomGenerator =new Random();
//       
//        
//        //XOR_A_N8
//        {
//	        Cpu c = new Cpu();
//	        Ram r = new Ram(0xFFFF);
//	        Bus b = connect(c, r);
//	        int adress = 0;
//	        for (int i=0;i<ITERATIONS; i++) {
//	        		b.write(adress,N8.encoding);
//	        		adress++;
//	        		int input = Bits.clip(8,randomGenerator.nextInt());
//	        		int current = getA(c);
//	        		b.write(adress,input);
//	        		adress++;
//	        		cycleCpu(c,N8.cycles);
//	        		int expected = (current==input) ? 1<<7 : 0;
//	        		System.out.println(current + " " + input);
//	        		assertEquals(expected ,getF(c));
//	        }
//        }
//        //XOR_A_R8
//        {
//        		Cpu c = new Cpu();
//	        Ram r = new Ram(0xFFFF);
//	        Bus b = connect(c, r);
//	        int adress = 0;
//	        for (int i=0 ; i<R8.length ; i++) {
//		        	for (int j=0;j<ITERATIONS; j++) {
//		        		b.write(adress,R8[i].encoding);
//		        		adress++;
//		        		int input = c._testGetPcSpAFBCDEHL()[i+4];
//		        		int current = getA(c);
//		        		
//		        		adress++;
//		        		cycleCpu(c,R8[i].cycles);
//		        		int expected = (current==input) ? 1<<7 : 0;
//		        		System.out.println(current + " " + input);
//		        		assertEquals(expected ,getF(c));
//		        }
//	        }
//        }
//        //XOR_A_HLR
//        {
//        	Cpu c = new Cpu();
//	        Ram r = new Ram(0xFFFF);
//	        Bus b = connect(c, r);
//	        int adress = 0;
//	        for (int i=0;i<ITERATIONS; i++) {
//	        		b.write(adress,HLR.encoding);
//	        		adress++;
//	        		int input = Bits.clip(8,randomGenerator.nextInt());
//	        		int current = getA(c);
//	        		b.write(getHL(c),input);
//	        		adress++;
//	        		cycleCpu(c,HLR.cycles);
//	        		int expected = (current==input) ? 1<<7 : 0;
//	        		System.out.println(current + " " + input);
//	        		assertEquals(expected ,getF(c));
//        	
//	        }
//        }        
//    }

    
    
    @Test
    void BitSetResWorks() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        int prefix = 0xCB;
        b.write(0, Opcode.LD_HL_N16.encoding);
        b.write(2, 0xFF);
        b.write(0xFF00, 0x0B);
        b.write(3, Opcode.ADD_A_HLR.encoding);
//        b.write(4, prefix);
//        b.write(5, Opcode.BIT_2_A.encoding);
        b.write(4, prefix);
        b.write(5, Opcode.RES_0_A.encoding);
        cycleCpu(c,Opcode.LD_HL_N16.cycles+Opcode.ADD_A_HLR.cycles+ Opcode.SET_4_A.cycles);
        assertArrayEquals(new int[] {getTotalBits(new Opcode[] {Opcode.ADD_A_HLR,Opcode.LD_HL_N16,Opcode.SET_4_A}),0,0xA,0,0,0,0,0,0xFF,0}, c._testGetPcSpAFBCDEHL());
    }
    
    @Test
    void SCFFWorks() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        b.write(0, Opcode.SCF.encoding);
        cycleCpu(c, 1);
        assertEquals(0x10, getF(c));
        b.write(1, Opcode.CCF.encoding);
        cycleCpu(c, 1);
        assertEquals(0x10, getF(c));

    }
    
    @Test
    void highRamIsProperlyMapped() {
        Cpu c = new Cpu();
        Bus b = new Bus();
        c.attachTo(b);
        b.write(AddressMap.HIGH_RAM_START, 42);
        assertEquals(42,b.read(AddressMap.HIGH_RAM_START));
        b.write(AddressMap.HIGH_RAM_END, 3);
        assertEquals(0xff, b.read(AddressMap.HIGH_RAM_END));
    }
    
    @Test
    void RSTWorks() {
        Opcode[] RST = new Opcode[] {Opcode.RST_0, Opcode.RST_1, Opcode.RST_2, Opcode.RST_3, Opcode.RST_4, Opcode.RST_5, Opcode.RST_6, Opcode.RST_7};
        int i = 0;
        for (Opcode o:RST) {
            Cpu c = new Cpu();
            Ram r = new Ram(0xFFFF);
            Bus b = connect(c, r);
            b.write(0, o.encoding);
            cycleCpu(c, o.cycles);
            assertEquals(8*i, getPC(c));
            ++i;
        }
    }
    
    @Test
    void settingIMEWorks() {
        Opcode[] EIDI = new Opcode[] {Opcode.EI, Opcode.DI};
        Interrupt interruption = Interrupt.VBLANK;
        for (Opcode o:EIDI) {
            Cpu c = new Cpu();
            Ram r = new Ram(0xFFFF);
            Bus b = connect(c, r);
            settingInterruptions(interruption, c);
            b.write(0, o.encoding);
            cycleCpu(c, o.cycles);
            if (o ==Opcode.EI) {
                assertEquals(AddressMap.INTERRUPTS[interruption.mask()], getPC(c));
            } else {
                assertEquals(o.totalBytes, getPC(c));
            }
        }
    }
    
    @Test
    void RETIWorks() {
        Interrupt interruption = Interrupt.VBLANK;
        Opcode o = Opcode.RETI;
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        settingInterruptions(interruption, c);
        b.write(0, o.encoding);
        cycleCpu(c, o.cycles);
        assertEquals(AddressMap.INTERRUPTS[interruption.mask()], getPC(c));
    }
    
    @Test
    void allInterruptionsWork() {
        Interrupt[] interrupts = new Interrupt[] {Interrupt.VBLANK, Interrupt.LCD_STAT, Interrupt.TIMER, Interrupt.SERIAL, Interrupt.JOYPAD};
        Opcode imeActivator = Opcode.EI;
        for (Interrupt i : interrupts) {
            Cpu c = new Cpu();
            Ram r = new Ram(0xFFFF);
            Bus b = connect(c, r);
            settingInterruptions(i, c);
            b.write(0, imeActivator.encoding);
            cycleCpu(c, imeActivator.cycles);
            assertEquals(AddressMap.INTERRUPTS[i.mask()], getPC(c));
        }
    }
    
    @Test
    void fibonacciTest() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        int[] bytes = new int[] {
                0x31, 0xFF, 0xFF, 0x3E,
                0x0B, 0xCD, 0x0A, 0x00,
                0x76, 0x00, 0xFE, 0x02,
                0xD8, 0xC5, 0x3D, 0x47,
                0xCD, 0x0A, 0x00, 0x4F,
                0x78, 0x3D, 0xCD, 0x0A,
                0x00, 0x81, 0xC1, 0xC9,
              };
        for (int i = 0;i<bytes.length;++i) {
            b.write(i, bytes[i]);
        }
        long i = 0;
        while (getPC(c)!=8) {
            c.cycle(i);
            ++i;
        }
        assertEquals(89, getA(c));
    }
    

    @Test
    void AbsoluteJumpsSetsPcCorrectly() { // /!\ Doesn't check cc
	    	GameBoy gameboy = new GameBoy(null);
	    	Cpu cpu = gameboy.cpu();
	    	Opcode[] arbitrary = {Opcode.INC_C,
	    			Opcode.INC_D};
	    	Assembler arb = new Assembler();
	    	for (Opcode op : arbitrary) {
	    		arb.emit(op);
	    	}
	    	
	    	
	    	{ //JP_HL
	    	Assembler asm = new Assembler();	   
	    	 int randomNum = ThreadLocalRandom.current().nextInt(0, 1<<16 );
		    	asm.emit(Opcode.LD_HL_N16,randomNum);
		    asm.emit(Opcode.JP_HL);
		    assertEquals(randomNum,(stateAfter(asm)).pc());
	    	}
	    	
	    	{ //JP_N16
	    		Assembler asm = new Assembler();	   
	    		int randomNum = ThreadLocalRandom.current().nextInt(0, 1<<16 );
		    	 asm.emit(Opcode.JP_N16,randomNum);
			    assertEquals(randomNum,(stateAfter(asm)).pc());
	    		
	    	}
	    
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
    
    private void settingInterruptions(Interrupt i, Cpu c) {
        c.requestInterrupt(i);
        c.write(AddressMap.REG_IE, i.mask());
    }
    
    private int getPC(Cpu c) {
        return c._testGetPcSpAFBCDEHL()[0]; 
}
    private int getA(Cpu c) {
    		return c._testGetPcSpAFBCDEHL()[2];	
    }
    private int getB(Cpu c) {
		return c._testGetPcSpAFBCDEHL()[4];	
}
    private int getF(Cpu c) {
		return c._testGetPcSpAFBCDEHL()[3];	
}
    private int getC(Cpu c) {
		return c._testGetPcSpAFBCDEHL()[5];	
}
    private int getD(Cpu c) {
		return c._testGetPcSpAFBCDEHL()[6];	
}
    private int getE(Cpu c) {
		return c._testGetPcSpAFBCDEHL()[7];	
}
    private int getH(Cpu c) {
		return c._testGetPcSpAFBCDEHL()[8];	
}
    private int getL(Cpu c) {
		return c._testGetPcSpAFBCDEHL()[9];	
}
    private int getHL(Cpu c) {
    		return Bits.make16(getH(c),getL(c));
    }
    
    private CpuState stateAfter(Assembler asm, Component... components) {
    		Program prog = asm.program();
        GameBoy gameboy = new GameBoy(null);
        Component rom = prog.rom();
        Cpu cpu = gameboy.cpu();
        Bus bus = gameboy.bus();
        rom.attachTo(bus);
        for (Component c2: components)
            c2.attachTo(bus);
        gameboy.runUntil(prog.cycles());
        return CpuState.ofArray(cpu._testGetPcSpAFBCDEHL());
    }
    
    
    
    
}
