package core.fen;

/**
 * Utility for comparing and manipulating FEN strings
 */
public class FenUtility {

    /**
     * Check if two FEN strings represent the same position
     * (ignoring move counters)
     *
     * @param fen1 First FEN string
     * @param fen2 Second FEN string
     * @return True if the positions are the same
     */
    public static boolean isSamePosition(String fen1, String fen2) {
        // To be implemented
        return false;
    }

    /**
     * Extract the piece placement part from a FEN string
     *
     * @param fen The complete FEN string
     * @return The piece placement part
     */
    public static String getPiecePlacement(String fen) {
        // To be implemented
        return "";
    }

    /**
     * Create a simplified FEN string with just piece positions
     *
     * @param fen The complete FEN string
     * @return Simplified FEN with just piece positions
     */
    public static String getSimplifiedFen(String fen) {
        // To be implemented
        return "";
    }
}