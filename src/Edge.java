import java.io.Serial;
import java.io.Serializable;

/**
 * Represents an edge in a graph, connecting two nodes with a strength value.
 * Implements the Serializable interface to allow for object serialization.
 *
 * @param <T> The type of data stored in the nodes.
 */
class Edge<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    //The source node of the edge.
    private final Node<T> source;

    //The target node of the edge.
    private final Node<T> target;

    //The strength of the edge.
    private float strength;

    /**
     * Constructs a new Edge object.
     *
     * @param source The source node of the edge.
     * @param target The target node of the edge.
     * @param strength The strength of the edge.
     */
    public Edge(Node<T> source, Node<T> target, float strength) {
        this.source = source;
        this.target = target;
        this.strength = strength;
    }

    /**
     * Returns the source node of the edge.
     *
     * @return The source node.
     */
    public Node<T> getSource() {
        return source;
    }

    /**
     * Returns the target node of the edge.
     *
     * @return The target node.
     */
    public Node<T> getTarget() {
        return target;
    }

    /**
     * Returns the strength of the edge.
     *
     * @return The strength of the edge.
     */
    public float getStrength() {
        return strength;
    }

    /**
     * Sets the strength of the edge.
     *
     * @param strength The new strength of the edge.
     */
    public void setStrength(float strength) {
        this.strength = strength;
    }
}
