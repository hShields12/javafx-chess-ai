package com.example.chess.ui;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;

public class ChessUI {
  private final Stage stage;

  public ChessUI(Stage stage) {
    this.stage = stage;
  }

  public void show() {
    // 1) Ask for player name
    TextInputDialog dialog = new TextInputDialog("Player");
    dialog.setTitle("Player Name");
    dialog.setHeaderText("Enter your name:");
    dialog.setContentText("Name:");
    Optional<String> result = dialog.showAndWait();
    String playerName = result.orElse("Player");
    // 2) Create the board UI with the player name
    showMenu(playerName);
    stage.show();
  }

  private void showMenu(String playerName) {
    Label title = new Label("Chess");
    Button vsAi = new Button("Versus AI");
    Button local = new Button("Local Multiplayer");
    Button online = new Button("Online Play");

    vsAi.setOnAction(e -> showAIDifficulty(playerName));
    local.setOnAction(e -> showLocalMultiplayer(playerName));
    online.setOnAction(e -> showOnlinePlay());

    VBox menu = new VBox(20, title, vsAi, local, online);
    menu.setStyle("-fx-padding: 40; -fx-alignment: center;");
    Scene scene = new Scene(menu, 400, 300);
    stage.setTitle("Main Menu");
    stage.setScene(scene);
  }

  private void showAIDifficulty(String playerName) {
    Label label = new Label("Select AI Difficulty:");
    Slider slider = new Slider(1, 5, 2);
    slider.setMajorTickUnit(1);
    slider.setMinorTickCount(0);
    slider.setSnapToTicks(true);
    slider.setShowTickLabels(true);
    slider.setShowTickMarks(true);
    Button start = new Button("Start");

    start.setOnAction(e -> {
      ChessBoardUI boardUI = new ChessBoardUI(stage, playerName, "Computer");
      Scene scene = new Scene(boardUI.getRoot());
      stage.setTitle("Versus AI");
      stage.setScene(scene);
    });

    VBox box = new VBox(15, label, slider, start);
    box.setStyle("-fx-padding: 30; -fx-alignment: center;");
    Scene scene = new Scene(box, 400, 200);
    stage.setTitle("AI Difficulty");
    stage.setScene(scene);
  }

  private void showLocalMultiplayer(String playerName) {
    TextInputDialog dialog2 = new TextInputDialog("Player2");
    dialog2.setTitle("Second Player's Name");
    dialog2.setHeaderText("Enter second player's name:");
    dialog2.setContentText("Name:");
    Optional<String> result2 = dialog2.showAndWait();
    String player2 = result2.orElse("Player2");
    ChessBoardUI boardUI = new ChessBoardUI(stage, playerName, player2);
    Scene scene = new Scene(boardUI.getRoot());
    stage.setTitle("Local Multiplayer");
    stage.setScene(scene);
  }

  private void showOnlinePlay() {
    Label coming = new Label("Online detail screen");
    Button back = new Button("Back");
    back.setOnAction(e -> showMenu("Player"));
    VBox box = new VBox(20, coming, back);
    box.setStyle("-fx-padding: 40; -fx-alignment: center;");
    Scene scene = new Scene(box, 400, 200);
    stage.setTitle("Online Play");
    stage.setScene(scene);
  }
}
