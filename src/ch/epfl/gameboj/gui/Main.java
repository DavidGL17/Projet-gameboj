package ch.epfl.gameboj.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Joypad.Key;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.lcd.LcdImage;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {

    private final static String ROM_FILE_NAME = "Kirby's Dream Land (USA, Europe).gb";
private static final int[] COLOR_MAP = null;

    Map<KeyCode, Key> keyCodeMap = Map.of(KeyCode.UP, Key.UP, KeyCode.DOWN,
            Key.DOWN, KeyCode.RIGHT, Key.RIGHT, KeyCode.LEFT, Key.LEFT);

    Map<String, Key> keyTextMap = Map.of("a", Key.A, "b", Key.B, "s", Key.START,
            " ", Key.SELECT);
    private int screenIndex=0;

    public static void main(String[] args) {
        Application.launch(new String[] { ROM_FILE_NAME });
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        List<String> romName = getParameters().getRaw();
        if (romName.size() != 1) {
            System.exit(1);
        }
        GameBoy gb = new GameBoy(Cartridge.ofFile(new File(romName.get(0))));

        ImageView imageView = new ImageView(
                ImageConverter.convert(gb.lcdController().currentImage()));
        imageView.setFitHeight(2 * gb.lcdController().height());
        imageView.setFitWidth(2 * gb.lcdController().width());

        /// Gestion des touches

        imageView.setOnKeyPressed((k) -> {
            String keyText = k.getText();
            if (keyText.equals("s")) {
            	//screenshot
            	int [] COLOR_MAP = new int[] { 0xFF_FF_FF, 0xD3_D3_D3,
                        0xA9_A9_A9, 0x00_00_00 };
            	try {
	                LcdImage li = gb.lcdController().currentImage();
	                BufferedImage i = new BufferedImage(LcdController.LCD_WIDTH,
	                        LcdController.LCD_HEIGHT, BufferedImage.TYPE_INT_RGB);
	                for (int y = 0; y < LcdController.LCD_HEIGHT; ++y)
	                    for (int x = 0; x < LcdController.LCD_WIDTH; ++x)
	                        i.setRGB(x, y, COLOR_MAP[li.get(x, y)]);
	                ImageIO.write(i, "png", new File("screen (" + screenIndex + ")" + gb.lcdController().test_getPalettes() + ".png"));
	                screenIndex++;
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
            } else if (keyText.equals("p")){
            	//get sprites'palettes and palettes
	            	gb.lcdController().test_PIsPressed=true;
	            } else if (keyText.equals("")) {
	                // non textual
	                Key joypadKey = keyCodeMap.get(k.getCode());
	                if (joypadKey != null) {
	                    gb.joypad().keyPressed(joypadKey);
	                }
	            } else {
	                // textual
	                Key joypadKey = keyTextMap.get(keyText);
	                if (joypadKey != null) {
	                    gb.joypad().keyPressed(joypadKey);
	                }
	            }
        });

        imageView.setOnKeyReleased((k) ->

        {
            String keyText = k.getText();
            if (keyText.equals("")) {
                // non textual
                Key joypadKey = keyCodeMap.get(k.getCode());
                if (joypadKey != null) {
                    gb.joypad().keyReleased(joypadKey);
                }
            } else {
                // textual
                Key joypadKey = keyTextMap.get(keyText);
                if (joypadKey != null) {
                    gb.joypad().keyReleased(joypadKey);
                }
            }
        });

        BorderPane pane = new BorderPane(imageView);
        Scene scene = new Scene(pane);

        primaryStage.setScene(scene);
        primaryStage.setTitle("gameboj");
        primaryStage.show();
        imageView.requestFocus();

        long start = System.nanoTime();
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long timeSpent = now - start;
                long cyclesOfGb = (long) (timeSpent
                        * GameBoy.cyclesPerNanosecond);
                gb.runUntil(cyclesOfGb);
                imageView.setImage(ImageConverter
                        .convert(gb.lcdController().currentImage()));
            }
        };
        timer.start();
    }

}
