package oopnet.chess.core;

import oopnet.chess.core.pieces.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Chessboard implements Serializable {

    //[0][0] is a8, [0][7] h8, [7][7] h1 etc
    private ChessPiece[][] pieces = new ChessPiece[8][8];
    private List<ChessPiece> captured = new ArrayList<>();

    public ChessPiece[][] getPieces() {
        return pieces;
    }

    public void setPieces(ChessPiece[][] pieces) {
        this.pieces = pieces;
    }

    /**
     * Convenience method to get all pieces as a list. Converts the two dimensional array to a list.
     *
     * @return all pieces as a list
     *
     * @see #getPieces()
     */
    public List<ChessPiece> getPiecesList() {
        return Arrays.stream(pieces).flatMap(Arrays::stream).collect(Collectors.toList());
    }

    /**
     * Load the default chessboard
     */
    public void loadDefault() {
        addNonPawns(ChessPiece.Color.BLACK, 8);
        addNonPawns(ChessPiece.Color.WHITE, 1);
        // loops 0..7 and adds pawns, don't touch
        IntStream.range(0, 8).forEach(file -> {
            pieces[6][file] = new Pawn(ChessPiece.Color.WHITE, new Position(2, file + 1));
            pieces[1][file] = new Pawn(ChessPiece.Color.BLACK, new Position(7, file + 1));
        });
    }

    /**
     * Add Rooks, Knights, Bishops, Queen and King
     *
     * @param color the color of the pieces
     * @param rank  the rank (row) where the pieces are added (usually 8 (black) or 1 (white))
     */
    private void addNonPawns(ChessPiece.Color color, int rank) {
        // The index in the matrix
        // Top row is 8 in the board
        int reverseRank = 8 - rank;
        pieces[reverseRank][0] = new Rook(color, new Position(rank, 'a'));
        pieces[reverseRank][7] = new Rook(color, new Position(rank, 'h'));

        pieces[reverseRank][1] = new Knight(color, new Position(rank, 'b'));
        pieces[reverseRank][6] = new Knight(color, new Position(rank, 'g'));

        pieces[reverseRank][2] = new Bishop(color, new Position(rank, 'c'));
        pieces[reverseRank][5] = new Bishop(color, new Position(rank, 'f'));

        pieces[reverseRank][3] = new Queen(color, new Position(rank, 'd'));
        pieces[reverseRank][4] = new King(color, new Position(rank, 'e'));
    }

    /**
     * @return pieces that were captured/dead/eaten and are not in {@link #getPieces()}
     */
    public List<ChessPiece> getCaptured() {
        return captured;
    }

}
