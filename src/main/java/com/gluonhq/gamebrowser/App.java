package com.gluonhq.gamebrowser;

import com.almasb.fxgl.app.FXGLPane;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import com.almasb.fxgl.app.GameApplication;

import java.io.IOException;
import java.util.List;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

/**
 * JavaFX App
 */
public class App extends Application {

    private BorderPane borderPane;
    private final CatalogOperation catalogOperation = new CatalogOperation();

    VBox leftArea = new VBox(20);
    VBox gameList = new VBox(10);
    VBox availableList = new VBox(10);

    @Override
    public void start(Stage stage) {
        borderPane = new BorderPane();
        leftArea.getChildren().addAll(availableList, gameList);
        borderPane.setLeft(leftArea);
        Label title = new Label("GameBrowser");
        borderPane.setTop(title);
        var scene = new Scene(borderPane, 640, 480);
        stage.setScene(scene);
        stage.show();
        try {
            List<GameHolder> remoteOnlyGames = catalogOperation.getRemoteOnlyGames();
            for (GameHolder candidate : remoteOnlyGames) {
                Label l = new Label(candidate.getName());
                Button b = new Button("install");
                b.setOnAction(e -> {
                        b.setDisable(true);
                        b.setText("Installing...");
                        Thread t = new Thread() {
                            @Override public void run() {
                                try {
                                    catalogOperation.fetchGame(candidate);
                                    Platform.runLater(() -> {
                                        l.setVisible(false);
                                        b.setVisible(false);
                                        updateLocalGames();
                                    });
                                } catch (IOException ioException) {
                                    ioException.printStackTrace();
                                }
                            }

                        };
                        t.start();
                });
                HBox box = new HBox(5, l, b);
                availableList.getChildren().add(box);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        updateLocalGames();
    }

    private void updateLocalGames() {
        gameList.getChildren().clear();
        List<GameApplication> games = catalogOperation.getRegisteredGames();
        for (GameApplication game : games) {
            Button b = new Button(game.getClass().getName());
            b.setOnAction(e ->{
                FXGLPane pane = GameApplication.embeddedLaunch(game);
                borderPane.setCenter(pane);
            });
            gameList.getChildren().add(b);
        }
    }

    public static void main(String[] args) {
        launch();
    }

   

}
