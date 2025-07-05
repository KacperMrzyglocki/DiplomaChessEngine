package core.engine;

public class SearchParameters {
    private int maxDepth;
    private int timeLimit; // in milliseconds
    private boolean useQuiescenceSearch;
    private boolean useTranspositionTable;
    private int maxQuiescenceDepth;
    public SearchParameters() {
        this.maxDepth = 4;
        this.timeLimit = 5000; // 5 seconds
        this.useQuiescenceSearch = true;
        this.useTranspositionTable = true;
        this.maxQuiescenceDepth = 5;
    }
    public int getMaxDepth() {
        return maxDepth;
    }
    public void setUseQuiescenceSearch(boolean useQuiescenceSearch) {
        this.useQuiescenceSearch = useQuiescenceSearch;
    }
    public void setUseTranspositionTable(boolean useTranspositionTable) {
        this.useTranspositionTable = useTranspositionTable;
    }
}