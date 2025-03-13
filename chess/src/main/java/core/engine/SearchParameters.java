package core.engine;

/**
 * Configuration parameters for the chess engine search
 */
public class SearchParameters {
    private int maxDepth;
    private int timeLimit; // in milliseconds
    private boolean useQuiescenceSearch;
    private boolean useTranspositionTable;
    private int maxQuiescenceDepth;

    /**
     * Creates search parameters with default values
     */
    public SearchParameters() {
        this.maxDepth = 4;
        this.timeLimit = 5000; // 5 seconds
        this.useQuiescenceSearch = true;
        this.useTranspositionTable = true;
        this.maxQuiescenceDepth = 5;
    }

    /**
     * Creates search parameters with custom depth and time limit
     *
     * @param maxDepth Maximum search depth
     * @param timeLimit Maximum search time in milliseconds
     */
    public SearchParameters(int maxDepth, int timeLimit) {
        this.maxDepth = maxDepth;
        this.timeLimit = timeLimit;
        this.useQuiescenceSearch = true;
        this.useTranspositionTable = true;
        this.maxQuiescenceDepth = 5;
    }

    // Getters and setters
    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public boolean isUseQuiescenceSearch() {
        return useQuiescenceSearch;
    }

    public void setUseQuiescenceSearch(boolean useQuiescenceSearch) {
        this.useQuiescenceSearch = useQuiescenceSearch;
    }

    public boolean isUseTranspositionTable() {
        return useTranspositionTable;
    }

    public void setUseTranspositionTable(boolean useTranspositionTable) {
        this.useTranspositionTable = useTranspositionTable;
    }

    public int getMaxQuiescenceDepth() {
        return maxQuiescenceDepth;
    }

    public void setMaxQuiescenceDepth(int maxQuiescenceDepth) {
        this.maxQuiescenceDepth = maxQuiescenceDepth;
    }
}