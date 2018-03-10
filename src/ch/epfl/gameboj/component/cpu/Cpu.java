/**
 * 
 */
package ch.epfl.gameboj.component.cpu;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Alu;

/**
 * @author David Gonzalez leon (270845)
 * @author Melvin Malonga-Matouba (288405)
 *
 */
public class Cpu implements Component, Clocked {
    private enum Reg implements Register{
        A,F,B,C,D,E,H,L
    }
    private enum Reg16 implements Register{
        AF,BC,DE,HL
    }
    
    private long nextNonIdleCycle = 0;
    private Bus bus;
    private RegisterFile<Reg> Regs = new RegisterFile<>(Reg.values());
    private int registerPC = 0;
    private int registerSP = 0;
    private static final Opcode[] DIRECT_OPCODE_TABLE = buildOpcodeTable(Opcode.Kind.DIRECT);
    private static final Opcode[] PREFIXED_OPCODE_TABLE = buildOpcodeTable(Opcode.Kind.PREFIXED);
    
    
    private static Opcode[] buildOpcodeTable(Opcode.Kind kind) {
        Opcode[] table = new Opcode[0XFFFF];
        for (Opcode o : Opcode.values()) {
            if (o.kind == kind) {
                table[o.encoding] = o;
            }
        }
        return table;
    }
    /**
     * Attaches the cpu to the given bus
     * 
     * @param bus
     *            the bus we want to attach our component to
     */
    @Override
    public void attachTo(Bus bus) {
        this.bus = bus;
        Component.super.attachTo(bus);
    }
    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        return NO_DATA;
    }
    
    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
    }
    
    public int[] _testGetPcSpAFBCDEHL() {
        return new int[] {registerPC,registerSP, Regs.get(Reg.A), Regs.get(Reg.F), Regs.get(Reg.B), Regs.get(Reg.C), Regs.get(Reg.D), Regs.get(Reg.E), Regs.get(Reg.H), Regs.get(Reg.L)};    
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Clocked#cycle(long)
     */
    @Override
    public void cycle(long cycle) {
        if(cycle >=nextNonIdleCycle) {
            if (bus.read(registerPC)==0xCB) {
                dispatch(PREFIXED_OPCODE_TABLE[bus.read(registerPC+1)],cycle);

            } else {
                dispatch(DIRECT_OPCODE_TABLE[bus.read(registerPC)],cycle);
            }
        }
    }
    
    private void setNextNonIdleCycle(long cycle, int cycles, int additionalCycles) { // Doesn't use additional cycle
        nextNonIdleCycle = cycle+cycles;
    }
    
    private void dispatch(Opcode opcode, long cycle) {
        
        switch(opcode.family) {
        case NOP: {
        } break;
        case LD_R8_HLR: {
            Regs.set(extractReg(opcode, 3), read8AtHl());
        } break;
        case LD_A_HLRU: {
          Regs.set(Reg.A, read8AtHl());
          setReg16(Reg16.HL, Bits.clip(16, reg16(Reg16.HL)+extractHlIncrement(opcode)));
        } break;
        case LD_A_N8R: {
            Regs.set(Reg.A, read8(AddressMap.REGS_START+read8AfterOpcode()));
        } break;
        case LD_A_CR: {
            Regs.set(Reg.A, read8(AddressMap.REGS_START+Regs.get(Reg.C)));
        } break;
        case LD_A_N16R: {
            Regs.set(Reg.A, read16AfterOpcode());
        } break;
        case LD_A_BCR: {
            Regs.set(Reg.A, read8(reg16(Reg16.BC)));
        } break;
        case LD_A_DER: {
            Regs.set(Reg.A, read8(reg16(Reg16.DE)));
        } break;
        case LD_R8_N8: {
            Regs.set(extractReg(opcode, 3), read8AfterOpcode());
        } break;
        case LD_R16SP_N16: {
            setReg16SP(extractReg16(opcode), read16AfterOpcode());
        } break;
        case POP_R16: {
            setReg16(extractReg16(opcode), pop16());
        } break;
        case LD_HLR_R8: {
            write8AtHl(Regs.get(extractReg(opcode, 0)));
        } break;
        case LD_HLRU_A: {
            write8AtHl(Regs.get(Reg.A));
            setReg16(Reg16.HL, (reg16(Reg16.HL) + extractHlIncrement(opcode)));
        } break;
        case LD_N8R_A: {
        	write8(0xFF00 + read8AfterOpcode(),Regs.get(Reg.A));
        } break;
        case LD_CR_A: {
        	write8(0xFF00 + Regs.get(Reg.C),Regs.get(Reg.A));
        } break;
        case LD_N16R_A: {
        		int destination = read16AfterOpcode();
        		int value = Regs.get(Reg.A);
        		write8(destination,value);
        } break;
        case LD_BCR_A: {
        		int destination = reg16(Reg16.BC);
        		int value = Regs.get(Reg.A);
        		write8(destination,value);
        } break;
        case LD_DER_A: {
        		int destination = reg16(Reg16.DE);
        		int value = Regs.get(Reg.A);
        		write8(destination,value);
        } break;
        case LD_HLR_N8: {
        		int destination = reg16(Reg16.HL);
        		int value = read8AfterOpcode();
        		write8(destination,value);
        } break;
        case LD_N16R_SP: {
        		int argument = read16AfterOpcode();
        		write16(argument,registerSP);
        } break;
        case LD_R8_R8: {
        		Reg destination = extractReg(opcode,2);
        		Reg store = extractReg(opcode,5);
        		int value=Regs.get(store);
        		Regs.set(destination,value);
        } break;
        case LD_SP_HL: {
        		registerSP=reg16(Reg16.HL);
        } break;
        case PUSH_R16: {
        		Reg16 reg = extractReg16(opcode);
        		push16(reg16(reg));
        } break;
        // Add
        case ADD_A_R8: {
            boolean c = extractCarry(opcode, Regs.get(Reg.A), Regs.get(extractReg(opcode, 0)));
            combineAluFlags(Alu.add(Regs.get(Reg.A), Regs.get(extractReg(opcode, 0)),c), FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);
            Regs.set(Reg.A, Alu.unpackValue(Alu.add(Regs.get(Reg.A), Regs.get(extractReg(opcode, 0)),c)));
        } break;
        case ADD_A_N8: {
            boolean c = extractCarry(opcode, Regs.get(Reg.A), read8AfterOpcode());
            combineAluFlags(Alu.add(Regs.get(Reg.A), read8AfterOpcode(),c), FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);
            Regs.set(Reg.A, Alu.unpackValue(Alu.add(Regs.get(Reg.A), read8AfterOpcode(),c)));
        } break;
        case ADD_A_HLR: {
            boolean c = extractCarry(opcode, Regs.get(Reg.A), read8AtHl());
            combineAluFlags(Alu.add(Regs.get(Reg.A), read8AtHl(),c), FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);
            Regs.set(Reg.A, Alu.unpackValue(Alu.add(Regs.get(Reg.A), read8AtHl(),c)));
        } break;
        case INC_R8: {
            combineAluFlags(Alu.add(Regs.get(extractReg(opcode, 4)), 1), FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.CPU);
            Regs.set(extractReg(opcode, 4), Alu.unpackValue(Alu.add(Regs.get(extractReg(opcode, 4)), 1)));
        } break;
        case INC_HLR: {
            combineAluFlags(Alu.add(read8AtHl(), 1), FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.CPU);
            write8AtHl(Alu.unpackValue(Alu.add(read8AtHl(), 1)));
        } break;
        case INC_R16SP: {
        } break;
        case ADD_HL_R16SP: {
        } break;
        case LD_HLSP_S8: {
        } break;

        // Subtract
        case SUB_A_R8: {
        } break;
        case SUB_A_N8: {
        } break;
        case SUB_A_HLR: {
        } break;
        case DEC_R8: {
        } break;
        case DEC_HLR: {
        } break;
        case CP_A_R8: {
        } break;
        case CP_A_N8: {
        } break;
        case CP_A_HLR: {
        } break;
        case DEC_R16SP: {
        } break;

        // And, or, xor, complement
        case AND_A_N8: {
        } break;
        case AND_A_R8: {
        } break;
        case AND_A_HLR: {
        } break;
        case OR_A_R8: {
        } break;
        case OR_A_N8: {
        } break;
        case OR_A_HLR: {
        } break;
        
        
        case XOR_A_R8: {
        } break;
        case XOR_A_N8: {
        } break;
        case XOR_A_HLR: {
        } break;
        case CPL: {
        } break;

        // Rotate, shift
        case ROTCA: {
        } break;
        case ROTA: {
        } break;
        case ROTC_R8: {
        } break;
        case ROT_R8: {
        } break;
        case ROTC_HLR: {
        } break;
        case ROT_HLR: {
        } break;
        case SWAP_R8: {
        } break;
        case SWAP_HLR: {
        } break;
        case SLA_R8: {
        } break;
        case SRA_R8: {
        } break;
        case SRL_R8: {
        } break;
        case SLA_HLR: {
        } break;
        case SRA_HLR: {
        } break;
        case SRL_HLR: {
        } break;

        // Bit test and set
        case BIT_U3_R8: {
        } break;
        case BIT_U3_HLR: {
        } break;
        case CHG_U3_R8: {
        } break;
        case CHG_U3_HLR: {
        } break;

        // Misc. ALU
        case DAA: {
        } break;
        case SCCF: {
        } break;
        default : {
        	System.out.println("Not yet treated");
        	throw new IllegalArgumentException();
        }
        
        }
        setNextNonIdleCycle(cycle, opcode.cycles, opcode.additionalCycles);
        registerPC += Bits.clip(16,opcode.totalBytes);
    }
    
    
    ///Accès au bus
    
    
    /**
     * Reads the byte at the given adress on the bus
     * @param adress a 16 bits integer
     * @return the stored 8-bits value
     */
    private int read8(int adress) {
    		return bus.read(Preconditions.checkBits16(adress));
    }
    
    /**
     * Reads the byte at the adress stored in HL on the bus
     * @return the stored 8-bits value
     */
    private int read8AtHl() {
    		return read8(reg16(Reg16.HL));
    }
    
    /**
     * Reads the byte following the Opcode
     * @return the store 8-bits value
     */
    private int read8AfterOpcode() {
    		return read8(Bits.clip(16,Preconditions.checkBits16(registerPC+1)));
    }
    
    /**
     * Reads the 16 bits value stored at an adress
     * @param adress a 16-bits integer
     * @return the 16-bits value represented
     */
    private int read16(int adress) {
    	return Bits.make16(read8(Preconditions.checkBits16(adress+1)),read8(adress));
    }
    
    /**
     * Reads the 16 bits-value stored after the Opcode
     * @return the 16-bits value represented
     */
    private int read16AfterOpcode() {
    		return read16(registerPC+1);
    }
    
    /**
     * Writes a 8-bit value at the desired adress
     * @param adress a 16-bits value
     * @param v the 8-bits value to represent
     */
    private void write8(int adress, int v) {
    	bus.write(Preconditions.checkBits16(adress),Preconditions.checkBits8(v));
    }
    
    /**
     * Writes a 16-bits value at the desired adress
     * @param adress a 16-bits value
     * @param v the 16-bits value to represent
     */
    private void write16(int adress, int v) {
    
    	write8(Preconditions.checkBits16(adress+1), Bits.extract(8,8,Preconditions.checkBits16(v)));
    	write8(adress, Bits.clip(8,v));
    
    }
    
    /**
     * Writes a 8-bits value at the adress stored in HL
     * @param v the 8-bits value to represent 
     */
    private void write8AtHl(int v) {
        write8(reg16(Reg16.HL),Preconditions.checkBits8(v));	
    }
    
    /**
     * Decreases SP by 2 and writes v at the adress SP
     * @param v the 16-bits value to represent
     */
    private void push16(int v) {
    	registerSP=Bits.clip(16,registerSP-2);
    	write16(registerSP,Preconditions.checkBits16(v));
    }
    
    /**
     * Reads what's stored at the adress SP and increases SP by 2
     * @return the 16-bits value represented
     */
    private int pop16() {
    	int sP = registerSP;
    	registerSP = Bits.clip(16, registerSP + 2);
    	return read16(sP);
    }
    
    
    ///Gestion des paires de registres
    
    
    /**
     * Returns the value contained in the given register
     * 
     * @param r, the 16 bit register
     * @return the value contained in the 16 bit register
     * @throws IllegalArgumentException if the register is null
     */
    private int reg16(Reg16 r) {
        Preconditions.checkArgument(r!=null);
        switch (r) {
        case AF :
            return Preconditions.checkBits16(Regs.get(Reg.A)<<8)+(Regs.get(Reg.F));
        case BC :
            return Preconditions.checkBits16(Regs.get(Reg.B)<<8)+(Regs.get(Reg.C));
        case DE :
            return Preconditions.checkBits16(Regs.get(Reg.D)<<8)+(Regs.get(Reg.E));
        case HL :
            return Preconditions.checkBits16(Regs.get(Reg.H)<<8)+(Regs.get(Reg.L));
        default :
            return 0;
        }
    }
    
    
    /**
     * Sets the value of the given 16 bit register to the given value
     * 
     * @param r, the 16 bit register
     * @param newV, the new value
     * @throws IllegalArgumentException, if the value is not a 16 bit number or if the register is null
     */
    private void setReg16(Reg16 r, int newV) {
        Preconditions.checkArgument(r!=null);
        Preconditions.checkBits16(newV);
        switch (r) {
        case AF :
            Regs.set(Reg.A, (newV&0xFF00)>>>8);
            Regs.set(Reg.F, Bits.clip(8, newV)&0xF0);
            break;
        case BC :
            Regs.set(Reg.B, (newV&0xFF00)>>>8);
            Regs.set(Reg.C, Bits.clip(8, newV));
            break;
        case DE :
            Regs.set(Reg.D, (newV&0xFF00)>>>8);
            Regs.set(Reg.E, Bits.clip(8, newV));
            break;
        case HL :
            Regs.set(Reg.H, (newV&0xFF00)>>>8);
            Regs.set(Reg.L, Bits.clip(8, newV));
            break;
        }
    }
    /**
     * Sets the value of the given 16 bit register to the given value. if the given register is AF, sets the value of SP instead
     * 
     * @param r, the 16 bit register
     * @param newV, the new value
     * @throws IllegalArgumentException, if the value is not a 16 bit number or if the register is null
     */
    private void setReg16SP(Reg16 r, int newV) {
        Preconditions.checkArgument(r!=null);
        switch (r) {
        case AF :
            registerSP = Preconditions.checkBits16(newV);
            break;
        default :
            setReg16(r, newV);
        }
    }
    
    
    ///Extraction des reg
    
    /**
     * Returns the length bit bits that identifies a register in the opcode value
     * 
     * @param opcodeValue, the value of the opcode
     * @param startBit, the bite where we start looking for the bits
     * @param length, the number of bits that identifies the register
     * @return The bits identifying the register
     */
    private int getRegValue(int opcodeValue, int startBit, int length) {
        Preconditions.checkBits8(opcodeValue);
        int value = 0;
        for (int i = 0;i<length;++i) {
            if(Bits.test(opcodeValue, i+startBit)) {
                value += Bits.mask(i);
            }
        }
        return value;
    }
    
    /**
     * Extracts and returns the identitie of an 8 bit register in the encodig of the given opcode at the given starting bit
     * 
     * @param opcode, the opocode's enconding we have to extract the value of
     * @param startBit, the bit we have to start extracting the value
     * @return the register that was encoded in the opcode's encoding
     */
    private Reg extractReg(Opcode opcode, int startBit) {
        Preconditions.checkArgument(opcode!=null);
        switch(getRegValue(opcode.encoding, startBit, 3)) {
        case 0b000 :
            return Reg.B;
        case 0b001 :
            return Reg.C;
        case 0b010 :
            return Reg.D;
        case 0b011 :
            return Reg.E;
        case 0b100 :
            return Reg.H;
        case 0b101 :
            return Reg.L;
        case 0b111 :
            return Reg.A;
        default :
            return null;
        }
    }
    /**
     * Extracts and returns the identitie of an 16 bit register in the encoding of the given opcode at the fourth bit
     * 
     * @param opcode, the opocode's enconding we have to extract the value of
     * @return the register that was encoded in the opcode's encoding
     */
    private Reg16 extractReg16(Opcode opcode) {
        Preconditions.checkArgument(opcode!=null);;
        switch (getRegValue(opcode.encoding, 4, 2)) {
        case 0b00:
            return Reg16.BC;
        case 0b01:
            return Reg16.DE;
        case 0b10:
            return Reg16.HL;
        case 0b11:
            return Reg16.AF;
        default :
            return null;
        }
    }
    /**
     * Returns +1 or -1 depending on the fourth bit of the given opcode's encoding
     * 
     * @param opcode, the opcode we have to analyse
     * @return +1 if the fourth bit is 0, -1 if the fourth bit is 1
     */
    private int extractHlIncrement(Opcode opcode) {
        Preconditions.checkArgument(opcode!=null);
        return (Bits.test(opcode.encoding, 4)?-1:+1);
    }
    
    
    
    
    
    
    
    
    
    /// Gestion des Fanions
    
    /**
     * Loads in the register r the value from packed vf
     * 
     * @param r - the destination Register
     * @param vf - the packed value/flags
     */
    private void setRegFromAlu(Reg r, int vf) {
    		Preconditions.checkArgument(r!=null);
    		Regs.set(r,Alu.unpackValue(vf));
    }
    
    /**
     * Sets F according to a given value/flags
     * 
     * @param valueFlags
     */
    private void setFlags(int valueFlags) {
    		Regs.set(Reg.F,Alu.unpackFlags(valueFlags));
    }
    
    /**
     * Loads in the register r the value and in F the flags from vf a value/flags
     * 
     * @param r - the destination Register
     * @param vf - the packed value/flags
     */
    private void setRegFlags(Reg r, int vf) {
    		setRegFromAlu(r,vf);
    		setFlags(vf);
    }
    
    /**
     * Load in F the flags and writes at [HL] the value from vf 
     * 
     * @param vf - the packed value/flags
     */
    private void write8AtHlAndSetFlags(int vf) {
    		write8(reg16(Reg16.HL) , Alu.unpackValue(vf));
    		setFlags(vf);
    }
    
    private enum FlagSrc {V0, V1, ALU, CPU };
    
    /**
     * Loads in F the flags according to the vf value, state of F, and FlagSrcs
     * 
     * @param vf - the packed value/flags
     * @param z - how z activation is determined
     * @param n - how n activation is determined
     * @param h - how h activation is determined
     * @param c - how c activation is determined
     */
    private void combineAluFlags(int vf, FlagSrc z, FlagSrc n, FlagSrc h, FlagSrc c ) {
    		FlagSrc [] FlagSources = { z,n,h,c };
    		for (FlagSrc i : FlagSources) {
    			Preconditions.checkArgument(i!=null);
    		}
    		boolean [] currentState = { Bits.test(Regs.get(Reg.F),Alu.Flag.Z), Bits.test(Regs.get(Reg.F),Alu.Flag.N), Bits.test(Regs.get(Reg.F),Alu.Flag.H), Bits.test(Regs.get(Reg.F),Alu.Flag.C)};
    		boolean [] activationInVf = { Bits.test(vf,Alu.Flag.Z), Bits.test(vf,Alu.Flag.N), Bits.test(vf,Alu.Flag.H), Bits.test(vf,Alu.Flag.C)};
    		boolean [] FlagActivation = new boolean [4];
  
    		for (int i=0 ; i<FlagSources.length ; i++) {
    			switch (FlagSources[i]) {
    			case V0:{
    				FlagActivation[i]=false;
    			} break;
    			case V1:{
    				FlagActivation[i]=true;
    			} break;
    			case ALU:{
    				FlagActivation[i]=activationInVf[i];
    			} break;
    			case CPU:{
    				FlagActivation[i]=currentState[i];
    			} break ;
    			}
    		}
    		
    		setFlags(Alu.maskZNHC(FlagActivation[0], FlagActivation[1], FlagActivation[2], FlagActivation[3]) );
    		
    		
    }
    
    
    ///Extraction du carry
    private boolean extractCarry(Opcode opcode, int l, int r) {
        if (Bits.test(opcode.encoding, 3)) {
            return (Bits.test(Alu.unpackFlags(Alu.add(l, r)), 4));
        }
        return false;
    }
    
    
}
