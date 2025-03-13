package core.eval;

import core.board.Board;
import core.bitboard.Bitboard;

/**
 * Basic material-based evaluator
 */
public class MaterialEvaluator implements Evaluator {
    // Material values in centipawns
    private static final int PAWN_VALUE = 100;
    private static final int KNIGHT_VALUE = 320;
    private static final int BISHOP_VALUE = 330;
    private static final int ROOK_VALUE = 500;
    private static final int QUEEN_VALUE = 900;

    @Override
    public int evaluate(Board board) {
        int whiteMaterial =
                Bitboard.popCount(board.getWhitePawns()) * PAWN_VALUE +
                        Bitboard.popCount(board.getWhiteKnights()) * KNIGHT_VALUE +
                        Bitboard.popCount(board.getWhiteBishops()) * BISHOP_VALUE +
                        Bitboard.popCount(board.getWhiteRooks()) * ROOK_VALUE +
                        Bitboard.popCount(board.getWhiteQueens()) * QUEEN_VALUE;

        int blackMaterial =
                Bitboard.popCount(board.getBlackPawns()) * PAWN_VALUE +
                        Bitboard.popCount(board.getBlackKnights()) * KNIGHT_VALUE +
                        Bitboard.popCount(board.getBlackBishops()) * BISHOP_VALUE +
                        Bitboard.popCount(board.getBlackRooks()) * ROOK_VALUE +
                        Bitboard.popCount(board.getBlackQueens()) * QUEEN_VALUE;

        int materialDifference = whiteMaterial - blackMaterial;

        return board.isWhiteToMove() ? materialDifference : -materialDifference;
    }
}
