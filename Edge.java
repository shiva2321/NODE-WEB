import java.io.Serializable;

class Edge<T> implements Serializable {
    private final Node<T> source;
    private final Node<T> target;
    private float strength;

    public Edge(Node<T> source, Node<T> target, float strength) {
        this.source = source;
        this.target = target;
        this.strength = strength;
    }

    public Node<T> getSource() {
        return source;
    }

    public Node<T> getTarget() {
        return target;
    }

    public float getStrength() {
        return strength;
    }

    public void setStrength(float strength) {
        this.strength = strength;
    }

    @Override
    public String toString() {
        return String.format("Edge { source: %s, target: %s, strength: %.2f }", source.getId(), target.getId(), strength);
    }
}
