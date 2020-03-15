package oopnet.chess.core;

import oopnet.chess.core.pieces.ChessPiece;

import java.io.Serializable;
import java.util.Objects;

public class Position implements Serializable {

    private final int rank;
    private final char file;

    /**
     * Create a new Position
     *
     * @param rank a number from 1 to 8
     * @param file a character from 'a' to 'h'
     */
    public Position(int rank, char file) {
        if (rank < 1 || rank > 8) {
            throw new IllegalArgumentException("Position rank must be between 1 and 8");
        }
        if (file < 'a' || file > 'h') {
            throw new IllegalArgumentException("Position file must be between 'a' and 'h'");
        }
        this.rank = rank;
        this.file = file;
    }

    /**
     * Create a new Position
     *
     * @param rank the rank, a number from 1 to 8
     * @param file the file, a number from 1 to 8
     */
    public Position(int rank, int file) {
        if (rank < 1 || rank > 8) {
            throw new IllegalArgumentException("Position rank must be between 1 and 8");
        }
        if (file < 1 || file > 8) {
            throw new IllegalArgumentException("Position file must be between 1 and 8");
        }
        this.rank = rank;
        this.file = (char) ('a' + file - 1);
    }

    public static Position parsePosition(String toParse) {
        if (toParse.length() != 2) {
            throw new IllegalArgumentException("toParse must be exactly 2 characters");
        }
        return new Position(toParse.charAt(0), toParse.charAt(1));
    }

    /**
     * Get the rank of this position, an integer from 1 to 8
     *
     * @return this position's rank
     */
    public int getRank() {
        return rank;
    }

    /**
     * Get the file of this position, an letter from 'a' to 'h' in lowercase ('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h')
     *
     * @return this position's file
     */
    public char getFile() {
        return file;
    }

    /**
     * Get the file as a number from 1 to 8
     *
     * @return the file as an int (1-8)
     */
    public int getFileAsInt() {
        // Subtracting 'a' will give int from 0 to 7, and adding +1 to get 1-8
        return file - 'a' + 1;
    }

    /**
     * A helper method to get the corresponding {@link ChessPiece} on the given {@link Chessboard}
     *
     * @param chessboard the chessboard to use, not null
     *
     * @return the {@link ChessPiece} at this position or null if none
     *
     * @see #getPiece(ChessPiece[][])
     */
    public ChessPiece getPiece(Chessboard chessboard) {
        return getPiece(chessboard.getPieces());
    }

    /**
     * A helper method to get the corresponding {@link ChessPiece} on the given {@link Chessboard}
     *
     * @param pieces the two dimensional {@link ChessPiece} array, not null. [0][0] is a8, [0][7] h8, [7][7] h1 etc
     *
     * @return the {@link ChessPiece} at this position or null if none existing or if the array is wrong size
     *
     * @see #getPiece(Chessboard)
     */
    public ChessPiece getPiece(ChessPiece[][] pieces) {
        // Convert to correct index (as specified in the documentation)
        int i = 7 - getRank() + 1;
        int j = getFileAsInt() - 1;
        if (pieces.length > i && pieces[i].length > j) {
            return pieces[i][j];
        }
        return null;
    }

    /**
     * Set the piece at this position. This also calls {@link ChessPiece#setPosition(Position)}
     *
     * @param chessboard the chessboard to modify
     * @param piece      the piece to set
     */
    public void setPiece(Chessboard chessboard, ChessPiece piece) {
        // Convert to correct index (as specified in the documentation)
        int i = 7 - getRank() + 1;
        int j = getFileAsInt() - 1;
        chessboard.getPieces()[i][j] = piece;
        if (piece != null) {
            piece.setPosition(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Position position = (Position) o;
        return rank == position.rank &&
                file == position.file;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rank, file);
    }

    /**
     * Get the position as a String
     *
     * @return a string consisting of the file and the rank (e.g "a8")
     */
    @Override
    public String toString() {
        // Don't remove the "" or replace with String#valueOf
        // You probably will break it
        return file + "" + rank;
    }

    /**
     * Validates that the given ints are between 1 and 8
     *
     * @param rank the rank to validate
     * @param file the file to validate
     *
     * @return true if both rank and file are valida, otherwise false
     */
    public static boolean validate(int rank, int file) {
        return rank >= 1 && rank <= 8 && file >= 1 && file <= 8;
    }

    public static boolean validate(int rank, char file) {
        return validate(rank, file - 'a' + 1);
    }

}
