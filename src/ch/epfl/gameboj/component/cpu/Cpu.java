/**
 * 
 */
package ch.epfl.gameboj.component.cpu;

import java.util.Objects;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Opcode.Kind;

/**
 * @author David Gonzalez leon (270845)
 *
 */
public class Cpu implements Component, Clocked {
    private enum Reg implements Register{
        A,F,B,C,D,E,H,L
    }
    private enum Reg16 implements Register{
        AF,BC,DE,HL
    }
    
    private long nextNonIdleCycle;
    private Bus bus;
    private RegisterFile<Reg> Regs = new RegisterFile<>(Reg.values());
    private int PC;
    private int SP;
    private static final Opcode[] DIRECT_OPCODE_TABLE = buildOpcodeTable(Opcode.Kind.DIRECT);
    
    
    private static Opcode[] buildOpcodeTable(Opcode.Kind kind) {
        Opcode[] table = new Opcode[256];
        int i = 0;
        for (Opcode o : Opcode.values()) {
            if (o.kind == Kind.DIRECT) {
                table[i] = o;
                ++i;
            }
        }
        return table;
    }
    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#attachTo(ch.epfl.gameboj.Bus)
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
        return null;    
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Clocked#cycle(long)
     */
    @Override
    public void cycle(long cycle) {
        if(cycle >=nextNonIdleCycle) {
            dispatch(getOpcode(bus.read(PC)));
        }
    }
    
    private Opcode getOpcode (int value) {
        for (int i = 0;i<DIRECT_OPCODE_TABLE.length;++i) {
            if (value==DIRECT_OPCODE_TABLE[i].encoding) {
                return DIRECT_OPCODE_TABLE[i];
            }
        }
        return null;
    }
    
    private void dispatch(Opcode opcode) {
        switch(opcode) {
        case NOP: {
        } break;
        case LD_R8_HLR: {
        } break;
        case LD_A_HLRU: {
        } break;
        case LD_A_N8R: {
        } break;
        case LD_A_CR: {
        } break;
        case LD_A_N16R: {
        } break;
        case LD_A_BCR: {
        } break;
        case LD_A_DER: {
        } break;
        case LD_R8_N8: {
        } break;
        case LD_R16SP_N16: {
        } break;
        case POP_R16: {
        } break;
        case LD_HLR_R8: {
        } break;
        
        case LD_HLRU_A: {
        } break;
        case LD_N8R_A: {
        } break;
        case LD_CR_A: {
        } break;
        case LD_N16R_A: {
        } break;
        case LD_BCR_A: {
        } break;
        case LD_DER_A: {
        } break;
        case LD_HLR_N8: {
        } break;
        case LD_N16R_SP: {
        } break;
        case LD_R8_R8: {
        } break;
        case LD_SP_HL: {
        } break;
        case PUSH_R16: {
        } break;
        }
    }
    
    private int reg16(Reg16 r) {
        Preconditions.checkArgument(r!=null);
        switch (r) {
        case AF :
            return (Regs.get(Reg.A)<<8)+(Regs.get(Reg.F));
        case BC :
            return (Regs.get(Reg.B)<<8)+(Regs.get(Reg.C));
        case DE :
            return (Regs.get(Reg.D)<<8)+(Regs.get(Reg.E));
        case HL :
            return (Regs.get(Reg.H)<<8)+(Regs.get(Reg.L));
        default :
            return 0;
        }
    }
    private void setReg16(Reg16 r, int newV) {
        Preconditions.checkArgument(r!=null);
        Preconditions.checkBits16(newV);
        switch (r) {
        case AF :
            Regs.set(Reg.A, newV>>>8);
            Regs.set(Reg.C, Bits.clip(8, newV)&0xF0);
            break;
        case BC :
            Regs.set(Reg.B, newV>>>8);
            Regs.set(Reg.C, Bits.clip(8, newV));
            break;
        case DE :
            Regs.set(Reg.D, newV>>>8);
            Regs.set(Reg.E, Bits.clip(8, newV));
            break;
        case HL :
            Regs.set(Reg.H, newV>>>8);
            Regs.set(Reg.L, Bits.clip(8, newV));
            break;
        }
    }
    private void setReg16SP(Reg16 r, int newV) {
        switch (r) {
        case AF :
            SP = Preconditions.checkBits16(newV);
        default :
            setReg16(r, newV);
        }
    }
}
