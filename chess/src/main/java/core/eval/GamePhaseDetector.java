package core.eval;
import core.bitboard.Bitboard;
import core.board.Board;

public class GamePhaseDetector {
    private static final int OPENING_MATERIAL_THRESHOLD = 2800; // Both sides have most pieces
    private static final int ENDGAME_MATERIAL_THRESHOLD = 1500; // Significant material reduction

    public enum GamePhase {
        OPENING,
        MIDDLEGAME,
        ENDGAME
    }

    public static GamePhase detectPhase(Board board) {
        int totalMaterial = getTotalMaterial(board);

        if (totalMaterial >= OPENING_MATERIAL_THRESHOLD) {
            return GamePhase.OPENING;
        } else if (totalMaterial <= ENDGAME_MATERIAL_THRESHOLD) {
            return GamePhase.ENDGAME;
        } else {
            return GamePhase.MIDDLEGAME;
        }
    }

    private static int getTotalMaterial(Board board) {
        // Calculate total material on board excluding kings
        // Material values match those in MaterialEvaluator
        int totalMaterial =
                Bitboard.popCount(board.getWhitePawns() | board.getBlackPawns()) * 100 +
                        Bitboard.popCount(board.getWhiteKnights() | board.getBlackKnights()) * 320 +
                        Bitboard.popCount(board.getWhiteBishops() | board.getBlackBishops()) * 330 +
                        Bitboard.popCount(board.getWhiteRooks() | board.getBlackRooks()) * 500 +
                        Bitboard.popCount(board.getWhiteQueens() | board.getBlackQueens()) * 900;

        return totalMaterial;
    }
}
