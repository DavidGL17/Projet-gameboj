package ch.epfl.gameboj.gui;

import java.io.File;
import java.util.List;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {

    private final long CYCLES_PER_ITERATION = 17556;

    public static void main(String[] args) {
        Application.launch(new String[] { "tasmaniaStory.gb" });
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
        // imageView.setOnKeyPressed(KeyEvent.KEY_PRESSED);

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
                System.out.println(cyclesOfGb-gb.cycles());
                gb.runUntil(cyclesOfGb);
                imageView.setImage(ImageConverter
                        .convert(gb.lcdController().currentImage()));
            }
        };
        timer.start();
    }

}
