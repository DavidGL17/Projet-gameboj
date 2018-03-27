/**
 * 
 */
package ch.epfl.gameboj.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;

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
        Cpu c = new Cpu();
        Timer t = new Timer(c);
        int randomNumber = Bits.clip(8, new Random().nextInt());
        System.out.println(randomNumber);
        for (int i = 0; i < 0xFFFF; ++i) {
            switch (i) {
            case AddressMap.REG_DIV:
                t.write(i, randomNumber);
                assertEquals(randomNumber, t.read(i));
                break;
            case AddressMap.REG_TAC:
                t.write(i, 0b1111);
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
        t.cycle(5);
        assertEquals(1, t.read(AddressMap.REG_TIMA));
    }

    @Test
    void TimerResetsSecondaryCounterCorrectly() {
        Cpu c = new Cpu();
        Timer t = new Timer(c);
        t.write(AddressMap.REG_TAC, 0b101);
        t.write(AddressMap.REG_TIMA, 0xFF);
        t.write(AddressMap.REG_TMA, 0xAB);
        t.cycle(4);
        assertEquals(0xAB, t.read(AddressMap.REG_TIMA));
    }

    @Test
    void TimerEvaluatesCorrectlyDIVsBit() {
        int[] TAC = new int[] { 0b101, 0b110, 0b111, 0b100 };
        for (int i = 0; i < 4; ++i) {
            Cpu c = new Cpu();
            Timer t = new Timer(c);
            t.write(AddressMap.REG_TAC, TAC[i]);
            switch (i) {
            case 0:
                t.cycle(4);
                assertEquals(3, t.read(AddressMap.REG_TIMA));
                break;
            case 1:
                t.cycle(16);
                assertEquals(8, t.read(AddressMap.REG_TIMA));
                break;
            case 2:
                t.cycle(64);
                assertEquals(32, t.read(AddressMap.REG_TIMA));
                break;
            case 3:
                t.cycle(256);
                assertEquals(128, t.read(AddressMap.REG_TIMA));
                break;
            }
        }
    }

    @Test
    void TimerThrowsExceptionTIMAFull() {
        Cpu c = new Cpu();
        Timer t = new Timer(c);
        t.write(AddressMap.REG_TAC, 0b101);
        t.write(AddressMap.REG_TIMA, 0xFF);
        t.write(AddressMap.REG_TMA, 0xAB);
        t.cycle(4);
        assertTrue(Bits.test(2, c.read(AddressMap.REG_IF)));
    }
}