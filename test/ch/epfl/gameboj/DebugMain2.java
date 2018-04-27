/**
 * 
 */
package ch.epfl.gameboj;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.lcd.LcdImage;

/**
 * @author David Gonzalez leon (270845)
 *
 */
public class DebugMain2 {
    private static String[] testsFailedPhase1 = new String[] {};
    private static String[] testsPassedPhase1 = new String[] { "01-special.gb",
            "02-interrupts.gb", "03-op sp,hl.gb", "04-op r,imm.gb",
            "05-op rp.gb", "06-ld r,r.gb", "07-jr,jp,call,ret,rst.gb",
            "08-misc instrs.gb", "09-op r,r.gb", "10-bit ops.gb",
            "11-op a,(hl).gb", "instr_timing.gb" };
    private static String[] testsFailedPhase2 = new String[] {
            "07-jr,jp,call,ret,rst.gb", "flappyboy.gb", "Tetris.gb" };
    private static String[] allTests = new String[] { "01-special.gb",
            "02-interrupts.gb", "03-op sp,hl.gb", "04-op r,imm.gb",
            "05-op rp.gb", "06-ld r,r.gb", "07-jr,jp,call,ret,rst.gb",
            "08-misc instrs.gb", "09-op r,r.gb", "10-bit ops.gb",
            "11-op a,(hl).gb", "instr_timing.gb", "07-jr,jp,call,ret,rst.gb",
            "flappyboy.gb", "Tetris.gb" };

    private static final int[] COLOR_MAP = new int[] { 0xFF_FF_FF, 0xD3_D3_D3,
            0xA9_A9_A9, 0x00_00_00 };

    public static void main(String[] args) {
//            test1(test, "30000000");
            test2("07-jr,jp,call,ret,rst.gb", "30000000");
    }

    private static void test1(String arg1, String arg2) {
        try {
            File romFile = new File(arg1);
            long cycles = Long.parseLong(arg2);

            GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));
            gb.runUntil(cycles);

            System.out.println("+--------------------+");
            for (int y = 0; y < 18; ++y) {
                System.out.print("|");
                for (int x = 0; x < 20; ++x) {
                    char c = (char) gb.bus().read(0x9800 + 32 * y + x);
                    System.out.print(Character.isISOControl(c) ? " " : c);
                }
                System.out.println("|");
            }
            System.out.println("+--------------------+");

//            LcdImage li = gb.lcdController().currentImage();
//            BufferedImage i = new BufferedImage(LcdController.LCD_WIDTH,
//                    LcdController.LCD_HEIGHT, BufferedImage.TYPE_INT_RGB);
//            for (int y = 0; y < LcdController.LCD_HEIGHT; ++y)
//                for (int x = 0; x < LcdController.LCD_WIDTH; ++x)
//                    i.setRGB(x, y, COLOR_MAP[li.get(x, y)]);
//            ImageIO.write(i, "png", new File("gb.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void test2(String arg1, String arg2) {
        try {
            File romFile = new File(arg1);
            long cycles = Long.parseLong(arg2);

            GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));
            gb.runUntil(cycles);

            System.out.println("+--------------------+");
            for (int y = 0; y < 18; ++y) {
                System.out.print("|");
                for (int x = 0; x < 20; ++x) {
                    char c = (char) gb.bus().read(0x9800 + 32 * y + x);
                    System.out.print(Character.isISOControl(c) ? " " : c);
                }
                System.out.println("|");
            }
            System.out.println("+--------------------+");

            LcdImage li = gb.lcdController().currentImage();
            BufferedImage i = new BufferedImage(LcdController.LCD_WIDTH,
                    LcdController.LCD_HEIGHT, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < LcdController.LCD_HEIGHT; ++y)
                for (int x = 0; x < LcdController.LCD_WIDTH; ++x)
                    i.setRGB(x, y, COLOR_MAP[li.get(x, y)]);
            ImageIO.write(i, "png", new File(arg1 + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
