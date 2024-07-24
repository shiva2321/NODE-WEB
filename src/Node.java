import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a node in a graph with additional functionality for handling sentence structures.
 *
 * @param <T> The type of data stored in the node.
 */
class Node<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String id;
    private final T data;
    private final Map<String, Edge<T>> neighbors;
    private final Map<String, Integer> sentenceStructures;
    private boolean isDeleted = false;
    private String tag;

    /**
     * Constructs a new node with the given id and data.
     *
     * @param id The unique identifier of the node.
     * @param data The data to be stored in the node.
     */
    public Node(String id, T data) {
        this.id = id;
        this.data = data;
        this.neighbors = new ConcurrentHashMap<>();
        this.sentenceStructures = new ConcurrentHashMap<>();
    }

    /**
     * Returns the unique identifier of the node.
     *
     * @return The id of the node.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the data stored in the node.
     *
     * @return The data of the node.
     */
    public T getData() {
        return data;
    }

    /**
     * Checks if the node has been marked as deleted.
     *
     * @return True if the node is deleted, false otherwise.
     */
    public boolean isDeleted() {
        return isDeleted;
    }

    /**
     * Marks the node as deleted and clears its neighbors.
     */
    public void markDeleted() {
        isDeleted = true;
        neighbors.clear();
    }

    /**
     * Returns a copy of the map of neighbors.
     *
     * @return A map of neighbors.
     */
    public Map<String, Edge<T>> getNeighbors() {
        return new HashMap<>(neighbors);
    }

    /**
     * Adds a neighbor to the node with the given strength.
     *
     * @param neighbor The neighbor node to be added.
     * @param strength The strength of the connection between the current node and the neighbor.
     */
    public void addNeighbor(Node<T> neighbor, float strength) {
        neighbors.put(neighbor.getId(), new Edge<>(this, neighbor, strength));
    }

    /**
     * Removes the neighbor with the given id from the node.
     *
     * @param neighborId The id of the neighbor to be removed.
     */
    public void removeNeighbor(String neighborId) {
        neighbors.remove(neighborId);
    }

    /**
     * Retrieves a map of neighboring nodes that have an edge pointing towards the current node.
     *
     * @return A map of neighboring nodes with their respective edges. The map is keyed by the neighbor's id,
     * and the value is the edge connecting the current node to the neighbor.
     */
    public Map<String, Edge<T>> getPreviousNeighbors() {
        Map<String, Edge<T>> inNeighbors = new HashMap<>();
        for (Edge<T> edge : neighbors.values()) {
            if (edge.getTarget().getId().equals(id)) {
                inNeighbors.put(edge.getSource().getId(), edge);
            }
        }
        return inNeighbors;
    }

    /**
     * Retrieves a map of neighboring nodes that have an edge pointing away from the current node.
     *
     * @return A map of neighboring nodes with their respective edges. The map is keyed by the neighbor's id,
     * and the value is the edge connecting the current node to the neighbor.
     */
    public Map<String, Edge<T>> getNextNeighbors() {
        Map<String, Edge<T>> outNeighbors = new HashMap<>();
        for (Edge<T> edge : neighbors.values()) {
            if (edge.getSource().getId().equals(id)) {
                outNeighbors.put(edge.getTarget().getId(), edge);
            }
        }
        return outNeighbors;
    }

    /**
     * Updates the strength of the connection between the current node and the neighbor with the given id.
     *
     * @param neighborId The id of the neighbor.
     * @param newStrength The new strength of the connection.
     */
    public void updateStrength(String neighborId, float newStrength) {
        Edge<T> edge = neighbors.get(neighborId);
        if (edge != null) {
            edge.setStrength(newStrength);
        }
    }

    /**
     * Returns the tag associated with the node.
     *
     * @return The tag of the node.
     */
    public String getTag() {
        return tag;
    }

    /**
     * Sets the tag associated with the node.
     *
     * @param tag The new tag for the node.
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * Adds a sentence structure to the node.
     *
     * @param structure The sentence structure to be added.
     */
    public void addSentenceStructure(String structure) {
        sentenceStructures.merge(structure, 1, Integer::sum);
    }

    /**
     * Returns a copy of the map of sentence structures.
     *
     * @return A map of sentence structures.
     */
    public Map<String, Integer> getSentenceStructures() {
        return new HashMap<>(sentenceStructures);
    }

    /**
     * Returns a string representation of the node.
     *
     * @return A string representation of the node.
     */
    @Override
    public String toString() {
        return "Node{id='" + id + "', data=" + data + ", neighbors=" + neighbors.size() +
                ", isDeleted=" + isDeleted + ", tag='" + tag + "'}";
    }
}