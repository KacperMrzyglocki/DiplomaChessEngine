package core.board;

import core.bitboard.Bitboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a chess board using bitboards
 */
public class Board {
    // Piece bitboards
    private long whitePawns, whiteKnights, whiteBishops, whiteRooks, whiteQueens, whiteKing;
    private long blackPawns, blackKnights, blackBishops, blackRooks, blackQueens, blackKing;

    private long whitePieces, blackPieces, allPieces;
    private boolean whiteToMove;
    private boolean castleWhiteKingside;
    private boolean castleWhiteQueenside;
    private boolean castleBlackKingside;
    private boolean castleBlackQueenside;
    private int enPassantSquare; // -1 if not available
    private int halfmoveClock;
    private int fullmoveNumber;
    private MoveGenerator moveGenerator;
    // Piece type constants needed by FenParser
    public static final int WHITE_PAWN = 0;
    public static final int WHITE_KNIGHT = 1;
    public static final int WHITE_BISHOP = 2;
    public static final int WHITE_ROOK = 3;
    public static final int WHITE_QUEEN = 4;
    public static final int WHITE_KING = 5;
    public static final int BLACK_PAWN = 6;
    public static final int BLACK_KNIGHT = 7;
    public static final int BLACK_BISHOP = 8;
    public static final int BLACK_ROOK = 9;
    public static final int BLACK_QUEEN = 10;
    public static final int BLACK_KING = 11;

    private final Stack<BoardState> moveHistory = new Stack<>();

    private Map<Long, Integer> positionHistory = new HashMap<>();

    /**
     * Initialize an empty chess board
     */
    public Board() {
        // Initialize empty bitboards
        whitePawns = whiteKnights = whiteBishops = whiteRooks = whiteQueens = whiteKing = 0L;
        blackPawns = blackKnights = blackBishops = blackRooks = blackQueens = blackKing = 0L;

        // Initialize convenience bitboards
        whitePieces = blackPieces = allPieces = 0L;

        // Initialize game state
        whiteToMove = true;
        castleWhiteKingside = castleWhiteQueenside = false;
        castleBlackKingside = castleBlackQueenside = false;
        enPassantSquare = -1;
        halfmoveClock = 0;
        fullmoveNumber = 1;
        this.moveGenerator = new MoveGenerator();
    }

    /**
     * Set up the initial chess position
     */
    public void setInitialPosition() {
        // White pieces
        whitePawns = 0x000000000000FF00L;
        whiteKnights = 0x0000000000000042L;
        whiteBishops = 0x0000000000000024L;
        whiteRooks = 0x0000000000000081L;
        whiteQueens = 0x0000000000000008L;
        whiteKing = 0x0000000000000010L;

        // Black pieces
        blackPawns = 0x00FF000000000000L;
        blackKnights = 0x4200000000000000L;
        blackBishops = 0x2400000000000000L;
        blackRooks = 0x8100000000000000L;
        blackQueens = 0x0800000000000000L;
        blackKing = 0x1000000000000000L;

        // Update convenience bitboards
        updateConvenienceBitboards();

        // Set game state
        whiteToMove = true;
        castleWhiteKingside = castleWhiteQueenside = true;
        castleBlackKingside = castleBlackQueenside = true;
        enPassantSquare = -1;
        halfmoveClock = 0;
        fullmoveNumber = 1;

        updatePositionHistory();
    }

    /**
     * Update the convenience bitboards based on individual piece bitboards
     */
    private void updateConvenienceBitboards() {
        whitePieces = whitePawns | whiteKnights | whiteBishops | whiteRooks | whiteQueens | whiteKing;
        blackPieces = blackPawns | blackKnights | blackBishops | blackRooks | blackQueens | blackKing;
        allPieces = whitePieces | blackPieces;
    }

    /**
     * Verify if the current position is valid (e.g., kings not in check simultaneously)
     */
    public boolean isValid() {
        // Implementation will go here
        return true;
    }

    /**
     * Check if the side to move is in check
     */
    /**
     * Determines if the side to move is in check
     *
     * @return true if the side to move is in check, false otherwise
     */
    public boolean isInCheck() {
        // Use the MoveGenerator's isKingInCheck method to determine if the current side's king is in check
        return moveGenerator.isKingInCheck(this, isWhiteToMove());
    }

    // Getters and setters

    public long getWhitePawns() {
        return whitePawns;
    }

    public long getWhiteKnights() {
        return whiteKnights;
    }

    public long getWhiteBishops() {
        return whiteBishops;
    }

    public long getWhiteRooks() {
        return whiteRooks;
    }

    public long getWhiteQueens() {
        return whiteQueens;
    }

    public long getWhiteKing() {
        return whiteKing;
    }

    public long getBlackPawns() {
        return blackPawns;
    }

    public long getBlackKnights() {
        return blackKnights;
    }

    public long getBlackBishops() {
        return blackBishops;
    }

    public long getBlackRooks() {
        return blackRooks;
    }

    public long getBlackQueens() {
        return blackQueens;
    }

    public long getBlackKing() {
        return blackKing;
    }

    public long getWhitePieces() {
        return whitePieces;
    }

    public long getBlackPieces() {
        return blackPieces;
    }

    public long getAllPieces() {
        return allPieces;
    }

    public boolean isWhiteToMove() {
        return whiteToMove;
    }

    /**
     * History entry to store board state for undoing moves
     */
    private static class BoardState {
        // Game state
        boolean whiteToMove;
        boolean castleWhiteKingside;
        boolean castleWhiteQueenside;
        boolean castleBlackKingside;
        boolean castleBlackQueenside;
        int enPassantSquare;
        int halfmoveClock;
        int fullmoveNumber;

        // Move information
        Move move;

        // Capture information
        long capturedPieceBB; // Bitboard with a single bit for the captured piece location
        int capturedPieceType; // Type of the captured piece (PAWN, KNIGHT, etc.)
        boolean isWhitePiece; // Whether the captured piece was white
        public long positionHash;
        public long newPositionHash;
    }

    // Add a move history to store previous states


    /**
     * Make a move on the board
     *
     * @param move The move to make
     * @return True if the move was legal and successfully made
     */
    public boolean makeMove(Move move) {
        int from = move.getFrom();
        int to = move.getTo();
        int moveType = move.getMoveType();

        // Create and store the current state
        BoardState state = new BoardState();
        state.positionHash = getPositionHash();
        state.whiteToMove = whiteToMove;
        state.castleWhiteKingside = castleWhiteKingside;
        state.castleWhiteQueenside = castleWhiteQueenside;
        state.castleBlackKingside = castleBlackKingside;
        state.castleBlackQueenside = castleBlackQueenside;
        state.enPassantSquare = enPassantSquare;
        state.halfmoveClock = halfmoveClock;
        state.fullmoveNumber = fullmoveNumber;
        state.move = move;

        // Find which piece is moving
        long fromBB = Bitboard.getBit(from);
        long toBB = Bitboard.getBit(to);

        // Check if it's a piece of the current player
        boolean isWhitePiece = Bitboard.isBitSet(whitePieces, from);
        boolean isBlackPiece = Bitboard.isBitSet(blackPieces, from);

        if ((whiteToMove && !isWhitePiece) || (!whiteToMove && !isBlackPiece)) {
            return false; // Wrong color piece
        }

        // Identify the moving piece
        int pieceType = identifyPiece(fromBB, isWhitePiece);
        if (pieceType == -1) {
            return false; // No piece found
        }

        // Check for captures
        long opponentPieces = whiteToMove ? blackPieces : whitePieces;
        boolean isCapture = Bitboard.isBitSet(opponentPieces, to);

        // Store capture information
        if (isCapture) {
            state.capturedPieceBB = toBB;
            state.capturedPieceType = identifyPiece(toBB, !isWhitePiece);
            state.isWhitePiece = !isWhitePiece;
        }

        // Handle en passant capture
        if (moveType == Move.EN_PASSANT) {
            int epCaptureSquare = whiteToMove ? to - 8 : to + 8;
            state.capturedPieceBB = Bitboard.getBit(epCaptureSquare);
            state.capturedPieceType = 0; // Pawn
            state.isWhitePiece = !isWhitePiece;
        }

        // Update halfmove clock
        if (pieceType == 0 || isCapture) { // Reset on pawn move or capture
            halfmoveClock = 0;
        } else {
            halfmoveClock++;
        }

        // Update fullmove number
        if (!whiteToMove) {
            fullmoveNumber++;
        }

        // Save current state to history
        moveHistory.push(state);

        // Execute the move based on piece type and move type
        switch (moveType) {
            case Move.NORMAL:
                executeNormalMove(from, to, pieceType, isWhitePiece, isCapture);
                break;
            case Move.PAWN_PROMOTION:
                executePawnPromotion(from, to, move.getPromotionPieceType(), isWhitePiece, isCapture);
                break;
            case Move.EN_PASSANT:
                executeEnPassantMove(from, to, isWhitePiece);
                break;
            case Move.CASTLING:
                executeCastlingMove(from, to, isWhitePiece);
                break;
            default:
                return false;
        }

        // Handle castling rights updates
        updateCastlingRights(from, to, pieceType);

        // Set en passant square for the next move
        if (pieceType == 0 && Math.abs(from - to) == 16) { // Pawn double move
            enPassantSquare = whiteToMove ? from + 8 : from - 8;
        } else {
            enPassantSquare = -1;
        }

        // Update side to move
        whiteToMove = !whiteToMove;

        // Update convenience bitboards
        updateConvenienceBitboards();

        // Now compute and store the new position hash AFTER the move is made
        state.newPositionHash = getPositionHash();

        updatePositionHistory();

        return true;
    }
    public boolean makeSearchMove(Move move) {
        int from = move.getFrom();
        int to = move.getTo();
        int moveType = move.getMoveType();

        // Create and store the current state
        BoardState state = new BoardState();
        state.whiteToMove = whiteToMove;
        state.castleWhiteKingside = castleWhiteKingside;
        state.castleWhiteQueenside = castleWhiteQueenside;
        state.castleBlackKingside = castleBlackKingside;
        state.castleBlackQueenside = castleBlackQueenside;
        state.enPassantSquare = enPassantSquare;
        state.halfmoveClock = halfmoveClock;
        state.fullmoveNumber = fullmoveNumber;
        state.move = move;

        // Find which piece is moving
        long fromBB = Bitboard.getBit(from);
        long toBB = Bitboard.getBit(to);

        // Check if it's a piece of the current player
        boolean isWhitePiece = Bitboard.isBitSet(whitePieces, from);
        boolean isBlackPiece = Bitboard.isBitSet(blackPieces, from);

        if ((whiteToMove && !isWhitePiece) || (!whiteToMove && !isBlackPiece)) {
            return false; // Wrong color piece
        }

        // Identify the moving piece
        int pieceType = identifyPiece(fromBB, isWhitePiece);
        if (pieceType == -1) {
            return false; // No piece found
        }

        // Check for captures
        long opponentPieces = whiteToMove ? blackPieces : whitePieces;
        boolean isCapture = Bitboard.isBitSet(opponentPieces, to);

        // Store capture information
        if (isCapture) {
            state.capturedPieceBB = toBB;
            state.capturedPieceType = identifyPiece(toBB, !isWhitePiece);
            state.isWhitePiece = !isWhitePiece;
        }

        // Handle en passant capture
        if (moveType == Move.EN_PASSANT) {
            int epCaptureSquare = whiteToMove ? to - 8 : to + 8;
            state.capturedPieceBB = Bitboard.getBit(epCaptureSquare);
            state.capturedPieceType = 0; // Pawn
            state.isWhitePiece = !isWhitePiece;
        }

        // Update halfmove clock
        if (pieceType == 0 || isCapture) { // Reset on pawn move or capture
            halfmoveClock = 0;
        } else {
            halfmoveClock++;
        }

        // Update fullmove number
        if (!whiteToMove) {
            fullmoveNumber++;
        }

        // Save current state to history
        moveHistory.push(state);

        // Execute the move based on piece type and move type
        switch (moveType) {
            case Move.NORMAL:
                executeNormalMove(from, to, pieceType, isWhitePiece, isCapture);
                break;
            case Move.PAWN_PROMOTION:
                executePawnPromotion(from, to, move.getPromotionPieceType(), isWhitePiece, isCapture);
                break;
            case Move.EN_PASSANT:
                executeEnPassantMove(from, to, isWhitePiece);
                break;
            case Move.CASTLING:
                executeCastlingMove(from, to, isWhitePiece);
                break;
            default:
                return false;
        }

        // Handle castling rights updates
        updateCastlingRights(from, to, pieceType);

        // Set en passant square for the next move
        if (pieceType == 0 && Math.abs(from - to) == 16) { // Pawn double move
            enPassantSquare = whiteToMove ? from + 8 : from - 8;
        } else {
            enPassantSquare = -1;
        }

        // Update side to move
        whiteToMove = !whiteToMove;

        // Update convenience bitboards
        updateConvenienceBitboards();

        return true;
    }

    public boolean undoSearchMove() {
        if (moveHistory.isEmpty()) {
            return false;
        }

        BoardState state = moveHistory.pop();

        // Restore game state
        whiteToMove = state.whiteToMove;
        castleWhiteKingside = state.castleWhiteKingside;
        castleWhiteQueenside = state.castleWhiteQueenside;
        castleBlackKingside = state.castleBlackKingside;
        castleBlackQueenside = state.castleBlackQueenside;
        enPassantSquare = state.enPassantSquare;
        halfmoveClock = state.halfmoveClock;
        fullmoveNumber = state.fullmoveNumber;

        // Get move information
        Move move = state.move;
        int from = move.getFrom();
        int to = move.getTo();
        int moveType = move.getMoveType();

        // Create bitboards for from and to squares
        long fromBB = Bitboard.getBit(from);
        long toBB = Bitboard.getBit(to);

        // Determine piece color and type
        boolean isWhitePiece = whiteToMove;

        // Undo the move based on move type
        switch (moveType) {
            case Move.NORMAL:
                undoNormalMove(from, to, isWhitePiece, state);
                break;
            case Move.PAWN_PROMOTION:
                undoPawnPromotion(from, to, move.getPromotionPieceType(), isWhitePiece, state);
                break;
            case Move.EN_PASSANT:
                undoEnPassantMove(from, to, isWhitePiece, state);
                break;
            case Move.CASTLING:
                undoCastlingMove(from, to, isWhitePiece);
                break;
        }

        // Restore a captured piece if there was one
        if (state.capturedPieceBB != 0) {
            restoreCapturedPiece(state.capturedPieceBB, state.capturedPieceType, state.isWhitePiece);
        }

        // Update convenience bitboards
        updateConvenienceBitboards();

        return true;
    }

    /**
     * Undo the last move made on the board
     *
     * @return True if a move was successfully undone
     */
    public boolean undoMove() {
        if (moveHistory.isEmpty()) {
            return false;
        }

        BoardState state = moveHistory.pop();

        // Get current position hash (before restoring state)
        long currentHash = getPositionHash();


        // Restore game state
        whiteToMove = state.whiteToMove;
        castleWhiteKingside = state.castleWhiteKingside;
        castleWhiteQueenside = state.castleWhiteQueenside;
        castleBlackKingside = state.castleBlackKingside;
        castleBlackQueenside = state.castleBlackQueenside;
        enPassantSquare = state.enPassantSquare;
        halfmoveClock = state.halfmoveClock;
        fullmoveNumber = state.fullmoveNumber;

        // Get move information
        Move move = state.move;
        int from = move.getFrom();
        int to = move.getTo();
        int moveType = move.getMoveType();

        // Create bitboards for from and to squares
        long fromBB = Bitboard.getBit(from);
        long toBB = Bitboard.getBit(to);

        // Determine piece color and type
        boolean isWhitePiece = whiteToMove;

        // Undo the move based on move type
        switch (moveType) {
            case Move.NORMAL:
                undoNormalMove(from, to, isWhitePiece, state);
                break;
            case Move.PAWN_PROMOTION:
                undoPawnPromotion(from, to, move.getPromotionPieceType(), isWhitePiece, state);
                break;
            case Move.EN_PASSANT:
                undoEnPassantMove(from, to, isWhitePiece, state);
                break;
            case Move.CASTLING:
                undoCastlingMove(from, to, isWhitePiece);
                break;
        }

        // Restore a captured piece if there was one
        if (state.capturedPieceBB != 0) {
            restoreCapturedPiece(state.capturedPieceBB, state.capturedPieceType, state.isWhitePiece);
        }


        // Undo the position history update
        undoPositionHistoryUpdate();

        // Update convenience bitboards
        if (positionHistory.containsKey(currentHash)) {
            int count = positionHistory.get(currentHash);
            if (count <= 1) {
                positionHistory.remove(currentHash);
            } else {
                positionHistory.put(currentHash, count - 1);
            }
        }

        updateConvenienceBitboards();

        return true;
    }

    // Add this method to undo the position history update
    private void undoPositionHistoryUpdate() {
        if (moveHistory.isEmpty()) {
            return; // Nothing to undo
        }

        // Get the top state from the history stack (the state we're returning to)
        BoardState lastState = moveHistory.peek();

        // Get the hash that was added by the move we're undoing
        long hashToRemove = lastState.newPositionHash;

        // Remove or decrement the counter for the position we're undoing
        int count = positionHistory.getOrDefault(hashToRemove, 0);
        if (count > 0) {
            if (count == 1) {
                positionHistory.remove(hashToRemove);
            } else {
                positionHistory.put(hashToRemove, count - 1);
            }
        }
    }

    /**
     * Identify the piece type at a given position
     *
     * @param position The bitboard representing the piece location
     * @param isWhite Whether to check white or black pieces
     * @return The piece type index (0 = pawn, 1 = knight, etc.) or -1 if not found
     */
    private int identifyPiece(long position, boolean isWhite) {
        if (isWhite) {
            if ((whitePawns & position) != 0) return 0;
            if ((whiteKnights & position) != 0) return 1;
            if ((whiteBishops & position) != 0) return 2;
            if ((whiteRooks & position) != 0) return 3;
            if ((whiteQueens & position) != 0) return 4;
            if ((whiteKing & position) != 0) return 5;
        } else {
            if ((blackPawns & position) != 0) return 0;
            if ((blackKnights & position) != 0) return 1;
            if ((blackBishops & position) != 0) return 2;
            if ((blackRooks & position) != 0) return 3;
            if ((blackQueens & position) != 0) return 4;
            if ((blackKing & position) != 0) return 5;
        }
        return -1;
    }

    /**
     * Execute a normal move (non-special)
     */
    private void executeNormalMove(int from, int to, int pieceType, boolean isWhite, boolean isCapture) {
        long fromBB = Bitboard.getBit(from);
        long toBB = Bitboard.getBit(to);

        // Remove the piece from the source square
        if (isWhite) {
            switch (pieceType) {
                case 0: whitePawns &= ~fromBB; break;
                case 1: whiteKnights &= ~fromBB; break;
                case 2: whiteBishops &= ~fromBB; break;
                case 3: whiteRooks &= ~fromBB; break;
                case 4: whiteQueens &= ~fromBB; break;
                case 5: whiteKing &= ~fromBB; break;
            }
        } else {
            switch (pieceType) {
                case 0: blackPawns &= ~fromBB; break;
                case 1: blackKnights &= ~fromBB; break;
                case 2: blackBishops &= ~fromBB; break;
                case 3: blackRooks &= ~fromBB; break;
                case 4: blackQueens &= ~fromBB; break;
                case 5: blackKing &= ~fromBB; break;
            }
        }

        // If there's a capture, remove captured piece
        if (isCapture) {
            if (isWhite) {
                blackPawns &= ~toBB;
                blackKnights &= ~toBB;
                blackBishops &= ~toBB;
                blackRooks &= ~toBB;
                blackQueens &= ~toBB;
                // King should never be captured
            } else {
                whitePawns &= ~toBB;
                whiteKnights &= ~toBB;
                whiteBishops &= ~toBB;
                whiteRooks &= ~toBB;
                whiteQueens &= ~toBB;
            }
        }

        // Add the piece to the destination square
        if (isWhite) {
            switch (pieceType) {
                case 0: whitePawns |= toBB; break;
                case 1: whiteKnights |= toBB; break;
                case 2: whiteBishops |= toBB; break;
                case 3: whiteRooks |= toBB; break;
                case 4: whiteQueens |= toBB; break;
                case 5: whiteKing |= toBB; break;
            }
        } else {
            switch (pieceType) {
                case 0: blackPawns |= toBB; break;
                case 1: blackKnights |= toBB; break;
                case 2: blackBishops |= toBB; break;
                case 3: blackRooks |= toBB; break;
                case 4: blackQueens |= toBB; break;
                case 5: blackKing |= toBB; break;
            }
        }
    }

    /**
     * Execute a pawn promotion move
     */
    private void executePawnPromotion(int from, int to, int promotionPieceType, boolean isWhite, boolean isCapture) {
        long fromBB = Bitboard.getBit(from);
        long toBB = Bitboard.getBit(to);

        // Remove the pawn from the source square
        if (isWhite) {
            whitePawns &= ~fromBB;
        } else {
            blackPawns &= ~fromBB;
        }

        // If there's a capture, remove captured piece
        if (isCapture) {
            if (isWhite) {
                blackPawns &= ~toBB;
                blackKnights &= ~toBB;
                blackBishops &= ~toBB;
                blackRooks &= ~toBB;
                blackQueens &= ~toBB;
            } else {
                whitePawns &= ~toBB;
                whiteKnights &= ~toBB;
                whiteBishops &= ~toBB;
                whiteRooks &= ~toBB;
                whiteQueens &= ~toBB;
            }
        }

        // Add the promoted piece to the destination square
        if (isWhite) {
            switch (promotionPieceType) {
                case Move.QUEEN_PROMOTION: whiteQueens |= toBB; break;
                case Move.ROOK_PROMOTION: whiteRooks |= toBB; break;
                case Move.BISHOP_PROMOTION: whiteBishops |= toBB; break;
                case Move.KNIGHT_PROMOTION: whiteKnights |= toBB; break;
            }
        } else {
            switch (promotionPieceType) {
                case Move.QUEEN_PROMOTION: blackQueens |= toBB; break;
                case Move.ROOK_PROMOTION: blackRooks |= toBB; break;
                case Move.BISHOP_PROMOTION: blackBishops |= toBB; break;
                case Move.KNIGHT_PROMOTION: blackKnights |= toBB; break;
            }
        }
    }

    /**
     * Execute an en passant capture
     */
    private void executeEnPassantMove(int from, int to, boolean isWhite) {
        long fromBB = Bitboard.getBit(from);
        long toBB = Bitboard.getBit(to);

        // Calculate the position of the captured pawn
        int capturedPawnSquare = isWhite ? to - 8 : to + 8;
        long capturedPawnBB = Bitboard.getBit(capturedPawnSquare);

        // Move the capturing pawn
        if (isWhite) {
            whitePawns &= ~fromBB;
            whitePawns |= toBB;
            blackPawns &= ~capturedPawnBB;
        } else {
            blackPawns &= ~fromBB;
            blackPawns |= toBB;
            whitePawns &= ~capturedPawnBB;
        }
    }

    /**
     * Execute a castling move
     */
    private void executeCastlingMove(int from, int to, boolean isWhite) {
        long fromBB = Bitboard.getBit(from);
        long toBB = Bitboard.getBit(to);

        // Move the king
        if (isWhite) {
            whiteKing &= ~fromBB;
            whiteKing |= toBB;
        } else {
            blackKing &= ~fromBB;
            blackKing |= toBB;
        }

        // Move the rook based on castling type
        int rookFrom, rookTo;
        if (to > from) { // Kingside
            rookFrom = isWhite ? 7 : 63;
            rookTo = isWhite ? 5 : 61;
        } else { // Queenside
            rookFrom = isWhite ? 0 : 56;
            rookTo = isWhite ? 3 : 59;
        }

        long rookFromBB = Bitboard.getBit(rookFrom);
        long rookToBB = Bitboard.getBit(rookTo);

        if (isWhite) {
            whiteRooks &= ~rookFromBB;
            whiteRooks |= rookToBB;
        } else {
            blackRooks &= ~rookFromBB;
            blackRooks |= rookToBB;
        }
    }

    /**
     * Update castling rights after a move
     */
    private void updateCastlingRights(int from, int to, int pieceType) {
        // King moves remove all castling rights for that side
        if (pieceType == 5) { // King move
            if (whiteToMove) {
                castleWhiteKingside = false;
                castleWhiteQueenside = false;
            } else {
                castleBlackKingside = false;
                castleBlackQueenside = false;
            }
        }

        // Rook moves remove castling rights for that side
        if (pieceType == 3) { // Rook move
            if (from == 0) castleWhiteQueenside = false;
            if (from == 7) castleWhiteKingside = false;
            if (from == 56) castleBlackQueenside = false;
            if (from == 63) castleBlackKingside = false;
        }

        // Captures on rook squares remove castling rights
        if (to == 0) castleWhiteQueenside = false;
        if (to == 7) castleWhiteKingside = false;
        if (to == 56) castleBlackQueenside = false;
        if (to == 63) castleBlackKingside = false;
    }

    /**
     * Undo a normal move
     */
    private void undoNormalMove(int from, int to, boolean isWhite, BoardState state) {
        long fromBB = Bitboard.getBit(from);
        long toBB = Bitboard.getBit(to);

        // Identify the piece type that was moved
        int pieceType = identifyPiece(toBB, isWhite);

        // Move the piece back
        if (isWhite) {
            switch (pieceType) {
                case 0: whitePawns &= ~toBB; whitePawns |= fromBB; break;
                case 1: whiteKnights &= ~toBB; whiteKnights |= fromBB; break;
                case 2: whiteBishops &= ~toBB; whiteBishops |= fromBB; break;
                case 3: whiteRooks &= ~toBB; whiteRooks |= fromBB; break;
                case 4: whiteQueens &= ~toBB; whiteQueens |= fromBB; break;
                case 5: whiteKing &= ~toBB; whiteKing |= fromBB; break;
            }
        } else {
            switch (pieceType) {
                case 0: blackPawns &= ~toBB; blackPawns |= fromBB; break;
                case 1: blackKnights &= ~toBB; blackKnights |= fromBB; break;
                case 2: blackBishops &= ~toBB; blackBishops |= fromBB; break;
                case 3: blackRooks &= ~toBB; blackRooks |= fromBB; break;
                case 4: blackQueens &= ~toBB; blackQueens |= fromBB; break;
                case 5: blackKing &= ~toBB; blackKing |= fromBB; break;
            }
        }
    }

    /**
     * Undo a pawn promotion
     */
    private void undoPawnPromotion(int from, int to, int promotionPieceType, boolean isWhite, BoardState state) {
        long fromBB = Bitboard.getBit(from);
        long toBB = Bitboard.getBit(to);

        // Remove the promoted piece
        if (isWhite) {
            switch (promotionPieceType) {
                case Move.QUEEN_PROMOTION: whiteQueens &= ~toBB; break;
                case Move.ROOK_PROMOTION: whiteRooks &= ~toBB; break;
                case Move.BISHOP_PROMOTION: whiteBishops &= ~toBB; break;
                case Move.KNIGHT_PROMOTION: whiteKnights &= ~toBB; break;
            }
            // Restore the pawn
            whitePawns |= fromBB;
        } else {
            switch (promotionPieceType) {
                case Move.QUEEN_PROMOTION: blackQueens &= ~toBB; break;
                case Move.ROOK_PROMOTION: blackRooks &= ~toBB; break;
                case Move.BISHOP_PROMOTION: blackBishops &= ~toBB; break;
                case Move.KNIGHT_PROMOTION: blackKnights &= ~toBB; break;
            }
            // Restore the pawn
            blackPawns |= fromBB;
        }
    }

    /**
     * Undo an en passant capture
     */
    private void undoEnPassantMove(int from, int to, boolean isWhite, BoardState state) {
        long fromBB = Bitboard.getBit(from);
        long toBB = Bitboard.getBit(to);

        // Move the pawn back
        if (isWhite) {
            whitePawns &= ~toBB;
            whitePawns |= fromBB;
        } else {
            blackPawns &= ~toBB;
            blackPawns |= fromBB;
        }

        // The captured pawn is restored in the undoMove method
    }

    /**
     * Undo a castling move
     */
    private void undoCastlingMove(int from, int to, boolean isWhite) {
        long fromBB = Bitboard.getBit(from);
        long toBB = Bitboard.getBit(to);

        // Move the king back
        if (isWhite) {
            whiteKing &= ~toBB;
            whiteKing |= fromBB;
        } else {
            blackKing &= ~toBB;
            blackKing |= fromBB;
        }

        // Move the rook back
        int rookTo, rookFrom;
        if (to > from) { // Kingside
            rookTo = isWhite ? 5 : 61;
            rookFrom = isWhite ? 7 : 63;
        } else { // Queenside
            rookTo = isWhite ? 3 : 59;
            rookFrom = isWhite ? 0 : 56;
        }

        long rookFromBB = Bitboard.getBit(rookFrom);
        long rookToBB = Bitboard.getBit(rookTo);

        if (isWhite) {
            whiteRooks &= ~rookToBB;
            whiteRooks |= rookFromBB;
        } else {
            blackRooks &= ~rookToBB;
            blackRooks |= rookFromBB;
        }
    }

    /**
     * Restore a captured piece
     */
    private void restoreCapturedPiece(long position, int pieceType, boolean isWhite) {
        if (isWhite) {
            switch (pieceType) {
                case 0: whitePawns |= position; break;
                case 1: whiteKnights |= position; break;
                case 2: whiteBishops |= position; break;
                case 3: whiteRooks |= position; break;
                case 4: whiteQueens |= position; break;
            }
        } else {
            switch (pieceType) {
                case 0: blackPawns |= position; break;
                case 1: blackKnights |= position; break;
                case 2: blackBishops |= position; break;
                case 3: blackRooks |= position; break;
                case 4: blackQueens |= position; break;
            }
        }
    }

    /**
     * Check if black can castle kingside
     *
     * @return True if black can castle kingside
     */
    public boolean canCastleBlackKingside() {
        return castleBlackKingside;
    }

    /**
     * Check if black can castle queenside
     *
     * @return True if black can castle queenside
     */
    public boolean canCastleBlackQueenside() {
        return castleBlackQueenside;
    }

    public boolean canCastleWhiteKingside() {
        return castleWhiteKingside;
    }

    /**
     * Check if black can castle queenside
     *
     * @return True if black can castle queenside
     */
    public boolean canCastleWhiteQueenside() {
        return castleWhiteQueenside;
    }

    /**
     * Get the current en passant target square
     *
     * @return The en passant square index, or -1 if not available
     */
    public int getEnPassantSquare() {
        return enPassantSquare;
    }
    /**
     * Clears the board, removing all pieces
     */
    public void clear() {
        // Clear all piece bitboards
        whitePawns = whiteKnights = whiteBishops = whiteRooks = whiteQueens = whiteKing = 0L;
        blackPawns = blackKnights = blackBishops = blackRooks = blackQueens = blackKing = 0L;

        // Clear convenience bitboards
        whitePieces = blackPieces = allPieces = 0L;

        // Reset game state
        whiteToMove = true;
        castleWhiteKingside = castleWhiteQueenside = false;
        castleBlackKingside = castleBlackQueenside = false;
        enPassantSquare = -1;
        halfmoveClock = 0;
        fullmoveNumber = 1;

        clearPositionHistory();
    }

    /**
     * Add a piece to the specified square
     *
     * @param square The square to add the piece to (0-63)
     * @param pieceType The type of piece to add
     */
    public void addPiece(int square, int pieceType) {
        long squareBB = Bitboard.getBit(square);

        switch (pieceType) {
            case WHITE_PAWN: whitePawns |= squareBB; break;
            case WHITE_KNIGHT: whiteKnights |= squareBB; break;
            case WHITE_BISHOP: whiteBishops |= squareBB; break;
            case WHITE_ROOK: whiteRooks |= squareBB; break;
            case WHITE_QUEEN: whiteQueens |= squareBB; break;
            case WHITE_KING: whiteKing |= squareBB; break;
            case BLACK_PAWN: blackPawns |= squareBB; break;
            case BLACK_KNIGHT: blackKnights |= squareBB; break;
            case BLACK_BISHOP: blackBishops |= squareBB; break;
            case BLACK_ROOK: blackRooks |= squareBB; break;
            case BLACK_QUEEN: blackQueens |= squareBB; break;
            case BLACK_KING: blackKing |= squareBB; break;
        }

        // Update convenience bitboards
        updateConvenienceBitboards();
    }
    /**
     * Returns the piece at the specified square index.
     *
     * @param squareIndex A square index from 0 to 63 (a1=0, h8=63)
     * @return An integer representing the piece type:
     *         0: Empty square
     *         1: White pawn    7: Black pawn
     *         2: White knight  8: Black knight
     *         3: White bishop  9: Black bishop
     *         4: White rook    10: Black rook
     *         5: White queen   11: Black queen
     *         6: White king    12: Black king
     */
    public int getPiece(int squareIndex) {
        long bitboardPosition = Bitboard.getBit(squareIndex);
        if ((whitePawns & bitboardPosition) != 0) return 1;
        if ((whiteKnights & bitboardPosition) != 0) return 2;
        if ((whiteBishops & bitboardPosition) != 0) return 3;
        if ((whiteRooks & bitboardPosition) != 0) return 4;
        if ((whiteQueens & bitboardPosition) != 0) return 5;
        if ((whiteKing & bitboardPosition) != 0) return 6;
        if ((blackPawns & bitboardPosition) != 0) return 7;
        if ((blackKnights & bitboardPosition) != 0) return 8;
        if ((blackBishops & bitboardPosition) != 0) return 9;
        if ((blackRooks & bitboardPosition) != 0) return 10;
        if ((blackQueens & bitboardPosition) != 0) return 11;
        if ((blackKing & bitboardPosition) != 0) return 12;
        return 0;
    }

    /**
     * Checks if a move is legal according to chess rules.
     * This includes checking:
     * - If the move follows piece movement rules
     * - If the move doesn't leave the king in check
     * - Special rules like castling, en passant, etc.
     *
     * @param move The move to validate
     * @return true if the move is legal, false otherwise
     */
    public boolean isLegalMove(Move move) {
        int fromSquare = move.getFrom();
        int toSquare = move.getTo();
        int piece = getPiece(fromSquare);

        if (piece == 0) return false;
        if ((whiteToMove && piece > 6) || (!whiteToMove && piece <= 6)) return false;

        List<Move> legalMoves = generateLegalMoves();
        return legalMoves.contains(move);
    }

    /**
     * Helper method to generate all legal moves in the current position.
     * This is used by isLegalMove to validate moves.
     *
     * @return An array of all legal moves in the current position
     */
    public List<Move> generateLegalMoves() {
        MoveList moveList = new MoveList(100); // Assuming a reasonable capacity
        moveGenerator.generateLegalMoves(this, moveList);
        List<Move> moves = new ArrayList<>();
        for (int i = 0; i < moveList.size(); i++) {
            moves.add(moveList.get(i));
        }
        return moves;
    }
    public long getPositionHash() {
        // Zobrist hashing would be ideal, but we'll use a simpler approach for now
        long hash = 0;

        // Include piece positions in the hash
        hash ^= whitePawns;
        hash ^= whiteKnights * 3;
        hash ^= whiteBishops * 5;
        hash ^= whiteRooks * 7;
        hash ^= whiteQueens * 11;
        hash ^= whiteKing * 13;
        hash ^= blackPawns * 17;
        hash ^= blackKnights * 19;
        hash ^= blackBishops * 23;
        hash ^= blackRooks * 29;
        hash ^= blackQueens * 31;
        hash ^= blackKing * 37;

        // Include game state in the hash
        hash ^= whiteToMove ? 1L << 50 : 0;
        hash ^= castleWhiteKingside ? 1L << 51 : 0;
        hash ^= castleWhiteQueenside ? 1L << 52 : 0;
        hash ^= castleBlackKingside ? 1L << 53 : 0;
        hash ^= castleBlackQueenside ? 1L << 54 : 0;

        // Include en passant square if available
        if (enPassantSquare != -1) {
            hash ^= 1L << (enPassantSquare);
        }

        return hash;
    }

    // Add this method to update the position history
    private void updatePositionHistory() {
        long hash = getPositionHash();
        positionHistory.put(hash, positionHistory.getOrDefault(hash, 0) + 1);
    }

    // Add this method to clear the position history
    public void clearPositionHistory() {
        positionHistory.clear();
    }

    // Add this method to check for threefold repetition
    public boolean isThreefoldRepetition() {
        long hash = getPositionHash();
        return positionHistory.getOrDefault(hash, 0) >= 3;
    }

    public boolean isThreefoldRepetitionDuringSearch(Map<Long, Integer> searchHistory) {
        long currentHash = getPositionHash();
        return positionHistory.getOrDefault(currentHash, 0) >= 2;
    }


    // Add this to your isDraw method for more detailed debugging
    public boolean isDraw() {
        boolean isTFR = isThreefoldRepetition();
        boolean isStale = isStalemate();
        boolean isInsuf = isInsufficientMaterial();

        if (isTFR) {
            System.out.println("Threefold repetition detected:");
            long currentHash = getPositionHash();
            System.out.println("Current position hash: " + currentHash);
            System.out.println("Repetition count: " + positionHistory.getOrDefault(currentHash, 0));
        }

        // Rest of your code...
        return isTFR || isStale || isInsuf;
    }
    private boolean isInsufficientMaterial() {
        // Count pieces
        int whitePieceCount = Long.bitCount(whitePieces);
        int blackPieceCount = Long.bitCount(blackPieces);

        // King vs King
        if (whitePieceCount == 1 && blackPieceCount == 1) {
            return true;
        }

        // King + Knight/Bishop vs King
        if ((whitePieceCount == 2 && blackPieceCount == 1) ||
                (whitePieceCount == 1 && blackPieceCount == 2)) {
            long knights = whiteKnights | blackKnights;
            long bishops = whiteBishops | blackBishops;

            // Check if the extra piece is a knight or bishop
            if (Long.bitCount(knights) == 1 || Long.bitCount(bishops) == 1) {
                return true;
            }
        }

        // King + Bishop vs King + Bishop of same color
        if (whitePieceCount == 2 && blackPieceCount == 2 &&
                Long.bitCount(whiteBishops) == 1 && Long.bitCount(blackBishops) == 1) {
            // Check if bishops are on the same color squares
            boolean whiteSquareBishop = (whiteBishops & Bitboard.WHITE_SQUARES) != 0;
            boolean blackSquareBishop = (blackBishops & Bitboard.WHITE_SQUARES) != 0;

            if (whiteSquareBishop == blackSquareBishop) {
                return true;
            }
        }

        return false;
    }
    /**
     * Determines if the current position is a checkmate
     * (side to move is in check and has no legal moves)
     *
     * @return true if the position is checkmate, false otherwise
     */
    /**
     * Determines if the current position is a checkmate
     * (side to move is in check and has no legal moves)
     *
     * @return true if the position is checkmate, false otherwise
     */
    public boolean isCheckmate() {
        // Check if the king is in check and there are no legal moves
        return isInCheck() && generateLegalMoves().isEmpty();
    }

    /**
     * Determines if the current position is a stalemate
     * (side to move is not in check but has no legal moves)
     *
     * @return true if the position is stalemate, false otherwise
     */
    public boolean isStalemate() {
        // Check if the king is NOT in check but there are no legal moves
        return !isInCheck() && generateLegalMoves().isEmpty();
    }



    /**
     * Set which side is to move
     *
     * @param whiteToMove True if white is to move, false if black
     */
    public void setWhiteToMove(boolean whiteToMove) {
        this.whiteToMove = whiteToMove;
    }

    /**
     * Set castling availability for white kingside
     *
     * @param canCastle True if white can castle kingside
     */
    public void setCastleWhiteKingside(boolean canCastle) {
        this.castleWhiteKingside = canCastle;
    }

    /**
     * Set castling availability for white queenside
     *
     * @param canCastle True if white can castle queenside
     */
    public void setCastleWhiteQueenside(boolean canCastle) {
        this.castleWhiteQueenside = canCastle;
    }

    /**
     * Set castling availability for black kingside
     *
     * @param canCastle True if black can castle kingside
     */
    public void setCastleBlackKingside(boolean canCastle) {
        this.castleBlackKingside = canCastle;
    }

    /**
     * Set castling availability for black queenside
     *
     * @param canCastle True if black can castle queenside
     */
    public void setCastleBlackQueenside(boolean canCastle) {
        this.castleBlackQueenside = canCastle;
    }

    /**
     * Set the en passant target square
     *
     * @param square The en passant square index, or -1 if not available
     */
    public void setEnPassantSquare(int square) {
        this.enPassantSquare = square;
    }

    /**
     * Set the halfmove clock (for 50-move rule)
     *
     * @param halfmoveClock The halfmove clock value
     */
    public void setHalfmoveClock(int halfmoveClock) {
        this.halfmoveClock = halfmoveClock;
    }

    /**
     * Set the fullmove number
     *
     * @param fullmoveNumber The fullmove number
     */
    public void setFullmoveNumber(int fullmoveNumber) {
        this.fullmoveNumber = fullmoveNumber;
    }
}