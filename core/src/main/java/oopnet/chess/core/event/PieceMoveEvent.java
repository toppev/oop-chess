package oopnet.chess.core.event;

import oopnet.chess.core.Chessboard;
import oopnet.chess.core.Move;

public class PieceMoveEvent extends Event {

    private Move move;

    public PieceMoveEvent(Move move) {
        this.move = move;
    }

    public Move getMove(Chessboard chessboard) {
        move.setChessboard(chessboard);
        return move;
    }
}
