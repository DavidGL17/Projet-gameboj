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
 * 
 * @author David Gonzalez leon (270845)
 *
 */
public final class Cartridge implements Component {

    private final Component MBC;
    private static final int MBC_IDENTIFICATION_ADDRESS = 0x147;
    private static final int MBC_SIZE_RAM_IDENTIFICATION_ADDRESS = 0x149;

    private Cartridge(Component MBC) {
        this.MBC = MBC;
    }

    /**
     * Builds a Cartridge accordingly to a file containing the Rom
     * 
     * @param romFile,
     *            the file
     * @return the built Cartridge
     * @throws IOException
     *             if the reading of romFile causes an IOException
     */
    public static Cartridge ofFile(File romFile) throws IOException {
        try (FileInputStream input = new FileInputStream(romFile)) {
            byte[] data = new byte[(int) romFile.length()];
            int n = 0;
            int i = 0;
            while ((n = input.read()) != -1) {
                data[i] = (byte) n;
                ++i;
            }
            Rom rom = new Rom(data);
            if (data[MBC_IDENTIFICATION_ADDRESS] == 0) {
                return new Cartridge(new MBC0(rom));
            } else if (data[MBC_IDENTIFICATION_ADDRESS] > 0
                    && data[MBC_IDENTIFICATION_ADDRESS] <= 3) {
                return new Cartridge(new MBC1(rom,
                        getRamSize(data[MBC_SIZE_RAM_IDENTIFICATION_ADDRESS])));
            } else {
                throw new IllegalArgumentException(
                        "not recongnized cartridge, data is : "
                                + data[MBC_IDENTIFICATION_ADDRESS]);
            }
        } catch (FileNotFoundException i) {
            throw new IOException();
        }
    }

    private static int getRamSize(int ramSizeIdentification) {
        switch (ramSizeIdentification) {
        case 1:
            return 2048;
        case 2:
            return 8192;
        case 3:
            return 32768;
        default:
            return 0;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        return MBC.read(Preconditions.checkBits16(address));
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        MBC.write(Preconditions.checkBits16(address),
                Preconditions.checkBits8(data));
    }

}