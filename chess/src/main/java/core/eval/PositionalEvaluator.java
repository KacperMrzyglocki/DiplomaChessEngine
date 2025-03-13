package core.eval;

import core.board.Board;
import core.bitboard.Bitboard;

/**
 * Evaluator that considers positional factors like piece placement,
 * pawn structure, king safety, etc.
 */
public class PositionalEvaluator implements Evaluator {
    // Piece-square tables for midgame evaluation (simplified)
    // Positive values favor centralization and key squares
    private static final int[] PAWN_TABLE = {
            0,  0,  0,  0,  0,  0,  0,  0,
            50, 50, 50, 50, 50, 50, 50, 50,
            10, 10, 20, 30, 30, 20, 10, 10,
            5,  5, 10, 25, 25, 10,  5,  5,
            0,  0,  0, 20, 20,  0,  0,  0,
            5, -5,-10,  0,  0,-10, -5,  5,
            5, 10, 10,-20,-20, 10, 10,  5,
            0,  0,  0,  0,  0,  0,  0,  0
    };

    private static final int[] KNIGHT_TABLE = {
            -50,-40,-30,-30,-30,-30,-40,-50,
            -40,-20,  0,  0,  0,  0,-20,-40,
            -30,  0, 10, 15, 15, 10,  0,-30,
            -30,  5, 15, 20, 20, 15,  5,-30,
            -30,  0, 15, 20, 20, 15,  0,-30,
            -30,  5, 10, 15, 15, 10,  5,-30,
            -40,-20,  0,  5,  5,  0,-20,-40,
            -50,-40,-30,-30,-30,-30,-40,-50
    };

    private static final int[] BISHOP_TABLE = {
            -20,-10,-10,-10,-10,-10,-10,-20,
            -10,  0,  0,  0,  0,  0,  0,-10,
            -10,  0, 10, 10, 10, 10,  0,-10,
            -10,  5,  5, 10, 10,  5,  5,-10,
            -10,  0,  5, 10, 10,  5,  0,-10,
            -10,  5,  5,  5,  5,  5,  5,-10,
            -10,  0,  5,  0,  0,  5,  0,-10,
            -20,-10,-10,-10,-10,-10,-10,-20
    };

    private static final int[] ROOK_TABLE = {
            0,  0,  0,  0,  0,  0,  0,  0,
            5, 10, 10, 10, 10, 10, 10,  5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            0,  0,  0,  5,  5,  0,  0,  0
    };

    private static final int[] QUEEN_TABLE = {
            -20,-10,-10, -5, -5,-10,-10,-20,
            -10,  0,  0,  0,  0,  0,  0,-10,
            -10,  0,  5,  5,  5,  5,  0,-10,
            -5,  0,  5,  5,  5,  5,  0, -5,
            0,  0,  5,  5,  5,  5,  0, -5,
            -10,  5,  5,  5,  5,  5,  0,-10,
            -10,  0,  5,  0,  0,  0,  0,-10,
            -20,-10,-10, -5, -5,-10,-10,-20
    };

    private static final int[] KING_MIDGAME_TABLE = {
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -20,-30,-30,-40,-40,-30,-30,-20,
            -10,-20,-20,-20,-20,-20,-20,-10,
            20, 20,  0,  0,  0,  0, 20, 20,
            20, 30, 10,  0,  0, 10, 30, 20
    };

    // Factor to weight the positional evaluation
    private static final int POSITIONAL_WEIGHT = 1;

    @Override
    public int evaluate(Board board) {
        int score = 0;

        // Evaluate piece placement using piece-square tables
        score += evaluatePiecePositions(board);

        // Additional positional factors could be added here:
        // - Pawn structure (doubled, isolated, passed pawns)
        // - King safety
        // - Piece mobility
        // - Control of center
        // - etc.

        return board.isWhiteToMove() ? score : -score;
    }

    private int evaluatePiecePositions(Board board) {
        int whiteScore = 0;
        int blackScore = 0;

        // White pieces evaluation
        whiteScore += evaluateBitboard(board.getWhitePawns(), PAWN_TABLE, false);
        whiteScore += evaluateBitboard(board.getWhiteKnights(), KNIGHT_TABLE, false);
        whiteScore += evaluateBitboard(board.getWhiteBishops(), BISHOP_TABLE, false);
        whiteScore += evaluateBitboard(board.getWhiteRooks(), ROOK_TABLE, false);
        whiteScore += evaluateBitboard(board.getWhiteQueens(), QUEEN_TABLE, false);
        whiteScore += evaluateBitboard(board.getWhiteKing(), KING_MIDGAME_TABLE, false);

        // Black pieces evaluation (flip the table)
        blackScore += evaluateBitboard(board.getBlackPawns(), PAWN_TABLE, true);
        blackScore += evaluateBitboard(board.getBlackKnights(), KNIGHT_TABLE, true);
        blackScore += evaluateBitboard(board.getBlackBishops(), BISHOP_TABLE, true);
        blackScore += evaluateBitboard(board.getBlackRooks(), ROOK_TABLE, true);
        blackScore += evaluateBitboard(board.getBlackQueens(), QUEEN_TABLE, true);
        blackScore += evaluateBitboard(board.getBlackKing(), KING_MIDGAME_TABLE, true);

        return (whiteScore - blackScore) * POSITIONAL_WEIGHT;
    }

    private int evaluateBitboard(long bitboard, int[] table, boolean flip) {
        int score = 0;
        long bb = bitboard;

        while (bb != 0) {
            int square = Bitboard.getLSB(bb);

            // For black pieces, flip the square index to use the same tables
            int tableIndex = flip ? (63 - square) : square;

            score += table[tableIndex];

            // Clear the least significant bit
            bb &= bb - 1;
        }

        return score;
    }
}