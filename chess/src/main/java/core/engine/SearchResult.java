package core.engine;

import core.board.Move;

/**
 * Container for search results
 */
public class SearchResult {
    private final Move bestMove;
    private final int score;
    private final int nodesSearched;

    public SearchResult(Move bestMove, int score, int nodesSearched) {
        this.bestMove = bestMove;
        this.score = score;
        this.nodesSearched = nodesSearched;
    }
    public Move getBestMove() {
        return bestMove;
    }
    public int getScore() {
        return score;
    }
    public int getNodesSearched() {
        return nodesSearched;
    }

    @Override
    public String toString() {
        return "Best move: " + bestMove +
                ", Score: " + score +
                ", Nodes searched: " + nodesSearched;
    }
}
