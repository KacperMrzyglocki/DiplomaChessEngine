package core.util;

/**
 * Utility class for handling chess move notation conversions.
 * Supports conversion between different notations like algebraic, coordinate, etc.
 */
public class MoveNotation {

    /**
     * Converts a move in algebraic notation to internal representation.
     *
     * @param algebraicMove The move in algebraic notation (e.g., "e4", "Nf3")
     * @return The move in internal representation
     */
    public static String algebraicToInternal(String algebraicMove) {
        // Simple implementation for common pawn moves
        if (algebraicMove.length() == 2) {
            char file = algebraicMove.charAt(0);
            char rank = algebraicMove.charAt(1);

            // Assuming the format is like "e4" for a pawn move
            if (file >= 'a' && file <= 'h' && rank >= '1' && rank <= '8') {
                int fileIndex = file - 'a';
                int rankIndex = rank - '1';

                // We need to infer the source square based on game rules
                // This is a simplified implementation
                return "P" + file + rank;
            }
        }
        // Handle piece moves (e.g., "Nf3")
        else if (algebraicMove.length() == 3) {
            char piece = algebraicMove.charAt(0);
            char file = algebraicMove.charAt(1);
            char rank = algebraicMove.charAt(2);

            if (file >= 'a' && file <= 'h' && rank >= '1' && rank <= '8') {
                return piece + String.valueOf(file) + rank;
            }
        }

        // This is a simplified implementation
        return algebraicMove;
    }

    /**
     * Converts internal move representation to algebraic notation.
     *
     * @param internalMove The move in internal representation
     * @return The move in algebraic notation
     */
    public static String internalToAlgebraic(String internalMove) {
        // Simple implementation - reverse of the above
        if (internalMove.length() >= 3) {
            // Skip the piece identifier for pawns
            if (internalMove.charAt(0) == 'P') {
                return internalMove.substring(1);
            } else {
                return internalMove;
            }
        }
        return internalMove;
    }

    /**
     * Converts a move from long algebraic notation to standard algebraic notation.
     *
     * @param longAlgebraic The move in long algebraic notation (e.g., "e2e4")
     * @return The move in standard algebraic notation (e.g., "e4")
     */
    public static String longToStandardAlgebraic(String longAlgebraic) {
        if (longAlgebraic.length() == 4) {
            // Simple pawn move like "e2e4" becomes "e4"
            return String.valueOf(longAlgebraic.charAt(2)) + longAlgebraic.charAt(3);
        }
        return longAlgebraic;
    }

    /**
     * Validates if a string represents a valid move in algebraic notation.
     *
     * @param moveStr The move string to validate
     * @return true if the move is valid, false otherwise
     */
    public static boolean isValidAlgebraic(String moveStr) {
        if (moveStr == null || moveStr.isEmpty()) {
            return false;
        }

        // Simple validation for pawn moves like "e4"
        if (moveStr.length() == 2) {
            char file = moveStr.charAt(0);
            char rank = moveStr.charAt(1);
            return file >= 'a' && file <= 'h' && rank >= '1' && rank <= '8';
        }

        // Simple validation for piece moves like "Nf3"
        if (moveStr.length() == 3) {
            char piece = moveStr.charAt(0);
            char file = moveStr.charAt(1);
            char rank = moveStr.charAt(2);
            return (piece == 'N' || piece == 'B' || piece == 'R' || piece == 'Q' || piece == 'K') &&
                    file >= 'a' && file <= 'h' && rank >= '1' && rank <= '8';
        }

        // More complex validation for captures and checks would go here
        return false;
    }

    /**
     * Converts a core.board.Move object to algebraic notation
     *
     * @param move The Move object
     * @return The move in algebraic notation
     */
    public static String toAlgebraic(core.board.Move move) {
        if (move == null) {
            return "";
        }

        int fromSquare = move.getFrom();
        int toSquare = move.getTo();

        char fromFile = (char)('a' + (fromSquare % 8));
        char fromRank = (char)('1' + (fromSquare / 8));
        char toFile = (char)('a' + (toSquare % 8));
        char toRank = (char)('1' + (toSquare / 8));

        // Simple implementation for now - just return the destination square
        // A real implementation would include the piece type and handle special cases
        return String.valueOf(toFile) + toRank;
    }
}