package core.board;

public class Move {
    // Constants for move types
    public static final int NORMAL = 0;
    public static final int PAWN_PROMOTION = 1;
    public static final int EN_PASSANT = 2;
    public static final int CASTLING = 3;

    // Constants for promotion piece types
    public static final int QUEEN_PROMOTION = 0;
    public static final int ROOK_PROMOTION = 1;
    public static final int BISHOP_PROMOTION = 2;
    public static final int KNIGHT_PROMOTION = 3;

    // Move data packed into an int for efficiency
    // Bit layout:
    // 0-5: From square (0-63)
    // 6-11: To square (0-63)
    // 12-13: Promotion piece type (0-3 for Q, R, B, N)
    // 14-15: Move type (NORMAL, PAWN_PROMOTION, EN_PASSANT, CASTLING)
    private final int moveData;

    /**
     * Create a normal move
     */
    public Move(int from, int to) {
        this.moveData = from | (to << 6) | (NORMAL << 14);
    }

    /**
     * Private constructor used by factory methods
     */
    private Move(int from, int to, int extraData, int moveType) {
        if (moveType == PAWN_PROMOTION) {
            // For promotion moves, extraData is the promotion piece type
            this.moveData = from | (to << 6) | (extraData << 12) | (moveType << 14);
        } else {
            // For other special moves, we don't use the extraData
            this.moveData = from | (to << 6) | (moveType << 14);
        }
    }

    /**
     * Static factory method for promotion moves
     */
    public static Move promotion(int from, int to, int promotionPieceType) {
        return new Move(from, to, promotionPieceType, PAWN_PROMOTION);
    }

    /**
     * Static factory method for special moves (en passant, castling)
     */
    public static Move special(int from, int to, int moveType) {
        if (moveType == PAWN_PROMOTION) {
            throw new IllegalArgumentException("Use promotion() factory method for pawn promotions");
        }
        return new Move(from, to, 0, moveType);
    }

    // Getters
    public int getFrom() {
        return moveData & 0x3F;
    }

    public int getTo() {
        return (moveData >> 6) & 0x3F;
    }

    public int getPromotionPieceType() {
        return (moveData >> 12) & 0x3;
    }

    public int getMoveType() {
        return (moveData >> 14) & 0x3;
    }

    public int getMoveData() {
        return moveData;
    }

    /**
     * Convert the move to algebraic notation (e.g., "e2e4", "a7a8q")
     */
    @Override
    public String toString() {
        String[] files = {"a", "b", "c", "d", "e", "f", "g", "h"};
        String[] ranks = {"1", "2", "3", "4", "5", "6", "7", "8"};
        String[] promotionPieces = {"q", "r", "b", "n"};

        int from = getFrom();
        int to = getTo();

        StringBuilder moveStr = new StringBuilder();
        moveStr.append(files[from % 8]).append(ranks[from / 8]);
        moveStr.append(files[to % 8]).append(ranks[to / 8]);

        if (getMoveType() == PAWN_PROMOTION) {
            moveStr.append(promotionPieces[getPromotionPieceType()]);
        }

        return moveStr.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Move other = (Move) obj;
        return moveData == other.moveData;
    }

    @Override
    public int hashCode() {
        return moveData;
    }
}