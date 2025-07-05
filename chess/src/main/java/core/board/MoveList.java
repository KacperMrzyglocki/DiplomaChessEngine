package core.board;

/**
 * A specialized list for efficiently storing and working with chess moves
 */
public class MoveList {
    private final Move[] moves;
    private int size;

    public MoveList(int capacity) {
        this.moves = new Move[capacity];
        this.size = 0;
    }
    public void add(Move move) {
        if (size < moves.length) {
            moves[size++] = move;
        }
    }
    public Move get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        return moves[index];
    }
    public int size() {
        return size;
    }
    public void clear() {
        size = 0;
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