package ch.epfl.gameboj.gui;

import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.lcd.LcdImage;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class ImageConverter {

    private static final int[] MAP_COLORS = new int[] { 0xFFFFFFFF, 0xFFD3D3D3,
            0xFFA9A9A9, 0xFF000000 };

    /**
     * Converts an LcdImage from the gameboy to a javafx Image
     * 
     * @param image,
     *            an LcdImage
     * @return the LcdImage converted to Image
     */
    public static Image convert(LcdImage image) {
        WritableImage finalImage = new WritableImage(LcdController.LCD_WIDTH,
                LcdController.LCD_HEIGHT);
        PixelWriter pixelWriter = finalImage.getPixelWriter();
        for (int y = 0; y < finalImage.getHeight(); ++y) {
            for (int x = 0; x < finalImage.getWidth(); ++x) {
                pixelWriter.setArgb(x, y, MAP_COLORS[image.get(x, y)]);
            }
        }
        return finalImage;
    }

}
