/**
 * 
 */
package ch.epfl.gameboj;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.Test;

/**
 * @author David (270845)
 *
 */
public class PreconditionsTestJU {
    
    @Test
    public void checkArgumentTest() {
        assertThrows(IllegalArgumentException.class, () -> {Preconditions.checkArgument(false);});
    }
    
}
