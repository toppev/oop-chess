package oopnet.chess.core;

import oopnet.chess.core.pieces.ChessPiece;

import java.util.HashSet;
import java.util.Set;
import java.util.function.IntUnaryOperator;

public class MovesBuilder {

    private final Set<Position> legalPositions = new HashSet<>();
    private final Chessboard chessboard;
    private final Position startingPosition;
    private final ChessPiece.Color color;

    public MovesBuilder(Chessboard chessboard, Position startingPosition, ChessPiece.Color color) {
        this.chessboard = chessboard;
        this.startingPosition = startingPosition;
        this.color = color;
    }

    /**
     * Applies the given functions until either the file or the rank is outside the chessboard or there is a piece in
     * the resulting position.
     * <br>
     * For example,
     * <pre>
     *      new LegalMoveBuilder(chessboard,position)
     *          .function(file -> file + 1, Function.identity())
     *          .getLegalPositions();
     * </pre>
     * Would return all positions in the same row. (Assuming there are no other pieces in the row)
     * <br>
     * I.e. all positions where the initial rank is constant
     *
     * @param fileFunc the function that returns the next file from the given current file
     * @param rankFunc the function that returns the next rank from the given current rank
     *
     * @return this for chaining
     */
    public MovesBuilder function(IntUnaryOperator fileFunc, IntUnaryOperator rankFunc) {
        return function(fileFunc, rankFunc, startingPosition.getFileAsInt(), startingPosition.getRank());
    }

    private MovesBuilder function(IntUnaryOperator fileFunc, IntUnaryOperator rankFunc, int file, int rank) {
        // Get new file and rank
        int newFile = fileFunc.applyAsInt(file);
        int newRank = rankFunc.applyAsInt(rank);
        int result = checkBoundaries(newFile, newRank);
        if (result != -1) {
            add(new Position(newRank, newFile));
            // Recursion
            if (result == 0) {
                function(fileFunc, rankFunc, newFile, newRank);
            }
        }
        return this;
    }

    /**
     * Checks whether the file and rank are valid positions and there are no pieces in the position
     *
     * @return -1 if invalid position, 0 if legal and 1 if there's an enemy in the given position
     */
    public int checkBoundaries(int file, int rank) {
        if (!Position.validate(rank, file)) {
            return -1;
        }
        Position pos = new Position(rank, file);
        ChessPiece piece = pos.getPiece(chessboard);
        if (piece != null) {
            return piece.getColor() == this.color ? -1 : 1;
        }
        return 0;
    }

    /**
     * Add the given position to legal moves
     *
     * @param position the position to add
     *
     * @return this for chaining
     */
    public MovesBuilder add(Position position) {
        legalPositions.add(position);
        return this;
    }

    /**
     * Add the given position to legal moves
     *
     * @param file the file of the position
     * @param rank the rank of the position
     *
     * @return this for chaining
     */
    public MovesBuilder add(char file, int rank) {
        return add(new Position(rank, file));
    }

    /**
     * Add the given additional file and rank to the given position. Finally adds the resulting in legal positions
     *
     * @param position       the "starting" position, not null
     * @param additionalFile the int (positive or negative) to add to the positions file
     * @param additionalRank the int (positive or negative) to add to the positions rank
     *
     * @return this for chaining
     *
     * @see #add(char, int)
     * @see #add(Position)
     * @see #addIfLegal(Position, int, int)
     */
    public MovesBuilder add(Position position, int additionalFile, int additionalRank) {
        return add((char) (position.getFile() + additionalFile), position.getRank() + additionalRank);
    }

    /**
     * Create an new position and add it only if it's a legal move
     *
     * @param file the file of the position
     * @param rank the rank of the position
     *
     * @return this for chaining
     *
     * @see #add(char, int)
     * @see #addIfLegal(Position, int, int)
     */
    public MovesBuilder addIfLegal(char file, int rank) {
        if (checkBoundaries(file - 'a' + 1, rank) == 0) {
            add(new Position(rank, file));
        }
        return this;
    }

    /**
     * Add the given additional file and rank to the given position. Finally adds the resulting in legal positions only
     * if it's a legal move
     *
     * @param position       the "starting" position, not null
     * @param additionalFile the int (positive or negative) to add to the positions file
     * @param additionalRank the int (positive or negative) to add to the positions rank
     *
     * @return this for chaining
     *
     * @see #addIfLegal(char, int)
     * @see #add(Position, int, int)
     */
    public MovesBuilder addIfLegal(Position position, int additionalFile, int additionalRank) {
        return addIfLegal((char) (position.getFile() + additionalFile), position.getRank() + additionalRank);
    }

    public Set<Position> getLegalPositions() {
        return legalPositions;
    }

}
