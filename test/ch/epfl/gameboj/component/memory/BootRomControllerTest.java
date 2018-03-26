/**
 * 
 */
package ch.epfl.gameboj.component.memory;

import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.component.cartridge.Cartridge;

/**
 * @author David Gonzalez leon (270845)
 *
 */
public class BootRomControllerTest {
    @Test
    void ThrowsExceptionWhenCartridgeIsNull() {
        assertThrows(NullPointerException.class, ()->{new BootRomController(null);});
    }
    
    @Test 
    void readWorksProperlyBeforeDisabling() throws IOException {
        int i = 0;
        BootRomController br = new BootRomController(Cartridge.ofFile(new File("01-special.gb")));
        for (byte b : BootRom.DATA) {
            assertEquals(b, br.read(i));
            ++i;
        }
    }
    
    @Test //Celui la est pas complétement juste
    void BootRomControllerDisablesHimselfCorrectly() throws IOException {
        int i = 0;
        BootRomController br = new BootRomController(Cartridge.ofFile(new File("01-special.gb")));
        br.write(AddressMap.REG_BOOT_ROM_DISABLE, 0);
        for (byte b : BootRom.DATA) {
            assertNotEquals(b, br.read(i));
            ++i;
        }
    }
}
