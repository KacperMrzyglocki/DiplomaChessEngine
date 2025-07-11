package core.eval;

import core.board.Board;
import java.util.ArrayList;
import java.util.List;

public class CombinedEvaluator implements Evaluator {
    private final List<Evaluator> evaluators;
    public static final int MATE_VALUE = Integer.MAX_VALUE - 1000; // Base mate value
    public static final int STALEMATE_VALUE = 0;

    public CombinedEvaluator() {
        this.evaluators = new ArrayList<>();
    }

    public void addEvaluator(Evaluator evaluator) {
        evaluators.add(evaluator);
    }

    @Override
    public int evaluate(Board board) {
        return evaluate(board, 0);
    }

    public int evaluate(Board board, int depth) {
        // Check for special cases first
        if (board.isCheckmate()) {
            int adjustedMateValue = MATE_VALUE - depth;
            int score = board.isWhiteToMove() ? -adjustedMateValue : adjustedMateValue;
            return score;
        }

        if (board.isDraw()) {
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

    public static boolean isMateScore(int score) {
        return Math.abs(score) > MATE_VALUE - 1000;
    }

    public static int getMateDistance(int score) {
        if (!isMateScore(score)) {
            return -1; // Not a mate score
        }
        return MATE_VALUE - Math.abs(score);
    }
}