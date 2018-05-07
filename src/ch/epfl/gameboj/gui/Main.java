package ch.epfl.gameboj.gui;

import java.io.File;
import java.util.List;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
	
	public static void main (String[] args) {
		Application.launch(args);
	}

    @Override
    public void start(Stage primaryStage) throws Exception {
        List<String> romName = getParameters().getRaw();
        if (romName.size()!=1) {
            System.exit(1);
        }
        
        GameBoy gb = new GameBoy(Cartridge.ofFile(new File(romName.get(0))));
        
    }

}
