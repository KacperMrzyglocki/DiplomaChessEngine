package core.eval;

import core.bitboard.Bitboard;
import core.board.Board;

public class KingSafetyEvaluator implements Evaluator {
    private static final int KING_SHIELD_BONUS = 10; // Bonus for each pawn shielding the king
    private static final int KING_OPEN_FILE_PENALTY = -30; // Penalty for king on open file
    private static final int KING_SEMI_OPEN_FILE_PENALTY = -15; // Penalty for king on semi-open file
    private static final int CASTLED_BONUS = 50; // Bonus for having castled
    private static final int PAWN_STORM_PENALTY = -10; // Penalty for each enemy pawn advancing toward king
    private static final int QUEEN_TROPISM_PENALTY = -5; // Penalty per square of proximity of enemy queen to king

    @Override
    public int evaluate(Board board) {
        int whiteKingSafety = evaluateKingSafety(board, true);
        int blackKingSafety = evaluateKingSafety(board, false);

        int safetyScore = whiteKingSafety - blackKingSafety;
        return board.isWhiteToMove() ? safetyScore : -safetyScore;
    }

    private int evaluateKingSafety(Board board, boolean isWhite) {
        int kingSquare = isWhite ?
                Bitboard.getLSB(board.getWhiteKing()) :
                Bitboard.getLSB(board.getBlackKing());

        int score = 0;

        // Only apply significant king safety evaluation in opening and middlegame
        GamePhaseDetector.GamePhase phase = GamePhaseDetector.detectPhase(board);
        if (phase == GamePhaseDetector.GamePhase.ENDGAME) {
            return score; // Minimal weight in endgame
        }

        // Get king file and rank
        int kingFile = kingSquare % 8;
        int kingRank = kingSquare / 8;

        // Check if king has castled (approximately, based on position)
        boolean hasCastled = false;
        if (isWhite) {
            // White king has likely castled if it's on g1 or c1
            if ((kingFile == 6 && kingRank == 0) || (kingFile == 2 && kingRank == 0)) {
                hasCastled = true;
                score += CASTLED_BONUS;
            }
        } else {
            // Black king has likely castled if it's on g8 or c8
            if ((kingFile == 6 && kingRank == 7) || (kingFile == 2 && kingRank == 7)) {
                hasCastled = true;
                score += CASTLED_BONUS;
            }
        }

        // Check pawn shield in front of king
        score += evaluatePawnShield(board, kingSquare, isWhite);

        // Check open files near king
        score += evaluateKingFilesSafety(board, kingSquare, isWhite);

        // Check enemy pawn storm
        score += evaluatePawnStorm(board, kingSquare, isWhite);

        // Check enemy piece attacks and proximity to king
        score += evaluateEnemyPiecesProximity(board, kingSquare, isWhite);

        return score;
    }

    private int evaluatePawnShield(Board board, int kingSquare, boolean isWhite) {
        int score = 0;
        long friendlyPawns = isWhite ? board.getWhitePawns() : board.getBlackPawns();
        int kingFile = kingSquare % 8;
        int kingRank = kingSquare / 8;

        // Define shield area (pawns in front of king and one square to each side)
        for (int fileOffset = -1; fileOffset <= 1; fileOffset++) {
            int file = kingFile + fileOffset;
            if (file < 0 || file > 7) continue;

            // For white, check ranks 1 and 2 in front of king
            if (isWhite && kingRank < 6) {
                for (int rankOffset = 1; rankOffset <= 2; rankOffset++) {
                    int rank = kingRank + rankOffset;
                    if (rank > 7) continue;

                    int pawnSquare = rank * 8 + file;
                    if (Bitboard.isBitSet(friendlyPawns, pawnSquare)) {
                        score += KING_SHIELD_BONUS;
                        // Bonus decreases with distance from king
                        score -= (rankOffset - 1) * 2;
                    }
                }
            }
            // For black, check ranks 1 and 2 in front of king (which is downward)
            else if (!isWhite && kingRank > 1) {
                for (int rankOffset = 1; rankOffset <= 2; rankOffset++) {
                    int rank = kingRank - rankOffset;
                    if (rank < 0) continue;

                    int pawnSquare = rank * 8 + file;
                    if (Bitboard.isBitSet(friendlyPawns, pawnSquare)) {
                        score += KING_SHIELD_BONUS;
                        // Bonus decreases with distance from king
                        score -= (rankOffset - 1) * 2;
                    }
                }
            }
        }

        return score;
    }

    private int evaluateKingFilesSafety(Board board, int kingSquare, boolean isWhite) {
        int score = 0;
        long whitePawns = board.getWhitePawns();
        long blackPawns = board.getBlackPawns();
        int kingFile = kingSquare % 8;

        // Check king's file
        long kingFileMask = Bitboard.createFileMask(kingFile);
        if ((whitePawns & kingFileMask) == 0 && (blackPawns & kingFileMask) == 0) {
            // Completely open file
            score += KING_OPEN_FILE_PENALTY;
        } else if (isWhite && (whitePawns & kingFileMask) == 0) {
            // Semi-open file (no friendly pawns)
            score += KING_SEMI_OPEN_FILE_PENALTY;
        } else if (!isWhite && (blackPawns & kingFileMask) == 0) {
            // Semi-open file (no friendly pawns)
            score += KING_SEMI_OPEN_FILE_PENALTY;
        }

        // Also check adjacent files
        for (int fileOffset : new int[]{-1, 1}) {
            int adjacentFile = kingFile + fileOffset;
            if (adjacentFile < 0 || adjacentFile > 7) continue;

            long adjacentFileMask = Bitboard.createFileMask(adjacentFile);
            if ((whitePawns & adjacentFileMask) == 0 && (blackPawns & adjacentFileMask) == 0) {
                // Adjacent file is completely open
                score += KING_OPEN_FILE_PENALTY / 2; // Half penalty for adjacent files
            } else if (isWhite && (whitePawns & adjacentFileMask) == 0) {
                // Semi-open adjacent file
                score += KING_SEMI_OPEN_FILE_PENALTY / 2;
            } else if (!isWhite && (blackPawns & adjacentFileMask) == 0) {
                // Semi-open adjacent file
                score += KING_SEMI_OPEN_FILE_PENALTY / 2;
            }
        }

        return score;
    }

    private int evaluatePawnStorm(Board board, int kingSquare, boolean isWhite) {
        int score = 0;
        long enemyPawns = isWhite ? board.getBlackPawns() : board.getWhitePawns();
        int kingFile = kingSquare % 8;
        int kingRank = kingSquare / 8;

        // Check for enemy pawns advancing toward the king's position
        // Define the area to check (2 files on each side of the king)
        for (int fileOffset = -2; fileOffset <= 2; fileOffset++) {
            int file = kingFile + fileOffset;
            if (file < 0 || file > 7) continue;

            long fileMask = Bitboard.createFileMask(file);
            long filePawns = enemyPawns & fileMask;

            if (filePawns != 0) {
                if (isWhite) {
                    // For white king, black pawns coming from above are dangerous
                    // Find the most advanced black pawn in this file
                    int pawnSquare = Bitboard.getLSB(filePawns);
                    int pawnRank = pawnSquare / 8;

                    // Closer pawns are more dangerous
                    int distance = Math.abs(pawnRank - kingRank);
                    if (distance <= 3) {
                        score += PAWN_STORM_PENALTY * (4 - distance);
                    }
                } else {
                    // For black king, white pawns coming from below are dangerous
                    // Find the most advanced white pawn in this file
                    // We need to find the most significant bit for white pawns
                    long pawns = filePawns;
                    int pawnSquare = 0;
                    while (pawns != 0) {
                        pawnSquare = Bitboard.getLSB(pawns);
                        pawns &= pawns - 1; // Clear LSB
                    }
                    int pawnRank = pawnSquare / 8;

                    // Closer pawns are more dangerous
                    int distance = Math.abs(pawnRank - kingRank);
                    if (distance <= 3) {
                        score += PAWN_STORM_PENALTY * (4 - distance);
                    }
                }
            }
        }

        return score;
    }

    private int evaluateEnemyPiecesProximity(Board board, int kingSquare, boolean isWhite) {
        int score = 0;

        // Check enemy queen distance to king (queen tropism)
        long enemyQueens = isWhite ? board.getBlackQueens() : board.getWhiteQueens();

        if (enemyQueens != 0) {
            int queenSquare = Bitboard.getLSB(enemyQueens);
            int queenFile = queenSquare % 8;
            int queenRank = queenSquare / 8;
            int kingFile = kingSquare % 8;
            int kingRank = kingSquare / 8;

            // Calculate Manhattan distance
            int distance = Math.abs(queenFile - kingFile) + Math.abs(queenRank - kingRank);

            // Closer queens are more dangerous
            if (distance <= 5) {
                score += QUEEN_TROPISM_PENALTY * (6 - distance);
            }
        }

        // More detailed attack evaluation could be added here:
        // - Count number of enemy pieces attacking squares around king
        // - Evaluate attacking potential on castled king position
        // - Count defended vs undefended squares in king zone

        return score;
    }
}