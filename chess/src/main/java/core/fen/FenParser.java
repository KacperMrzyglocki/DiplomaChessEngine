package core.fen;

import core.board.Board;

public class FenParser {

    public static boolean loadPosition(Board board, String fen) {
        return parseFen(fen, board);
    }
    public static boolean parseFen(String fen, Board board) {
        // Check for null or empty FEN
        if (fen == null || fen.trim().isEmpty()) {
            return false;
        }

        // Split FEN string into components
        String[] fenParts = fen.split(" ");
        if (fenParts.length < 4) {
            return false;  // Minimum required parts
        }

        // Clear the board
        board.clear();

        // 1. Piece placement
        String placement = fenParts[0];
        int file = 0;
        int rank = 7;  // FEN starts from the 8th rank (index 7)

        for (char c : placement.toCharArray()) {
            if (c == '/') {
                // Move to next rank
                file = 0;
                rank--;
            } else if (c >= '1' && c <= '8') {
                // Skip empty squares
                file += (c - '0');
            } else {
                // Place a piece
                int square = rank * 8 + file;

                switch (c) {
                    case 'P': board.addPiece(square, Board.WHITE_PAWN); break;
                    case 'N': board.addPiece(square, Board.WHITE_KNIGHT); break;
                    case 'B': board.addPiece(square, Board.WHITE_BISHOP); break;
                    case 'R': board.addPiece(square, Board.WHITE_ROOK); break;
                    case 'Q': board.addPiece(square, Board.WHITE_QUEEN); break;
                    case 'K': board.addPiece(square, Board.WHITE_KING); break;
                    case 'p': board.addPiece(square, Board.BLACK_PAWN); break;
                    case 'n': board.addPiece(square, Board.BLACK_KNIGHT); break;
                    case 'b': board.addPiece(square, Board.BLACK_BISHOP); break;
                    case 'r': board.addPiece(square, Board.BLACK_ROOK); break;
                    case 'q': board.addPiece(square, Board.BLACK_QUEEN); break;
                    case 'k': board.addPiece(square, Board.BLACK_KING); break;
                    default: return false;  // Invalid character
                }
                file++;
            }
        }

        // 2. Active color
        if (fenParts[1].equals("w")) {
            board.setWhiteToMove(true);
        } else if (fenParts[1].equals("b")) {
            board.setWhiteToMove(false);
        } else {
            return false;  // Invalid active color
        }

        // 3. Castling availability
        String castling = fenParts[2];
        board.setCastleWhiteKingside(castling.contains("K"));
        board.setCastleWhiteQueenside(castling.contains("Q"));
        board.setCastleBlackKingside(castling.contains("k"));
        board.setCastleBlackQueenside(castling.contains("q"));

        // 4. En passant target square
        String enPassant = fenParts[3];
        if (enPassant.equals("-")) {
            board.setEnPassantSquare(-1);
        } else if (enPassant.length() == 2) {
            char fileChar = enPassant.charAt(0);
            char rankChar = enPassant.charAt(1);

            if (fileChar >= 'a' && fileChar <= 'h' && rankChar >= '1' && rankChar <= '8') {
                int fileIndex = fileChar - 'a';
                int rankIndex = rankChar - '1';
                int square = rankIndex * 8 + fileIndex;
                board.setEnPassantSquare(square);
            } else {
                return false;  // Invalid en passant square
            }
        } else {
            return false;  // Invalid en passant notation
        }

        // 5. Halfmove clock (optional)
        if (fenParts.length >= 5) {
            try {
                int halfmoveClock = Integer.parseInt(fenParts[4]);
                board.setHalfmoveClock(halfmoveClock);
            } catch (NumberFormatException e) {
                return false;  // Invalid halfmove clock
            }
        }

        // 6. Fullmove number (optional)
        if (fenParts.length >= 6) {
            try {
                int fullmoveNumber = Integer.parseInt(fenParts[5]);
                board.setFullmoveNumber(fullmoveNumber);
            } catch (NumberFormatException e) {
                return false;  // Invalid fullmove number
            }
        }

        return board.isValid();
    }
}