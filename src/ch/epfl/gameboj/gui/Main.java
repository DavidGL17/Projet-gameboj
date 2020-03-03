package ch.epfl.gameboj.gui;

import static ch.epfl.gameboj.component.cartridge.Cartridge.ofFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Joypad.Key;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class Main extends Application {

    Map<KeyCode, Key> keyCodeMap = Map.of(KeyCode.UP, Key.UP, KeyCode.DOWN,
            Key.DOWN, KeyCode.RIGHT, Key.RIGHT, KeyCode.LEFT, Key.LEFT);

    Map<String, Key> keyTextMap = Map.of("a", Key.A, "b", Key.B, "s", Key.START,
            " ", Key.SELECT);

    private final List<String> games = Arrays.asList("2048.gb", "bomberman.gb",
            "donkeyKong.gb", "flappyboy.gb", "Kirby'sDreamLand.gb",
            "Megaman.gb", "MetroidIIReturnofSamus.gb", "snake.gb",
            "superMarioLand.gb", "superMarioLand2.gb", "tasmaniaStory.gb",
            "Tetris.gb", "Zelda.gb");

    private boolean gameStart = false;
    private boolean gameStarted = false;
    private String game = "";
    private GameBoy gb;
    private ImageView imageView;
    private Scene scene;
    private long start = 0;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        GridPane grid = new GridPane();
        Label gamebojName = new Label("Gameboj");
        grid.add(gamebojName, 1, 0, 1, 1);
        // ChoiceBox<String> dropdown = new ChoiceBox<String>();
        for (int i = 0; i < games.size(); ++i) {
            Button b = new Button("Play " + games.get(i));
            b.setOnMousePressed((k) -> {
                gameStart = true;
                game = b.getText().substring(5, b.getText().length());
            });
            Label name = new Label(games.get(i));
            grid.add(b, 1, i + 1, 2, 1);
            grid.add(name, 0, i + 1, 1, 1);
            GridPane.setHalignment(b, HPos.CENTER);
            GridPane.setHalignment(name, HPos.CENTER);
        }

        scene = new Scene(grid);
        primaryStage.setScene(scene);
        primaryStage.setTitle("gameboj");
        primaryStage.sizeToScene();
        primaryStage.show();

        start = System.nanoTime();
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gameStarted) {
                    long timeSpent = now - start;
                    long cyclesOfGb = (long) (timeSpent
                            * GameBoy.cyclesPerNanosecond);
                    gb.runUntil(cyclesOfGb);
                    imageView.setImage(ImageConverter
                            .convert(gb.lcdController().currentImage()));
                }

                if (gameStart) {
                    try {
                        gb = new GameBoy(ofFile(new File("games/" + game)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    imageView = new ImageView(ImageConverter
                            .convert(gb.lcdController().currentImage()));
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
                    scene = new Scene(pane);

                    primaryStage.setScene(scene);
                    primaryStage.setTitle("gameboj");
                    primaryStage.show();
                    imageView.requestFocus();
                    gameStarted = true;
                    gameStart = false;
                    start = now;
                }
            }
        };
        timer.start();
    }

}
