package core.fen;

import core.board.Board;

public class FenGenerator {

    public static String generateFen(Board board) {
        StringBuilder fen = new StringBuilder();

        // 1. Piece placement
        for (int rank = 7; rank >= 0; rank--) {
            int emptyCount = 0;

            for (int file = 0; file < 8; file++) {
                int square = rank * 8 + file;
                int piece = board.getPiece(square);

                if (piece == 0) {
                    // Empty square
                    emptyCount++;
                } else {
                    // If there were empty squares before this piece, add the count
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }

                    // Append the piece character
                    fen.append(getPieceChar(piece));
                }
            }

            // If there are empty squares at the end of the rank
            if (emptyCount > 0) {
                fen.append(emptyCount);
            }

            // Add rank separator except after the last rank
            if (rank > 0) {
                fen.append('/');
            }
        }

        // 2. Active color
        fen.append(' ').append(board.isWhiteToMove() ? 'w' : 'b');

        // 3. Castling availability
        fen.append(' ');
        boolean hasCastling = false;

        if (board.canCastleWhiteKingside()) {
            fen.append('K');
            hasCastling = true;
        }
        if (board.canCastleWhiteQueenside()) {
            fen.append('Q');
            hasCastling = true;
        }
        if (board.canCastleBlackKingside()) {
            fen.append('k');
            hasCastling = true;
        }
        if (board.canCastleBlackQueenside()) {
            fen.append('q');
            hasCastling = true;
        }

        if (!hasCastling) {
            fen.append('-');
        }

        // 4. En passant target square
        fen.append(' ');
        int enPassantSquare = board.getEnPassantSquare();
        if (enPassantSquare == -1) {
            fen.append('-');
        } else {
            int file = enPassantSquare % 8;
            int rank = enPassantSquare / 8;
            fen.append((char)('a' + file)).append((char)('1' + rank));
        }

        // 5. Halfmove clock
        fen.append(' ').append(board.getHalfmoveClock());

        // 6. Fullmove number
        fen.append(' ').append(board.getFullmoveNumber());

        return fen.toString();
    }

    private static char getPieceChar(int piece) {
        switch (piece) {
            case 1: return 'P';  // White pawn
            case 2: return 'N';  // White knight
            case 3: return 'B';  // White bishop
            case 4: return 'R';  // White rook
            case 5: return 'Q';  // White queen
            case 6: return 'K';  // White king
            case 7: return 'p';  // Black pawn
            case 8: return 'n';  // Black knight
            case 9: return 'b';  // Black bishop
            case 10: return 'r'; // Black rook
            case 11: return 'q'; // Black queen
            case 12: return 'k'; // Black king
            default: return '.';
        }
    }
}