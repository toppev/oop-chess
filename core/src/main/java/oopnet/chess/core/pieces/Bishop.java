package oopnet.chess.core.pieces;

import oopnet.chess.core.Chessboard;
import oopnet.chess.core.MovesBuilder;
import oopnet.chess.core.Position;

import java.util.Set;

public class Bishop extends ChessPiece {

    public Bishop(Color color, Position position) {
        super(color, position);
    }

    @Override
    public String getIcon() {
        return getColor() == Color.BLACK ? "♝" : "♗";
    }

    @Override
    public Set<Position> getLegalMovePositions(Chessboard chessboard, Position position) {
        return new MovesBuilder(chessboard, position, getColor())
                .function(i -> i + 1, i -> i + 1)
                .function(i -> i - 1, i -> i - 1)
                .function(i -> i + 1, i -> i - 1)
                .function(i -> i - 1, i -> i + 1)
                .getLegalPositions();
    }

    @Override
    public char getNotationLetter() {
        return 'B';
    }
}
