import java.io.Serial;
import java.io.Serializable;

/**
 * Represents an edge in a graph structure.
 * This class is designed to be lightweight and serializable.
 *
 * @param <T> The type of data stored in the connected nodes.
 */
class Edge<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String sourceId;       // ID of the source node
    private final String targetId;       // ID of the target node
    private volatile float strength;     // Strength of the connection

    /**
     * Constructs a new Edge between two nodes.
     *
     * @param source   The source node.
     * @param target   The target node.
     * @param strength The strength of the connection.
     */
    public Edge(Node<T> source, Node<T> target, float strength) {
        this.sourceId = source.getId();
        this.targetId = target.getId();
        this.strength = strength;
    }

    /**
     * @return The ID of the source node.
     */
    public String getSourceId() {
        return sourceId;
    }

    /**
     * @return The ID of the target node.
     */
    public String getTargetId() {
        return targetId;
    }

    /**
     * @return The strength of the connection.
     */
    public float getStrength() {
        return strength;
    }

    /**
     * Sets a new strength for the connection.
     *
     * @param strength The new strength to set.
     */
    public void setStrength(float strength) {
        this.strength = strength;
    }

    @Override
    public String toString() {
        return String.format("Edge{source: %s, target: %s, strength: %.2f}", sourceId, targetId, strength);
    }
}
