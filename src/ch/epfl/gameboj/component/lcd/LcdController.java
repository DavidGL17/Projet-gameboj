package ch.epfl.gameboj.component.lcd;

import static ch.epfl.gameboj.bits.Bits.*;
import static ch.epfl.gameboj.Preconditions.checkBits16;
import static ch.epfl.gameboj.Preconditions.checkBits8;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;
import ch.epfl.gameboj.component.lcd.LcdImage.Builder;
import ch.epfl.gameboj.component.memory.Ram;

import static java.util.Arrays.fill;
import static java.util.Arrays.asList;
import static java.util.Arrays.sort;
import static ch.epfl.gameboj.AddressMap.*;

/**
 * @author David Gonzalez leon (270845)
 * @author Melvin Malonga-Matouba (288405)
 *
 */
public final class LcdController implements Clocked, Component {

    /**
     * Represents the registers of the LcdController, is used as the type of the
     * registerFile regs, allowing a quick implementation of these registers
     */
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
    private final static int LINE_CYCLES = 114;

    private final Cpu cpu;
    private final Ram videoRam;
    private final Ram objectAttributeMemory;
    private final RegisterFile<Reg> regs = new RegisterFile<>(Reg.values());
    private Bus bus;

    private LcdImage currentImage;
    private LcdImage.Builder nextImageBuilder = new Builder(LCD_WIDTH,
            LCD_HEIGHT);
    private LcdImage DEFAULT_IMAGE;
    private long lcdOnCycle;
    private long nextNonIdleCycle = Long.MAX_VALUE;
    private boolean firstLineDrawn = false;
    private int winY = 0;
    private long imagesDrawn = 0;

    private boolean oamCopy = false;
    private int addressToCopy = 0;
    private int octetsCopiedToOam = 0;

    /**
     * Creates a new LcdController
     * 
     * @param cpu
     */
    public LcdController(Cpu cpu) {
        this.cpu = cpu;
        videoRam = new Ram(VIDEO_RAM_SIZE);
        objectAttributeMemory = new Ram(OAM_RAM_SIZE);
        LcdImageLine[] lines = new LcdImageLine[LCD_HEIGHT];
        fill(lines, LcdImageLine.ZERO_OF_SIZE_160);
        DEFAULT_IMAGE = new LcdImage(asList(lines), LCD_WIDTH,
                LCD_HEIGHT);
        currentImage = DEFAULT_IMAGE;
        regs.set(Reg.OBP0, 0b11100100);
        regs.set(Reg.OBP1, 0b11100100);
        regs.set(Reg.BGP, 0b11100100);
    }

    /**
     * @return LCD_WIDTH, the width of the LcdImage as seen on a GameBoy screen
     */
    public int width() {
        return LCD_WIDTH;
    }

    /**
     * @return LCD_HEIGHT, the height of the LcdImage as seen on a GameBoy
     *         screen
     */
    public int height() {
        return LCD_HEIGHT;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#attachTo(ch.epfl.gameboj.Bus)
     */
    @Override
    public void attachTo(Bus bus) {
        this.bus = bus;
        Component.super.attachTo(bus);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        if (checkBits16(address) >= VIDEO_RAM_START
                && address < VIDEO_RAM_END) {
            return videoRam.read(address - VIDEO_RAM_START);
        }
        if (checkBits16(address) >= OAM_START
                && address < OAM_END) {
            return objectAttributeMemory.read(address - OAM_START);
        }
        if (address >= REGS_LCDC_START
                && address < REGS_LCDC_END) {
            return regs.get(Reg.values()[address - REGS_LCDC_START]);
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
        checkBits8(data);
        if (checkBits16(address) >= VIDEO_RAM_START
                && address < VIDEO_RAM_END) {
            videoRam.write(address - VIDEO_RAM_START, data);
        }
        if (address >= OAM_START && address < OAM_END) {
            objectAttributeMemory.write(address - OAM_START, data);
        }
        if (address >= REGS_LCDC_START
                && address < REGS_LCDC_END) {
            switch (address - REGS_LCDC_START) {
            case 0:
                if (regs.testBit(Reg.LCDC, LCDCBit.LCD_STATUS)
                        && !test(data, LCDCBit.LCD_STATUS.index())) {
                    regs.set(Reg.LY, 0);
                    imagesDrawn = 0;
                    winY = 0;
                    nextImageBuilder = new LcdImage.Builder(LCD_WIDTH,
                            LCD_HEIGHT);
                    checkIfLYEqualsLYC();
                    setMode(0);
                    nextNonIdleCycle = Long.MAX_VALUE;
                    currentImage = DEFAULT_IMAGE;
                }
                regs.set(Reg.LCDC, data);
                break;
            case 1:
                regs.set(Reg.STAT, data & 0xF8 | regs.get(Reg.STAT) & 0x07);
                checkIfLYEqualsLYC();
                break;
            case 4:
                break;
            case 5:
                regs.set(Reg.LYC, data);
                checkIfLYEqualsLYC();
                break;
            case 6:
                oamCopy = true;
                octetsCopiedToOam = 0;
                addressToCopy = data << 8;
                break;
            default:
                regs.set(Reg.values()[address - REGS_LCDC_START],
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
        if (regs.testBit(Reg.LCDC, LCDCBit.LCD_STATUS)) {
            if (nextNonIdleCycle == Long.MAX_VALUE) {
                lcdOnCycle = cycle;
                nextNonIdleCycle = cycle;
                setMode(2);
                imagesDrawn = 0;
                regs.set(Reg.LY, 0);
                checkIfLYEqualsLYC();
                reallyCycle(cycle);
            }

            if (cycle >= nextNonIdleCycle) {
                switch (getMode()) {
                case 0:
                    if (regs.get(Reg.LY) == LCD_HEIGHT) {
                        setMode(1);
                    } else if (regs.get(Reg.LY) < LCD_HEIGHT) {
                        setMode(2);
                    }
                    break;
                case 1:
                    if (regs.get(Reg.LY) == LCD_HEIGHT + 9) {
                        firstLineDrawn = false;
                        ++imagesDrawn;
                        regs.set(Reg.LY, 0);
                        checkIfLYEqualsLYC();
                        setMode(2);
                        winY = 0;
                    }
                    break;
                case 2:
                    setMode(3);
                    break;
                case 3:
                    setMode(0);
                    break;
                }
                reallyCycle(cycle);
            }
        }

        if (oamCopy) {
            if (octetsCopiedToOam >= 160) {
                oamCopy = false;
            } else {
                objectAttributeMemory.write(octetsCopiedToOam,
                        bus.read(addressToCopy | octetsCopiedToOam));
                ++octetsCopiedToOam;
            }
        }
    }

    private void reallyCycle(long cycle) {
        switch (getMode()) {
        case 0:
            if (firstLineDrawn) {
                regs.set(Reg.LY, regs.get(Reg.LY) + 1);
            }
            checkIfLYEqualsLYC();
            nextNonIdleCycle = lcdOnCycle
                    + imagesDrawn * LINE_CYCLES * (LCD_HEIGHT + 10)
                    + (regs.get(Reg.LY) + 1) * LINE_CYCLES;
            break;
        case 1:
            regs.set(Reg.LY, regs.get(Reg.LY) + 1);
            checkIfLYEqualsLYC();
            if (regs.get(Reg.LY) == LCD_HEIGHT + 9) {
                currentImage = nextImageBuilder.build();
                nextImageBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);
            }
            nextNonIdleCycle = lcdOnCycle
                    + imagesDrawn * LINE_CYCLES * (LCD_HEIGHT + 10)
                    + (regs.get(Reg.LY) + 1) * LINE_CYCLES;
            break;
        case 2:
            nextNonIdleCycle = lcdOnCycle
                    + imagesDrawn * LINE_CYCLES * (LCD_HEIGHT + 10)
                    + regs.get(Reg.LY) * LINE_CYCLES + 20;
            break;
        case 3:
            nextImageBuilder.setLine(computeLine(regs.get(Reg.LY)),
                    regs.get(Reg.LY));
            nextNonIdleCycle = lcdOnCycle
                    + imagesDrawn * LINE_CYCLES * (LCD_HEIGHT + 10)
                    + regs.get(Reg.LY) * LINE_CYCLES + 20 + 43;
            firstLineDrawn = true;
            break;
        }
    }

    /// Manages the current mode of the LCD controller

    private void setMode(int mode) {
        int statValue = regs.get(Reg.STAT);
        int previousMode = clip(2, statValue);
        regs.set(Reg.STAT, set(set(statValue, 0, test(mode, 0)),
                1, test(mode, 1)));
        if (previousMode != 1 && mode == 1) {
            cpu.requestInterrupt(Interrupt.VBLANK);
        }
        if (mode != 3) {
            if (checkStatBit(mode + 3)) {
                cpu.requestInterrupt(Interrupt.LCD_STAT);
            }
        }
    }

    private int getMode() {
        int statValue = regs.get(Reg.STAT);
        return (test(statValue, STATBit.MODE1) ? 1 << 1 : 0)
                | (test(statValue, STATBit.MODE0) ? 1 : 0);
    }

    /// Manages the Bits in Stat

    private void checkIfLYEqualsLYC() {
        int statValue = regs.get(Reg.STAT);
        boolean equal = regs.get(Reg.LYC) == regs.get(Reg.LY);
        regs.set(Reg.STAT,
                set(statValue, STATBit.LYC_EQ_LY.index(), equal));
        if (equal && test(statValue, STATBit.INT_LYC)) {
            cpu.requestInterrupt(Interrupt.LCD_STAT);
        }
    }

    private boolean checkStatBit(int index) {
        return test(regs.get(Reg.STAT), index);
    }

    /**
     * Returns the last image drawn by the LcdController
     * 
     * @return currentImage the last LcdImage drawn by the LcdController
     */
    public LcdImage currentImage() {
        return currentImage;
    }

    /**
     * Computes a line of the LcdController's image
     * 
     * @param line,
     *            the line to compute
     * @return an LcdImageLine containing the background, the window and both
     *         the background and foreground sprites
     */
    private LcdImageLine computeLine(int line) {
        LcdImageLine bgLine = LcdImageLine.ZERO_OF_SIZE_160;
        if (regs.testBit(Reg.LCDC, LCDCBit.BG)) {
            bgLine = buildBgLine(
                    Math.floorMod(line + regs.get(Reg.SCY), BG_SIZE));
        }

        LcdImageLine bgAndWindow;

        if (regs.testBit(Reg.LCDC, LCDCBit.WIN) && line >= regs.get(Reg.WY)
                && (regs.get(Reg.WX) < 167 && regs.get(Reg.WX) >= 6)) {
            LcdImageLine windowLine = buildWindowLine();
            bgAndWindow = bgLine.join(windowLine,
                    Math.max(0, regs.get(Reg.WX) - 8));
        } else {
            bgAndWindow = bgLine;
        }

        LcdImageLine behindSpritesLine = LcdImageLine.ZERO_OF_SIZE_160;
        LcdImageLine foregroundSpritesLine = LcdImageLine.ZERO_OF_SIZE_160;

        if (regs.testBit(Reg.LCDC, LCDCBit.OBJ)) {
            int[] tab = spritesIntersectingLine(line);
            LcdImageLine[] temp = buildSpritesLines(tab, line);
            foregroundSpritesLine = temp[1];
            behindSpritesLine = temp[0];
        }

        LcdImageLine imageAndBehindSprites = bgAndWindow
                .below(behindSpritesLine.below(bgAndWindow));

        return imageAndBehindSprites.below(foregroundSpritesLine);
    }

    private LcdImageLine buildBgLine(int line) {
        return buildLine(line, true)
                .extractWrapped(regs.get(Reg.SCX), LCD_WIDTH)
                .mapColors(regs.get(Reg.BGP));
    }

    private LcdImageLine buildWindowLine() {
        LcdImageLine res = buildLine(winY, false).shift(regs.get(Reg.WX) - 7)
                .mapColors(regs.get(Reg.BGP));
        winY++;
        return res;
    }

    private LcdImageLine buildLine(int line, boolean background) {
        int size = background ? BG_SIZE : LCD_WIDTH;
        LcdImageLine.Builder lineBuilder = new LcdImageLine.Builder(size);
        boolean plage = background ? regs.testBit(Reg.LCDC, LCDCBit.BG_AREA)
                : regs.testBit(Reg.LCDC, LCDCBit.WIN_AREA);
        for (int i = 0; i < size / 8; ++i) {
            int tileIndex = read(BG_DISPLAY_DATA[plage ? 1 : 0]
                    + Math.floorDiv(line, 8) * 32 + i);
            int tileAddress = 0;
            if (tileIndex > 0x7F) {
                tileAddress = TILE_SOURCE[0] + 16 * (tileIndex - 0x80);
            } else {
                if (regs.testBit(Reg.LCDC, LCDCBit.TILE_SOURCE)) {
                    tileAddress = TILE_SOURCE[1] + 16 * tileIndex;
                } else {
                    tileAddress = 0x9000 + 16 * tileIndex;
                }
            }
            int lsbBg = read(tileAddress + Math.floorMod(line, 8) * 2);
            int msbBg = read(tileAddress + Math.floorMod(line, 8) * 2 + 1);

            lineBuilder.setBytes(i, reverse8(msbBg), reverse8(lsbBg));
        }
        return lineBuilder.build();

    }

    private int[] spritesIntersectingLine(int line) {
        int[] res = { 0b1 << 30, 0b1 << 30, 0b1 << 30, 0b1 << 30, 0b1 << 30,
                0b1 << 30, 0b1 << 30, 0b1 << 30, 0b1 << 30, 0b1 << 30, 0 };
        int spriteSizeAdjustement = regs.testBit(Reg.LCDC, LCDCBit.OBJ_SIZE) ? 0
                : -8;
        int filled = 0;
        int index = 0;
        while (filled < 10 && index < 40) {
            int y = spriteGetY(index);
            if (line >= y - 16 && line < y + spriteSizeAdjustement) {
                res[filled] = (spriteGetX(index) << 16) | (index << 8) | y;
                ++filled;
            }
            ++index;
        }
        res[10] = filled;

        sort(res, 0, filled);

        return res;
    }

    private LcdImageLine[] buildSpritesLines(int[] tab, int line) {

        LcdImageLine behindBgLine = LcdImageLine.ZERO_OF_SIZE_160;
        LcdImageLine foregroundLine = LcdImageLine.ZERO_OF_SIZE_160;

        int filled = tab[10];

        for (int i = 0; i < filled; ++i) {
            int xindexy = tab[i];
            int index = extract(xindexy, 8, 8);
            if (spriteIsBehindBg(index)) {
                behindBgLine = buildSpriteLine(xindexy, line)
                        .below(behindBgLine);
            } else {
                foregroundLine = buildSpriteLine(xindexy, line)
                        .below(foregroundLine);
            }
        }
        return new LcdImageLine[] { behindBgLine, foregroundLine };
    }

    private LcdImageLine buildSpriteLine(int xindexy, int line) {
        int index = extract(xindexy, 8, 8);
        int yAbsolute = extract(xindexy, 0, 8) - 16;
        boolean isHFlipped = spriteIsHFlipped(index);
        boolean isVFlipped = spriteIsVFlipped(index);
        int tileAddress = spriteGetTileAddress(index);
        int relativeAddress = 0;

        if (isVFlipped) {
            if (regs.testBit(Reg.LCDC, LCDCBit.OBJ_SIZE)) {
                relativeAddress = 2 * (15 - line + yAbsolute);
            } else {
                relativeAddress = 2 * (7 - line + yAbsolute);
            }
        } else {
            relativeAddress = 2 * (line - yAbsolute);
        }

        LcdImageLine.Builder res = new LcdImageLine.Builder(LCD_WIDTH);

        int msb = isHFlipped ? read(tileAddress + relativeAddress + 1)
                : reverse8(read(tileAddress + relativeAddress + 1));

        int lsb = isHFlipped ? read(tileAddress + relativeAddress)
                : reverse8(read(tileAddress + relativeAddress));

        return (res.setBytes(0, msb, lsb).build()
                .shift(extract(xindexy, 16, 8) - 8))
                        .mapColors(spriteGetPalette(index));

    }

    private enum SpriteBit implements Bit {

        PALETTE, FLIP_H, FLIP_V, BEHIND_BG;

        @Override
        public int index() {
            return this.ordinal() + 4;
        }
    }

    private int spriteGetY(int index) {
        return objectAttributeMemory.read(4 * index);
    }

    private int spriteGetX(int index) {
        return objectAttributeMemory.read((4 * index) + 1);
    }

    private int spriteGetTileAddress(int index) {
        if (regs.testBit(Reg.LCDC, LCDCBit.OBJ_SIZE))
            return 0x8000 + 16 * 
                    set(objectAttributeMemory.read((4 * index) + 2), 0, false);
        return 0x8000 + 16 * objectAttributeMemory.read((4 * index) + 2);
    }

    private boolean spriteIsVFlipped(int index) {
        return test(objectAttributeMemory.read((4 * index) + 3),
                SpriteBit.FLIP_V);
    }

    private boolean spriteIsHFlipped(int index) {
        return test(objectAttributeMemory.read((4 * index) + 3),
                SpriteBit.FLIP_H);
    }

    private boolean spriteIsBehindBg(int index) {
        return test(objectAttributeMemory.read((4 * index) + 3),
                SpriteBit.BEHIND_BG);
    }

    private int spriteGetPalette(int index) {
        return test(objectAttributeMemory.read((4 * index) + 3),
                SpriteBit.PALETTE) ? regs.get(Reg.OBP1) : regs.get(Reg.OBP0);

    }
}
