import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

class Graph<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private final ConcurrentMap<String, Node<T>> nodes;
    private final ConcurrentMap<String, AtomicInteger> nodeAccessCount;
    private final Cache<String, Node<T>> cache;
    private float maxStrength = 1.0f;

    public Graph() {
        this.nodes = new ConcurrentHashMap<>();
        this.nodeAccessCount = new ConcurrentHashMap<>();
        this.cache = new Cache<>(10000); // Example cache size
    }

    public void addNode(String id, T data) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Node ID cannot be null or empty");
        }
        nodes.putIfAbsent(id, new Node<>(id, data));
    }

    public Node<T> getNode(String id) {
        Node<T> node = cache.get(id);
        if (node == null) {
            node = nodes.get(id);
            if (node != null && !node.isDeleted()) {
                cache.put(id, node);
            }
        }
        if (node == null || node.isDeleted()) {
            throw new NodeNotFoundException("Node not found or marked as deleted: " + id);
        }
        nodeAccessCount.computeIfAbsent(id, k -> new AtomicInteger(0)).incrementAndGet();
        return node;
    }

    public void removeNode(String id) {
        Node<T> node = nodes.get(id);
        if (node != null) {
            for (String neighborId : node.getNeighbors().keySet()) {
                Node<T> neighbor = nodes.get(neighborId);
                if (neighbor != null) {
                    neighbor.removeNeighbor(id);
                }
            }
            node.markDeleted();
            cache.remove(id);
        }
    }

    public void addEdge(String id1, String id2, float strength) {
        Node<T> node1 = getNode(id1);
        Node<T> node2 = getNode(id2);
        node1.addNeighbor(node2, strength);
        node2.addNeighbor(node1, strength);
        updateMaxStrength(strength);
    }

    public void updateEdgeStrength(String id1, String id2, float newStrength) {
        Node<T> node1 = getNode(id1);
        Node<T> node2 = getNode(id2);
        node1.updateStrength(id2, newStrength);
        node2.updateStrength(id1, newStrength);
        updateMaxStrength(newStrength);
    }

    private void updateMaxStrength(float strength) {
        if (strength > maxStrength) {
            maxStrength = strength;
        }
    }

    private void normalizeEdgeStrengths() {
        for (Node<T> node : getAllNodes()) {
            for (Edge<T> edge : node.getNeighbors().values()) {
                float normalizedStrength = edge.getStrength() / maxStrength;
                edge.setStrength(normalizedStrength);
            }
        }
    }

    public void removeEdge(String id1, String id2) {
        Node<T> node1 = getNode(id1);
        Node<T> node2 = getNode(id2);
        node1.removeNeighbor(id2);
        node2.removeNeighbor(id1);
    }

    public boolean edgeExists(String id1, String id2) {
        Node<T> node1 = getNode(id1);
        return node1.getNeighbors().containsKey(id2);
    }

    public Collection<Node<T>> getAllNodes() {
        return nodes.values();
    }

    public Collection<Edge<T>> getAllEdges() {
        Set<String> visitedEdges = new HashSet<>();
        List<Edge<T>> edges = new ArrayList<>();
        for (Node<T> node : nodes.values()) {
            for (Edge<T> edge : node.getNeighbors().values()) {
                String edgeKey = node.getId() + "-" + edge.getTarget().getId();
                String reverseEdgeKey = edge.getTarget().getId() + "-" + node.getId();
                if (!visitedEdges.contains(reverseEdgeKey)) {
                    edges.add(edge);
                    visitedEdges.add(edgeKey);
                }
            }
        }
        return edges;
    }

    public int getTotalNodes() {
        return nodes.size();
    }

    public int getTotalEdges() {
        return getAllEdges().size();
    }

    public List<List<Node<T>>> getConnectedComponents() {
        List<List<Node<T>>> components = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        for (Node<T> node : nodes.values()) {
            if (!visited.contains(node.getId()) && !node.isDeleted()) {
                List<Node<T>> component = new ArrayList<>();
                Queue<Node<T>> queue = new LinkedList<>();
                queue.add(node);
                visited.add(node.getId());

                while (!queue.isEmpty()) {
                    Node<T> current = queue.poll();
                    component.add(current);

                    for (Edge<T> edge : current.getNeighbors().values()) {
                        Node<T> neighbor = edge.getTarget();
                        if (!visited.contains(neighbor.getId())) {
                            visited.add(neighbor.getId());
                            queue.add(neighbor);
                        }
                    }
                }
                components.add(component);
            }
        }
        return components;
    }

    public void displayNode(String id) {
        try {
            Node<T> node = getNode(id);
            if (node.isDeleted()) {
                System.out.println("Node " + id + " is marked as deleted.");
                return;
            }
            System.out.println("Node ID: " + node.getId());
            System.out.println("Data: " + node.getData());
            System.out.println("Tag: " + node.getTag());
            System.out.println("Sentence Structures:");
            List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(node.getSentenceStructures().entrySet());
            sortedEntries.sort(Map.Entry.comparingByValue());
            for (Map.Entry<String, Integer> entry : sortedEntries) {
                System.out.println(" - " + entry.getKey() + ": " + entry.getValue());
            }
            System.out.println("Neighbors:");
            List<Edge<T>> neighbors = new ArrayList<>(node.getNeighbors().values());
            neighbors.sort((e1, e2) -> Float.compare(e2.getStrength(), e1.getStrength()));
            for (Edge<T> edge : neighbors) {
                System.out.printf(" - Neighbor ID: %s, Strength: %.2f%n", edge.getTarget().getId(), edge.getStrength());
            }
        } catch (NodeNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public List<Node<T>> getPreviousNeighbors(String id) {
        Node<T> node = getNode(id);
        List<Node<T>> previousNeighbors = new ArrayList<>();
        for (Edge<T> edge : node.getNeighbors().values()) {
            Node<T> neighbor = edge.getTarget();
            if (nodeAccessCount.get(neighbor.getId()).get() < nodeAccessCount.get(id).get()) {
                previousNeighbors.add(neighbor);
            }
        }
        previousNeighbors.sort((n1, n2) -> Float.compare(node.getNeighbors().get(n2.getId()).getStrength(), node.getNeighbors().get(n1.getId()).getStrength()));
        return previousNeighbors;
    }

    public List<Node<T>> getNextNeighbors(String id) {
        Node<T> node = getNode(id);
        List<Node<T>> nextNeighbors = new ArrayList<>();
        for (Edge<T> edge : node.getNeighbors().values()) {
            Node<T> neighbor = edge.getTarget();
            if (nodeAccessCount.get(neighbor.getId()).get() > nodeAccessCount.get(id).get()) {
                nextNeighbors.add(neighbor);
            }
        }
        nextNeighbors.sort((n1, n2) -> Float.compare(node.getNeighbors().get(n2.getId()).getStrength(), node.getNeighbors().get(n1.getId()).getStrength()));
        return nextNeighbors;
    }

    public List<Node<T>> getTopNextNeighbors(String id, int limit) {
        List<Node<T>> nextNeighbors = getNextNeighbors(id);
        return nextNeighbors.subList(0, Math.min(limit, nextNeighbors.size()));
    }

    public List<Node<T>> getTopPreviousNeighbors(String id, int limit) {
        List<Node<T>> previousNeighbors = getPreviousNeighbors(id);
        return previousNeighbors.subList(0, Math.min(limit, previousNeighbors.size()));
    }

    // Save graph to file
    public void saveGraph(String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(this);
        }
    }

    // Load graph from file
    public static <T> Graph<T> loadGraph(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (Graph<T>) ois.readObject();
        }
    }
}
