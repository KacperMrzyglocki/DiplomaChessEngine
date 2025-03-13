package core.board;

/**
 * A specialized list for efficiently storing and working with chess moves
 */
public class MoveList {
    private final Move[] moves;
    private int size;

    /**
     * Create a new move list with the specified capacity
     */
    public MoveList(int capacity) {
        this.moves = new Move[capacity];
        this.size = 0;
    }

    /**
     * Add a move to the list
     */
    public void add(Move move) {
        if (size < moves.length) {
            moves[size++] = move;
        }
    }

    /**
     * Get a move at the specified index
     */
    public Move get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        return moves[index];
    }

    /**
     * Get the current number of moves in the list
     */
    public int size() {
        return size;
    }

    /**
     * Clear the move list
     */
    public void clear() {
        size = 0;
    }

    /**
     * Check if the list is empty
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Check if the list contains a specific move
     */
    public boolean contains(Move move) {
        for (int i = 0; i < size; i++) {
            if (moves[i].equals(move)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sort the move list using a simple heuristic score (captured piece value, promotion, etc.)
     * This helps improve alpha-beta pruning effectiveness
     */
    public void sort() {
        // Simple bubble sort for demonstration purposes
        // In a real engine, you'd use a more efficient sorting algorithm
        boolean swapped;
        for (int i = 0; i < size - 1; i++) {
            swapped = false;
            for (int j = 0; j < size - i - 1; j++) {
                if (getMoveScore(moves[j]) < getMoveScore(moves[j + 1])) {
                    // Swap moves
                    Move temp = moves[j];
                    moves[j] = moves[j + 1];
                    moves[j + 1] = temp;
                    swapped = true;
                }
            }
            if (!swapped) break;
        }
    }

    /**
     * Get a simple score for a move to help with move ordering
     * Higher scores should be searched first
     */
    private int getMoveScore(Move move) {
        // Simple heuristic: promotions > captures > normal moves
        if (move.getMoveType() == Move.PAWN_PROMOTION) {
            return 10000 + move.getPromotionPieceType();
        } else if (move.getMoveType() == Move.EN_PASSANT) {
            return 5000;
        } else if (move.getMoveType() == Move.CASTLING) {
            return 3000;
        }
        // Captures would normally be scored based on MVV-LVA (Most Valuable Victim - Least Valuable Attacker)
        // But we don't have that information here without the board state
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MoveList [size=").append(size).append(", moves=[");
        for (int i = 0; i < size; i++) {
            if (i > 0) sb.append(", ");
            sb.append(moves[i]);
        }
        sb.append("]]");
        return sb.toString();
    }
}