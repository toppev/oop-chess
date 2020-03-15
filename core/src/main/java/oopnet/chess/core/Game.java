package oopnet.chess.core;

import oopnet.chess.core.event.PieceMoveEvent;
import oopnet.chess.core.pieces.ChessPiece;

import java.io.Serializable;

public class Game implements Serializable {

    private Chessboard chessboard;
    // The player who is moving now
    // White starts
    private ChessPiece.Color currentPlayer = ChessPiece.Color.WHITE;
    // The player who offered a draw
    private ChessPiece.Color drawOffer;
    private GameResult result;

    public Game(Chessboard chessboard) {
        this.chessboard = chessboard;
    }

    public ChessPiece handleMove(PieceMoveEvent moveEvent) {
        Move move = moveEvent.getMove(chessboard);
        Position to = move.getTo();
        ChessPiece captured = to.getPiece(chessboard);
        if (captured != null) {
            chessboard.getCaptured().add(captured);
        }
        // Remove the piece that was moved
        ChessPiece oldPiece = move.getTo().getPiece(chessboard);
        move.getFrom().setPiece(chessboard, null);
        // Replace the captured piece (or null) with the moved piece
        to.setPiece(chessboard, move.getPiece());
        move.getPiece().setPosition(to);
        setCurrentPlayer(move.getPiece().getColor().getOpposite());
        return oldPiece;
    }

    public Chessboard getChessboard() {
        return chessboard;
    }

    public ChessPiece.Color getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(ChessPiece.Color currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public ChessPiece.Color getDrawOffer() {
        return drawOffer;
    }

    public void setDrawOffer(ChessPiece.Color drawOffer) {
        this.drawOffer = drawOffer;
    }

    public GameResult getResult() {
        return result;
    }

    public enum GameResult {
        DRAW,
        WHITE_WINS,
        BLACK_WINS,
        WHITE_SURRENDERS,
        BLACK_SURRENDERS
    }

}
