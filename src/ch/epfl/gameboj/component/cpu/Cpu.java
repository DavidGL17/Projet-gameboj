package ch.epfl.gameboj.component.cpu;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Alu.Flag;
import ch.epfl.gameboj.component.cpu.Alu.RotDir;
import ch.epfl.gameboj.component.memory.Ram;

/**
 * 
 * @author David Gonzalez leon (270845)
 * @author Melvin Malonga-Matouba (288405)
 *
 */
public final class Cpu implements Component, Clocked {

    /**
     * Represents the 8 bit registers of the cpu
     */
    private enum Reg implements Register {
        A, F, B, C, D, E, H, L
    }

    /**
     * Represents the 16 bit registers of the cpu
     */
    private enum Reg16 implements Register {
        AF, BC, DE, HL
    }

    /**
     * Represents the different type of Interruption
     */
    public enum Interrupt implements Bit {
        VBLANK, LCD_STAT, TIMER, SERIAL, JOYPAD
    }

    private Bus bus;
    private final Ram highRam = new Ram(AddressMap.HIGH_RAM_SIZE);
    private final RegisterFile<Reg> regs = new RegisterFile<>(Reg.values());
    private int registerPC = 0;
    private int registerSP = 0;
    private int registerIE = 0;
    private int registerIF = 0;
    private boolean IME = false;
    private long nextNonIdleCycle = 0;
    private final int PREFIX = 0xCB;
    private static final Opcode[] DIRECT_OPCODE_TABLE = buildOpcodeTable(
            Opcode.Kind.DIRECT);
    private static final Opcode[] PREFIXED_OPCODE_TABLE = buildOpcodeTable(
            Opcode.Kind.PREFIXED);

    
    /**
     * Builds a table of opcodes of the specified kind
     */
    private static Opcode[] buildOpcodeTable(Opcode.Kind kind) {
        Opcode[] table = new Opcode[0x100];
        for (Opcode o : Opcode.values()) {
            if (o.kind == kind) {
                table[o.encoding] = o;
            }
        }
        return table;
    }

    /// Methods imposed by Component

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
        if (Preconditions.checkBits16(address) >= AddressMap.HIGH_RAM_START
                && address < AddressMap.HIGH_RAM_END) {
            return highRam.read(address - AddressMap.HIGH_RAM_START);
        } else if (address == AddressMap.REG_IE) {
            return registerIE;
        } else if (address == AddressMap.REG_IF) {
            return registerIF;
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
        if (Preconditions.checkBits16(address) >= AddressMap.HIGH_RAM_START
                && address < AddressMap.HIGH_RAM_END) {
            highRam.write(address - AddressMap.HIGH_RAM_START,
                    Preconditions.checkBits8(data));
        } else if (address == AddressMap.REG_IE) {
            registerIE = Bits.clip(5, Preconditions.checkBits8(data));
        } else if (address == AddressMap.REG_IF) {
            registerIF = Bits.clip(5, Preconditions.checkBits8(data));
        }
    }

    /// Functionality offered to other Components

    /**
     * Allows an Interruption to be raised
     * 
     * @param i
     *            - the interruption to raise
     */
    public void requestInterrupt(Interrupt i) {
        registerIF = registerIF | i.mask();
    }

    /// Visibility for test purposes

    /**
     * Allows the state of the Cpu to be known for test purposes
     * 
     * @return an array containing the values contained by each Register or pair
     *         of Registers
     */
    public int[] _testGetPcSpAFBCDEHL() {
        return new int[] { registerPC, registerSP, regs.get(Reg.A),
                regs.get(Reg.F), regs.get(Reg.B), regs.get(Reg.C),
                regs.get(Reg.D), regs.get(Reg.E), regs.get(Reg.H),
                regs.get(Reg.L) };
    }

    /// Method imposed by Clocked

    /**
     * Determines wether the gameboy should be functionning or waiting in order
     * to simulate a gameboy if the gameboy is in HALT, determines wether the
     * GameBoy should start functionning again
     * 
     * @param cycle
     *            - the cycle to execute
     */
    @Override
    public void cycle(long cycle) {
    	boolean noneRaisedAndActive = false; //Only if no Interrupt is raisedAndActive
        if (nextNonIdleCycle == Long.MAX_VALUE) {
            int RaisedAndActive = registerIE & registerIF;
            int toManage = 31 - Integer.numberOfLeadingZeros(
                    Integer.lowestOneBit(RaisedAndActive));
            if ((toManage >= 0) && (toManage <= 4)) {
                nextNonIdleCycle = cycle; 
                interruptHandler(toManage);
            } else {
            	noneRaisedAndActive=true;
                return;
            }

        }
        if (cycle >= nextNonIdleCycle) {
            reallyCycle(cycle,noneRaisedAndActive);
        }
    }

    /**
     * Performs a cycle of GameBoy
     * 
     * @param cycle
     *            - the cycle to execute
     */
    private void reallyCycle(long cycle, boolean noneRaisedAndActive) {
        if (IME&&!noneRaisedAndActive) {
            int RaisedAndActive = registerIE & registerIF;
            int toManage = 31 - Integer.numberOfLeadingZeros(
                    Integer.lowestOneBit(RaisedAndActive));
            if ((toManage >= 0) && (toManage <= 4)) {
                interruptHandler(toManage);
                nextNonIdleCycle = cycle + 5;  
                return;
            }
        }
        if (read8(registerPC) == PREFIX) {
            dispatch(PREFIXED_OPCODE_TABLE[read8AfterOpcode()], cycle);
        } else {
            dispatch(DIRECT_OPCODE_TABLE[read8(registerPC)], cycle);
        }
    }

    /**
     * Sets SP, PC, IME, and IF according to the management of interruption
     * toMangage
     * 
     * @param toManage
     *            the index of the interruption, possibly invalid
     */
    private void interruptHandler(int toManage) {
        if ((toManage >= 0) && (toManage <= 4) && IME) {
            IME = false;
            registerIF = Bits.set(registerIF, toManage, false);
            push16(registerPC);
            registerPC = AddressMap.INTERRUPTS[toManage];
            
        }
    }

    /**
     * Sets the nextNonIdleCycle according to the current cycle and the opcode
     * being executed
     * 
     * @param cycle
     *            - the cycle being executed
     * @param opcode
     */
    private void setNextNonIdleCycle(long cycle, Opcode opcode) {
        int additional = 0;
        Opcode.Family[] conditionals = { Opcode.Family.JP_CC_N16,
                Opcode.Family.JR_CC_E8, Opcode.Family.CALL_CC_N16,
                Opcode.Family.RET_CC };
        if (Arrays.asList(conditionals).contains(opcode.family)) {
            additional = (conditionalInstruction(opcode))
                    ? opcode.additionalCycles
                            : 0;
        }
        nextNonIdleCycle = cycle + opcode.cycles + additional;   
    }

    /**
     * Operates the action corresponding to the opcode and updates
     * nextNonIdleCycle accordingly
     * 
     * @param opcode
     *            - the opcode
     * @param cycle
     *            - the cycle being executed
     */
    private void dispatch(Opcode opcode, long cycle) {
    	
        int nextPC = Bits.clip(16, registerPC + opcode.totalBytes);
        setNextNonIdleCycle(cycle, opcode);
        switch (opcode.family) {
        case NOP: {
        }
        break;
        case LD_R8_HLR: {
            regs.set(extractReg(opcode, 3), read8AtHl());
        }
        break;
        case LD_A_HLRU: {
            regs.set(Reg.A, read8AtHl());
            setReg16(Reg16.HL, Bits.clip(16,
                    reg16(Reg16.HL) + extractHlIncrement(opcode)));
        }
        break;
        case LD_A_N8R: {
            regs.set(Reg.A, read8(AddressMap.REGS_START + read8AfterOpcode()));
        }
        break;
        case LD_A_CR: {
            regs.set(Reg.A, read8(AddressMap.REGS_START + regs.get(Reg.C)));
        }
        break;
        case LD_A_N16R: {
            regs.set(Reg.A, read8(read16AfterOpcode()));
        }
        break;
        case LD_A_BCR: {
            regs.set(Reg.A, read8(reg16(Reg16.BC)));
        }
        break;
        case LD_A_DER: {
            regs.set(Reg.A, read8(reg16(Reg16.DE)));
        }
        break;
        case LD_R8_N8: {
            regs.set(extractReg(opcode, 3), read8AfterOpcode());
        }
        break;
        case LD_R16SP_N16: {
            setReg16SP(extractReg16(opcode), read16AfterOpcode());
        }
        break;
        case POP_R16: {
            setReg16(extractReg16(opcode), pop16());
        }
        break;
        case LD_HLR_R8: {
            write8AtHl(regs.get(extractReg(opcode, 0)));
        }
        break;

        case LD_HLRU_A: {
            write8AtHl(regs.get(Reg.A));
            setReg16(Reg16.HL, (reg16(Reg16.HL) + extractHlIncrement(opcode)));
        }
        break;
        case LD_N8R_A: {
            write8(AddressMap.REGS_START + read8AfterOpcode(), regs.get(Reg.A));
        }
        break;
        case LD_CR_A: {
            write8(AddressMap.REGS_START + regs.get(Reg.C), regs.get(Reg.A));
        }
        break;
        case LD_N16R_A: {
            int destination = read16AfterOpcode();
            int value = regs.get(Reg.A);
            write8(destination, value);
        }
        break;
        case LD_BCR_A: {
            int destination = reg16(Reg16.BC);
            int value = regs.get(Reg.A);
            write8(destination, value);
        }
        break;
        case LD_DER_A: {
            int destination = reg16(Reg16.DE);
            int value = regs.get(Reg.A);
            write8(destination, value);
        }
        break;
        case LD_HLR_N8: {
            int value = read8AfterOpcode();
            write8AtHl(value);
        }
        break;
        case LD_N16R_SP: {
            int argument = read16AfterOpcode();
            write16(argument, registerSP);
        }
        break;
        case LD_R8_R8: {
            Reg store = extractReg(opcode, 0);
            Reg destination = extractReg(opcode, 3);
            int value = regs.get(store);
            regs.set(destination, value);
        }
        break;
        case LD_SP_HL: {
            registerSP = reg16(Reg16.HL);
        }
        break;
        case PUSH_R16: {
            Reg16 reg = extractReg16(opcode);
            push16(reg16(reg));
        }
        break;

        // Add
        case ADD_A_R8: {
            boolean c = extractCarry(opcode);
            setRegFlags(
            		Reg.A,
                    Alu.add(regs.get(Reg.A), regs.get(extractReg(opcode, 0)),
                            c));
        }
        break;
        case ADD_A_N8: {
            boolean c = extractCarry(opcode);
            setRegFlags(
            		Reg.A,
            		Alu.add(regs.get(Reg.A), read8AfterOpcode(), c));
        }
        break;
        case ADD_A_HLR: {
            boolean c = extractCarry(opcode);
            setRegFlags(
            		Reg.A,
            		Alu.add(regs.get(Reg.A), read8AtHl(), c));
        }
        break;
        case INC_R8: {
            combineAluFlags(Alu.add(regs.get(extractReg(opcode, 3)), 1),
                    FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.CPU);
            regs.set(extractReg(opcode, 3),
                    Bits.clip(8, regs.get(extractReg(opcode, 3)) + 1));
        }
        break;
        case INC_HLR: {
            combineAluFlags(Alu.add(read8AtHl(), 1), FlagSrc.ALU, FlagSrc.V0,
                    FlagSrc.ALU, FlagSrc.CPU);
            write8AtHl(Bits.clip(8, read8AtHl() + 1));
        }
        break;
        case INC_R16SP: {
            setReg16SP(extractReg16(opcode),
                    Bits.clip(16, reg16SP(extractReg16(opcode)) + 1));
        }
        break;
        case ADD_HL_R16SP: {
            combineAluFlags(
                    Alu.add16H(reg16(Reg16.HL), reg16SP(extractReg16(opcode))),
                    FlagSrc.CPU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);
            setReg16(Reg16.HL, Bits.clip(16,
                    reg16(Reg16.HL) + reg16SP(extractReg16(opcode))));
        }
        break;
        case LD_HLSP_S8: {
            int s8 = read8AfterOpcode();
            boolean negativeNumber = false;
            if (Bits.test(s8, 7)) {
                negativeNumber = true;
                s8 = Bits.complement8(s8 - 1);
            }
            if (Bits.test(opcode.encoding, 4)) {
                if (negativeNumber) {
                    combineAluFlags(Alu.add16L(registerSP, Bits.clip(8, -s8)),
                            FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);
                    setReg16(Reg16.HL, Bits.clip(16, registerSP - s8));
                } else {
                    combineAluFlags(Alu.add16L(registerSP, s8), FlagSrc.V0,
                            FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);
                    setReg16(Reg16.HL, Bits.clip(16, registerSP + s8));
                }
            } else {
                if (negativeNumber) {
                    combineAluFlags(Alu.add16L(registerSP, Bits.clip(8, -s8)),
                            FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);
                    registerSP = Bits.clip(16, registerSP - s8);
                } else {
                    combineAluFlags(Alu.add16L(registerSP, s8), FlagSrc.V0,
                            FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);
                    registerSP = Bits.clip(16, registerSP + s8);
                }
            }
        }
        break;

        // Subtract
        case SUB_A_R8: {
            boolean c = extractCarry(opcode);
            setRegFlags(
            		Reg.A,
                    Alu.sub(regs.get(Reg.A), regs.get(extractReg(opcode, 0)),
                            c))
                  ;
        }
        break;
        case SUB_A_N8: {
            boolean c = extractCarry(opcode);
            setRegFlags( Reg.A,
            		Alu.sub(regs.get(Reg.A), read8AfterOpcode(),
            				c));
        }
        break;
        case SUB_A_HLR: {
            boolean c = extractCarry(opcode);
            setRegFlags(Reg.A,
            		Alu.sub(regs.get(Reg.A), read8AtHl(), c));
        }
        break;
        case DEC_R8: {
            combineAluFlags(Alu.sub(regs.get(extractReg(opcode, 3)), 1),
                    FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU, FlagSrc.CPU);
            regs.set(extractReg(opcode, 3),
                    Bits.clip(8, regs.get(extractReg(opcode, 3)) - 1));
        }
        break;
        case DEC_HLR: {
            combineAluFlags(Alu.sub(read8AtHl(), 1), FlagSrc.ALU, FlagSrc.V1,
                    FlagSrc.ALU, FlagSrc.CPU);
            write8AtHl(Bits.clip(8, read8AtHl() - 1));
        }
        break;
        case CP_A_R8: {
            combineAluFlags(
                    Alu.sub(regs.get(Reg.A), regs.get(extractReg(opcode, 0))),
                    FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU, FlagSrc.ALU);
        }
        break;
        case CP_A_N8: {
            combineAluFlags(Alu.sub(regs.get(Reg.A), read8AfterOpcode()),
                    FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU, FlagSrc.ALU);
        }
        break;
        case CP_A_HLR: {
            combineAluFlags(Alu.sub(regs.get(Reg.A), read8AtHl()), FlagSrc.ALU,
                    FlagSrc.V1, FlagSrc.ALU, FlagSrc.ALU);
        }
        break;
        case DEC_R16SP: {
            setReg16SP(extractReg16(opcode),
                    Bits.clip(16, reg16SP(extractReg16(opcode)) - 1));
        }
        break;

        // And, or, xor, complement
        case AND_A_N8: {
            setRegFlags(
            		Reg.A,
            		Alu.and(regs.get(Reg.A), read8AfterOpcode()));
        }
        break;
        case AND_A_R8: {
            setRegFlags(
            		Reg.A,
                    Alu.and(regs.get(Reg.A), regs.get(extractReg(opcode, 0))));
        }
        break;
        case AND_A_HLR: {
            setRegFlags(
            		Reg.A,
            		Alu.and(regs.get(Reg.A), read8AtHl()));
        }
        break;
        case OR_A_R8: {
            setRegFlags(
            		Reg.A,
                    Alu.or(regs.get(Reg.A), regs.get(extractReg(opcode, 0))));
        }
        break;
        case OR_A_N8: {
            setRegFlags(
            		Reg.A,
            		Alu.or(regs.get(Reg.A), read8AfterOpcode()));
        }
        break;
        case OR_A_HLR: {
            setRegFlags(
            		Reg.A,
            		Alu.or(regs.get(Reg.A), read8AtHl()));
        }
        break;

        case XOR_A_R8: {
            setRegFlags(
            		Reg.A,
            		Alu.xor(regs.get(Reg.A), regs.get(extractReg(opcode, 0))));
        }
        break;
        case XOR_A_N8: {
            int vf = Alu.xor(regs.get(Reg.A), read8AfterOpcode());
            regs.set(Reg.A, Bits.clip(8, Alu.unpackValue(vf)));
            combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0,
                    FlagSrc.V0);
        }
        break;
        case XOR_A_HLR: {
            int vf = Alu.xor(regs.get(Reg.A), read8AtHl());
            regs.set(Reg.A, Bits.clip(8, Alu.unpackValue(vf)));
            combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0,
                    FlagSrc.V0); /// Sometimes sets N and H !!!!!
        }
        break;
        case CPL: {
            regs.set(Reg.A, Bits.complement8(regs.get(Reg.A)));
            combineAluFlags(0, FlagSrc.CPU, FlagSrc.V1, FlagSrc.V1,
                    FlagSrc.CPU);

        }
        break;

        // Rotate, shift
        case ROTCA: {
            int vf = Alu.rotate(extractRotDir(opcode), regs.get(Reg.A));
            setRegFromAlu(Reg.A, vf);
            combineAluFlags(vf, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0,
                    FlagSrc.ALU);
        }
        break;
        case ROTA: {
            int vf = Alu.rotate(extractRotDir(opcode), regs.get(Reg.A),
                    Bits.test(regs.get(Reg.F), Flag.C));
            setRegFromAlu(Reg.A, vf);
            combineAluFlags(vf, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0,
                    FlagSrc.ALU);
        }
        break;
        case ROTC_R8: {
            Reg reg = extractReg(opcode, 0);
            int vf = Alu.rotate(extractRotDir(opcode), regs.get(reg));
            setRegFromAlu(reg, vf);
            combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0,
                    FlagSrc.ALU);
        }
        break;
        case ROT_R8: {
            Reg reg = extractReg(opcode, 0);
            int vf = Alu.rotate(extractRotDir(opcode), regs.get(reg),
                    Bits.test(regs.get(Reg.F), Flag.C));
            setRegFromAlu(reg, vf);
            combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0,
                    FlagSrc.ALU);
        }
        break;
        case ROTC_HLR: {
            int vf = Alu.rotate(extractRotDir(opcode), read8AtHl());
            write8AtHl(Bits.clip(8, Alu.unpackValue(vf)));
            combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0,
                    FlagSrc.ALU);
        }
        break;
        case ROT_HLR: {
            int vf = Alu.rotate(extractRotDir(opcode), read8AtHl(),
                    Bits.test(regs.get(Reg.F), Flag.C));
            write8AtHl(Bits.clip(8, Alu.unpackValue(vf)));
            combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0,
                    FlagSrc.ALU);
        }
        break;
        case SWAP_R8: {
            Reg reg = extractReg(opcode, 0);
            int vf = Alu.swap(regs.get(reg));
            setRegFlags(reg, vf);
        }
        break;
        case SWAP_HLR: {
            int value = read8AtHl();
            int vf = Alu.swap(value);
            write8AtHl(Alu.unpackValue(vf));
            setFlags(vf);
        }
        break;
        case SLA_R8: {
            Reg reg = extractReg(opcode, 0);
            int vf = Alu.shiftLeft(regs.get(reg));
            setRegFlags(reg, vf);
        }
        break;
        case SRA_R8: {
            Reg reg = extractReg(opcode, 0);
            int vf = Alu.shiftRightA(regs.get(reg));
            setRegFlags(reg, vf);
        }
        break;
        case SRL_R8: {
            Reg reg = extractReg(opcode, 0);
            int vf = Alu.shiftRightL(regs.get(reg));
            setRegFlags(reg, vf);
        }
        break;
        case SLA_HLR: {
            int value = read8AtHl();
            int vf = Alu.shiftLeft(value);
            write8AtHl(Alu.unpackValue(vf));
            setFlags(vf);
        }
        break;
        case SRA_HLR: {
            int value = read8AtHl();
            int vf = Alu.shiftRightA(value);
            write8AtHl(Bits.clip(8, Alu.unpackValue(vf)));
            setFlags(vf);
        }
        break;
        case SRL_HLR: {
            int value = read8AtHl();
            int vf = Alu.shiftRightL(value);
            write8AtHl(Bits.clip(8, Alu.unpackValue(vf)));
            setFlags(vf);
        }
        break;

        // Bit test and set
        case BIT_U3_R8: {
            int value = Bits.extract(opcode.encoding, 3, 3);
            Reg reg = extractReg(opcode, 0);
            combineAluFlags(Alu.testBit(regs.get(reg), value), FlagSrc.ALU,
                    FlagSrc.ALU, FlagSrc.ALU, FlagSrc.CPU);
        }
        break;
        case BIT_U3_HLR: {
            int value = Bits.extract(opcode.encoding, 3, 3);
            combineAluFlags(Alu.testBit(read8AtHl(), value), FlagSrc.ALU,
                    FlagSrc.ALU, FlagSrc.ALU, FlagSrc.CPU);
        }
        break;
        case CHG_U3_R8: {
            int value = Bits.extract(opcode.encoding, 3, 3);
            Reg reg = extractReg(opcode, 0);
            regs.set(reg, Bits.set(regs.get(reg), value, extractSet(opcode)));
        }
        break;
        case CHG_U3_HLR: {
            int value = Bits.extract(opcode.encoding, 3, 3);
            write8AtHl(Bits.set(read8AtHl(), value, extractSet(opcode)));
        }
        break;

        // Misc. ALU
        case DAA: {
        	 setRegFlags(
             		Reg.A,
             		Alu.bcdAdjust(
             				regs.get(Reg.A),
             				Bits.test(regs.get(Reg.F), Flag.N),
             				Bits.test(regs.get(Reg.F),Flag.H),
             				Bits.test(regs.get(Reg.F), Flag.C)));
//            combineAluFlags(vf, FlagSrc.ALU, FlagSrc.CPU, FlagSrc.V0,
//                    FlagSrc.ALU);
        }
        break;
        case SCCF: {
            int res = 0;
            if (Bits.test(opcode.encoding, 3)) {
                res += (Bits.test(regs.get(Reg.F), Flag.C)) ? 0
                        : Flag.C.mask();
            } else {
                res += Flag.C.mask();

            }
            res += (Bits.test(regs.get(Reg.F), Flag.Z)) ? Flag.Z.mask() : 0;
            setFlags(res);
        }
        break;

        // Jumps
        case JP_HL: {
            nextPC = reg16(Reg16.HL);
        }
        break;
        case JP_N16: {
            nextPC = read16AfterOpcode();
        }
        break;
        case JP_CC_N16: {
            if (conditionalInstruction(opcode)) {
                nextPC = read16AfterOpcode();
            }
        }
        break;
        case JR_E8: {
            int s8 = read8AfterOpcode();
            boolean negativeNumber = false;
            if (Bits.test(s8, 7)) {
                negativeNumber = true;
                s8 = Bits.complement8(s8 - 1);
            }
            if (negativeNumber) {
                nextPC = Bits.clip(16, nextPC - s8);
            } else {
                nextPC = Bits.clip(16, nextPC + s8);
            }
        }
        break;
        case JR_CC_E8: {
            if (conditionalInstruction(opcode)) {
                int s8 = read8AfterOpcode();
                boolean negativeNumber = false;
                if (Bits.test(s8, 7)) {
                    negativeNumber = true;
                    s8 = Bits.complement8(s8 - 1);
                }
                if (negativeNumber) {
                    nextPC = Bits.clip(16, nextPC - s8);
                } else {
                    nextPC = Bits.clip(16, nextPC + s8);
                }
            }
        }
        break;

        // Calls and returns
        case CALL_N16: {
            push16(Bits.clip(16, nextPC));
            nextPC = read16AfterOpcode();
        }
        break;
        case CALL_CC_N16: {
            if (conditionalInstruction(opcode)) {
                push16(nextPC);
                nextPC = read16AfterOpcode();
            }
        }
        break;

        case RST_U3: {
            push16(registerPC + 1); // PC' = PC+1 ? (opcode)
            nextPC = (Bits.extract(opcode.encoding, 3, 3) * 8);
        }
        break;
        case RET: {
            nextPC = pop16();
        }
        break;
        case RET_CC: {
            if (conditionalInstruction(opcode)) {
                nextPC = pop16();
            }
        }
        break;

        // Interrupts
        case EDI: {
            IME = Bits.test(opcode.encoding, 3); // IME
        }
        break;
        case RETI: {
            IME = true;
            nextPC = pop16();
        }
        break;

        // Misc control
        case HALT: {
            nextNonIdleCycle = Long.MAX_VALUE;  
            nextPC = registerPC + 1;
        }
        break;
        case STOP:
            throw new Error("STOP is not implemented");
        }
        registerPC = nextPC;
    }

    /// Access to the Bus

    /**
     * Reads the byte at the given address on the bus
     * 
     * @param address
     *            a 16 bits integer
     * @return the stored 8-bits value
     */
    private int read8(int address) {
        return bus.read(Preconditions.checkBits16(address));
    }

    /**
     * Reads the byte at the address stored in HL on the bus
     * 
     * @return the stored 8-bits value
     */
    private int read8AtHl() {
        return read8(reg16(Reg16.HL));
    }

    /**
     * Reads the byte following the Opcode
     * 
     * @return the store 8-bits value
     */
    private int read8AfterOpcode() {
        return read8(Bits.clip(16, Preconditions.checkBits16(registerPC + 1)));
    }

    /**
     * Reads the 16 bits value stored at an address
     * 
     * @param address
     *            a 16-bits integer
     * @return the 16-bits value represented
     */
    private int read16(int address) {
        return Bits.make16(read8(Preconditions.checkBits16(address + 1)),
                read8(address));
    }

    /**
     * Reads the 16 bits-value stored after the Opcode
     * 
     * @return the 16-bits value represented
     */
    private int read16AfterOpcode() {
        return read16(registerPC + 1);
    }

    /**
     * Writes a 8-bit value at the desired address
     * 
     * @param address
     *            a 16-bits value
     * @param v
     *            the 8-bits value to represent
     */
    private void write8(int address, int v) {
        bus.write(Preconditions.checkBits16(address),
                Preconditions.checkBits8(v));
    }

    /**
     * Writes a 16-bits value at the desired address
     * 
     * @param address
     *            a 16-bits value
     * @param v
     *            the 16-bits value to represent
     */
    private void write16(int address, int v) {
        write8(Preconditions.checkBits16(address + 1),
                Bits.extract(Preconditions.checkBits16(v), 8, 8));
        write8(address, Bits.clip(8, v));
    }

    /**
     * Writes a 8-bits value at the address stored in HL
     * 
     * @param v
     *            the 8-bits value to represent
     */
    private void write8AtHl(int v) {
        write8(reg16(Reg16.HL), Preconditions.checkBits8(v));
    }

    // Management of the stack

    /**
     * Decreases SP by 2 and writes v at the address SP
     * 
     * @param v
     *            the 16-bits value to represent
     */
    private void push16(int v) {
        registerSP = Bits.clip(16, registerSP - 2);
        write16(registerSP, Preconditions.checkBits16(v));
    }

    /**
     * Reads what's stored at the address SP and increases SP by 2
     * 
     * @return the 16-bits value represented
     */
    private int pop16() {
        int sP = registerSP;
        registerSP = Bits.clip(16, registerSP + 2);
        return read16(sP);
    }

    /// Management of the sets of registers

    /**
     * Returns the value contained in the given register
     * 
     * @param r,
     *            the 16 bit register
     * @return the value contained in the 16 bit register
     * @throws IllegalArgumentException
     *             if the register is null
     */
    private int reg16(Reg16 r) {
        switch (r) {
        case AF:
            return Bits.make16(regs.get(Reg.A),regs.get(Reg.F));
        case BC:
            return Bits.make16(regs.get(Reg.B),regs.get(Reg.C));
        case DE:
            return Bits.make16(regs.get(Reg.D),regs.get(Reg.E));
        case HL:
        	return Bits.make16(regs.get(Reg.H),regs.get(Reg.L));
        default:
            return 0;
        }
    }

    /**
     * Returns the value contained in the given register. If the register is AF,
     * returns SP instead
     * 
     * @param r,
     *            the 16 bit register
     * @return the value contained in the 16 bit register
     * @throws IllegalArgumentException
     *             if the register is null
     */
    private int reg16SP(Reg16 r) {
        switch (r) {
        case AF:
            return registerSP;
        default:
            return reg16(r);
        }
    }

    /**
     * Sets the value of the given 16 bit register to the given value
     * 
     * @param r,
     *            the 16 bit register
     * @param newV,
     *            the new value
     * @throws IllegalArgumentException,
     *             if the value is not a 16 bit number or if the register is
     *             null
     */
    private void setReg16(Reg16 r, int newV) {
        switch (r) {
        case AF:
            regs.set(Reg.A, (newV & 0xFF00) >>> 8);
            regs.set(Reg.F, Bits.clip(8, newV) & 0xF0);
            break;
        case BC:
            regs.set(Reg.B, (newV & 0xFF00) >>> 8);
            regs.set(Reg.C, Bits.clip(8, newV));
            break;
        case DE:
            regs.set(Reg.D, (newV & 0xFF00) >>> 8);
            regs.set(Reg.E, Bits.clip(8, newV));
            break;
        case HL:
            regs.set(Reg.H, (newV & 0xFF00) >>> 8);
            regs.set(Reg.L, Bits.clip(8, newV));
            break;
        }
    }

    /**
     * Sets the value of the given 16 bit register to the given value. if the
     * given register is AF, sets the value of SP instead
     * 
     * @param r,
     *            the 16 bit register
     * @param newV,
     *            the new value
     * @throws IllegalArgumentException,
     *             if the value is not a 16 bit number or if the register is
     *             null
     */
    private void setReg16SP(Reg16 r, int newV) {
        switch (r) {
        case AF:
            registerSP = Preconditions.checkBits16(newV);
            break;
        default:
            setReg16(r, newV);
            break;
        }
    }

    /// Extraction of information from an opcode's encoding

    /**
     * Returns the length bit bits that identifies a register in the opcode
     * value
     * 
     * @param opcodeValue,
     *            the value of the opcode
     * @param startBit,
     *            the bite where we start looking for the bits
     * @param length,
     *            the number of bits that identifies the register
     * @return The bits identifying the register
     */
    private int getRegValue(int opcodeValue, int startBit, int length) {
        int value = 0;
        for (int i = 0; i < length; ++i) {
            if (Bits.test(opcodeValue, i + startBit)) {
                value += Bits.mask(i);
            }
        }
        return value;
    }

    /**
     * Extracts and returns the identitie of an 8 bit register in the encodig of
     * the given opcode at the given starting bit
     * 
     * @param opcode,
     *            the opocode's enconding we have to extract the value of
     * @param startBit,
     *            the bit we have to start extracting the value
     * @return the register that was encoded in the opcode's encoding
     */
    private Reg extractReg(Opcode opcode, int startBit) {
        switch (getRegValue(opcode.encoding, startBit, 3)) {
        case 0b000:
            return Reg.B;
        case 0b001:
            return Reg.C;
        case 0b010:
            return Reg.D;
        case 0b011:
            return Reg.E;
        case 0b100:
            return Reg.H;
        case 0b101:
            return Reg.L;
        case 0b111:
            return Reg.A;
        default:
            return null;
        }
    }

    /**
     * Extracts and returns the identitie of an 16 bit register in the encoding
     * of the given opcode at the fourth bit
     * 
     * @param opcode,
     *            the opocode's enconding we have to extract the value of
     * @return the register that was encoded in the opcode's encoding
     */
    private Reg16 extractReg16(Opcode opcode) {
        switch (getRegValue(opcode.encoding, 4, 2)) {
        case 0b00:
            return Reg16.BC;
        case 0b01:
            return Reg16.DE;
        case 0b10:
            return Reg16.HL;
        case 0b11:
            return Reg16.AF;
        default:
            return null;
        }
    }

    /**
     * Returns +1 or -1 depending on the fourth bit of the given opcode's
     * encoding
     * 
     * @param opcode,
     *            the opcode we have to analyse
     * @return +1 if the fourth bit is 0, -1 if the fourth bit is 1
     */
    private int extractHlIncrement(Opcode opcode) {
        return (Bits.test(opcode.encoding, 4) ? -1 : +1);
    }

    /**
     * Auxiliary method for ADD/ADC opcodes
     * 
     * @param opcode,
     *            the opcode
     * @return true if the opcode is an ADC_A_... and the fanion C in register F
     *         is activated, returns false otherwise
     */
    private boolean extractCarry(Opcode opcode) {
        if (Bits.test(opcode.encoding, 3)) {
            return (Bits.test(regs.get(Reg.F), 4));
        } else {
            return false;
        }
    }

    /**
     * AUXILIARY
     * 
     * @param opcode
     * @return
     */
    private RotDir extractRotDir(Opcode opcode) { // /!\ Always returns Right ?
        assert (opcode.encoding < 0x20);
        if (Bits.clip(4, opcode.encoding) < 0x08) {
            return RotDir.LEFT;
        } else {
            return RotDir.RIGHT;
        }
    }

    private boolean extractSet(Opcode opcode) {
        if (Bits.test(opcode.encoding, 6)) {
            return true;
        } else {
            return false;
        }

    }

    /// Management of interruptions

    /**
     * Checks if the condition described associated to an opcode is satisfied.
     * 
     * @param opcode
     *            - the opcode
     * @return wether the condition is satisfied
     */
    private boolean conditionalInstruction(Opcode opcode) {
        switch (Bits.extract(opcode.encoding, 3, 2)) {
        case 0b00:
            return !Bits.test(regs.get(Reg.F), 7);
        case 0b01:
            return Bits.test(regs.get(Reg.F), 7);
        case 0b10:
            return !Bits.test(regs.get(Reg.F), 4);
        case 0b11:
            return Bits.test(regs.get(Reg.F), 4);
        default:
            throw new IllegalArgumentException();
        }
    }

    /// Management of the Flags

    /*
     * Represents the different sources that may be used to determine the actual
     * value that should take Register F
     */
    private enum FlagSrc {
        V0, V1, ALU, CPU
    }

    /**
     * Loads in the register r the value from packed vf
     * 
     * @param r
     *            - the destination Register
     * @param vf
     *            - the packed value/flags
     */
    private void setRegFromAlu(Reg r, int vf) {
        Preconditions.checkArgument(r != null);
        regs.set(r, Bits.clip(8, Alu.unpackValue(vf)));
    }

    /**
     * Sets F according to a given value/flags
     * 
     * @param valueFlags
     */
    private void setFlags(int valueFlags) {
        regs.set(Reg.F, Alu.unpackFlags(valueFlags));
    }

    /**
     * Loads in the register r the value and in F the flags from vf a
     * value/flags
     * 
     * @param r
     *            - the destination Register
     * @param vf
     *            - the packed value/flags
     */
    private void setRegFlags(Reg r, int vf) {
        setRegFromAlu(r, vf);
        setFlags(vf);
    }

    /**
     * Load in F the flags and writes at [HL] the value from vf
     * 
     * @param vf
     *            - the packed value/flags
     */
    private void write8AtHlAndSetFlags(int vf) {
        write8(reg16(Reg16.HL), Alu.unpackValue(vf));
        setFlags(vf);
    }

    /**
     * Loads in F the flags according to the vf value, state of F, and FlagSrcs
     * 
     * @param vf
     *            - the packed value/flags
     * @param z
     *            - how z activation is determined
     * @param n
     *            - how n activation is determined
     * @param h
     *            - how h activation is determined
     * @param c
     *            - how c activation is determined
     */
    private void combineAluFlags(int vf, FlagSrc z, FlagSrc n, FlagSrc h,
            FlagSrc c) {
    	
    	FlagSrc ref = FlagSrc.ALU;
    	int toTake = Alu.maskZNHC(z==ref,n==ref,h==ref,c==ref);
    	ref = FlagSrc.CPU;
    	int toKeep = Alu.maskZNHC(z==ref,n==ref,h==ref,c==ref);
    	ref = FlagSrc.V1;
    	int toEnable = Alu.maskZNHC(z==ref,n==ref,h==ref,c==ref);
    	ref = FlagSrc.V0;
    	int toDisable = Alu.maskZNHC(z==ref,n==ref,h==ref,c==ref);
    	
    	int res = Alu.unpackFlags(vf) & toTake;
    	res = res | (regs.get(Reg.F) & toKeep);
    	res = res | toEnable;
    	res = res & Bits.complement8(toDisable);
    	
    	regs.set(Reg.F, res);
    }
}
