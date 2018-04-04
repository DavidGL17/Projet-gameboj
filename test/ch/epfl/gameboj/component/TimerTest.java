/**
 * 
 */
package ch.epfl.gameboj.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;

/**
 * @author David Gonzalez leon (270845)
 *
 */
class TimerTest {

    @Test
    void ConstructorThrowsException() {
        assertThrows(NullPointerException.class, () -> {
            new Timer(null);
        });
    }

    @Test
    void TimerReadAndWriteWork() {
        // int randomNumber = Bits.clip(8, new Random().nextInt());
        int randomNumber = 9;
        System.out.println(randomNumber);
        for (int i = 0; i < 0xFFFF; ++i) {
            Cpu c = new Cpu();
            Timer t = new Timer(c);
            switch (i) {
            case AddressMap.REG_DIV:
                t.write(i, randomNumber);
                assertEquals(randomNumber, t.read(i));
                break;
            case AddressMap.REG_TAC: // Ce test ne passes pas si on met un
                                     // chiffre plus grand que 3 bits. On
                                     // devrait clip la valeur de tac, pour
                                     // quelle soit pas plus grande que 3 bit?
                t.write(i, 0b111);
                assertEquals(0b111, t.read(i));
                break;
            case AddressMap.REG_TIMA:
                t.write(i, randomNumber);
                assertEquals(randomNumber, t.read(i));
                break;
            case AddressMap.REG_TMA:
                t.write(i, randomNumber);
                assertEquals(randomNumber, t.read(i));
                break;
            default:
                assertEquals(Component.NO_DATA, t.read(i));
                break;
            }
        }
    }

    @Test
    void TimerIncreasesTIMACorrectly() {
        Cpu c = new Cpu();
        Timer t = new Timer(c);
        t.write(AddressMap.REG_TAC, 0b101);
        for (int i = 0;i<4;++i) {
            t.cycle(i);
        }
        assertEquals(1, t.read(AddressMap.REG_TIMA));
    }

    @Test
    void TimerResetsSecondaryCounterCorrectly() {
        Cpu c = new Cpu();
        Timer t = new Timer(c);
        t.write(AddressMap.REG_TAC, 0b101);
        t.write(AddressMap.REG_TIMA, 0xFF);
        t.write(AddressMap.REG_TMA, 0xAB);
        for (int i = 0;i<4;++i) {
            t.cycle(i);
        }
        assertEquals(0xAB, t.read(AddressMap.REG_TIMA));
    }

    
    @Disabled
    @Test 
    void TimerEvaluatesCorrectlyDIVsBit() {
        int[] TAC = new int[] { 0b101, 0b110, 0b111, 0b100 };
//        for (int i = 0; i < 4; ++i) {
            Cpu c = new Cpu();
            Timer t = new Timer(c);
            t.write(AddressMap.REG_TAC, TAC[3]);
            t.cycle(256);
            assertEquals(1, t.read(AddressMap.REG_TIMA));
//        }
    }

    @Test
    void TimerRequestsInterruptionTIMAFull() {
    		{
	        Cpu c = new Cpu();
	        Timer t = new Timer(c);
	        t.write(AddressMap.REG_TAC, 0b101);
	        t.write(AddressMap.REG_TIMA, 0xFF);
	        t.write(AddressMap.REG_TMA, 0xAB);
	        for (int i = 0;i<4;++i) {
	            t.cycle(i);
	        }
	        assertTrue(Bits.test(c.read(AddressMap.REG_IF),Interrupt.TIMER.index()));
    		}
        
	    	{
	        Cpu c = new Cpu();
	        Timer t = new Timer(c);
	        t.write(AddressMap.REG_TAC, 0b100);
	        t.write(AddressMap.REG_TIMA, 0xFF);
	        t.write(AddressMap.REG_TMA, 0xAB);
	        for (int i = 0;i<(1<<8);++i) {
	            t.cycle(i);
	        }
	        assertTrue(Bits.test(c.read(AddressMap.REG_IF),Interrupt.TIMER.index()));
	    	}
	    	
	    	{
	    		Cpu c = new Cpu();
			Timer t = new Timer(c);
			t.write(AddressMap.REG_TAC, 0b110);
			t.write(AddressMap.REG_TIMA, 0xFF);
			t.write(AddressMap.REG_TMA, 0xAB);
			for (int i = 0; i < (1<<4); ++i) {
				t.cycle(i);
			}
			assertTrue(Bits.test(c.read(AddressMap.REG_IF),Interrupt.TIMER.index()));
	    	}
	    	
	    	{
	    		Cpu c = new Cpu();
			Timer t = new Timer(c);
			t.write(AddressMap.REG_TAC, 0b101);
			t.write(AddressMap.REG_TIMA, 0xFF);
			t.write(AddressMap.REG_TMA, 0xAB);
			for (int i = 0; i < (1<<7); ++i) {
				t.cycle(i);
			}
			assertTrue(Bits.test(c.read(AddressMap.REG_IF),Interrupt.TIMER.index()));
	    	}
    }
    
    
    
}