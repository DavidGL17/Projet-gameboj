/**
 * 
 */
package ch.epfl.gameboj;

import java.util.ArrayList;
import java.util.Objects;

import ch.epfl.gameboj.component.Component;

/**
 * @author David (270845)
 *
 */
public final class Bus {
    ArrayList<Component> composants = new ArrayList<Component>();
    
    public void attach(Component component) {
        Objects.requireNonNull(component);
        composants.add(component);
    }
    
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (composants.size()>0) {
            for (int i = 0;i<composants.size();++i) {
                int value = composants.get(i).read(address);
                if (value != Component.NO_DATA) {
                    return value;
                }
            }
            return 0xff;
            
        } else {
            return 0xff;
        }
    }
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        if (composants.size()>0) {
            for (int i = 0;i<composants.size();++i) {
                composants.get(i).write(address, data);
            }
        }
    }
}
