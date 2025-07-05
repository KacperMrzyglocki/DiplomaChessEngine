package core.util;

import core.board.Board;
import core.board.Move;
public class MoveNotation {
    public static String toAlgebraic(Board board, Move move) {
        if (move == null) {
            return "";
        }

        int fromSquare = move.getFrom();
        int toSquare = move.getTo();
        int pieceType = board.getPieceType(fromSquare);
        boolean isCapture = board.hasPiece(toSquare) || (pieceType == Piece.PAWN && toSquare == board.getEnPassantSquare());
        boolean isCheck = false;
        boolean isCheckmate = false;

        // Make the move on a temporary board to check for check/checkmate
        Board tempBoard = board.clone();
        tempBoard.makeMove(move);

        if (tempBoard.isInCheck()) {
            isCheck = true;

            // Check if it's checkmate
            if (tempBoard.isCheckmate()) {
                isCheckmate = true;
            }
        }

        StringBuilder algebraic = new StringBuilder();

        // Castling
        if (pieceType == Piece.KING && Math.abs(fromSquare - toSquare) == 2) {
            if (toSquare > fromSquare) {
                algebraic.append("O-O"); // Kingside castling
            } else {
                algebraic.append("O-O-O"); // Queenside castling
            }
        } else {
            // Non-castling moves
            char pieceChar = getPieceChar(pieceType);

            // Add piece type except for pawns
            if (pieceType != Piece.PAWN) {
                algebraic.append(pieceChar);
            }

            // Check if disambiguation is needed
            if (needsDisambiguation(board, move)) {
                char fromFile = (char)('a' + (fromSquare % 8));
                char fromRank = (char)('1' + (fromSquare / 8));

                // Try to use file first, then rank, then both
                if (isFileDisambiguationSufficient(board, move)) {
                    algebraic.append(fromFile);
                } else if (isRankDisambiguationSufficient(board, move)) {
                    algebraic.append(fromRank);
                } else {
                    algebraic.append(fromFile).append(fromRank);
                }
            }

            // Add capture symbol if needed
            if (isCapture) {
                if (pieceType == Piece.PAWN) {
                    char fromFile = (char)('a' + (fromSquare % 8));
                    algebraic.append(fromFile);
                }
                algebraic.append("x");
            }

            // Add destination square
            char toFile = (char)('a' + (toSquare % 8));
            char toRank = (char)('1' + (toSquare / 8));
            algebraic.append(toFile).append(toRank);

            // Add promotion if applicable
            if (move.isPromotion()) {
                algebraic.append("=").append(getPieceChar(move.getPromotionPieceType()));
            }
        }

        // Add check or checkmate symbol
        if (isCheckmate) {
            algebraic.append("#");
        } else if (isCheck) {
            algebraic.append("+");
        }

        return algebraic.toString();
    }
    private static char getPieceChar(int pieceType) {
        switch (pieceType) {
            case Piece.KING: return 'K';
            case Piece.QUEEN: return 'Q';
            case Piece.ROOK: return 'R';
            case Piece.BISHOP: return 'B';
            case Piece.KNIGHT: return 'N';
            case Piece.PAWN: return 'P';
            default: return '?';
        }
    }
    private static boolean needsDisambiguation(Board board, Move move) {
        int fromSquare = move.getFrom();
        int toSquare = move.getTo();
        int pieceType = board.getPieceType(fromSquare);
        boolean isWhitePiece = board.isWhitePiece(fromSquare);

        // Pawns use file disambiguation only for captures, which is handled separately
        if (pieceType == Piece.PAWN) {
            return false;
        }

        // Find all pieces of the same type and color that can move to the same square
        for (int square = 0; square < 64; square++) {
            if (square == fromSquare) continue;

            if (board.hasPiece(square) &&
                    board.getPieceType(square) == pieceType &&
                    board.isWhitePiece(square) == isWhitePiece) {

                // Check if this piece can legally move to the destination
                if (board.isLegalMove(new Move(square, toSquare))) {
                    return true;
                }
            }
        }

        return false;
    }
    private static boolean isFileDisambiguationSufficient(Board board, Move move) {
        int fromSquare = move.getFrom();
        int toSquare = move.getTo();
        int pieceType = board.getPieceType(fromSquare);
        boolean isWhitePiece = board.isWhitePiece(fromSquare);
        int fromFile = fromSquare % 8;

        // Check if any piece of the same type and color on a different file can move to the same square
        for (int square = 0; square < 64; square++) {
            if (square == fromSquare) continue;

            if (board.hasPiece(square) &&
                    board.getPieceType(square) == pieceType &&
                    board.isWhitePiece(square) == isWhitePiece &&
                    square % 8 != fromFile) {

                // Check if this piece can legally move to the destination
                if (board.isLegalMove(new Move(square, toSquare))) {
                    return false;
                }
            }
        }

        return true;
    }
    private static boolean isRankDisambiguationSufficient(Board board, Move move) {
        int fromSquare = move.getFrom();
        int toSquare = move.getTo();
        int pieceType = board.getPieceType(fromSquare);
        boolean isWhitePiece = board.isWhitePiece(fromSquare);
        int fromRank = fromSquare / 8;

        // Check if any piece of the same type and color on a different rank can move to the same square
        for (int square = 0; square < 64; square++) {
            if (square == fromSquare) continue;

            if (board.hasPiece(square) &&
                    board.getPieceType(square) == pieceType &&
                    board.isWhitePiece(square) == isWhitePiece &&
                    square / 8 != fromRank) {

                // Check if this piece can legally move to the destination
                if (board.isLegalMove(new Move(square, toSquare))) {
                    return false;
                }
            }
        }

        return true;
    }
}