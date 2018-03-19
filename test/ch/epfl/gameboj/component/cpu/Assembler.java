// Gameboj stage 4

package ch.epfl.gameboj.component.cpu;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import ch.epfl.gameboj.component.Component;

// Represents a sequence of instructions in an easy to edit way
public final class Assembler {
    private ByteArrayOutputStream s = new ByteArrayOutputStream(); //How the sequence of instruction must be written on the Bus
    private int cycles = 0; // the number of cycle to perform the whole sequence of instructions
    
    /**
     * (Non-official java-doc)
     * Updates the assembler so that it at the end runs opcode op
     * @param op, the opcode
     * @return updated assembler
     */
    public Assembler emit(Opcode op) {
        if (op.kind == Opcode.Kind.PREFIXED)
            s.write(0xCB);
        s.write(op.encoding);
        cycles += op.cycles;
        return this;
    }

    /**
     * (Non-official java-doc)
     * Updates the assembler so that it at the end runs opcode op with parameter n
     * @param op, the opcode
     * @param n, the parameter
     * @return updated assembler
     */
    public Assembler emit(Opcode op, int n) {
        assert op.kind == Opcode.Kind.DIRECT; // (Non direct opcodes do not take a parameter n)
        switch (op.totalBytes) { //Update the OutputStream
        case 2: {	//opcode with totalBytes=2 only take 8 bits parameter
            assert (n & 0xFF) == n; //Check n is 8 bits
            emit(op);
            s.write(n);
        } break;
        
        case 3: {	//opcode with totalBytes=3 only take 16 bits parameter
            assert (n & 0xFFFF) == n; //Check n is 16 bits
            emit(op);
            s.write(n & 0xFF);
            s.write(n >> 8);
        } break;
        
        default:
            throw new Error("invalid opcode size: " + op.totalBytes);
        }
        return this;
    }
    
    /**
     * (Non-official java-doc)
     * Updates the assembler so that it at the end of OutputStream writes n
     * @param n, the byte
     * @return updated assembler
     */
    public Assembler emitData8(int n) {
        assert (n & 0xFF) == n;
        s.write(n);
        return this;
    }
    
    /**
     * @return Program corresponding to Assembler
     */
    public Program program() {
        return new Program(s.toByteArray(), cycles);
    }
    
    // Represents a sequence of instruction and a corresponding Rom in an 'executable' way
    public static class Program {
        private final int cycles, bytesCount;
        private final Component rom;
        
        public Program(byte[] bytes, int cycles) {
            this.cycles = cycles;
            this.bytesCount = bytes.length;
            this.rom = new ProgRom(bytes);
        }
        
        public Component rom() { return rom; }
        public int cycles() { return cycles; }
        public int bytes() { return bytesCount; }
    }
    
    // Represents a Rom affilated to Program
    private static class ProgRom implements Component {
        private final byte[] p;

        public ProgRom(byte[] p) {
            this.p = Arrays.copyOf(p, p.length);
        }

        @Override
        public int read(int address) {
            if (0 <= address && address < p.length)
                return Byte.toUnsignedInt(p[address]);
            else
                return 0x100;
        }

        @Override
        public void write(int address, int data) { }
    }
}
