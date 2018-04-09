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

    private static String[] testsFailed = new String[] {};
    private static String[] testsPassed = new String[] { "01-special.gb",
            "02-interrupts.gb", "03-op sp,hl.gb", "04-op r,imm.gb",
            "05-op rp.gb", "06-ld r,r.gb", "07-jr,jp,call,ret,rst.gb",
            "08-misc instrs.gb", "09-op r,r.gb", "10-bit ops.gb",
            "11-op a,(hl).gb", "instr_timing.gb", };

    public static void main(String[] args) {
        for (String test : testsPassed) {
            test(test, "30000000");
        }
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
