/**
 * 
 */
package ch.epfl.gameboj.component.cartridge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

/**
 * @author David Gonzalez leon (270845)
 *
 */
public class Cartridge implements Component {

    private final Component MBC;
    
    private Cartridge(Component MBC) {
        this.MBC = MBC;
    }
    
    public static Cartridge ofFile(File romFile) throws IOException {
        try(FileInputStream input = new FileInputStream(romFile)){
            byte[] data = new byte[0x8000];
            int n = 0;
            do {
                n = input.read(data);
            } while (n != -1);
            Preconditions.checkArgument(data[0x147]==0);
            return new Cartridge(new MBC0(new Rom(data)));
        } catch (FileNotFoundException i) {
            throw new IOException();
        }
    }
    
    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        return MBC.read(Preconditions.checkBits16(address));
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        MBC.write(Preconditions.checkBits16(address), Preconditions.checkBits8(data));
    }

}
