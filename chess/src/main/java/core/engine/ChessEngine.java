package core.engine;

import core.board.Board;
import core.board.Move;
import core.board.MoveGenerator;
import core.eval.CombinedEvaluator;
import core.eval.Evaluator;
import core.eval.MaterialEvaluator;
import core.eval.PositionalEvaluator;

/**
 * Main chess engine class that coordinates the search and evaluation
 */
public class ChessEngine {
    private Minimax minimax;
    private SearchParameters params;
    private int searchDepth;
    private MoveGenerator moveGenerator;
    private Evaluator evaluator;

    public ChessEngine() {
        this.params = new SearchParameters();
        this.searchDepth = params.getMaxDepth();

        // Disable features you don't want to use
        this.params.setUseQuiescenceSearch(false);
        this.params.setUseTranspositionTable(false);
    }

    /**
     * Constructor with custom search parameters
     *
     * @param params Custom search parameters
     */
    public ChessEngine(SearchParameters params) {
        this.params = params;
        this.searchDepth = params.getMaxDepth();

        // Disable features you don't want to use
        this.params.setUseQuiescenceSearch(false);
        this.params.setUseTranspositionTable(false);
    }

    /**
     * Initializes the engine with specific parameters
     */
    public void init() {
        this.moveGenerator = new MoveGenerator();
        this.evaluator = new CombinedEvaluator();
        this.minimax = new Minimax(evaluator, moveGenerator);
    }

    /**
     * Searches for the best move in the current position
     *
     * @param board The current board position
     * @return SearchResult containing the best move and evaluation
     */
    public SearchResult search(Board board) {
        if (minimax == null) {
            init(); // Initialize if not already initialized
        }

        return minimax.findBestMove(board, searchDepth);
    }

    /**
     * Sets the search depth
     *
     * @param depth Maximum search depth
     */
    public void setSearchDepth(int depth) {
        this.searchDepth = depth;
    }

    /**
     * Gets the current search parameters
     *
     * @return The current search parameters
     */
    public SearchParameters getSearchParameters() {
        return params;
    }

    /**
     * Sets new search parameters
     *
     * @param params The new search parameters
     */
    public void setSearchParameters(SearchParameters params) {
        this.params = params;
        this.searchDepth = params.getMaxDepth();
    }

    public MoveGenerator getMoveGenerator() {
        return this.moveGenerator;
    }
}