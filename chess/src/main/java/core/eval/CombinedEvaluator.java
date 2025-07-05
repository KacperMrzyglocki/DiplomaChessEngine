package core.eval;

import core.board.Board;
import java.util.ArrayList;
import java.util.List;

/**
 * Combines multiple evaluators to produce a comprehensive evaluation score
 * Also handles special cases for checkmate and stalemate
 */
public class CombinedEvaluator implements Evaluator {
    private final List<Evaluator> evaluators;
    public static final int MATE_VALUE = Integer.MAX_VALUE - 1000; // Slightly less than max to allow for search depth adjustments
    public static final int STALEMATE_VALUE = 0;

    public CombinedEvaluator() {
        this.evaluators = new ArrayList<>();
    }
    public void addEvaluator(Evaluator evaluator) {
        evaluators.add(evaluator);
    }

    @Override
    public int evaluate(Board board) {
        // Check for special cases first
        if (board.isCheckmate()) {
            int score = board.isWhiteToMove() ? -MATE_VALUE : MATE_VALUE;
            System.out.println("Checkmate detected: " + score);
            return score;
        }

        if (board.isDraw()) {
            System.out.println("Draw detected, returning: " + STALEMATE_VALUE);
            return STALEMATE_VALUE;
        }

        // For normal positions, combine the evaluations
        int score = 0;
        for (Evaluator evaluator : evaluators) {
            int eval = evaluator.evaluate(board);
            score += eval;
        }
        return score;
    }
}