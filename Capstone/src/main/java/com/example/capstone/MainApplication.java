package com.example.capstone;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader =
                new FXMLLoader(
                        MainApplication.class.getResource(
                                "hello-view.fxml"
                        )
                );

        Scene scene =
                new Scene(
                        loader.load(),
                        900,
                        600
                );

        stage.setTitle(
                "CoreStock"
        );

        stage.setScene(
                scene
        );

        stage.show();
    }

    public static void main(String[] args) {

        launch();
    }
}
