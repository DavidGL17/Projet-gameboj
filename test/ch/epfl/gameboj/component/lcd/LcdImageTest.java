package ch.epfl.gameboj.component.lcd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.BitVectorTest;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.lcd.LcdImage.Builder;

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
    void getWorksBasic() {
        int[] lines = new int[] { 0x000F0000, 0xFFF0FFFF };
        List<LcdImageLine> linesList = new ArrayList<>();
        linesList.add(new LcdImageLine(BitVectorTest.BitVectorInstantiation(new int[] { 0x000F0000 }),
                BitVectorTest.BitVectorInstantiation(new int[] { 0xFFF0FFFF }),
                BitVectorTest.BitVectorInstantiation(new int[] { 0x00000000 })));
        LcdImage image = new LcdImage(linesList, 32, 1);
        for (int i = 0; i < 32; ++i) {
            int expected = (Bits.test(lines[0], i) ? 1 << 1 : 0)
                    | (Bits.test(lines[1], i) ? 1 : 0);
            assertEquals(expected, image.get(i, 0));
        }
    }

    @Test
    void getWorksforRandom() {
        Random rng = new Random();
        int[][] lines = new int[rng.nextInt(20) * 2][rng.nextInt(5)];
        for (int i = 0; i < lines.length; ++i) {
            for (int j = 0; j < lines[i].length; ++j) {
                lines[i][j] = rng.nextInt();
            }
        }
        List<LcdImageLine> linesList = new ArrayList<>();
        for (int i = 0; i < lines.length / 2; ++i) {
            BitVector msb = BitVectorTest.BitVectorInstantiation(lines[2 * i]);
            BitVector lsb = BitVectorTest.BitVectorInstantiation(lines[2 * i + 1]);
            linesList.add(new LcdImageLine(msb, lsb, msb.or(lsb)));
        }

        LcdImage image = new LcdImage(linesList, lines[0].length * 32,
                linesList.size());

        for (int i = 0; i < lines.length / 2; ++i) {
            for (int j = 0; j < lines[0].length * 32; ++j) {
                int expected = (Bits.test(lines[2 * i][j / 32], j % 32) ? 1 << 1
                        : 0)
                        | (Bits.test(lines[2 * i + 1][j / 32], j % 32) ? 1 : 0);
                assertEquals(expected, image.get(j, i));
            }
        }
    }

    @Test
    void BuilderWorks() {
        Random rng = new Random();
        int[][] lines = new int[rng.nextInt(20) * 2][rng.nextInt(5)];
        for (int i = 0; i < lines.length; ++i) {
            for (int j = 0; j < lines[i].length; ++j) {
                lines[i][j] = rng.nextInt();
            }
        }
        List<LcdImageLine> linesList = new ArrayList<>();
        for (int i = 0; i < lines.length / 2; ++i) {
            BitVector msb = BitVectorTest.BitVectorInstantiation(lines[2 * i]);
            BitVector lsb = BitVectorTest.BitVectorInstantiation(lines[2 * i + 1]);
            linesList.add(new LcdImageLine(msb, lsb, msb.or(lsb)));
        }

        LcdImage image = new LcdImage(linesList, lines[0].length * 32,
                linesList.size());

        LcdImage.Builder b = new Builder(lines[0].length * 32,
                linesList.size());
        int i = 0;
        for (LcdImageLine l : linesList) {
            b.setLine(l, i);
            ++i;
        }
        assertEquals(image, b.build());
    }

}
