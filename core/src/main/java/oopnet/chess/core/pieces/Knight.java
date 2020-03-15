package oopnet.chess.core.pieces;

import oopnet.chess.core.Chessboard;
import oopnet.chess.core.MovesBuilder;
import oopnet.chess.core.Position;

import java.util.Set;

public class Knight extends ChessPiece {

    public Knight(Color color, Position position) {
        super(color, position);
    }

    @Override
    public String getIcon() {
        return getColor() == Color.BLACK ? "♞" : "♘";
    }

    @Override
    public Set<Position> getLegalMovePositions(Chessboard chessboard, Position position) {
        return new MovesBuilder(chessboard, position, getColor())
                .addIfLegal(position, 2, 1)
                .addIfLegal(position, 2, -1)
                .addIfLegal(position, -2, 1)
                .addIfLegal(position, -2, -1)

                .addIfLegal(position, 1, 2)
                .addIfLegal(position, -1, 2)
                .addIfLegal(position, 1, -2)
                .addIfLegal(position, -1, -2)
                .getLegalPositions();
    }

    @Override
    public char getNotationLetter() {
        return 'K';
    }
}
