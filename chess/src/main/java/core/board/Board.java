package core.board;

import core.bitboard.Bitboard;
import core.fen.FenGenerator;
import core.util.MoveNotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.HashMap;
import java.util.Map;

public class Board {
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

        clearPositionHistory();

        updatePositionHistory();
    }
    private void updateConvenienceBitboards() {
        whitePieces = whitePawns | whiteKnights | whiteBishops | whiteRooks | whiteQueens | whiteKing;
        blackPieces = blackPawns | blackKnights | blackBishops | blackRooks | blackQueens | blackKing;
        allPieces = whitePieces | blackPieces;
    }
    public boolean isValid() {
        // Check if exactly one king of each color exists
        if (Long.bitCount(whiteKing) != 1 || Long.bitCount(blackKing) != 1) {
            return false;
        }

        // Check if kings are adjacent (illegal position)
        int whiteKingSquare = Long.numberOfTrailingZeros(whiteKing);
        int blackKingSquare = Long.numberOfTrailingZeros(blackKing);
        int fileDiff = Math.abs((whiteKingSquare % 8) - (blackKingSquare % 8));
        int rankDiff = Math.abs((whiteKingSquare / 8) - (blackKingSquare / 8));
        if (fileDiff <= 1 && rankDiff <= 1) {
            return false;
        }

        // Check if the side not to move is in check (illegal position)
        boolean originalWhiteToMove = whiteToMove;
        whiteToMove = !whiteToMove;
        boolean inCheck = isInCheck();
        whiteToMove = originalWhiteToMove;
        if (inCheck) {
            return false;
        }

        // Check for valid piece counts
        if (Long.bitCount(whitePawns) > 8 || Long.bitCount(blackPawns) > 8) {
            return false;
        }

        // Check for pawns on first or last rank
        if ((whitePawns & 0xFF00000000000000L) != 0 || (blackPawns & 0x00000000000000FFL) != 0) {
            return false;
        }

        // Check for overlapping pieces
        long whitePieceSum = whitePawns | whiteKnights | whiteBishops | whiteRooks | whiteQueens | whiteKing;
        long blackPieceSum = blackPawns | blackKnights | blackBishops | blackRooks | blackQueens | blackKing;

        if (whitePieceSum != whitePieces || blackPieceSum != blackPieces) {
            return false;
        }

        if ((whitePieces & blackPieces) != 0) {
            return false;
        }

        // Check for valid castling rights
        if (castleWhiteKingside && (whiteKing & 0x0000000000000010L) == 0) {
            return false;
        }
        if (castleWhiteQueenside && (whiteKing & 0x0000000000000010L) == 0) {
            return false;
        }
        if (castleBlackKingside && (blackKing & 0x1000000000000000L) == 0) {
            return false;
        }
        if (castleBlackQueenside && (blackKing & 0x1000000000000000L) == 0) {
            return false;
        }

        // Check for valid en passant square
        if (enPassantSquare != -1) {
            int rank = enPassantSquare / 8;
            if (whiteToMove && rank != 5) {
                return false;
            }
            if (!whiteToMove && rank != 2) {
                return false;
            }
        }

        return true;
    }
    public boolean isInCheck() {
        // Use the MoveGenerator's isKingInCheck method to determine if the current side's king is in check
        return moveGenerator.isKingInCheck(this, isWhiteToMove());
    }
    public int getHalfmoveClock() {
        return halfmoveClock;
    }
    public int getFullmoveNumber() {
        return fullmoveNumber;
    }
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
    public int getPieceType(int square) {
        long squareBB = Bitboard.getBit(square);

        // Check white pieces
        if ((whitePawns & squareBB) != 0) return 0;
        if ((whiteKnights & squareBB) != 0) return 1;
        if ((whiteBishops & squareBB) != 0) return 2;
        if ((whiteRooks & squareBB) != 0) return 3;
        if ((whiteQueens & squareBB) != 0) return 4;
        if ((whiteKing & squareBB) != 0) return 5;

        // Check black pieces
        if ((blackPawns & squareBB) != 0) return 0;
        if ((blackKnights & squareBB) != 0) return 1;
        if ((blackBishops & squareBB) != 0) return 2;
        if ((blackRooks & squareBB) != 0) return 3;
        if ((blackQueens & squareBB) != 0) return 4;
        if ((blackKing & squareBB) != 0) return 5;

        return -1; // No piece found
    }
    public boolean hasPiece(int square) {
        long squareBB = Bitboard.getBit(square);
        return (allPieces & squareBB) != 0;
    }
    public boolean isWhitePiece(int square) {
        long squareBB = Bitboard.getBit(square);
        return (whitePieces & squareBB) != 0;
    }

    @Override
    public Board clone() {
        Board clonedBoard = new Board();

        // Copy bitboards
        clonedBoard.whitePawns = this.whitePawns;
        clonedBoard.whiteKnights = this.whiteKnights;
        clonedBoard.whiteBishops = this.whiteBishops;
        clonedBoard.whiteRooks = this.whiteRooks;
        clonedBoard.whiteQueens = this.whiteQueens;
        clonedBoard.whiteKing = this.whiteKing;

        clonedBoard.blackPawns = this.blackPawns;
        clonedBoard.blackKnights = this.blackKnights;
        clonedBoard.blackBishops = this.blackBishops;
        clonedBoard.blackRooks = this.blackRooks;
        clonedBoard.blackQueens = this.blackQueens;
        clonedBoard.blackKing = this.blackKing;

        // Copy convenience bitboards
        clonedBoard.whitePieces = this.whitePieces;
        clonedBoard.blackPieces = this.blackPieces;
        clonedBoard.allPieces = this.allPieces;

        // Copy game state
        clonedBoard.whiteToMove = this.whiteToMove;
        clonedBoard.castleWhiteKingside = this.castleWhiteKingside;
        clonedBoard.castleWhiteQueenside = this.castleWhiteQueenside;
        clonedBoard.castleBlackKingside = this.castleBlackKingside;
        clonedBoard.castleBlackQueenside = this.castleBlackQueenside;
        clonedBoard.enPassantSquare = this.enPassantSquare;
        clonedBoard.halfmoveClock = this.halfmoveClock;
        clonedBoard.fullmoveNumber = this.fullmoveNumber;

        return clonedBoard;
    }
    public List<String> getGameHistoryAsFEN() {
        List<String> fenHistory = new ArrayList<>();
        Board tempBoard = new Board();
        tempBoard.setInitialPosition();
        fenHistory.add(FenGenerator.generateFen(tempBoard));
        Stack<Move> moves = getMoveSequence();
        for (Move move : moves) {
            tempBoard.makeMove(move);
            fenHistory.add(FenGenerator.generateFen(tempBoard));
        }
        return fenHistory;
    }
    public List<String> getGameHistoryAsPGN() {
        List<String> pgnMoves = new ArrayList<>();
        Board tempBoard = new Board();
        tempBoard.setInitialPosition();
        Stack<Move> moves = getMoveSequence();
        int moveNumber = 1;
        boolean isWhiteMove = true;
        for (Move move : moves) {
            String algebraicMove = MoveNotation.toAlgebraic(tempBoard, move);
            if (isWhiteMove) {
                pgnMoves.add(moveNumber + ". " + algebraicMove);
            } else {
                pgnMoves.add(algebraicMove);
                moveNumber++;
            }
            tempBoard.makeMove(move);
            isWhiteMove = !isWhiteMove;
        }

        return pgnMoves;
    }
    private Stack<Move> getMoveSequence() {
        Stack<Move> moves = new Stack<>();
        for (BoardState state : moveHistory) {
            moves.add(state.move);
        }

        return moves;
    }
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
        Move move;
        long capturedPieceBB; // Bitboard with a single bit for the captured piece location
        int capturedPieceType; // Type of the captured piece (PAWN, KNIGHT, etc.)
        boolean isWhitePiece; // Whether the captured piece was white
        public long positionHash;
        public long newPositionHash;
    }
    public boolean makeMove(Move move) {
        int from = move.getFrom();
        int to = move.getTo();
        int moveType = move.getMoveType();

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

        long fromBB = Bitboard.getBit(from);
        long toBB = Bitboard.getBit(to);

        boolean isWhitePiece = Bitboard.isBitSet(whitePieces, from);
        boolean isBlackPiece = Bitboard.isBitSet(blackPieces, from);

        if ((whiteToMove && !isWhitePiece) || (!whiteToMove && !isBlackPiece)) {
            return false; // Wrong color piece
        }

        int pieceType = identifyPiece(from, isWhitePiece);
        if (pieceType == -1) {
            return false; // No piece found
        }

        long opponentPieces = whiteToMove ? blackPieces : whitePieces;
        boolean isCapture = Bitboard.isBitSet(opponentPieces, to);

        if (isCapture) {
            state.capturedPieceBB = toBB;
            state.capturedPieceType = identifyPiece(to, !isWhitePiece);
            state.isWhitePiece = !isWhitePiece;
        }
        if (moveType == Move.EN_PASSANT) {
            int epCaptureSquare = whiteToMove ? to - 8 : to + 8;
            state.capturedPieceBB = Bitboard.getBit(epCaptureSquare);
            state.capturedPieceType = 0; // Pawn
            state.isWhitePiece = !isWhitePiece;
        }
        if (pieceType == 0 || isCapture) { // Reset on pawn move or capture
            halfmoveClock = 0;
        } else {
            halfmoveClock++;
        }
        if (!whiteToMove) {
            fullmoveNumber++;
        }
        moveHistory.push(state);
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
        updateCastlingRights(from, to, pieceType);
        if (pieceType == 0 && Math.abs(from - to) == 16) { // Pawn double move
            enPassantSquare = whiteToMove ? from + 8 : from - 8;
        } else {
            enPassantSquare = -1;
        }
        whiteToMove = !whiteToMove;
        updateConvenienceBitboards();
        state.newPositionHash = getPositionHash();
        updatePositionHistory();
        return true;
    }
    public boolean makeSearchMove(Move move) {
        int from = move.getFrom();
        int to = move.getTo();
        int moveType = move.getMoveType();

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

        long fromBB = Bitboard.getBit(from);
        long toBB = Bitboard.getBit(to);
        boolean isWhitePiece = Bitboard.isBitSet(whitePieces, from);
        boolean isBlackPiece = Bitboard.isBitSet(blackPieces, from);

        if ((whiteToMove && !isWhitePiece) || (!whiteToMove && !isBlackPiece)) {
            System.out.println("Error: Wrong color piece at " + from);
            return false; // Wrong color piece
        }

        // FIX: Pass the square index, not the bitboard
        int pieceType = identifyPiece(from, isWhitePiece);
        if (pieceType == -1) {
            System.out.println("Error: No piece found at " + from);
            return false; // No piece found
        }


        long opponentPieces = whiteToMove ? blackPieces : whitePieces;
        boolean isCapture = Bitboard.isBitSet(opponentPieces, to);

        if (isCapture) {
            int capturedPieceType = identifyPiece(to, !isWhitePiece);
            state.capturedPieceBB = toBB;
            state.capturedPieceType = capturedPieceType;
            state.isWhitePiece = !isWhitePiece;
        }

        if (moveType == Move.EN_PASSANT) {
            int epCaptureSquare = whiteToMove ? to - 8 : to + 8;
            state.capturedPieceBB = Bitboard.getBit(epCaptureSquare);
            state.capturedPieceType = 0; // Pawn
            state.isWhitePiece = !isWhitePiece;
        }

        if (pieceType == 0 || isCapture) { // Reset on pawn move or capture
            halfmoveClock = 0;
        } else {
            halfmoveClock++;
        }

        if (!whiteToMove) {
            fullmoveNumber++;
        }

        moveHistory.push(state);

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


        updateCastlingRights(from, to, pieceType);

        if (pieceType == 0 && Math.abs(from - to) == 16) { // Pawn double move
            enPassantSquare = whiteToMove ? from + 8 : from - 8;
        } else {
            enPassantSquare = -1;
        }

        whiteToMove = !whiteToMove;
        updateConvenienceBitboards();
        return true;
    }
    public boolean undoSearchMove() {
        if (moveHistory.isEmpty()) {
            return false;
        }

        BoardState state = moveHistory.pop();
        whiteToMove = state.whiteToMove;
        castleWhiteKingside = state.castleWhiteKingside;
        castleWhiteQueenside = state.castleWhiteQueenside;
        castleBlackKingside = state.castleBlackKingside;
        castleBlackQueenside = state.castleBlackQueenside;
        enPassantSquare = state.enPassantSquare;
        halfmoveClock = state.halfmoveClock;
        fullmoveNumber = state.fullmoveNumber;
        Move move = state.move;
        int from = move.getFrom();
        int to = move.getTo();
        int moveType = move.getMoveType();

        long fromBB = Bitboard.getBit(from);
        long toBB = Bitboard.getBit(to);

        boolean isWhitePiece = whiteToMove;
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
        if (state.capturedPieceBB != 0) {
            restoreCapturedPiece(state.capturedPieceBB, state.capturedPieceType, state.isWhitePiece);
        }
        updateConvenienceBitboards();
        return true;
    }
    public boolean undoMove() {
        if (moveHistory.isEmpty()) {
            return false;
        }

        BoardState state = moveHistory.pop();
        long currentHash = getPositionHash();
        whiteToMove = state.whiteToMove;
        castleWhiteKingside = state.castleWhiteKingside;
        castleWhiteQueenside = state.castleWhiteQueenside;
        castleBlackKingside = state.castleBlackKingside;
        castleBlackQueenside = state.castleBlackQueenside;
        enPassantSquare = state.enPassantSquare;
        halfmoveClock = state.halfmoveClock;
        fullmoveNumber = state.fullmoveNumber;

        Move move = state.move;
        int from = move.getFrom();
        int to = move.getTo();
        int moveType = move.getMoveType();

        long fromBB = Bitboard.getBit(from);
        long toBB = Bitboard.getBit(to);

        boolean isWhitePiece = whiteToMove;
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
        if (state.capturedPieceBB != 0) {
            restoreCapturedPiece(state.capturedPieceBB, state.capturedPieceType, state.isWhitePiece);
        }
        undoPositionHistoryUpdate();

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
    private void undoPositionHistoryUpdate() {
        if (moveHistory.isEmpty()) {
            return; // Nothing to undo
        }
        BoardState lastState = moveHistory.peek();
        long hashToRemove = lastState.newPositionHash;
        int count = positionHistory.getOrDefault(hashToRemove, 0);
        if (count > 0) {
            if (count == 1) {
                positionHistory.remove(hashToRemove);
            } else {
                positionHistory.put(hashToRemove, count - 1);
            }
        }
    }
    private int identifyPiece(int square, boolean isWhite) {
        long position = Bitboard.getBit(square);
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
    private void executeNormalMove(int from, int to, int pieceType, boolean isWhite, boolean isCapture) {
        long fromBB = Bitboard.getBit(from);
        long toBB = Bitboard.getBit(to);

        // Remove captured piece FIRST if this is a capture
        if (isCapture) {
            if (isWhite) {
                // White is capturing, so remove whatever black piece is on the target square
                if (Bitboard.isBitSet(blackPawns, to)) { blackPawns &= ~toBB;  }
                else if (Bitboard.isBitSet(blackKnights, to)) { blackKnights &= ~toBB;  }
                else if (Bitboard.isBitSet(blackBishops, to)) { blackBishops &= ~toBB;  }
                else if (Bitboard.isBitSet(blackRooks, to)) { blackRooks &= ~toBB;  }
                else if (Bitboard.isBitSet(blackQueens, to)) { blackQueens &= ~toBB;  }
                else if (Bitboard.isBitSet(blackKing, to)) { blackKing &= ~toBB;  }
            } else {
                // Black is capturing, so remove whatever white piece is on the target square
                if (Bitboard.isBitSet(whitePawns, to)) { whitePawns &= ~toBB;  }
                else if (Bitboard.isBitSet(whiteKnights, to)) { whiteKnights &= ~toBB;  }
                else if (Bitboard.isBitSet(whiteBishops, to)) { whiteBishops &= ~toBB;  }
                else if (Bitboard.isBitSet(whiteRooks, to)) { whiteRooks &= ~toBB;  }
                else if (Bitboard.isBitSet(whiteQueens, to)) { whiteQueens &= ~toBB;  }
                else if (Bitboard.isBitSet(whiteKing, to)) { whiteKing &= ~toBB;  }
            }
        }

        // Remove the moving piece from its original square
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

        // Place the moving piece on its destination square
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
    private void executePawnPromotion(int from, int to, int promotionPieceType, boolean isWhite, boolean isCapture) {
        long fromBB = Bitboard.getBit(from);
        long toBB = Bitboard.getBit(to);

        if (isWhite) {
            whitePawns &= ~fromBB;
        } else {
            blackPawns &= ~fromBB;
        }

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
    private void executeEnPassantMove(int from, int to, boolean isWhite) {
        long fromBB = Bitboard.getBit(from);
        long toBB = Bitboard.getBit(to);

        int capturedPawnSquare = isWhite ? to - 8 : to + 8;
        long capturedPawnBB = Bitboard.getBit(capturedPawnSquare);

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
    private void executeCastlingMove(int from, int to, boolean isWhite) {
        long fromBB = Bitboard.getBit(from);
        long toBB = Bitboard.getBit(to);

        if (isWhite) {
            whiteKing &= ~fromBB;
            whiteKing |= toBB;
        } else {
            blackKing &= ~fromBB;
            blackKing |= toBB;
        }

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
    private void updateCastlingRights(int from, int to, int pieceType) {
        if (pieceType == 5) { // King move
            if (whiteToMove) {
                castleWhiteKingside = false;
                castleWhiteQueenside = false;
            } else {
                castleBlackKingside = false;
                castleBlackQueenside = false;
            }
        }

        if (pieceType == 3) { // Rook move
            if (from == 0) castleWhiteQueenside = false;
            if (from == 7) castleWhiteKingside = false;
            if (from == 56) castleBlackQueenside = false;
            if (from == 63) castleBlackKingside = false;
        }

        if (to == 0) castleWhiteQueenside = false;
        if (to == 7) castleWhiteKingside = false;
        if (to == 56) castleBlackQueenside = false;
        if (to == 63) castleBlackKingside = false;
    }
    private void undoNormalMove(int from, int to, boolean isWhite, BoardState state) {
        long fromBB = Bitboard.getBit(from);
        long toBB = Bitboard.getBit(to);

        // FIX: Pass the square index (to), not the bitboard (toBB)
        int pieceType = identifyPiece(to, isWhite);


        // Remove the piece from the destination square and place it back on the source square
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
    private void undoPawnPromotion(int from, int to, int promotionPieceType, boolean isWhite, BoardState state) {
        long fromBB = Bitboard.getBit(from);
        long toBB = Bitboard.getBit(to);

        if (isWhite) {
            switch (promotionPieceType) {
                case Move.QUEEN_PROMOTION: whiteQueens &= ~toBB; break;
                case Move.ROOK_PROMOTION: whiteRooks &= ~toBB; break;
                case Move.BISHOP_PROMOTION: whiteBishops &= ~toBB; break;
                case Move.KNIGHT_PROMOTION: whiteKnights &= ~toBB; break;
            }
            whitePawns |= fromBB;
        } else {
            switch (promotionPieceType) {
                case Move.QUEEN_PROMOTION: blackQueens &= ~toBB; break;
                case Move.ROOK_PROMOTION: blackRooks &= ~toBB; break;
                case Move.BISHOP_PROMOTION: blackBishops &= ~toBB; break;
                case Move.KNIGHT_PROMOTION: blackKnights &= ~toBB; break;
            }
            blackPawns |= fromBB;
        }
    }
    private void undoEnPassantMove(int from, int to, boolean isWhite, BoardState state) {
        long fromBB = Bitboard.getBit(from);
        long toBB = Bitboard.getBit(to);

        if (isWhite) {
            whitePawns &= ~toBB;
            whitePawns |= fromBB;
        } else {
            blackPawns &= ~toBB;
            blackPawns |= fromBB;
        }
    }
    private void undoCastlingMove(int from, int to, boolean isWhite) {
        long fromBB = Bitboard.getBit(from);
        long toBB = Bitboard.getBit(to);

        if (isWhite) {
            whiteKing &= ~toBB;
            whiteKing |= fromBB;
        } else {
            blackKing &= ~toBB;
            blackKing |= fromBB;
        }

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
    public boolean canCastleBlackKingside() {
        return castleBlackKingside;
    }
    public boolean canCastleBlackQueenside() {
        return castleBlackQueenside;
    }
    public boolean canCastleWhiteKingside() {
        return castleWhiteKingside;
    }
    public boolean canCastleWhiteQueenside() {
        return castleWhiteQueenside;
    }
    public int getEnPassantSquare() {
        return enPassantSquare;
    }
    public void clear() {
        whitePawns = whiteKnights = whiteBishops = whiteRooks = whiteQueens = whiteKing = 0L;
        blackPawns = blackKnights = blackBishops = blackRooks = blackQueens = blackKing = 0L;

        whitePieces = blackPieces = allPieces = 0L;

        whiteToMove = true;
        castleWhiteKingside = castleWhiteQueenside = false;
        castleBlackKingside = castleBlackQueenside = false;
        enPassantSquare = -1;
        halfmoveClock = 0;
        fullmoveNumber = 1;

        clearPositionHistory();
    }
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
        updateConvenienceBitboards();
    }
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
    public boolean isLegalMove(Move move) {
        int fromSquare = move.getFrom();
        int piece = getPiece(fromSquare);

        if (piece == 0) return false;
        if ((whiteToMove && piece > 6) || (!whiteToMove && piece <= 6)) return false;

        List<Move> legalMoves = generateLegalMoves();
        return legalMoves.contains(move);
    }
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
        long hash = 0;
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

        hash ^= whiteToMove ? 1L << 50 : 0;
        hash ^= castleWhiteKingside ? 1L << 51 : 0;
        hash ^= castleWhiteQueenside ? 1L << 52 : 0;
        hash ^= castleBlackKingside ? 1L << 53 : 0;
        hash ^= castleBlackQueenside ? 1L << 54 : 0;

        if (enPassantSquare != -1) {
            hash ^= 1L << (enPassantSquare);
        }

        return hash;
    }
    private void updatePositionHistory() {
        long hash = getPositionHash();
        positionHistory.put(hash, positionHistory.getOrDefault(hash, 0) + 1);
    }
    public void clearPositionHistory() {
        positionHistory.clear();
    }
    public boolean isThreefoldRepetition() {
        long hash = getPositionHash();
        return positionHistory.getOrDefault(hash, 0) >= 3;
    }

    public boolean isThreefoldRepetitionDuringSearch(Map<Long, Integer> searchHistory) {
        long currentHash = getPositionHash();
        return positionHistory.getOrDefault(currentHash, 0) >= 2;
    }
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
        return isTFR || isStale || isInsuf;
    }
    private boolean isInsufficientMaterial() {
        int whitePieceCount = Long.bitCount(whitePieces);
        int blackPieceCount = Long.bitCount(blackPieces);

        if (whitePieceCount == 1 && blackPieceCount == 1) {
            return true;
        }

        if ((whitePieceCount == 2 && blackPieceCount == 1) ||
                (whitePieceCount == 1 && blackPieceCount == 2)) {
            long knights = whiteKnights | blackKnights;
            long bishops = whiteBishops | blackBishops;

            if (Long.bitCount(knights) == 1 || Long.bitCount(bishops) == 1) {
                return true;
            }
        }

        if (whitePieceCount == 2 && blackPieceCount == 2 &&
                Long.bitCount(whiteBishops) == 1 && Long.bitCount(blackBishops) == 1) {
            boolean whiteSquareBishop = (whiteBishops & Bitboard.WHITE_SQUARES) != 0;
            boolean blackSquareBishop = (blackBishops & Bitboard.WHITE_SQUARES) != 0;

            if (whiteSquareBishop == blackSquareBishop) {
                return true;
            }
        }

        return false;
    }
    public boolean isCheckmate() {
        // Check if the king is in check and there are no legal moves
        return isInCheck() && generateLegalMoves().isEmpty();
    }
    public boolean isStalemate() {
        return !isInCheck() && generateLegalMoves().isEmpty();
    }
    public void setWhiteToMove(boolean whiteToMove) {
        this.whiteToMove = whiteToMove;
    }
    public void setCastleWhiteKingside(boolean canCastle) {
        this.castleWhiteKingside = canCastle;
    }
    public void setCastleWhiteQueenside(boolean canCastle) {
        this.castleWhiteQueenside = canCastle;
    }
    public void setCastleBlackKingside(boolean canCastle) {
        this.castleBlackKingside = canCastle;
    }
    public void setCastleBlackQueenside(boolean canCastle) {
        this.castleBlackQueenside = canCastle;
    }
    public void setEnPassantSquare(int square) {
        this.enPassantSquare = square;
    }
    public void setHalfmoveClock(int halfmoveClock) {
        this.halfmoveClock = halfmoveClock;
    }
    public void setFullmoveNumber(int fullmoveNumber) {
        this.fullmoveNumber = fullmoveNumber;
    }
}