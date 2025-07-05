package core.util;

/**
 * Represents a square on the chess board.
 * Provides utilities for square manipulation and conversion between different square representations.
 */
public class Square {
    private static final int NUM_FILES = 8;
    private final int square;
    public Square(int square) {
        this.square = square;
    }
    public static Square fromIndex(int index) {
        return new Square(index);
    }
    public int getFile() {
        return square % NUM_FILES;
    }
    public int getRank() {
        return square / NUM_FILES;
    }
    public int getSquare() {
        return square;
    }
    public String toAlgebraic() {
        char file = (char) ('a' + getFile());
        char rank = (char) ('1' + getRank());
        return "" + file + rank;
    }

    @Override
    public String toString() {
        return toAlgebraic();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Square other = (Square) obj;
        return square == other.square;
    }

    @Override
    public int hashCode() {
        return square;
    }
}