/**
 * 
 */
package ch.epfl.gameboj.component.catridge;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.component.cartridge.MBC0;
import ch.epfl.gameboj.component.memory.Rom;

/**
 * @author David Gonzalez leon (270845)
 *
 */
class MBC0Test {

    @Test
    void MBC0ThrowsExceptiontest() {
        assertThrows(NullPointerException.class, ()->{new MBC0(null);});
        assertThrows(IllegalArgumentException.class, ()->{new MBC0(new Rom(new byte[32765]));});
    }

    @Test
    void ReadAndWriteWorkProperlyTest() {
        byte[] bytes = new byte[32768];
        for (int i = 0;i<bytes.length;++i) {
            bytes[i] = 25;
        }
        MBC0 mbc0 = new MBC0(new Rom(bytes));
        for (int i = 0;i<bytes.length;++i) {
            mbc0.write(i, 24);
            assertEquals(25, mbc0.read(i));
        }
    }
    
}
