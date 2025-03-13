package core.eval;
import core.bitboard.Bitboard;
import core.board.Board;

public class EndgameEvaluator implements Evaluator {
    // Different piece-square tables for endgame
    private static final int[] KING_ENDGAME_TABLE = {
            -50,-40,-30,-20,-20,-30,-40,-50,
            -30,-20,-10,  0,  0,-10,-20,-30,
            -30,-10, 20, 30, 30, 20,-10,-30,
            -30,-10, 30, 40, 40, 30,-10,-30,
            -30,-10, 30, 40, 40, 30,-10,-30,
            -30,-10, 20, 30, 30, 20,-10,-30,
            -30,-30,  0,  0,  0,  0,-30,-30,
            -50,-30,-30,-30,-30,-30,-30,-50
    };

    @Override
    public int evaluate(Board board) {
        // Only apply significant weight in endgame situations
        if (GamePhaseDetector.detectPhase(board) != GamePhaseDetector.GamePhase.ENDGAME) {
            return 0;
        }

        int score = 0;

        // Evaluate king centralization in endgame
        score += evaluateKingCentralization(board);

        // Evaluate passed pawns (more important in endgame)
        score += evaluatePassedPawns(board);

        return board.isWhiteToMove() ? score : -score;
    }

    private int evaluateKingCentralization(Board board) {
        int score = 0;

        // Get king positions
        long whiteKing = board.getWhiteKing();
        long blackKing = board.getBlackKing();

        if (whiteKing != 0) {
            int whiteKingSquare = Bitboard.getLSB(whiteKing);
            score += KING_ENDGAME_TABLE[whiteKingSquare];
        }

        if (blackKing != 0) {
            int blackKingSquare = Bitboard.getLSB(blackKing);
            // For black, we flip the square to use the same table
            score -= KING_ENDGAME_TABLE[63 - blackKingSquare];
        }

        return score;
    }

    private int evaluatePassedPawns(Board board) {
        int score = 0;

        // White passed pawns
        long whitePawns = board.getWhitePawns();
        long blackPawns = board.getBlackPawns();

        // Evaluate each white pawn
        long tempPawns = whitePawns;
        while (tempPawns != 0) {
            int square = Bitboard.getLSB(tempPawns);
            int file = square % 8;
            int rank = square / 8;

            // Check if it's a passed pawn (no enemy pawns ahead on same or adjacent files)
            boolean isPassed = true;

            // Create a mask for the current file and adjacent files
            long fileMask = Bitboard.createFileMask(file);
            long adjacentMask = 0L;
            if (file > 0) adjacentMask |= Bitboard.createFileMask(file - 1);
            if (file < 7) adjacentMask |= Bitboard.createFileMask(file + 1);

            // Combine all three files
            long filesMask = fileMask | adjacentMask;

            // Create a mask for all squares ahead of this pawn for white
            long aheadMask = ~0L << (square + 8);

            // If there are any black pawns ahead in these files, it's not passed
            if ((blackPawns & filesMask & aheadMask) != 0) {
                isPassed = false;
            }

            if (isPassed) {
                // The further advanced a passed pawn is, the more valuable it becomes
                score += 20 + (rank * 10); // More bonus for pawns closer to promotion
            }

            // Clear the processed pawn
            tempPawns &= tempPawns - 1;
        }

        // Evaluate each black pawn
        tempPawns = blackPawns;
        while (tempPawns != 0) {
            int square = Bitboard.getLSB(tempPawns);
            int file = square % 8;
            int rank = 7 - (square / 8); // Flipped for black

            // Check if it's a passed pawn
            boolean isPassed = true;

            // Create masks
            long fileMask = Bitboard.createFileMask(file);
            long adjacentMask = 0L;
            if (file > 0) adjacentMask |= Bitboard.createFileMask(file - 1);
            if (file < 7) adjacentMask |= Bitboard.createFileMask(file + 1);
            long filesMask = fileMask | adjacentMask;

            // For black pawns, "ahead" means toward the first rank
            long aheadMask = ~0L >>> (64 - square);

            // If there are any white pawns ahead in these files, it's not passed
            if ((whitePawns & filesMask & aheadMask) != 0) {
                isPassed = false;
            }

            if (isPassed) {
                score -= 20 + (rank * 10); // More penalty for black pawns closer to promotion
            }

            // Clear the processed pawn
            tempPawns &= tempPawns - 1;
        }

        return score;
    }
}
