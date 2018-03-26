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
            byte[] data = new byte[(int)romFile.length()];
            int n = 0;
            while (n != -1){
                n = input.read(data);
            };
            Preconditions.checkArgument(data[0x147]==0);
            Rom rom = new Rom(data);
            Cartridge c = new Cartridge(new MBC0(rom));
            return c;
        } catch (FileNotFoundException i) {
            throw new IOException();
        }
    }
    
    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        return MBC.read(address);
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        MBC.write(address, data);
    }

}
