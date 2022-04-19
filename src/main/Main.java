package main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("../fxml/main2.fxml"));
        primaryStage.setTitle("Tweet Tracker Team 19");
        primaryStage.setScene(new Scene(root));
        primaryStage.centerOnScreen();
        primaryStage.show();
        primaryStage.setOnCloseRequest((ae -> {
            Platform.exit();
            System.exit(0);
        }));
    }


    public static void main(String[] args) {
        launch(args);
    }
}
