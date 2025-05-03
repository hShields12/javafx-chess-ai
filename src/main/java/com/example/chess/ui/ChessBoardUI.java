package com.example.chess.ui;

import com.example.chess.logic.Board;
import com.example.chess.logic.AI;
import com.example.chess.model.Move;
import com.example.chess.model.Color;
import com.example.chess.model.Piece;
import com.example.chess.model.PieceType;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChessBoardUI {
  private final Stage stage;
  private final String player1, player2;
  private Board board;
  private AI ai;
  private final GridPane grid = new GridPane();
  private final StackPane rootStack = new StackPane();
  private BorderPane mainPane;
  private int selRow = -1, selCol = -1;
  private boolean gameOver = false;
  private Move lastMove = null;
  private final Map<String, Image> cache = new HashMap<>();
  private final Label timer1 = new Label("10:00");
  private final Label timer2 = new Label("10:00");
  private final ListView<String> moveList = new ListView<>();
  private final FlowPane captured1 = new FlowPane(5, 5);
  private final FlowPane captured2 = new FlowPane(5, 5);
  private Timeline timeline;
  private boolean whiteTurn = true;
  private int seconds1 = 600, seconds2 = 600;
  private MediaPlayer musicPlayer;
  private boolean muted = false;

  public ChessBoardUI(Stage stage, String player1, String player2) {
    this.stage = stage;
    this.player1 = player1;
    this.player2 = player2;
    captured1.setPrefWrapLength(240);
    captured2.setPrefWrapLength(240);
  }

  /** Initialize new game state and draw first board. */
  public Parent getRoot() {
    board = new Board();
    ai = new AI();
    selRow = selCol = -1;
    gameOver = false;
    lastMove = null;
    setupMusic();

    grid.getRowConstraints().clear();
    grid.getColumnConstraints().clear();
    for (int i = 0; i < 8; i++) {
      RowConstraints rc = new RowConstraints(60);
      rc.setVgrow(Priority.NEVER);
      grid.getRowConstraints().add(rc);
      ColumnConstraints cc = new ColumnConstraints(60);
      cc.setHgrow(Priority.NEVER);
      grid.getColumnConstraints().add(cc);
    }

    grid.setHgap(0);
    grid.setVgap(0);
    grid.setPadding(Insets.EMPTY);

    if (!player2.equals("Computer")) startTimer();
    draw();

    mainPane = new BorderPane();

    HBox top = new HBox(50);
    if (player2.equals("Computer")) {
      top.getChildren().addAll(new Label(player1), new Label("vs"), new Label(player2));
    } else
    {
      top.getChildren().addAll(new VBox(new Label(player1), timer1), new VBox(new Label(player2), timer2));
    }
    top.setAlignment(Pos.CENTER);
    top.setPadding(new Insets(5));
    mainPane.setTop(top);

    //board and co ords
    VBox ranks = new VBox(0);
    for (int r = 8; r >= 1; r--) {
      Label lbl = new Label(String.valueOf(r));
      lbl.setMinSize(20, 60);
      lbl.setAlignment(Pos.CENTER_RIGHT);
      ranks.getChildren().add(lbl);
    }
    HBox files = new HBox(0);
    for (char f = 'a'; f <= 'h'; f++) {
      Label lbl = new Label(String.valueOf(f));
      lbl.setMinSize(60, 20);
      lbl.setAlignment(Pos.CENTER);
      files.getChildren().add(lbl);
    }
    BorderPane boardPane = new BorderPane();
    boardPane.setCenter(grid);
    boardPane.setLeft(ranks);
    boardPane.setBottom(files);

    // captures
    Label cap1Label = new Label(player1 + " captured:");
    Label cap2Label = new Label(player2 + " captured:");
    ScrollPane sp1 = new ScrollPane(captured1);
    sp1.setPrefViewportHeight(50);
    sp1.setPrefViewportWidth(240);
    sp1.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    ScrollPane sp2 = new ScrollPane(captured2);
    sp2.setPrefViewportHeight(50);
    sp2.setPrefViewportWidth(240);
    sp2.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    VBox capturedBox = new VBox(5, cap1Label, sp1, cap2Label, sp2);
    capturedBox.setAlignment(Pos.CENTER);

    VBox centerBox = new VBox(10, boardPane, capturedBox);
    centerBox.setAlignment(Pos.CENTER);
    centerBox.setPadding(new Insets(10));
    mainPane.setCenter(centerBox);

    //  moves
    moveList.setPrefHeight(8 * 60);
    VBox right = new VBox(5, new Label("Moves"), moveList);
    right.setPadding(new Insets(10));
    mainPane.setRight(right);

    // menu/mute
    Button back = new Button("Back to Menu");
    back.setOnAction(e -> {
      Alert a = new Alert(Alert.AlertType.CONFIRMATION,
              "Return to main menu?", ButtonType.YES, ButtonType.NO);
      a.setHeaderText(null);
      a.showAndWait().ifPresent(b -> {
        if (b == ButtonType.YES) {
          if (timeline != null) timeline.stop();
          if (musicPlayer != null) musicPlayer.stop();
          new ChessUI(stage).show();
        }
      });
    });
    Button mute = new Button("Mute");
    mute.setOnAction(e -> {
      muted = !muted;
      if (musicPlayer != null) musicPlayer.setMute(muted);
      mute.setText(muted ? "Unmute" : "Mute");
    });
    HBox bottom = new HBox(20, back, mute);
    bottom.setAlignment(Pos.CENTER);
    bottom.setPadding(new Insets(10));
    mainPane.setBottom(bottom);

    rootStack.getChildren().setAll(mainPane);
    return rootStack;
  }

  private void setupMusic() {
    try {
      Media m = new Media(getClass().getResource("/music/loop.mp3").toExternalForm());
      musicPlayer = new MediaPlayer(m);
      musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
      musicPlayer.play();
    } catch (Exception ignored) {}
  }

  private void startTimer() {
    timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
      if (whiteTurn) {
        seconds1--;
        timer1.setText(format(seconds1));
        if (seconds1 <= 0) endOnTime(player2);
      } else {
        seconds2--;
        timer2.setText(format(seconds2));
        if (seconds2 <= 0) endOnTime(player1);
      }
    }));
    timeline.setCycleCount(Timeline.INDEFINITE);
    timeline.play();
  }

  private void endOnTime(String winner) {
    if (timeline != null) timeline.stop();
    showGameOver(winner);
  }

  private String format(int secs) {
    int m = secs / 60, s = secs % 60;
    return String.format("%d:%02d", m, s);
  }

  /** Draws the 8×8 board with pieces and highlights. */
  private void draw() {
    grid.getChildren().clear();
    Set<String> highlights = new HashSet<>();
    if (selRow >= 0) {
      Piece p = board.getPiece(selRow, selCol);
      if (p != null) {
        for (Move m : board.generateLegalMoves(p.getColor())) {
          if (m.fromRow == selRow && m.fromCol == selCol) {
            highlights.add(m.toRow + "," + m.toCol);
          }
        }
      }
    }

    for (int r = 0; r < 8; r++) {
      for (int c = 0; c < 8; c++) {
        StackPane cell = new StackPane();
        Rectangle bg = new Rectangle(60, 60,
                Paint.valueOf((r + c) % 2 == 0 ? "#EEEED2" : "#769656"));

        if (lastMove != null &&
                ((r == lastMove.fromRow && c == lastMove.fromCol) ||
                        (r == lastMove.toRow && c == lastMove.toCol))) {
          bg.setStroke(Paint.valueOf("yellow"));
          bg.setStrokeWidth(3);
        }
        if (r == selRow && c == selCol) {
          bg.setStroke(Paint.valueOf("red"));
          bg.setStrokeWidth(3);
        }
        cell.getChildren().add(bg);

        Piece p = board.getPiece(r, c);
        if (p != null) {
          String key = (p.getColor() == Color.WHITE ? "white_" : "black_")
                  + p.getType().name().toLowerCase() + ".png";
          Image img = cache.computeIfAbsent(key,
                  k -> new Image(getClass().getResourceAsStream("/images/" + k)));
          ImageView iv = new ImageView(img);
          iv.setFitWidth(50);
          iv.setFitHeight(50);
          cell.getChildren().add(iv);
        }

        if (highlights.contains(r + "," + c)) {
          Circle dot = new Circle(5, Paint.valueOf("#4285F4"));
          cell.getChildren().add(dot);
        }

        final int rr = r, cc = c;
        cell.setOnMouseClicked(e -> handleClick(rr, cc));
        grid.add(cell, c, r);
      }
    }
  }
  /** Handle a click at (r,c): select/move or ignore if game over. */
  private void handleClick(int r, int c) {
    if (gameOver) return;
    // 1) First click: select piece
    if (selRow < 0) {
      Piece p = board.getPiece(r, c);
      if (p != null && p.getColor() == (whiteTurn ? Color.WHITE : Color.BLACK)) {
        selRow = r;
        selCol = c;
        draw();
      }
      return;
    }

    // 2) Second click: build humanMove (with promotion dialog if needed)
    Piece target = board.getPiece(r, c);
    Piece moving = board.getPiece(selRow, selCol);
    Move move;
    if (moving.getType() == PieceType.PAWN && (r == 0 || r == 7)) {
      ChoiceDialog<PieceType> dlg = new ChoiceDialog<>(
              PieceType.QUEEN,
              PieceType.QUEEN, PieceType.ROOK,
              PieceType.BISHOP, PieceType.KNIGHT
      );
      dlg.setTitle("Pawn Promotion");
      dlg.setHeaderText("Your pawn has reached the last rank!");
      dlg.setContentText("Choose a piece:");
      move = new Move(selRow, selCol, r, c, dlg.showAndWait().orElse(PieceType.QUEEN));
    } else {
      move = new Move(selRow, selCol, r, c);
    }
    // 3) Attempt the move
    if (board.isLegal(move)) {
      board.applyMove(move);
      lastMove = move;

      if (target != null) {
        ImageView cap = new ImageView(
                cache.get((target.getColor() == Color.WHITE ? "white_" : "black_")
                        + target.getType().name().toLowerCase() + ".png"));
        cap.setFitWidth(20);
        cap.setFitHeight(20);
        (target.getColor() == Color.WHITE ? captured1 : captured2).getChildren().add(cap);
      }

      moveList.getItems().add((whiteTurn ? player1 : player2) + ": " +
              coord(selRow, selCol) + "→" + coord(r, c));

      whiteTurn = !whiteTurn;
      selRow = selCol = -1;
      draw();

      if (board.isInCheckmate(whiteTurn ? Color.WHITE : Color.BLACK)) {
        gameOver = true;
        showGameOver(whiteTurn ? player2 : player1);
        return;
      }

      if (player2.equals("Computer")) {
        Move aiMove = ai.nextMove(board);
        Piece aiTarget = board.getPiece(aiMove.toRow, aiMove.toCol);
        board.applyMove(aiMove);
        lastMove = aiMove;

        if (aiTarget != null) {
          ImageView cap2 = new ImageView(
                  cache.get((aiTarget.getColor() == Color.WHITE ? "white_" : "black_")
                          + aiTarget.getType().name().toLowerCase() + ".png"));
          cap2.setFitWidth(20);
          cap2.setFitHeight(20);
          (aiTarget.getColor() == Color.WHITE ? captured1 : captured2).getChildren().add(cap2);
        }

        moveList.getItems().add(player2 + ": " +
                coord(aiMove.fromRow, aiMove.fromCol) + "→" + coord(aiMove.toRow, aiMove.toCol));

        whiteTurn = !whiteTurn;
        draw();

        if (board.isInCheckmate(whiteTurn ? Color.WHITE : Color.BLACK)) {
          gameOver = true;
          showGameOver(whiteTurn ? player2 : player1);
        }
      }
    } else {
      // 4) Clear selection & redraw
      selRow = selCol = -1;
      draw();
    }
  }

  private String coord(int row, int col) {
    return "" + (char)('a' + col) + (char)('8' - row);
  }

  private void showGameOver(String winner) {
    if (timeline != null) timeline.stop();
    Pane overlay = new Pane();
    overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5)");
    overlay.setPrefSize(rootStack.getWidth(), rootStack.getHeight());
    VBox box = new VBox(20);
    box.setAlignment(Pos.CENTER);
    box.setPadding(new Insets(10));
    box.setStyle("-fx-background-color: white; -fx-border-color: black;");
    Label msg = new Label("Congratulations, " + winner + "!");
    Button again = new Button("Play Again");
    again.setOnAction(e -> {
      overlay.setVisible(false);
      timeline.stop();
      musicPlayer.stop();
      ChessBoardUI fresh = new ChessBoardUI(stage, player1, player2);
      stage.getScene().setRoot(fresh.getRoot());
    });
    Button exit = new Button("Exit");
    exit.setOnAction(e -> Platform.exit());

    box.getChildren().addAll(msg, again, exit);
    box.setLayoutX((rootStack.getWidth() - 200) / 2);
    box.setLayoutY((rootStack.getHeight() - 150) / 2);
    overlay.getChildren().add(box);
    rootStack.getChildren().add(overlay);
  }
}
