package core.eval;
import core.board.Board;
import core.bitboard.Bitboard;

public class PawnStructureEvaluator implements Evaluator {
    private static final int DOUBLED_PAWN_PENALTY = -10;
    private static final int ISOLATED_PAWN_PENALTY = -20;
    private static final int PASSED_PAWN_BONUS = 20;
    private static final int PROTECTED_PAWN_BONUS = 10;

    @Override
    public int evaluate(Board board) {
        int whiteScore = evaluatePawnStructure(board, true);
        int blackScore = evaluatePawnStructure(board, false);

        int structureScore = whiteScore - blackScore;
        return board.isWhiteToMove() ? structureScore : -structureScore;
    }

    private int evaluatePawnStructure(Board board, boolean isWhite) {
        long pawns = isWhite ? board.getWhitePawns() : board.getBlackPawns();
        long enemyPawns = isWhite ? board.getBlackPawns() : board.getWhitePawns();
        int score = 0;

        // Check for doubled pawns (multiple pawns on the same file)
        for (int file = 0; file < 8; file++) {
            long fileMask = Bitboard.createFileMask(file);
            int pawnsOnFile = Bitboard.popCount(pawns & fileMask);

            if (pawnsOnFile > 1) {
                score += (pawnsOnFile - 1) * DOUBLED_PAWN_PENALTY;
            }
        }

        // Check for isolated and passed pawns
        long tempPawns = pawns;
        while (tempPawns != 0) {
            int square = Bitboard.getLSB(tempPawns);
            int file = square % 8;
            int rank = square / 8;

            if (!isWhite) {
                rank = 7 - rank; // Flip rank for black
            }

            // Check if isolated (no friendly pawns on adjacent files)
            long adjacentFileMask = 0L;
            if (file > 0) adjacentFileMask |= Bitboard.createFileMask(file - 1);
            if (file < 7) adjacentFileMask |= Bitboard.createFileMask(file + 1);

            if ((pawns & adjacentFileMask) == 0) {
                score += ISOLATED_PAWN_PENALTY;
            }

            // Check if passed (no enemy pawns ahead on same or adjacent files)
            long combinedMask = Bitboard.createFileMask(file) | adjacentFileMask;
            boolean isPassed = true;

            if (isWhite) {
                // For white, "ahead" is toward the 8th rank
                long aheadMask = ~0L << (square + 8);
                if ((enemyPawns & combinedMask & aheadMask) != 0) {
                    isPassed = false;
                }
            } else {
                // For black, "ahead" is toward the 1st rank
                long aheadMask = ~0L >>> (64 - square);
                if ((enemyPawns & combinedMask & aheadMask) != 0) {
                    isPassed = false;
                }
            }

            if (isPassed) {
                // Add bonus for passed pawns, with more bonus for advanced pawns
                int advancementBonus = isWhite ? rank : (7 - rank);
                score += PASSED_PAWN_BONUS + (advancementBonus * 5);
            }

            // Check if protected by another pawn
            long protectorMask = 0L;
            if (isWhite) {
                // White pawns are protected by pawns behind them on adjacent files
                if (file > 0) protectorMask |= Bitboard.getBit(square - 9);
                if (file < 7) protectorMask |= Bitboard.getBit(square - 7);
            } else {
                // Black pawns are protected by pawns behind them on adjacent files
                if (file > 0) protectorMask |= Bitboard.getBit(square + 7);
                if (file < 7) protectorMask |= Bitboard.getBit(square + 9);
            }

            if ((pawns & protectorMask) != 0) {
                score += PROTECTED_PAWN_BONUS;
            }

            // Clear the processed pawn
            tempPawns &= tempPawns - 1;
        }

        return score;
    }
}