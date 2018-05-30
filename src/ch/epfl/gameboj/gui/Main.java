package ch.epfl.gameboj.gui;

import java.io.File;
import java.util.List;
import java.util.Map;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Joypad.Key;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {

    private final static String ROM_FILE_NAME = "TasmaniaStory.gb";

    Map<KeyCode, Key> keyCodeMap = Map.of(KeyCode.UP, Key.UP, KeyCode.DOWN,
            Key.DOWN, KeyCode.RIGHT, Key.RIGHT, KeyCode.LEFT, Key.LEFT);

    Map<String, Key> keyTextMap = Map.of("a", Key.A, "b", Key.B, "s", Key.START,
            " ", Key.SELECT);

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
            if (keyText.equals("")) {
                Key joypadKey = keyCodeMap.get(k.getCode());
                if (joypadKey != null) {
                    gb.joypad().keyPressed(joypadKey);
                }
            } else {
                Key joypadKey = keyTextMap.get(keyText);
                if (joypadKey != null) {
                    gb.joypad().keyPressed(joypadKey);
                }
            }
        });

        imageView.setOnKeyReleased((k) -> {
            String keyText = k.getText();
            if (keyText.equals("")) {
                Key joypadKey = keyCodeMap.get(k.getCode());
                if (joypadKey != null) {
                    gb.joypad().keyReleased(joypadKey);
                }
            } else {
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
