import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents a node in a graph structure.
 * This class is designed for high concurrency and large-scale graph operations.
 *
 * @param <T> The type of data stored in the node.
 */
class Node<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String id;                                    // Unique identifier for the node
    private volatile T data;                                    // Data stored in the node
    private final Map<String, Edge<T>> neighbors;               // Map of neighboring nodes
    private final AtomicBoolean isDeleted;                      // Flag to mark if the node is deleted
    private volatile String tag;                                // Tag for categorizing the node
    private final Map<String, Integer> sentenceStructures;      // Map to store sentence structure frequencies
    private final ReadWriteLock lock = new ReentrantReadWriteLock();  // Lock for managing concurrent read/write operations

    /**
     * Constructs a new Node with the given ID and data.
     *
     * @param id   The unique identifier for the node.
     * @param data The data to be stored in the node.
     * @throws IllegalArgumentException if the ID is null or empty.
     */
    public Node(String id, T data) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Node ID cannot be null or empty");
        }
        this.id = id;
        this.data = data;
        this.neighbors = new ConcurrentHashMap<>();
        this.isDeleted = new AtomicBoolean(false);
        this.tag = "";
        this.sentenceStructures = new ConcurrentHashMap<>();
    }

    /**
     * @return The unique identifier of the node.
     */
    public String getId() {
        return id;
    }

    /**
     * Retrieves the data stored in the node.
     *
     * @return The data stored in the node.
     */
    public T getData() {
        lock.readLock().lock();
        try {
            return data;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Sets new data for the node.
     *
     * @param data The new data to be stored.
     */
    public void setData(T data) {
        lock.writeLock().lock();
        try {
            this.data = data;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Retrieves a copy of the neighbors map.
     *
     * @return A new HashMap containing the node's neighbors.
     */
    public Map<String, Edge<T>> getNeighbors() {
        return new HashMap<>(neighbors);
    }

    /**
     * Checks if the node has been marked as deleted.
     *
     * @return true if the node is deleted, false otherwise.
     */
    public boolean isDeleted() {
        return isDeleted.get();
    }

    /**
     * Marks the node as deleted and clears its neighbors.
     */
    public void markDeleted() {
        if (isDeleted.compareAndSet(false, true)) {
            neighbors.clear();
        }
    }

    /**
     * Adds a neighbor to this node.
     *
     * @param neighbor The neighboring node to add.
     * @param strength The strength of the connection.
     */
    public void addNeighbor(Node<T> neighbor, float strength) {
        neighbors.put(neighbor.getId(), new Edge<>(this, neighbor, strength));
    }

    /**
     * Removes a neighbor from this node.
     *
     * @param neighborId The ID of the neighbor to remove.
     */
    public void removeNeighbor(String neighborId) {
        neighbors.remove(neighborId);
    }

    /**
     * Updates the strength of the connection to a neighbor.
     *
     * @param neighborId  The ID of the neighbor.
     * @param newStrength The new strength of the connection.
     */
    public void updateStrength(String neighborId, float newStrength) {
        Edge<T> edge = neighbors.get(neighborId);
        if (edge != null) {
            edge.setStrength(newStrength);
        }
    }

    /**
     * @return The tag associated with this node.
     */
    public String getTag() {
        return tag;
    }

    /**
     * Sets a new tag for the node.
     *
     * @param tag The new tag to set.
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * Adds a sentence structure to the node's statistics.
     *
     * @param structure The sentence structure to add.
     */
    public void addSentenceStructure(String structure) {
        sentenceStructures.merge(structure, 1, Integer::sum);
    }

    /**
     * Retrieves a copy of the sentence structures map.
     *
     * @return A new HashMap containing the sentence structures and their frequencies.
     */
    public Map<String, Integer> getSentenceStructures() {
        return new HashMap<>(sentenceStructures);
    }

    @Override
    public String toString() {
        return "Node{id='" + id + "', data=" + data + ", neighbors=" + neighbors.size() +
                ", isDeleted=" + isDeleted + ", tag='" + tag + "'}";
    }
}