package core.bitboard;

/**
 * Utility class for bitboard operations
 */
public class Bitboard {
    public static final long WHITE_SQUARES = 0x55AA55AA55AA55AAL;
    /**
     * Get a bitboard with a single bit set at the specified square
     */
    public static long getBit(int square) {
        return 1L << square;
    }

    /**
     * Test if a bit is set at a specific square on a bitboard
     */
    public static boolean isBitSet(long bitboard, int square) {
        return (bitboard & getBit(square)) != 0;
    }

    /**
     * Set a bit at a specific square on a bitboard
     */
    public static long setBit(long bitboard, int square) {
        return bitboard | getBit(square);
    }

    /**
     * Clear a bit at a specific square on a bitboard
     */
    public static long clearBit(long bitboard, int square) {
        return bitboard & ~getBit(square);
    }

    /**
     * Get the least significant bit index from a bitboard
     */
    public static int getLSB(long bitboard) {
        return Long.numberOfTrailingZeros(bitboard);
    }

    /**
     * Remove the least significant bit and return a new bitboard
     */
    public static long popLSB(long bitboard) {
        return bitboard & (bitboard - 1);
    }

    /**
     * Count the number of set bits in a bitboard
     */
    public static int popCount(long bitboard) {
        return Long.bitCount(bitboard);
    }

    /**
     * Print a visual representation of a bitboard (for debugging)
     */
    public static void printBitboard(long bitboard) {
        System.out.println("  +---+---+---+---+---+---+---+---+");

        for (int rank = 7; rank >= 0; rank--) {
            System.out.print((rank + 1) + " |");

            for (int file = 0; file < 8; file++) {
                int square = rank * 8 + file;
                char piece = isBitSet(bitboard, square) ? 'X' : ' ';
                System.out.print(" " + piece + " |");
            }

            System.out.println("\n  +---+---+---+---+---+---+---+---+");
        }

        System.out.println("    a   b   c   d   e   f   g   h  ");
    }
    public static long createFileMask(int file) {
        if (file < 0 || file > 7) {
            throw new IllegalArgumentException("File must be between 0 and 7");
        }
        return 0x0101010101010101L << file;
    }
}
