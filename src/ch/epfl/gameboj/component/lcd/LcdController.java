/**
 * 
 */
package ch.epfl.gameboj.component.lcd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;
import ch.epfl.gameboj.component.memory.Ram;

/**
 * @author David Gonzalez leon (270845)
 *
 */
public final class LcdController implements Clocked, Component {

    private enum Reg implements Register {
        LCDC(0xFF40), STAT(0xFF41), SCY(0xFF42), SCX(0xFF43), LY(0xFF44), LYC(
                0xFF45), DMA(0xFF46), BGP(0xFF47), OBP0(
                        0xFF48), OBP1(0xFF49), WY(0xFF4A), WX(0xFF4B);

        public final int regAddress;

        private Reg(int regAddress) {
            this.regAddress = regAddress;
        }
    }

    public final static int LCD_WIDTH = 160;
    public final static int LCD_HEIGHT = 140;

    private final Cpu cpu;
    private LcdImage defaultImage;
    private final Ram videoRam;
    private final RegisterFile<Reg> regs = new RegisterFile<>(Reg.values());

    public LcdController(Cpu cpu) {
        this.cpu = cpu;
        videoRam = new Ram(AddressMap.VIDEO_RAM_SIZE);
        LcdImageLine[] lines = new LcdImageLine[LCD_HEIGHT];
        Arrays.fill(lines, new LcdImageLine(new BitVector(LCD_WIDTH),
                new BitVector(LCD_WIDTH), new BitVector(LCD_WIDTH)));
        defaultImage = new LcdImage(Arrays.asList(lines), LCD_WIDTH, LCD_HEIGHT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        if (Preconditions.checkBits16(address) >= AddressMap.VIDEO_RAM_START
                && address < AddressMap.VIDEO_RAM_END) {
            return videoRam.read(address - AddressMap.VIDEO_RAM_START);
        }
        if (address >= AddressMap.REGS_LCDC_START
                && address < AddressMap.REGS_LCDC_END) {
            switch (address - AddressMap.REGS_LCDC_START) {
            case 0:
                return regs.get(Reg.LCDC);
            case 1:
                return regs.get(Reg.STAT);
            case 2:
                return regs.get(Reg.SCY);
            case 3:
                return regs.get(Reg.SCX);
            case 4:
                return regs.get(Reg.LY);
            case 5:
                return regs.get(Reg.LYC);
            case 6:
                return regs.get(Reg.DMA);
            case 7:
                return regs.get(Reg.BGP);
            case 8:
                return regs.get(Reg.OBP0);
            case 9:
                return regs.get(Reg.OBP1);
            case 10:
                return regs.get(Reg.WY);
            case 11:
                return regs.get(Reg.WX);
            default:
                break;
            }
        }
        return NO_DATA;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        Preconditions.checkBits8(data);
        if (Preconditions.checkBits16(address) >= AddressMap.VIDEO_RAM_START
                && address < AddressMap.VIDEO_RAM_END) {
            videoRam.write(address - AddressMap.VIDEO_RAM_START, data);
        }
        if (address >= AddressMap.REGS_LCDC_START
                && address < AddressMap.REGS_LCDC_END) {
            switch (address - AddressMap.REGS_LCDC_START) {
            case 0:
                if (Bits.test(data, 7)) {
                    regs.set(Reg.LY, 0);
                    setMode(0);
                    nextNonIdleCycle = Long.MAX_VALUE;
                }
                regs.set(Reg.LCDC, data);
                break;
            case 1:
                regs.set(Reg.STAT, data & 0xF8);
                break;
            case 2:
                regs.set(Reg.SCY, data);
                break;
            case 3:
                regs.set(Reg.SCX, data);
                break;
            case 4:
                regs.set(Reg.LY, data);
                checkIfLYEqualsLYC();
                break;
            case 5:
                regs.set(Reg.LYC, data);
                checkIfLYEqualsLYC();
                break;
            case 6:
                regs.set(Reg.DMA, data);
                break;
            case 7:
                regs.set(Reg.BGP, data);
                break;
            case 8:
                regs.set(Reg.OBP0, data);
                break;
            case 9:
                regs.set(Reg.OBP1, data);
                break;
            case 10:
                regs.set(Reg.WY, data);
                break;
            case 11:
                regs.set(Reg.WX, data);
                break;
            default:
                break;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Clocked#cycle(long)
     */
    @Override
    public void cycle(long cycle) {
        // TODO Auto-generated method stub

    }

    // todo
    public LcdImage currentImage() {
        return defaultImage;
    }

    /// Manages the current mode of the LCD controller

    private void setMode(int mode) {
        int statValue = regs.get(Reg.STAT);
        regs.set(Reg.STAT, Bits.set(Bits.set(statValue, 0, Bits.test(mode, 0)),
                1, Bits.test(mode, 1)));
        if (mode != 3) {
            if (checkStatBit(mode + 3)) {
                cpu.requestInterrupt(Interrupt.LCD_STAT);
            }
        }
        if (mode == 1) {
            cpu.requestInterrupt(Interrupt.VBLANK);
        }
    }

    private int getMode() {
        int statValue = regs.get(Reg.STAT);
        return (Bits.test(statValue, 1) ? 1 << 1 : 0)
                | (Bits.test(statValue, 0) ? 1 : 0);
    }

    /// Manages the Bits in Stat that throw exceptions if they are on

    private void checkIfLYEqualsLYC() {
        int statValue = regs.get(Reg.STAT);
        boolean equal = regs.get(Reg.LYC) == regs.get(Reg.LY);
        regs.set(Reg.STAT, Bits.set(statValue, 2, equal));
        if (equal && Bits.test(statValue, 6)) {
            cpu.requestInterrupt(Interrupt.LCD_STAT);
        }
    }

    private boolean checkStatBit(int index) {
        return Bits.test(regs.get(Reg.STAT), index);
    }
}
