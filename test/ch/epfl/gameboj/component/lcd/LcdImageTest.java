package ch.epfl.gameboj.component.lcd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;

/**
 * @author David Gonzalez leon (270845)
 *
 */
class LcdImageTest {

    @Test
    void WidthAndHeightWork() {
        List<LcdImageLine> lines = new ArrayList<>();
        for (int i = 0; i < 10; ++i) {
            lines.add(null);
        }
        LcdImage image = new LcdImage(lines, 32, 10);
        assertEquals(32, image.getWidth());
        assertEquals(10, image.getHeight());
    }

    @Test
    void getWorks() {
        Random rng = new Random();
        int[][] lines = new int[rng.nextInt(20)*2][1];
        for (int i = 0; i < lines.length; ++i) {
            for (int j = 0; j < lines[i].length; ++j) {
                lines[i][j] = rng.nextInt();
            }
        }
        List<LcdImageLine> linesList = new ArrayList<>();
        for (int i = 0;i<lines.length/2;++i) {
            BitVector msb = new BitVector(lines[2*i]);
            BitVector lsb = new BitVector(lines[2*i +1]);
            linesList.add(new LcdImageLine(msb, lsb, msb.or(lsb)));
        }
        
        LcdImage image = new LcdImage(linesList, linesList.get(0).size(),linesList.size());
        
        for (int i = 0;i<lines.length/2;++i) {
            for (int j = 0;j<32;++i) {
                int expected = (Bits.test(lines[i][j/32], j)?1<<1:0) + (Bits.test(lines[i+1][j/32], j)?1:0);
                assertEquals(expected, image.get(j, i));
            }
        }
    }

}
