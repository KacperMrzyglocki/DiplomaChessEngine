package core.engine;

import core.board.Board;
import core.board.Move;
import core.board.MoveGenerator;
import core.board.MoveList;
import core.eval.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Basic minimax implementation with alpha-beta pruning
 */
public class Minimax {
    private final Evaluator evaluator;
    private final MoveGenerator moveGenerator;
    private int nodesSearched;

    private Map<Long, Integer> searchPositionHistory;

    public Minimax(Evaluator evaluator, MoveGenerator moveGenerator) {
        this.evaluator = evaluator;
        ((CombinedEvaluator)evaluator).addEvaluator(new MaterialEvaluator());
        ((CombinedEvaluator)evaluator).addEvaluator(new PositionalEvaluator());
        ((CombinedEvaluator)evaluator).addEvaluator(new PawnStructureEvaluator());
        ((CombinedEvaluator)evaluator).addEvaluator(new EndgameEvaluator());
        ((CombinedEvaluator)evaluator).addEvaluator(new KingSafetyEvaluator());
        this.moveGenerator = moveGenerator;
    }
    public SearchResult findBestMove(Board board, int depth) {
        nodesSearched = 0;
        searchPositionHistory = new HashMap<>();
        searchPositionHistory.put(board.getPositionHash(), 1);
        Move bestMove = null;
        boolean isMaximizing = board.isWhiteToMove();  // Determine if White or Black
        int bestScore = isMaximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        MoveList moves = new MoveList(256);
        moveGenerator.generateLegalMoves(board, moves);

        if (moves.size() == 0) {
            return new SearchResult(null, 0, nodesSearched);
        }

        for (int i = 0; i < moves.size(); i++) {
            Move move = moves.get(i);
            board.makeSearchMove(move);
            long newPosHash = board.getPositionHash();
            searchPositionHistory.put(newPosHash, searchPositionHistory.getOrDefault(newPosHash, 0) + 1);

            int score = alphaBeta(board, depth - 1, alpha, beta, !isMaximizing);

            searchPositionHistory.put(newPosHash, searchPositionHistory.get(newPosHash) - 1);
            board.undoSearchMove();

            System.out.println("Move: " + move + ", Score: " + score);

            if ((isMaximizing && score > bestScore) || (!isMaximizing && score < bestScore)) {
                bestScore = score;
                bestMove = move;

                if (isMaximizing) {
                    alpha = Math.max(alpha, bestScore);
                } else {
                    beta = Math.min(beta, bestScore);
                }
            }
        }

        System.out.println("Selected best move: " + bestMove + " with score: " + bestScore);
        return new SearchResult(bestMove, bestScore, nodesSearched);
    }
    private int alphaBeta(Board board, int depth, int alpha, int beta, boolean isMaximizing) {
        nodesSearched++;

        if (board.isThreefoldRepetitionDuringSearch(searchPositionHistory)) {
            return CombinedEvaluator.STALEMATE_VALUE; // Return draw evaluation
        }

        if (depth <= 0) {
            return evaluator.evaluate(board) * (isMaximizing ? 1 : -1); // Flip for black
        }

        MoveList moves = new MoveList(256);
        moveGenerator.generateLegalMoves(board, moves);

        if (moves.size() == 0) {
            if (board.isInCheck()) {
                return isMaximizing ? -30000 + depth : 30000 - depth; // Checkmate
            } else {
                return 0; // Stalemate
            }
        }

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (int i = 0; i < moves.size(); i++) {
                Move move = moves.get(i);
                board.makeSearchMove(move);
                int eval = alphaBeta(board, depth - 1, alpha, beta, false);
                board.undoSearchMove();

                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break;  // Beta cutoff
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int i = 0; i < moves.size(); i++) {
                Move move = moves.get(i);
                board.makeSearchMove(move);
                int eval = alphaBeta(board, depth - 1, alpha, beta, true);
                board.undoSearchMove();

                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    break;  // Alpha cutoff
                }
            }
            return minEval;
        }
    }
}