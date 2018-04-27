package ch.epfl.gameboj.component.lcd;

import java.util.Arrays;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;
import ch.epfl.gameboj.component.lcd.LcdImage.Builder;
import ch.epfl.gameboj.component.memory.Ram;

/**
 * @author David Gonzalez leon (270845)
 * @author Melvin Malonga-Matouba (288405)
 *
 */
public final class LcdController implements Clocked, Component {

    private enum Reg implements Register {
        LCDC, STAT, SCY, SCX, LY, LYC, DMA, BGP, OBP0, OBP1, WY, WX;
    }

    private enum LCDCBit implements Bit {
        BG, OBJ, OBJ_SIZE, BG_AREA, TILE_SOURCE, WIN, WIN_AREA, LCD_STATUS
    }

    private enum STATBit implements Bit {
        MODE0, MODE1, LYC_EQ_LY, INT_MODE0, INT_MODE1, INT_MODE2, INT_LYC, UNUSED
    }

    public final static int LCD_WIDTH = 160;
    public final static int LCD_HEIGHT = 144;
    private final static int BG_SIZE = 256;
    private final static int LINE_CYCLES = 20 + 43 + 51;

    private final Cpu cpu;
    private LcdImage currentImage;
    private LcdImage.Builder nextImageBuilder = new Builder(LCD_WIDTH,
            LCD_HEIGHT);
    private final Ram videoRam;
    private final RegisterFile<Reg> regs = new RegisterFile<>(Reg.values());
    private long lcdOnCycle;
    private long nextNonIdleCycle;

    private boolean firstLineDrawn = false;

    public LcdController(Cpu cpu) {
        this.cpu = cpu;
        videoRam = new Ram(AddressMap.VIDEO_RAM_SIZE);
        LcdImageLine[] lines = new LcdImageLine[LCD_HEIGHT];
        Arrays.fill(lines, new LcdImageLine(new BitVector(LCD_WIDTH),
                new BitVector(LCD_WIDTH), new BitVector(LCD_WIDTH)));
        currentImage = new LcdImage(Arrays.asList(lines), LCD_WIDTH,
                LCD_HEIGHT);
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
            return regs.get(Reg.values()[address - AddressMap.REGS_LCDC_START]);
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
            switch (address) {
            case 0xFF40:
                if (Bits.test(data, 7)) {
                    regs.set(Reg.LY, 0);
                    checkIfLYEqualsLYC();
                    setMode(0);
                    nextNonIdleCycle = Long.MAX_VALUE;
                }
                regs.set(Reg.LCDC, data);
                break;
            case 0xFF41:
                regs.set(Reg.STAT, data & 0xF8 | regs.get(Reg.STAT) & 0x07);
                checkIfLYEqualsLYC();
                break;
            case 0xFF44:
                break;
            case 0xFF45:
                regs.set(Reg.LYC, data);
                checkIfLYEqualsLYC();
                break;
            default:
                regs.set(Reg.values()[address - AddressMap.REGS_LCDC_START],
                        data);
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
        int drawnImages = (int) ((cycle - lcdOnCycle) / LINE_CYCLES
                / (LCD_HEIGHT + 10));
        if (regs.testBit(Reg.LCDC, LCDCBit.LCD_STATUS)) {
            if (nextNonIdleCycle == Long.MAX_VALUE) {
                lcdOnCycle = cycle;
                nextNonIdleCycle = cycle;
                setMode(2);
                regs.set(Reg.LY, 0);
                checkIfLYEqualsLYC();
                reallyCycle(cycle, drawnImages);
            }

            if (cycle >= nextNonIdleCycle) {
                switch (getMode()) {
                case 0:
                    if (regs.get(Reg.LY) == LCD_HEIGHT - 1) { // if image is
                        // complete
                        setMode(1);
                    } else if (regs.get(Reg.LY) < LCD_HEIGHT) {
                        setMode(2);
                    }
                    break;
                case 1:
                    if (regs.get(Reg.LY) == LCD_HEIGHT + 9) {
                        firstLineDrawn = false;
                        setMode(2);
                        regs.set(Reg.LY, 0);
                        checkIfLYEqualsLYC();
                    }
                    break;
                case 2:
                    setMode(3);
                    break;
                case 3:
                    setMode(0);
                    break;
                }
                reallyCycle(cycle, drawnImages);
            }
        } else {
            setMode(0);
            regs.set(Reg.LY, 0);
            checkIfLYEqualsLYC();
        }

    }

    private void reallyCycle(long cycle, int drawnImages) {
        // Peut être mettre les deux variables ci dssous en argument, vu qu'on
        // les calcule déjà dans cycle?
        switch (getMode()) {
        case 0:
            // mode 0 //Completed
            nextNonIdleCycle = (drawnImages) * LINE_CYCLES * (LCD_HEIGHT + 10)
                    + (regs.get(Reg.LY) + 1) * LINE_CYCLES;
            break;
        case 1:
            // mode 1 //Completed
            regs.set(Reg.LY, regs.get(Reg.LY) + 1);
            checkIfLYEqualsLYC();
            if (regs.get(Reg.LY)==144) {
            	currentImage = nextImageBuilder.build();
            	nextImageBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);
            }
            nextNonIdleCycle = (drawnImages + 1) * LINE_CYCLES
                    * (LCD_HEIGHT + 10);
            break;
        case 2:
            // mode 2 // Completed
            if (firstLineDrawn) { // if vient de commencer une image
                regs.set(Reg.LY, regs.get(Reg.LY) + 1);
            }
            checkIfLYEqualsLYC();
            nextNonIdleCycle = (drawnImages) * LINE_CYCLES * (LCD_HEIGHT + 10)
                    + regs.get(Reg.LY) * LINE_CYCLES + 20;

            break;
        case 3:
            // mode 3 //Ajouter sprite
            // Accès mémoire tuiles de sprite/background
            nextImageBuilder.setLine(computeLine(regs.get(Reg.LY)),
                    regs.get(Reg.LY));
            nextNonIdleCycle = (drawnImages) * LINE_CYCLES * (LCD_HEIGHT + 10)
                    + regs.get(Reg.LY) * LINE_CYCLES + 20 + 43;
            firstLineDrawn = true;
            break;
        }
        ;
        // TODO
    }

    /**
     * Returns the last image drawn by the LcdController
     * 
     * @return currentImage a LcdImage
     */
    public LcdImage currentImage() {
        return currentImage;
    }

    private LcdImageLine computeLine(int line) {
        LcdImageLine bgLine = computeBgLine(Math.floorMod(line+regs.get(Reg.SCY),256));
        bgLine = bgLine.extractWrapped(regs.get(Reg.SCX), LCD_WIDTH);
        return bgLine;
    }
    
    private LcdImageLine computeBgLine(int index) {
        LcdImageLine.Builder lineBuilder = new LcdImageLine.Builder(BG_SIZE);
        for (int i = 0; i < BG_SIZE / 8; ++i) {
            int tileIndex = read(
                    AddressMap.BG_DISPLAY_DATA[regs.testBit(Reg.LCDC,
                            LCDCBit.BG_AREA) ? 1 : 0] + i + (index / 8) * 32);
            int tileAddress = 0;
            if (tileIndex >0x7F) {
            	tileAddress = 0x8800 + tileIndex;
            } else {
            	if (regs.testBit(Reg.LCDC,LCDCBit.TILE_SOURCE)){
            		tileAddress =  0x8000 + tileIndex ;
            	} else {
            		tileAddress = 0x9000 + tileIndex;
            	}
            }
            int lsbBg = read( tileAddress + Math.floorMod(index , 8) * 16  );
            int msbBg = read( tileAddress + Math.floorMod(index , 8) * 16 + 1);
            lineBuilder.setBytes(i, msbBg, lsbBg);
        }
        return lineBuilder.build();
    }

    /// Manages the current mode of the LCD controller

    private void setMode(int mode) {
        int statValue = regs.get(Reg.STAT);
        int previousMode = Bits.clip(2, statValue);
        regs.set(Reg.STAT, Bits.set(Bits.set(statValue, 0, Bits.test(mode, 0)),
                1, Bits.test(mode, 1)));
        if (previousMode != 1 && mode == 1)
            cpu.requestInterrupt(Interrupt.VBLANK);
        if (mode != 3) {
            if (checkStatBit(mode + 3)) {
                cpu.requestInterrupt(Interrupt.LCD_STAT);
            }
        }

    }

    private int getMode() {
        int statValue = regs.get(Reg.STAT);
        return (Bits.test(statValue, STATBit.MODE1) ? 1 << 1 : 0)
                | (Bits.test(statValue, STATBit.MODE0) ? 1 : 0);
    }

    /// Manages the Bits in Stat that throw exceptions if they are on

    private void checkIfLYEqualsLYC() {
        int statValue = regs.get(Reg.STAT);
        boolean equal = regs.get(Reg.LYC) == regs.get(Reg.LY);
        regs.set(Reg.STAT,
                Bits.set(statValue, STATBit.LYC_EQ_LY.index(), equal));
        if (equal && Bits.test(statValue, STATBit.INT_LYC)) {
            cpu.requestInterrupt(Interrupt.LCD_STAT);
        }
    }

    private boolean checkStatBit(int index) {
        return Bits.test(regs.get(Reg.STAT), index);
    }

}
