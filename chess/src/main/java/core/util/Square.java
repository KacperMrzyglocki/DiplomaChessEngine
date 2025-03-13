package core.util;

/**
 * Represents a square on the chess board.
 * Provides utilities for square manipulation and conversion between different square representations.
 */
public class Square {
    // Standard chess board has 64 squares (8x8)
    public static final int NUM_SQUARES = 64;

    // File (column) and rank (row) constants
    private static final int NUM_FILES = 8;
    private static final int NUM_RANKS = 8;

    // The internal representation of the square (0-63)
    private final int square;

    public static final int A1 = 0;
    public static final int B1 = 1;
    public static final int C1 = 2;
    public static final int D1 = 3;
    public static final int E1 = 4;
    public static final int F1 = 5;
    public static final int G1 = 6;
    public static final int H1 = 7;
    public static final int A2 = 8;
    public static final int B2 = 9;
    public static final int C2 = 10;
    public static final int D2 = 11;
    public static final int E2 = 12;
    public static final int F2 = 13;
    public static final int G2 = 14;
    public static final int H2 = 15;
    public static final int A3 = 16;
    public static final int B3 = 17;
    public static final int C3 = 18;
    public static final int D3 = 19;
    public static final int E3 = 20;
    public static final int F3 = 21;
    public static final int G3 = 22;
    public static final int H3 = 23;
    public static final int A4 = 24;
    public static final int B4 = 25;
    public static final int C4 = 26;
    public static final int D4 = 27;
    public static final int E4 = 28;
    public static final int F4 = 29;
    public static final int G4 = 30;
    public static final int H4 = 31;
    public static final int A5 = 32;
    public static final int B5 = 33;
    public static final int C5 = 34;
    public static final int D5 = 35;
    public static final int E5 = 36;
    public static final int F5 = 37;
    public static final int G5 = 38;
    public static final int H5 = 39;
    public static final int A6 = 40;
    public static final int B6 = 41;
    public static final int C6 = 42;
    public static final int D6 = 43;
    public static final int E6 = 44;
    public static final int F6 = 45;
    public static final int G6 = 46;
    public static final int H6 = 47;
    public static final int A7 = 48;
    public static final int B7 = 49;
    public static final int C7 = 50;
    public static final int D7 = 51;
    public static final int E7 = 52;
    public static final int F7 = 53;
    public static final int G7 = 54;
    public static final int H7 = 55;
    public static final int A8 = 56;
    public static final int B8 = 57;
    public static final int C8 = 58;
    public static final int D8 = 59;
    public static final int E8 = 60;
    public static final int F8 = 61;
    public static final int G8 = 62;
    public static final int H8 = 63;

    /**
     * Creates a square from a 0-63 index.
     *
     * @param square The square index (0-63)
     */
    public Square(int square) {
        this.square = square;
    }

    /**
     * Creates a square from file and rank coordinates.
     *
     * @param file The file (0-7, where 0 is 'a' and 7 is 'h')
     * @param rank The rank (0-7, where 0 is '1' and 7 is '8')
     */
    public Square(int file, int rank) {
        this.square = rank * NUM_FILES + file;
    }

    /**
     * Creates a square from a 0-63 index.
     *
     * @param index The square index (0-63)
     * @return A new Square object
     */
    public static Square fromIndex(int index) {
        return new Square(index);
    }

    /**
     * Creates a square from algebraic notation.
     *
     * @param algebraic The algebraic notation (e.g., "e4")
     * @return A new Square object
     */
    public static Square fromAlgebraic(String algebraic) {
        if (algebraic == null || algebraic.length() != 2) {
            throw new IllegalArgumentException("Invalid algebraic notation: " + algebraic);
        }

        char fileChar = algebraic.charAt(0);
        char rankChar = algebraic.charAt(1);

        int file = fileChar - 'a';
        int rank = rankChar - '1';

        if (file < 0 || file >= NUM_FILES || rank < 0 || rank >= NUM_RANKS) {
            throw new IllegalArgumentException("Invalid algebraic notation: " + algebraic);
        }

        return new Square(file, rank);
    }

    /**
     * Gets the file (column) of this square.
     *
     * @return The file (0-7)
     */
    public int getFile() {
        return square % NUM_FILES;
    }

    /**
     * Gets the rank (row) of this square.
     *
     * @return The rank (0-7)
     */
    public int getRank() {
        return square / NUM_FILES;
    }

    /**
     * Gets the internal representation of this square.
     *
     * @return The square index (0-63)
     */
    public int getSquare() {
        return square;
    }

    /**
     * Gets the square index (0-63).
     * Alias for getSquare() to maintain compatibility with GUI code.
     *
     * @return The square index (0-63)
     */
    public int getIndex() {
        return square;
    }

    /**
     * Converts this square to algebraic notation.
     *
     * @return The algebraic notation of this square
     */
    public String toAlgebraic() {
        char file = (char) ('a' + getFile());
        char rank = (char) ('1' + getRank());
        return "" + file + rank;
    }

    /**
     * Checks if two squares are on the same file.
     *
     * @param other The other square
     * @return true if on the same file, false otherwise
     */
    public boolean isSameFile(Square other) {
        return getFile() == other.getFile();
    }

    /**
     * Checks if two squares are on the same rank.
     *
     * @param other The other square
     * @return true if on the same rank, false otherwise
     */
    public boolean isSameRank(Square other) {
        return getRank() == other.getRank();
    }

    /**
     * Checks if two squares are on the same diagonal.
     *
     * @param other The other square
     * @return true if on the same diagonal, false otherwise
     */
    public boolean isSameDiagonal(Square other) {
        return Math.abs(getFile() - other.getFile()) == Math.abs(getRank() - other.getRank());
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