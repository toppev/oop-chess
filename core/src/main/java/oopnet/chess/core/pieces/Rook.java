package oopnet.chess.core.pieces;

import oopnet.chess.core.Chessboard;
import oopnet.chess.core.MovesBuilder;
import oopnet.chess.core.Position;

import java.util.Set;

import static java.util.function.IntUnaryOperator.identity;

public class Rook extends ChessPiece {


    public Rook(Color color, Position position) {
        super(color, position);
    }

    @Override
    public Set<Position> getLegalMovePositions(Chessboard chessboard, Position position) {
        Set<Position> list = new MovesBuilder(chessboard, position, getColor())
                .function(f -> f + 1, identity())
                .function(f -> f - 1, identity())
                .function(identity(), f -> f + 1)
                .function(identity(), f -> f - 1)
                .getLegalPositions();
        return list;
    }

    @Override
    public char getNotationLetter() {
        return 'R';
    }


    @Override
    public String getIcon() {
        return getColor() == Color.BLACK ? "♜" : "♖";
    }
}
