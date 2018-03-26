/**
 * 
 */
package ch.epfl.gameboj.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        assertThrows(NullPointerException.class, ()->{new Timer(null);});
    }

    @Test
    void TimerReadAndWriteWork() {
        Cpu c = new Cpu();
        Timer t = new Timer(c);
        int randomNumber = Bits.clip(8, new Random().nextInt());
        System.out.println(randomNumber);
        for (int i = 0;i<0xFFFF;++i) {
            if (isATimerReg(i)) {
                t.write(i, randomNumber);
                assertEquals(randomNumber, t.read(i));
            } else {
                assertEquals(Component.NO_DATA, t.read(i));
            }
        }
    }
    
   
    
    
    private boolean isATimerReg(int index) {
        return (index == AddressMap.REG_DIV)||(index == AddressMap.REG_TAC)||(index == AddressMap.REG_TMA)||(index == AddressMap.REG_TIMA);
    }
    
}
