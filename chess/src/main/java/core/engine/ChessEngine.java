package core.engine;

import core.board.Board;
import core.board.MoveGenerator;
import core.eval.CombinedEvaluator;
public class ChessEngine {
    private Minimax minimax;
    private SearchParameters params;
    private int searchDepth;
    private MoveGenerator moveGenerator;
    private CombinedEvaluator evaluator;

    public ChessEngine() {
        this.params = new SearchParameters();
        this.searchDepth = params.getMaxDepth();

    }
    public void init() {
        this.moveGenerator = new MoveGenerator();
        this.evaluator = new CombinedEvaluator();
        this.minimax = new Minimax(evaluator, moveGenerator);
    }
    public SearchResult search(Board board) {
        if (minimax == null) {
            init(); // Initialize if not already initialized
        }

        return minimax.findBestMove(board, searchDepth);
    }
    public MoveGenerator getMoveGenerator() {
        return this.moveGenerator;
    }
}