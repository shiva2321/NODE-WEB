import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class Node<T> implements Serializable {
    private final String id;
    private T data;
    private final Map<String, Edge<T>> neighbors;
    private boolean isDeleted;
    private String tag; // Tag for the type of information
    private final Map<String, Integer> sentenceStructures; // Map of sentence structures and their counts

    public Node(String id, T data) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Node ID cannot be null or empty");
        }
        this.id = id;
        this.data = data;
        this.neighbors = new ConcurrentHashMap<>();
        this.isDeleted = false;
        this.tag = ""; // Initialize tag as empty
        this.sentenceStructures = new ConcurrentHashMap<>(); // Initialize sentence structures
    }

    public String getId() {
        return id;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Map<String, Edge<T>> getNeighbors() {
        return neighbors;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void markDeleted() {
        this.isDeleted = true;
        this.neighbors.clear();
    }

    public void addNeighbor(Node<T> neighbor, float strength) {
        neighbors.put(neighbor.getId(), new Edge<>(this, neighbor, strength));
        //normalizeStrengths();
    }

    public void removeNeighbor(String neighborId) {
        neighbors.remove(neighborId);
        //normalizeStrengths();
    }

    public void updateStrength(String neighborId, float newStrength) {
        if (neighbors.containsKey(neighborId)) {
            neighbors.get(neighborId).setStrength(newStrength);
        }
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void addSentenceStructure(String structure) {
        sentenceStructures.put(structure, sentenceStructures.getOrDefault(structure, 0) + 1);
        sortSentenceStructures();
    }

    public void sortSentenceStructures() {
        sentenceStructures.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(entry -> sentenceStructures.put(entry.getKey(), entry.getValue()));
    }

    public Map<String, Integer> getSentenceStructures() {
        return sentenceStructures;
    }

    private void normalizeStrengths() {
        float totalStrength = 0;
        for (Edge<T> edge : neighbors.values()) {
            totalStrength += edge.getStrength();
        }
        if (totalStrength > 1.0f) {
            for (Edge<T> edge : neighbors.values()) {
                edge.setStrength(edge.getStrength() / totalStrength);
            }
        }
    }

    @Override
    public String toString() {
        return "Node{" +
                "id='" + id + '\'' +
                ", data=" + data +
                ", neighbors=" + neighbors +
                ", isDeleted=" + isDeleted +
                ", tag='" + tag + '\'' +
                ", sentenceStructures=" + sentenceStructures +
                '}';
    }
}
