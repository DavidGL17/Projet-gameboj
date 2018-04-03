/**
 * 
 */
package ch.epfl.gameboj.component.catridge;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.component.cartridge.Cartridge;

/**
 * @author David Gonzalez leon (270845)
 *
 */
class CartridgeTest {

    @Test
    void CartrdigeOfFileThrowsExceptiontest() {
    		//IOExceptions
    			//if file doesn't exist
    	assertThrows(IOException.class, ()->{Cartridge.ofFile(new File("Projet-Gameboj/unexisting"));});
    		//IllegalArgumentException
    			//if the file doesn't conform
    		
    		
        
    }
    
    	@Test
    	void ReadAndWriteThrowsException() {
    		try {
    			Cartridge cartridge  = Cartridge.ofFile(new File("01-special.gb"));
    			
    		assertThrows(IllegalArgumentException.class, ()->{cartridge.read(0xFFFF12);});
    		assertThrows(IllegalArgumentException.class, ()->{cartridge.write(0xFFFF12,0);});
    		assertThrows(IllegalArgumentException.class, ()-> {cartridge.write(0xFFF0,0x10F);});
    		} catch (IOException e) {
    			
    		}
    		
    	}
    
  
}
