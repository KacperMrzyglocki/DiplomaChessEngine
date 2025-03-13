package core.eval;

import core.board.Board;

/**
 * Interface for position evaluation
 */
public interface Evaluator {
    /**
     * Evaluate the current board position from the perspective of the side to move.
     * Positive values favor the side to move, negative values favor the opponent.
     *
     * @param board The board position to evaluate
     * @return Evaluation score in centipawns
     */
    int evaluate(Board board);
}
