class SentenceStructure {
    private final String structure;
    private int count;

    public SentenceStructure(String structure) {
        this.structure = structure;
        this.count = 1;
    }

    public String getStructure() {
        return structure;
    }

    public int getCount() {
        return count;
    }

    public void incrementCount() {
        this.count++;
    }

    @Override
    public String toString() {
        return structure + " (Count: " + count + ")";
    }
}
