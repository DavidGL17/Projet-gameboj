/**
 * 
 */
package ch.epfl.gameboj;

import java.io.File;
import java.io.IOException;

import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;

/**
 * @author David Gonzalez leon (270845)
 *
 */
public final class DebugMain {
    public static void main(String[] args) {
        test("01-special.gb", "40000000");;
    }

    private static void test(String arg1, String arg2) {
        try {
            File romFile = new File(arg1);
            long cycles = Long.parseLong(arg2);

            GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));
            Component printer = new DebugPrintComponent();
            printer.attachTo(gb.bus());
            while (gb.cycles() < cycles) {
                long nextCycles = Math.min(gb.cycles() + 17556, cycles);
                gb.runUntil(nextCycles);
                gb.cpu().requestInterrupt(Cpu.Interrupt.VBLANK);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
