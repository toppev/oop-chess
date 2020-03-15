package oopnet.chess.core.pieces;

import oopnet.chess.core.Chessboard;
import oopnet.chess.core.MovesBuilder;
import oopnet.chess.core.Position;

import java.util.Set;

import static java.util.function.IntUnaryOperator.identity;

public class Queen extends ChessPiece {

    public Queen(Color color, Position position) {
        super(color, position);
    }

    @Override
    public String getIcon() {
        return getColor() == Color.BLACK ? "♛" : "♕";
    }

    @Override
    public Set<Position> getLegalMovePositions(Chessboard chessboard, Position position) {
        return new MovesBuilder(chessboard, position, getColor())
                .function(i -> i + 1, i -> i + 1)
                .function(i -> i - 1, i -> i - 1)
                .function(i -> i + 1, i -> i - 1)
                .function(i -> i - 1, i -> i + 1)

                .function(f -> f + 1, identity())
                .function(f -> f - 1, identity())
                .function(identity(), f -> f + 1)
                .function(identity(), f -> f - 1)
                .getLegalPositions();
    }

    @Override
    public char getNotationLetter() {
        return 'Q';
    }
}
