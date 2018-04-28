/**
 * 
 */
package ch.epfl.gameboj.component.lcd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;

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
        while (i<AddressMap.VIDEO_RAM_SIZE) {
            int n =  Bits.clip(8, rng.nextInt());
            c.write(AddressMap.VIDEO_RAM_START+i,n);
            assertEquals(n, c.read(AddressMap.VIDEO_RAM_START+i));
            ++i;
        }
    }
}
