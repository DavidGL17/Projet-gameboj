package ch.epfl.gameboj.gui;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Joypad.Key;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {

    private final static String ROM_FILE_NAME = "flappyboy.gb";
    Map<KeyCode, Key> keyMap = Map.of(KeyCode.A, Key.A, KeyCode.B, Key.B,
            KeyCode.S, Key.START, KeyCode.SPACE, Key.SELECT, KeyCode.UP, Key.UP,
            KeyCode.DOWN, Key.DOWN, KeyCode.RIGHT, Key.RIGHT, KeyCode.LEFT,
            Key.LEFT);

    public static void main(String[] args) {
        Application.launch(new String[] { ROM_FILE_NAME });
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        List<String> romName = getParameters().getRaw();
        if (romName.size() != 1) {
            System.exit(1);
        }
        long start = System.nanoTime();
        GameBoy gb = new GameBoy(Cartridge.ofFile(new File(romName.get(0))));

        ImageView imageView = new ImageView(
                ImageConverter.convert(gb.lcdController().currentImage()));
        imageView.setFitHeight(2 * gb.lcdController().height());
        imageView.setFitWidth(2 * gb.lcdController().width());

        /// Gestion des touches

        imageView.setOnKeyPressed((k) -> {
            Key joypadKey = keyMap.get(k.getCode());
            if (joypadKey == null) {
                return;
            } else {
                gb.joypad().keyPressed(joypadKey);
            }
        });
        imageView.setOnKeyReleased((k) -> {
            Key joypadKey = keyMap.get(k.getCode());
            if (joypadKey == null) {
                return;
            } else {
                gb.joypad().keyReleased(joypadKey);
            }
        });

        BorderPane pane = new BorderPane(imageView);
        Scene scene = new Scene(pane);

        primaryStage.setScene(scene);
        primaryStage.setTitle("gameboj");
        primaryStage.show();
        imageView.requestFocus();

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
