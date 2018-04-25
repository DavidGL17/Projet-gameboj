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
    		BG,
    		OBJ,
    		OBJ_SIZE,
    		BG_AREA,
    		TILE_SOURCE,
    		WIN,
    		WIN_AREA,
    		LCD_STATUS
    }
    
    private enum STAT implements Bit {
    		MODE0,
    		MODE1,
    		LYC_EQ_LY,
    		INT_MODE0,
    		INT_MODE1,
    		INT_MODE2,
    		INT_LYC,
    		UNUSED
    }

    public final static int LCD_WIDTH = 160;
    public final static int LCD_HEIGHT = 140;
    public final static int LINE_CYCLES = 20+43+51;

    private final Cpu cpu;
    private LcdImage currentImage;
    private LcdImage.Builder nextImageBuilder = new Builder(LCD_WIDTH, LCD_HEIGHT);
    private LcdImageLine currentlyBuiltLine; //Pour passer du mode 0 au mode 2
    private final Ram videoRam;
    private final RegisterFile<Reg> regs = new RegisterFile<>(Reg.values());
    private long lcdOnCycle;
    private long nextNonIdleCycle;

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
                    setMode(0);
                    nextNonIdleCycle = Long.MAX_VALUE;
                }
                regs.set(Reg.LCDC, data);
                break;
            case 0xFF41:
                regs.set(Reg.STAT, data & 0xF8 | regs.get(Reg.STAT) & 0x07);
                break;
            case 0xFF44:
                regs.set(Reg.LY, data);
                checkIfLYEqualsLYC();
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
    	//Detecte si le cycle doit être vide pour permettre que l'émulation soit correcte
    	//Effectue le changement de mode adéquat	: cela permet de rendre la méthode
    	//reallyCycle très "impérative", diminuer le nombre de branchements conditionnels/check
    		int drawnImages=(int) ((cycle-lcdOnCycle)/LINE_CYCLES/(LCD_HEIGHT+10));
		int drawnLines =(int) ((cycle-lcdOnCycle)/LINE_CYCLES%(LCD_HEIGHT+10));
    		if (regs.testBit(Reg.LCDC,LCDCBit.LCD_STATUS)) {
    			
	    		if (nextNonIdleCycle==Long.MAX_VALUE) {
	    			lcdOnCycle=cycle;
	    			nextNonIdleCycle=cycle;
	    			reallyCycle(cycle);
	    		}
	        
	    		if (cycle>=nextNonIdleCycle) {
	    			switch (getMode()) {
	    			case 0:
	    				setMode(2);
	    				break;
	    			case 1:
	    				if (cycle >= drawnImages*LINE_CYCLES*(LCD_HEIGHT+10) + drawnLines*LINE_CYCLES) {
	    					setMode(0);
	    				} else {
	    					new IllegalArgumentException("error in computation of drawnLines/drawnImages"); // Provisoire
	    				}
	    				break;
	    			case 2: 
	    				setMode(3);
	    				break;
	    			case 3: 
	    				if (drawnLines==LCD_HEIGHT) { //if image is complete
	    					setMode(1);
	    				} else if (drawnLines<LCD_HEIGHT) {
	    					setMode(0);
	    				} else {
	    					new IllegalArgumentException("error in computation of drawnLines"); // Provisoire
	    				}
	    				break;
	    			}
	    			reallyCycle(cycle);
	    		}
    		} else {
    			setMode(0);
    			regs.set(Reg.LY,0);
    		}

    }
    
    public void reallyCycle(long cycle) {
    		
    		int drawnImages=(int) ((cycle-lcdOnCycle)/LINE_CYCLES/(LCD_HEIGHT+10));
    		int drawnLines =(int) ((cycle-lcdOnCycle)/LINE_CYCLES%(LCD_HEIGHT+10));
    		switch (getMode()){
    		case 0 :
    			//mode 0
//    			Accès mémoire tuiles de backGround et construction ligne
//    			currentlyBuiltLine=
    			
//    			nextNonIdleCycle=(drawnImages)*LINE_CYCLES*(LCD_HEIGHT+10)+drawnLines*LINE_CYCLES+20;
    			
    			break;
    		case 1:
    			currentImage=nextImageBuilder.build();
    			nextImageBuilder= new LcdImage.Builder(LCD_WIDTH,LCD_HEIGHT);
    			nextNonIdleCycle=(drawnImages+1)*LINE_CYCLES*(LCD_HEIGHT+10);
    			break;
    		case 2:
    			//mode 2
//    			Accès mémoire tuiles de sprite/background
//    			LcdImageLine spriteLine=
//    			currentlyBuiltLine = currentlyBuiltLine.below(spriteLine);
//    			nextNonIdleCycle=(drawnImages)*LINE_CYCLES*(LCD_HEIGHT+10)+drawnLines*LINE_CYCLES+20+43;
    			break;
    		case 3 :
    			//mode 3
//    			Ajouter la ligne batie au builderImage
//    			Réinitialiser le "Builder" de ligne
//    			nextImageBuilder.setLine(drawnLines,currentlyBuiltLine);
//    			nextNonIdleCycle=(drawnImages)*LINE_CYCLES*(LCD_HEIGHT+10)+(drawnLines+1)*LINE_CYCLES;
    			
    			break;
    		};
    		//TODO
    }

    // todo
    public LcdImage currentImage() {
        return currentImage;
    }

    /// Manages the current mode of the LCD controller

    private void setMode(int mode) {
    	// if /Bits.clip(2,regs.get(Reg.STAT)!=mod) {  
    	// Non ? Si le mode ne change pas, pas de traitement spécifique en particulier
    	// pas de levée systématique de l'interruption pour mode1
        int statValue = regs.get(Reg.STAT);
        regs.set(Reg.STAT, Bits.set(Bits.set(statValue, 0, Bits.test(mode, 0)),
                1, Bits.test(mode, 1)));
        if (mode != 3) {
            if (checkStatBit(mode + 3)) {
                cpu.requestInterrupt(Interrupt.LCD_STAT);
            }
        }
        if (getMode() != 1 && mode == 1) {
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
    
    ///Checks 
}
