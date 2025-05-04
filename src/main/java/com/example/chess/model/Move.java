package com.example.chess.model;

public class Move {
  public final int fromRow, fromCol, toRow, toCol;
  public final PieceType promotion;    // <— null if no promotion

  // existing ctor: no promotion
  public Move(int fromRow, int fromCol, int toRow, int toCol) {
    this(fromRow, fromCol, toRow, toCol, null);
  }

  // new ctor: with promotion
  public Move(int fromRow, int fromCol, int toRow, int toCol, PieceType promotion) {
    this.fromRow  = fromRow;
    this.fromCol  = fromCol;
    this.toRow    = toRow;
    this.toCol    = toCol;
    this.promotion = promotion;
  }
}
