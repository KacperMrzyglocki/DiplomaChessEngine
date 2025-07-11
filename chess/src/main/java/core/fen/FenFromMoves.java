package core.fen;

public class FenFromMoves {
    // Starting position
    private static final int[][] INITIAL_BOARD = {
            {-4, -2, -3, -5, -6, -3, -2, -4}, // Black pieces (rank 8)
            {-1, -1, -1, -1, -1, -1, -1, -1}, // Black pawns (rank 7)
            {0, 0, 0, 0, 0, 0, 0, 0},         // Empty (rank 6)
            {0, 0, 0, 0, 0, 0, 0, 0},         // Empty (rank 5)
            {0, 0, 0, 0, 0, 0, 0, 0},         // Empty (rank 4)
            {0, 0, 0, 0, 0, 0, 0, 0},         // Empty (rank 3)
            {1, 1, 1, 1, 1, 1, 1, 1},         // White pawns (rank 2)
            {4, 2, 3, 5, 6, 3, 2, 4}          // White pieces (rank 1)
    };

    // Piece values: 1=pawn, 2=knight, 3=bishop, 4=rook, 5=queen, 6=king
    // Positive = white, negative = black

    public static String convertMovesToFen(String moves) {
        if (moves == null || moves.trim().isEmpty()) {
            return "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        }

        // Create working board
        int[][] board = new int[8][8];
        for (int i = 0; i < 8; i++) {
            System.arraycopy(INITIAL_BOARD[i], 0, board[i], 0, 8);
        }

        // Track game state
        boolean whiteToMove = true;
        boolean whiteKingMoved = false;
        boolean blackKingMoved = false;
        boolean whiteRookA1Moved = false;
        boolean whiteRookH1Moved = false;
        boolean blackRookA8Moved = false;
        boolean blackRookH8Moved = false;
        String enPassantTarget = "-";
        int halfMoveClock = 0;
        int fullMoveNumber = 1;

        String[] moveList = moves.trim().split(" ");

        for (String uciMove : moveList) {
            if (uciMove.length() < 4) continue;

            int fromFile = uciMove.charAt(0) - 'a';
            int fromRank = uciMove.charAt(1) - '1';
            int toFile = uciMove.charAt(2) - 'a';
            int toRank = uciMove.charAt(3) - '1';

            // Convert to array indices (rank 0 = rank 8, rank 7 = rank 1)
            int fromRow = 7 - fromRank;
            int fromCol = fromFile;
            int toRow = 7 - toRank;
            int toCol = toFile;

            int piece = board[fromRow][fromCol];
            int capturedPiece = board[toRow][toCol];

            // Check for castling
            if (Math.abs(piece) == 6) { // King move
                if (fromCol == 4 && toCol == 6) { // Kingside castling
                    // Move rook too
                    board[toRow][5] = board[toRow][7];
                    board[toRow][7] = 0;
                }
                if (fromCol == 4 && toCol == 2) { // Queenside castling
                    // Move rook too
                    board[toRow][3] = board[toRow][0];
                    board[toRow][0] = 0;
                }

                if (piece > 0) whiteKingMoved = true;
                else blackKingMoved = true;
            }

            // Check for rook moves (affects castling rights)
            if (Math.abs(piece) == 4) {
                if (fromRow == 7 && fromCol == 0) whiteRookA1Moved = true;
                if (fromRow == 7 && fromCol == 7) whiteRookH1Moved = true;
                if (fromRow == 0 && fromCol == 0) blackRookA8Moved = true;
                if (fromRow == 0 && fromCol == 7) blackRookH8Moved = true;
            }

            // Check for en passant
            enPassantTarget = "-";
            if (Math.abs(piece) == 1) { // Pawn move
                if (Math.abs(fromRank - toRank) == 2) { // Two-square pawn move
                    int epRank = (fromRank + toRank) / 2;
                    enPassantTarget = "" + (char)('a' + toFile) + (epRank + 1);
                }

                // En passant capture
                if (fromFile != toFile && capturedPiece == 0) {
                    // Remove captured pawn
                    board[fromRow][toCol] = 0;
                }
            }

            // Make the move
            board[toRow][toCol] = piece;
            board[fromRow][fromCol] = 0;

            // Handle promotion
            if (uciMove.length() > 4) {
                char promotionChar = uciMove.charAt(4);
                int promotedPiece = charToPiece(promotionChar);
                if (piece > 0) {
                    board[toRow][toCol] = promotedPiece;
                } else {
                    board[toRow][toCol] = -promotedPiece;
                }
            }

            // Update move counters
            if (capturedPiece != 0 || Math.abs(piece) == 1) {
                halfMoveClock = 0;
            } else {
                halfMoveClock++;
            }

            if (!whiteToMove) {
                fullMoveNumber++;
            }

            whiteToMove = !whiteToMove;
        }

        // Build FEN string
        String position = buildPositionString(board);
        String activeColor = whiteToMove ? "w" : "b";
        String castlingRights = buildCastlingRights(whiteKingMoved, blackKingMoved,
                whiteRookA1Moved, whiteRookH1Moved,
                blackRookA8Moved, blackRookH8Moved);

        return position + " " + activeColor + " " + castlingRights + " " +
                enPassantTarget + " " + halfMoveClock + " " + fullMoveNumber;
    }

    private static String buildPositionString(int[][] board) {
        StringBuilder fen = new StringBuilder();

        for (int rank = 0; rank < 8; rank++) {
            int emptyCount = 0;

            for (int file = 0; file < 8; file++) {
                int piece = board[rank][file];

                if (piece == 0) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    fen.append(pieceToChar(piece));
                }
            }

            if (emptyCount > 0) {
                fen.append(emptyCount);
            }

            if (rank < 7) {
                fen.append('/');
            }
        }

        return fen.toString();
    }

    private static String buildCastlingRights(boolean whiteKingMoved, boolean blackKingMoved,
                                              boolean whiteRookA1Moved, boolean whiteRookH1Moved,
                                              boolean blackRookA8Moved, boolean blackRookH8Moved) {
        StringBuilder rights = new StringBuilder();

        if (!whiteKingMoved) {
            if (!whiteRookH1Moved) rights.append('K');
            if (!whiteRookA1Moved) rights.append('Q');
        }

        if (!blackKingMoved) {
            if (!blackRookH8Moved) rights.append('k');
            if (!blackRookA8Moved) rights.append('q');
        }

        return rights.length() == 0 ? "-" : rights.toString();
    }

    private static char pieceToChar(int piece) {
        char[] pieces = {'_', 'p', 'n', 'b', 'r', 'q', 'k'};
        char c = pieces[Math.abs(piece)];
        return piece > 0 ? Character.toUpperCase(c) : c;
    }

    private static int charToPiece(char c) {
        switch (Character.toLowerCase(c)) {
            case 'p': return 1;
            case 'n': return 2;
            case 'b': return 3;
            case 'r': return 4;
            case 'q': return 5;
            case 'k': return 6;
            default: return 5; // Default to queen for promotion
        }
    }
}
