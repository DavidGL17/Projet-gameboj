package ch.epfl.gameboj;

import ch.epfl.gameboj.component.Joypad.Key;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.lcd.LcdImage;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public final class DebugMain2Live extends Application {

    /** Configuration */

    private static final String ROM_PATH = "flappyboy.gb";

    private static final float EMULATION_SPEED = 1f;
    private static final int CYCLES_PER_ITERATION = (int) (17_556
            * EMULATION_SPEED);
    private static final int[] COLOR_MAP = new int[] { 0xFF_FF_FF, 0xD3_D3_D3,
            0xA9_A9_A9, 0x00_00_00 };

    @Override
    public void start(Stage stage) throws IOException, InterruptedException {
        // Create GameBoy
        File romFile = new File(ROM_PATH);
        GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));

        // Create Scene
        ImageView imageView = new ImageView();
        imageView.setImage(getImage(gb));
        imageView.setSmooth(false);
        Group root = new Group();
        Scene scene = new Scene(root);
        imageView.fitWidthProperty().bind(scene.widthProperty());
        imageView.fitHeightProperty().bind(scene.heightProperty());
        scene.setFill(Color.BLACK);
        HBox box = new HBox();
        box.getChildren().add(imageView);
        root.getChildren().add(box);
        stage.setWidth(LcdController.LCD_WIDTH);
        stage.setHeight(LcdController.LCD_HEIGHT);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.minWidthProperty().bind(scene.heightProperty());
        stage.minHeightProperty().bind(scene.widthProperty());
        stage.setTitle("gameboj");
        stage.show();

        // Update GameBoy
        new AnimationTimer() {
            public void handle(long currentNanoTime) {
                gb.runUntil(gb.cycles() + CYCLES_PER_ITERATION);
                if (gb.cycles()>10000000) {
                    gb.joypad().keyPressed(Key.A);
                }
                if (gb.cycles()>10100000) {
                    gb.joypad().keyReleased(Key.A);
                }
                imageView.setImage(null);
                imageView.setImage(getImage(gb));
            }
        }.start();
    }

    private static final Image getImage(GameBoy gb) {
        LcdImage lcdImage = gb.lcdController().currentImage();
        BufferedImage bufferedImage = new BufferedImage(LcdController.LCD_WIDTH,
                LcdController.LCD_HEIGHT, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < LcdController.LCD_HEIGHT; ++y)
            for (int x = 0; x < LcdController.LCD_WIDTH; ++x)
                bufferedImage.setRGB(x, y, COLOR_MAP[lcdImage.get(x, y)]);
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

}