package core.board;

import core.bitboard.Bitboard;
import core.bitboard.BitboardConstants;

/**
 * Generates legal moves for a given chess position
 */
public class MoveGenerator {
    // Precomputed move patterns (would normally be initialized in static block)
    private final long[] knightAttacks = new long[64];
    private final long[] kingAttacks = new long[64];

    public MoveGenerator() {
        initializeAttackTables();
    }

    /**
     * Initialize precomputed attack tables for non-sliding pieces
     */
    private void initializeAttackTables() {
        // Knight moves
        int[] knightOffsets = {-17, -15, -10, -6, 6, 10, 15, 17};
        for (int square = 0; square < 64; square++) {
            long attacks = 0L;
            for (int offset : knightOffsets) {
                int targetSquare = square + offset;
                if (targetSquare >= 0 && targetSquare < 64 && getSquareDistance(square, targetSquare) <= 2) {
                    attacks |= Bitboard.getBit(targetSquare);
                }
            }
            knightAttacks[square] = attacks;
        }

        // King moves
        int[] kingOffsets = {-9, -8, -7, -1, 1, 7, 8, 9};
        for (int square = 0; square < 64; square++) {
            long attacks = 0L;
            for (int offset : kingOffsets) {
                int targetSquare = square + offset;
                if (targetSquare >= 0 && targetSquare < 64 && getSquareDistance(square, targetSquare) <= 1) {
                    attacks |= Bitboard.getBit(targetSquare);
                }
            }
            kingAttacks[square] = attacks;
        }
    }

    /**
     * Helper method to get the distance between two squares (for move validation)
     */
    private int getSquareDistance(int sq1, int sq2) {
        int file1 = sq1 % 8;
        int rank1 = sq1 / 8;
        int file2 = sq2 % 8;
        int rank2 = sq2 / 8;

        return Math.max(Math.abs(file1 - file2), Math.abs(rank1 - rank2));
    }

    /**
     * Generate all legal moves for the current position
     */
    public void generateLegalMoves(Board board, MoveList moveList) {
        // Clear the move list
        moveList.clear();

        // Generate pseudo-legal moves
        if (board.isWhiteToMove()) {
            generateWhiteMoves(board, moveList);
        } else {
            generateBlackMoves(board, moveList);
        }

        // Filter out illegal moves (those that leave the king in check)
        filterIllegalMoves(board, moveList);
    }

    /**
     * Generate all pseudo-legal moves for white
     */
    private void generateWhiteMoves(Board board, MoveList moveList) {
        long whitePieces = board.getWhitePieces();
        long blackPieces = board.getBlackPieces();
        long allPieces = whitePieces | blackPieces;
        long emptySquares = ~allPieces;

        // Generate pawn moves
        generateWhitePawnMoves(board, moveList, emptySquares, blackPieces);

        // Generate knight moves
        generateKnightMoves(board, moveList, board.getWhiteKnights(), emptySquares, blackPieces);

        // Generate bishop moves
        generateBishopMoves(board, moveList, board.getWhiteBishops(), emptySquares, blackPieces, allPieces);

        // Generate rook moves
        generateRookMoves(board, moveList, board.getWhiteRooks(), emptySquares, blackPieces, allPieces);

        // Generate queen moves (combination of bishop and rook)
        generateBishopMoves(board, moveList, board.getWhiteQueens(), emptySquares, blackPieces, allPieces);
        generateRookMoves(board, moveList, board.getWhiteQueens(), emptySquares, blackPieces, allPieces);

        // Generate king moves
        generateWhiteKingMoves(board, moveList, emptySquares, blackPieces);
    }

    /**
     * Generate all pseudo-legal moves for black
     */
    private void generateBlackMoves(Board board, MoveList moveList) {
        long whitePieces = board.getWhitePieces();
        long blackPieces = board.getBlackPieces();
        long allPieces = whitePieces | blackPieces;
        long emptySquares = ~allPieces;

        // Generate pawn moves
        generateBlackPawnMoves(board, moveList, emptySquares, whitePieces);

        // Generate knight moves
        generateKnightMoves(board, moveList, board.getBlackKnights(), emptySquares, whitePieces);

        // Generate bishop moves
        generateBishopMoves(board, moveList, board.getBlackBishops(), emptySquares, whitePieces, allPieces);

        // Generate rook moves
        generateRookMoves(board, moveList, board.getBlackRooks(), emptySquares, whitePieces, allPieces);

        // Generate queen moves (combination of bishop and rook)
        generateBishopMoves(board, moveList, board.getBlackQueens(), emptySquares, whitePieces, allPieces);
        generateRookMoves(board, moveList, board.getBlackQueens(), emptySquares, whitePieces, allPieces);

        // Generate king moves
        generateBlackKingMoves(board, moveList, emptySquares, whitePieces);
    }

    /**
     * Generate white pawn moves
     */
    private void generateWhitePawnMoves(Board board, MoveList moveList, long emptySquares, long blackPieces) {
        long whitePawns = board.getWhitePawns();

        // Single push
        long singlePush = (whitePawns << 8) & emptySquares;

        // Double push
        long doublePush = ((singlePush & BitboardConstants.RANK_3) << 8) & emptySquares;

        // Captures to the right
        long captureRight = (whitePawns << 9) & blackPieces & ~BitboardConstants.FILE_A;

        // Captures to the left
        long captureLeft = (whitePawns << 7) & blackPieces & ~BitboardConstants.FILE_H;

        // En passant captures
        int epSquare = board.getEnPassantSquare();
        if (epSquare != -1) {
            long epTarget = Bitboard.getBit(epSquare);

            // Right en passant capture
            if ((whitePawns & (epTarget >> 9) & ~BitboardConstants.FILE_A) != 0) {
                moveList.add(Move.special(epSquare - 9, epSquare, Move.EN_PASSANT));
            }

            // Left en passant capture
            if ((whitePawns & (epTarget >> 7) & ~BitboardConstants.FILE_H) != 0) {
                moveList.add(Move.special(epSquare - 7, epSquare, Move.EN_PASSANT));
            }
        }

        // Process single pushes
        while (singlePush != 0) {
            int to = Bitboard.getLSB(singlePush);
            int from = to - 8;

            // Check for promotion
            if (to >= 56) { // Rank 8
                moveList.add(Move.promotion(from, to, Move.QUEEN_PROMOTION));
                moveList.add(Move.promotion(from, to, Move.ROOK_PROMOTION));
                moveList.add(Move.promotion(from, to, Move.BISHOP_PROMOTION));
                moveList.add(Move.promotion(from, to, Move.KNIGHT_PROMOTION));
            } else {
                moveList.add(new Move(from, to));
            }

            singlePush = Bitboard.popLSB(singlePush);
        }

        // Process double pushes
        while (doublePush != 0) {
            int to = Bitboard.getLSB(doublePush);
            int from = to - 16;
            moveList.add(new Move(from, to));
            doublePush = Bitboard.popLSB(doublePush);
        }

        // Process right captures
        while (captureRight != 0) {
            int to = Bitboard.getLSB(captureRight);
            int from = to - 9;

            // Check for promotion
            if (to >= 56) { // Rank 8
                moveList.add(Move.promotion(from, to, Move.QUEEN_PROMOTION));
                moveList.add(Move.promotion(from, to, Move.ROOK_PROMOTION));
                moveList.add(Move.promotion(from, to, Move.BISHOP_PROMOTION));
                moveList.add(Move.promotion(from, to, Move.KNIGHT_PROMOTION));
            } else {
                moveList.add(new Move(from, to));
            }

            captureRight = Bitboard.popLSB(captureRight);
        }

        // Process left captures
        while (captureLeft != 0) {
            int to = Bitboard.getLSB(captureLeft);
            int from = to - 7;

            // Check for promotion
            if (to >= 56) { // Rank 8
                moveList.add(Move.promotion(from, to, Move.QUEEN_PROMOTION));
                moveList.add(Move.promotion(from, to, Move.ROOK_PROMOTION));
                moveList.add(Move.promotion(from, to, Move.BISHOP_PROMOTION));
                moveList.add(Move.promotion(from, to, Move.KNIGHT_PROMOTION));
            } else {
                moveList.add(new Move(from, to));
            }

            captureLeft = Bitboard.popLSB(captureLeft);
        }
    }

    /**
     * Generate black pawn moves
     */
    private void generateBlackPawnMoves(Board board, MoveList moveList, long emptySquares, long whitePieces) {
        long blackPawns = board.getBlackPawns();

        // Single push
        long singlePush = (blackPawns >> 8) & emptySquares;

        // Double push
        long doublePush = ((singlePush & BitboardConstants.RANK_6) >> 8) & emptySquares;

        // Captures to the right
        long captureRight = (blackPawns >> 7) & whitePieces & ~BitboardConstants.FILE_A;

        // Captures to the left
        long captureLeft = (blackPawns >> 9) & whitePieces & ~BitboardConstants.FILE_H;

        // En passant captures
        int epSquare = board.getEnPassantSquare();
        if (epSquare != -1) {
            long epTarget = Bitboard.getBit(epSquare);

            // Right en passant capture
            if ((blackPawns & (epTarget << 7) & ~BitboardConstants.FILE_A) != 0) {
                moveList.add(Move.special(epSquare + 7, epSquare, Move.EN_PASSANT));
            }

            // Left en passant capture
            if ((blackPawns & (epTarget << 9) & ~BitboardConstants.FILE_H) != 0) {
                moveList.add(Move.special(epSquare + 9, epSquare, Move.EN_PASSANT));
            }
        }

        // Process single pushes
        while (singlePush != 0) {
            int to = Bitboard.getLSB(singlePush);
            int from = to + 8;

            // Check for promotion
            if (to <= 7) { // Rank 1
                moveList.add(Move.promotion(from, to, Move.QUEEN_PROMOTION));
                moveList.add(Move.promotion(from, to, Move.ROOK_PROMOTION));
                moveList.add(Move.promotion(from, to, Move.BISHOP_PROMOTION));
                moveList.add(Move.promotion(from, to, Move.KNIGHT_PROMOTION));
            } else {
                moveList.add(new Move(from, to));
            }

            singlePush = Bitboard.popLSB(singlePush);
        }

        // Process double pushes
        while (doublePush != 0) {
            int to = Bitboard.getLSB(doublePush);
            int from = to + 16;
            moveList.add(new Move(from, to));
            doublePush = Bitboard.popLSB(doublePush);
        }

        // Process right captures
        while (captureRight != 0) {
            int to = Bitboard.getLSB(captureRight);
            int from = to + 7;

            // Check for promotion
            if (to <= 7) { // Rank 1
                moveList.add(Move.promotion(from, to, Move.QUEEN_PROMOTION));
                moveList.add(Move.promotion(from, to, Move.ROOK_PROMOTION));
                moveList.add(Move.promotion(from, to, Move.BISHOP_PROMOTION));
                moveList.add(Move.promotion(from, to, Move.KNIGHT_PROMOTION));
            } else {
                moveList.add(new Move(from, to));
            }

            captureRight = Bitboard.popLSB(captureRight);
        }

        // Process left captures
        while (captureLeft != 0) {
            int to = Bitboard.getLSB(captureLeft);
            int from = to + 9;

            // Check for promotion
            if (to <= 7) { // Rank 1
                moveList.add(Move.promotion(from, to, Move.QUEEN_PROMOTION));
                moveList.add(Move.promotion(from, to, Move.ROOK_PROMOTION));
                moveList.add(Move.promotion(from, to, Move.BISHOP_PROMOTION));
                moveList.add(Move.promotion(from, to, Move.KNIGHT_PROMOTION));
            } else {
                moveList.add(new Move(from, to));
            }

            captureLeft = Bitboard.popLSB(captureLeft);
        }
    }

    /**
     * Generate knight moves
     */
    private void generateKnightMoves(Board board, MoveList moveList, long knights, long emptySquares, long enemyPieces) {
        while (knights != 0) {
            int from = Bitboard.getLSB(knights);
            long attacks = knightAttacks[from];

            // Captures
            long captures = attacks & enemyPieces;
            while (captures != 0) {
                int to = Bitboard.getLSB(captures);
                moveList.add(new Move(from, to));
                captures = Bitboard.popLSB(captures);
            }

            // Quiet moves
            long quietMoves = attacks & emptySquares;
            while (quietMoves != 0) {
                int to = Bitboard.getLSB(quietMoves);
                moveList.add(new Move(from, to));
                quietMoves = Bitboard.popLSB(quietMoves);
            }

            knights = Bitboard.popLSB(knights);
        }
    }

    /**
     * Generate bishop moves
     */
    private void generateBishopMoves(Board board, MoveList moveList, long bishops, long emptySquares, long enemyPieces, long allPieces) {
        while (bishops != 0) {
            int from = Bitboard.getLSB(bishops);

            // Generate diagonal attacks
            long attacks = generateDiagonalAttacks(from, allPieces);

            // Captures
            long captures = attacks & enemyPieces;
            while (captures != 0) {
                int to = Bitboard.getLSB(captures);
                moveList.add(new Move(from, to));
                captures = Bitboard.popLSB(captures);
            }

            // Quiet moves
            long quietMoves = attacks & emptySquares;
            while (quietMoves != 0) {
                int to = Bitboard.getLSB(quietMoves);
                moveList.add(new Move(from, to));
                quietMoves = Bitboard.popLSB(quietMoves);
            }

            bishops = Bitboard.popLSB(bishops);
        }
    }

    /**
     * Generate rook moves
     */
    private void generateRookMoves(Board board, MoveList moveList, long rooks, long emptySquares, long enemyPieces, long allPieces) {
        while (rooks != 0) {
            int from = Bitboard.getLSB(rooks);

            // Generate horizontal and vertical attacks
            long attacks = generateHorizontalAndVerticalAttacks(from, allPieces);

            // Captures
            long captures = attacks & enemyPieces;
            while (captures != 0) {
                int to = Bitboard.getLSB(captures);
                moveList.add(new Move(from, to));
                captures = Bitboard.popLSB(captures);
            }

            // Quiet moves
            long quietMoves = attacks & emptySquares;
            while (quietMoves != 0) {
                int to = Bitboard.getLSB(quietMoves);
                moveList.add(new Move(from, to));
                quietMoves = Bitboard.popLSB(quietMoves);
            }

            rooks = Bitboard.popLSB(rooks);
        }
    }

    /**
     * Generate white king moves including castling
     */
    private void generateWhiteKingMoves(Board board, MoveList moveList, long emptySquares, long blackPieces) {
        long whiteKing = board.getWhiteKing();
        if (whiteKing == 0) return;

        int from = Bitboard.getLSB(whiteKing);
        long attacks = kingAttacks[from];

        // Normal king moves

        // Captures
        long captures = attacks & blackPieces;
        while (captures != 0) {
            int to = Bitboard.getLSB(captures);
            moveList.add(new Move(from, to));
            captures = Bitboard.popLSB(captures);
        }

        // Quiet moves
        long quietMoves = attacks & emptySquares;
        while (quietMoves != 0) {
            int to = Bitboard.getLSB(quietMoves);
            moveList.add(new Move(from, to));
            quietMoves = Bitboard.popLSB(quietMoves);
        }

        // Castling
        if (from == 4) { // King is on e1
            // Kingside castling
            if (board.canCastleWhiteKingside() &&
                    (emptySquares & BitboardConstants.WHITE_KINGSIDE_CASTLE_MASK) == BitboardConstants.WHITE_KINGSIDE_CASTLE_MASK &&
                    !isSquareAttacked(board, 4, false) &&
                    !isSquareAttacked(board, 5, false) &&
                    !isSquareAttacked(board, 6, false)) {
                moveList.add(Move.special(4, 6, Move.CASTLING));
            }

            // Queenside castling
            if (board.canCastleWhiteQueenside() &&
                    (emptySquares & BitboardConstants.WHITE_QUEENSIDE_CASTLE_MASK) == BitboardConstants.WHITE_QUEENSIDE_CASTLE_MASK &&
                    !isSquareAttacked(board, 4, false) &&
                    !isSquareAttacked(board, 3, false) &&
                    !isSquareAttacked(board, 2, false)) {
                moveList.add(Move.special(4, 2, Move.CASTLING));
            }
        }
    }

    /**
     * Generate black king moves including castling
     */
    private void generateBlackKingMoves(Board board, MoveList moveList, long emptySquares, long whitePieces) {
        long blackKing = board.getBlackKing();
        if (blackKing == 0) return;

        int from = Bitboard.getLSB(blackKing);
        long attacks = kingAttacks[from];

        // Normal king moves

        // Captures
        long captures = attacks & whitePieces;
        while (captures != 0) {
            int to = Bitboard.getLSB(captures);
            moveList.add(new Move(from, to));
            captures = Bitboard.popLSB(captures);
        }

        // Quiet moves
        long quietMoves = attacks & emptySquares;
        while (quietMoves != 0) {
            int to = Bitboard.getLSB(quietMoves);
            moveList.add(new Move(from, to));
            quietMoves = Bitboard.popLSB(quietMoves);
        }

        // Castling
        if (from == 60) { // King is on e8
            // Kingside castling
            if (board.canCastleBlackKingside() &&
                    (emptySquares & BitboardConstants.BLACK_KINGSIDE_CASTLE_MASK) == BitboardConstants.BLACK_KINGSIDE_CASTLE_MASK &&
                    !isSquareAttacked(board, 60, true) &&
                    !isSquareAttacked(board, 61, true) &&
                    !isSquareAttacked(board, 62, true)) {
                moveList.add(Move.special(60, 62, Move.CASTLING));
            }

            // Queenside castling
            if (board.canCastleBlackQueenside() &&
                    (emptySquares & BitboardConstants.BLACK_QUEENSIDE_CASTLE_MASK) == BitboardConstants.BLACK_QUEENSIDE_CASTLE_MASK &&
                    !isSquareAttacked(board, 60, true) &&
                    !isSquareAttacked(board, 59, true) &&
                    !isSquareAttacked(board, 58, true)) {
                moveList.add(Move.special(60, 58, Move.CASTLING));
            }
        }
    }

    /**
     * Generate diagonal attacks (for bishops and queens)
     */
    private long generateDiagonalAttacks(int square, long occupancy) {
        // In a real engine, this would use magic bitboards or similar techniques
        // This is a simplified implementation

        long result = 0L;

        int rank = square / 8;
        int file = square % 8;

        // North-East
        for (int r = rank + 1, f = file + 1; r < 8 && f < 8; r++, f++) {
            int to = r * 8 + f;
            result |= Bitboard.getBit(to);
            if ((occupancy & Bitboard.getBit(to)) != 0) break;
        }

        // South-East
        for (int r = rank - 1, f = file + 1; r >= 0 && f < 8; r--, f++) {
            int to = r * 8 + f;
            result |= Bitboard.getBit(to);
            if ((occupancy & Bitboard.getBit(to)) != 0) break;
        }

        // South-West
        for (int r = rank - 1, f = file - 1; r >= 0 && f >= 0; r--, f--) {
            int to = r * 8 + f;
            result |= Bitboard.getBit(to);
            if ((occupancy & Bitboard.getBit(to)) != 0) break;
        }

        // North-West
        for (int r = rank + 1, f = file - 1; r < 8 && f >= 0; r++, f--) {
            int to = r * 8 + f;
            result |= Bitboard.getBit(to);
            if ((occupancy & Bitboard.getBit(to)) != 0) break;
        }

        return result;
    }

    /**
     * Generate horizontal and vertical attacks (for rooks and queens)
     */
    private long generateHorizontalAndVerticalAttacks(int square, long occupancy) {
        // In a real engine, this would use magic bitboards or similar techniques
        // This is a simplified implementation

        long result = 0L;

        int rank = square / 8;
        int file = square % 8;

        // North
        for (int r = rank + 1; r < 8; r++) {
            int to = r * 8 + file;
            result |= Bitboard.getBit(to);
            if ((occupancy & Bitboard.getBit(to)) != 0) break;
        }

        // East
        for (int f = file + 1; f < 8; f++) {
            int to = rank * 8 + f;
            result |= Bitboard.getBit(to);
            if ((occupancy & Bitboard.getBit(to)) != 0) break;
        }

        // South
        for (int r = rank - 1; r >= 0; r--) {
            int to = r * 8 + file;
            result |= Bitboard.getBit(to);
            if ((occupancy & Bitboard.getBit(to)) != 0) break;
        }

        // West
        for (int f = file - 1; f >= 0; f--) {
            int to = rank * 8 + f;
            result |= Bitboard.getBit(to);
            if ((occupancy & Bitboard.getBit(to)) != 0) break;
        }

        return result;
    }

    /**
     * Filter out illegal moves (those that leave the king in check)
     */
    private void filterIllegalMoves(Board board, MoveList moveList) {
        MoveList legalMoves = new MoveList(moveList.size());

        for (int i = 0; i < moveList.size(); i++) {
            Move move = moveList.get(i);
            board.makeSearchMove(move);

            // If after making the move, our king is not in check, the move is legal
            if (!isKingInCheck(board, !board.isWhiteToMove())) {
                legalMoves.add(move);
            }

            board.undoSearchMove();
        }

        // Clear the original list and add back only the legal moves
        moveList.clear();
        for (int i = 0; i < legalMoves.size(); i++) {
            moveList.add(legalMoves.get(i));
        }
    }

    /**
     * Check if the specified square is under attack
     */
    private boolean isSquareAttacked(Board board, int square, boolean byWhite) {
        if (byWhite) {
            // Check for attacks by white pawns
            long pawns = board.getWhitePawns();
            if ((((Bitboard.getBit(square) << 7) & ~BitboardConstants.FILE_A) & pawns) != 0 ||
                    (((Bitboard.getBit(square) << 9) & ~BitboardConstants.FILE_H) & pawns) != 0) {
                return true;
            }

            // Check for attacks by white knights
            if ((knightAttacks[square] & board.getWhiteKnights()) != 0) {
                return true;
            }

            // Check for attacks by white king
            if ((kingAttacks[square] & board.getWhiteKing()) != 0) {
                return true;
            }

            // Check for attacks by white bishops or queens
            long bishopsAndQueens = board.getWhiteBishops() | board.getWhiteQueens();
            if ((generateDiagonalAttacks(square, board.getAllPieces()) & bishopsAndQueens) != 0) {
                return true;
            }

            // Check for attacks by white rooks or queens
            long rooksAndQueens = board.getWhiteRooks() | board.getWhiteQueens();
            if ((generateHorizontalAndVerticalAttacks(square, board.getAllPieces()) & rooksAndQueens) != 0) {
                return true;
            }
        } else {
            // Check for attacks by black pawns
            long pawns = board.getBlackPawns();
            if ((((Bitboard.getBit(square) >> 7) & ~BitboardConstants.FILE_H) & pawns) != 0 ||
                    (((Bitboard.getBit(square) >> 9) & ~BitboardConstants.FILE_A) & pawns) != 0) {
                return true;
            }

            // Check for attacks by black knights
            if ((knightAttacks[square] & board.getBlackKnights()) != 0) {
                return true;
            }

            // Check for attacks by black king
            if ((kingAttacks[square] & board.getBlackKing()) != 0) {
                return true;
            }

            // Check for attacks by black bishops or queens
            long bishopsAndQueens = board.getBlackBishops() | board.getBlackQueens();
            if ((generateDiagonalAttacks(square, board.getAllPieces()) & bishopsAndQueens) != 0) {
                return true;
            }

            // Check for attacks by black rooks or queens
            long rooksAndQueens = board.getBlackRooks() | board.getBlackQueens();
            if ((generateHorizontalAndVerticalAttacks(square, board.getAllPieces()) & rooksAndQueens) != 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if the king of the specified side is in check
     */
    public boolean isKingInCheck(Board board, boolean whiteKing) {
        long king = whiteKing ? board.getWhiteKing() : board.getBlackKing();
        if (king == 0) return false; // No king on the board

        int kingSquare = Bitboard.getLSB(king);
        return isSquareAttacked(board, kingSquare, !whiteKing);
    }

    /**
     * Add methods to get access to attack tables for external use
     */
    public long getKnightAttacks(int square) {
        return knightAttacks[square];
    }

    public long getKingAttacks(int square) {
        return kingAttacks[square];
    }
}