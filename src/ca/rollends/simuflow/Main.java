package ca.rollends.simuflow;

import ca.rollends.simuflow.blocks.TransferFunction;
import ca.rollends.simuflow.blocks.python.Statement;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        System.out.println("Entering start method");
        // A stage is the application window automatically created by the framework
        // The scene holds the content to be displayed, which is stored as tree
        Label label = new Label("Hello JavaFX");
        Scene scene = new Scene(new StackPane(label), 640, 480);

        // We can have multiple scenes. Setup this one, and tell the stage to show it.
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        System.out.println("Entering init method");
    }

}
