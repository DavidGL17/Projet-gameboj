package ch.epfl.gameboj.gui;

import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.lcd.LcdImage;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class ImageConverter {

    private static final int[] MAP_COLORS = new int[] {0xFFFFFFFF, 0xD3D3D3D3, 0xA9A9A9A9, 0x00000000};
    
    public static Image convert(LcdImage image) {
        WritableImage finalImage = new WritableImage(LcdController.LCD_WIDTH, LcdController.LCD_HEIGHT);
        PixelWriter pixelWriter = finalImage.getPixelWriter();
        for (int y = 0;y<finalImage.getHeight();++y) {
            for (int x = 0;x<finalImage.getWidth();++x) {
                pixelWriter.setArgb(x, y, MAP_COLORS[image.get(x, y)]);
            }
        }
        return finalImage;
    }

}
