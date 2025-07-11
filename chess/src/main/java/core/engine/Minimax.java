package core.engine;

import core.board.Board;
import core.board.Move;
import core.board.MoveGenerator;
import core.board.MoveList;
import core.eval.*;

import java.util.HashMap;
import java.util.Map;


public class Minimax {
    private final CombinedEvaluator evaluator;
    private final MoveGenerator moveGenerator;
    private int nodesSearched;
    private int maxDepth; // Track the original search depth

    private Map<Long, Integer> searchPositionHistory;

    public Minimax(CombinedEvaluator evaluator, MoveGenerator moveGenerator) {
        this.evaluator = evaluator;
        this.evaluator.addEvaluator(new MaterialEvaluator());
        this.evaluator.addEvaluator(new PositionalEvaluator());
        this.evaluator.addEvaluator(new PawnStructureEvaluator());
        this.evaluator.addEvaluator(new EndgameEvaluator());
        this.evaluator.addEvaluator(new KingSafetyEvaluator());
        this.moveGenerator = moveGenerator;
    }

    public SearchResult findBestMove(Board board, int depth) {
        nodesSearched = 0;
        maxDepth = depth;
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

        return new SearchResult(bestMove, bestScore, nodesSearched);
    }

    private int alphaBeta(Board board, int depth, int alpha, int beta, boolean isMaximizing) {
        nodesSearched++;

        if (board.isThreefoldRepetitionDuringSearch(searchPositionHistory)) {
            return CombinedEvaluator.STALEMATE_VALUE; // Return draw evaluation
        }

        // Calculate current search depth for mate scoring
        int currentDepth = maxDepth - depth;

        if (depth <= 0) {
            int eval = evaluator.evaluate(board, currentDepth);
            return eval * (isMaximizing ? 1 : -1); // Flip for black
        }

        MoveList moves = new MoveList(256);
        moveGenerator.generateLegalMoves(board, moves);

        if (moves.size() == 0) {
            if (board.isInCheck()) {
                // Use depth-aware mate scoring
                int mateScore = CombinedEvaluator.MATE_VALUE - currentDepth;
                return isMaximizing ? -mateScore : mateScore;
            } else {
                return CombinedEvaluator.STALEMATE_VALUE; // Stalemate
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