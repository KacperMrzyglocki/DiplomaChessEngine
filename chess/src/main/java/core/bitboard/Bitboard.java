package core.bitboard;

/**
 * Utility class for bitboard operations
 */
public class Bitboard {
    public static final long WHITE_SQUARES = 0x55AA55AA55AA55AAL;
    public static long getBit(int square) {
        return 1L << square;
    }
    public static boolean isBitSet(long bitboard, int square) {
        return (bitboard & getBit(square)) != 0;
    }
    public static int getLSB(long bitboard) {
        return Long.numberOfTrailingZeros(bitboard);
    }
    public static long popLSB(long bitboard) {
        return bitboard & (bitboard - 1);
    }
    public static int popCount(long bitboard) {
        return Long.bitCount(bitboard);
    }
    public static long createFileMask(int file) {
        if (file < 0 || file > 7) {
            throw new IllegalArgumentException("File must be between 0 and 7");
        }
        return 0x0101010101010101L << file;
    }
}
