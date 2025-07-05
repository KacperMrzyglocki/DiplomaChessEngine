package core.bitboard;

public class BitboardConstants {
    // Files (vertical columns)
    public static final long FILE_A = 0x0101010101010101L;
    public static final long FILE_B = 0x0202020202020202L;
    public static final long FILE_C = 0x0404040404040404L;
    public static final long FILE_D = 0x0808080808080808L;
    public static final long FILE_E = 0x1010101010101010L;
    public static final long FILE_F = 0x2020202020202020L;
    public static final long FILE_G = 0x4040404040404040L;
    public static final long FILE_H = 0x8080808080808080L;

    // Ranks (horizontal rows)
    public static final long RANK_1 = 0x00000000000000FFL;
    public static final long RANK_2 = 0x000000000000FF00L;
    public static final long RANK_3 = 0x0000000000FF0000L;
    public static final long RANK_4 = 0x00000000FF000000L;
    public static final long RANK_5 = 0x000000FF00000000L;
    public static final long RANK_6 = 0x0000FF0000000000L;
    public static final long RANK_7 = 0x00FF000000000000L;
    public static final long RANK_8 = 0xFF00000000000000L;

    public static final long WHITE_KINGSIDE_CASTLE_MASK = 0x0000000000000060L;   // f1, g1
    public static final long WHITE_QUEENSIDE_CASTLE_MASK = 0x000000000000000EL;  // b1, c1, d1
    public static final long BLACK_KINGSIDE_CASTLE_MASK = 0x6000000000000000L;   // f8, g8
    public static final long BLACK_QUEENSIDE_CASTLE_MASK = 0x0E00000000000000L;  // b8, c8, d8
}