import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

class Graph<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    public ConcurrentMap<String, Node<T>> nodes;
    private final ConcurrentMap<String, AtomicInteger> nodeAccessCount;
    private final Cache<String, Node<T>> cache;
    private float maxStrength = 1.0f;

    /**
     * Constructs a new instance of the Graph class with an empty set of nodes,
     * node access counts, and a cache of a specified size.
     *
     * @throws IllegalArgumentException if the cache size is less than or equal to zero.
     */
    public Graph() {
        this.nodes = new ConcurrentHashMap<>();
        this.nodeAccessCount = new ConcurrentHashMap<>();
        this.cache = new Cache<>(5000); // Example cache size
    }
    
        /**
     * Adds a new node to the graph with the given ID and data.
         an edge must be

     *
     * @param id The unique identifier for the new node. Cannot be null or empty.
     * @param data The data associated with the new node.
     *
     * @throws IllegalArgumentException If the provided ID is null or empty.
     */
    public void addNode(String id, T data) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Node ID cannot be null or empty");
        }
        nodes.putIfAbsent(id, new Node<>(id, data));
    }

    /**
     * Retrieves a node from the graph based on its unique identifier.
     * If the node is not found in the cache, it will be fetched from the main data structure.
     * If the node is not found or marked as deleted, a NodeNotFoundException will be thrown.
     *
     * @param id The unique identifier of the node to retrieve. Cannot be null or empty.
     * @return The retrieved node.
     * @throws NodeNotFoundException If the provided ID does not correspond to an existing, non-deleted node.
     */
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

    /**
     * Removes a node from the graph based on its unique identifier.
     * If the node exists, it will be removed from the main data structure and the cache.
     * Additionally, all edges connected to the removed node will be removed from their respective neighbors.
     *
     * @param id The unique identifier of the node to remove. Cannot be null or empty.
     * @throws IllegalArgumentException If the provided ID is null or empty.
     */
    public void removeNode(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Node ID cannot be null or empty");
        }
    
        Node<T> node = nodes.get(id);
        if (node != null) {
            // Remove edges from neighbors
            for (String neighborId : node.getNeighbors().keySet()) {
                Node<T> neighbor = nodes.get(neighborId);
                if (neighbor != null) {
                    neighbor.removeNeighbor(id);
                }
            }
        }
    
        // Remove node from main data structure and cache
        nodes.remove(id);
        cache.remove(id);
    }

    /**
     * Adds a new edge between two nodes in the graph.
     *
     * @param id1 The unique identifier of the first node. Cannot be null or empty.
     * @param id2 The unique identifier of the second node. Cannot be null or empty.
     * @param strength The strength of the edge. Must be a non-negative value.
     *
     * @throws NodeNotFoundException If either of the provided IDs does not correspond to an existing, non-deleted node.
     * @throws IllegalArgumentException If the provided IDs are null or empty, or if the strength is negative.
     */
    public void addEdge(String id1, String id2, float strength) {
        if (id1 == null || id1.isEmpty() || id2 == null || id2.isEmpty()) {
            throw new IllegalArgumentException("Node IDs cannot be null or empty");
        }
        if (strength < 0) {
            throw new IllegalArgumentException("Strength must be a non-negative value");
        }

        Node<T> node1 = getNode(id1);
        Node<T> node2 = getNode(id2);
        node1.addNeighbor(node2, strength); // Directional edge from node1 to node2
        updateMaxStrength(strength);
    }

    /**
     * Updates the strength of an edge between two nodes in the graph.
     *
     * @param id1 The unique identifier of the first node. Cannot be null or empty.
     * @param id2 The unique identifier of the second node. Cannot be null or empty.
     * @param newStrength The new strength value for the edge. Must be a non-negative value.
     *
     * @throws NodeNotFoundException If either of the provided IDs does not correspond to an existing, non-deleted node.
     * @throws IllegalArgumentException If the provided IDs are null or empty, or if the newStrength is negative.
     */
    public void updateEdgeStrength(String id1, String id2, float newStrength) {
        if (id1 == null || id1.isEmpty() || id2 == null || id2.isEmpty()) {
            throw new IllegalArgumentException("Node IDs cannot be null or empty");
        }
        if (newStrength < 0) {
            throw new IllegalArgumentException("Strength must be a non-negative value");
        }
    
        Node<T> node1 = getNode(id1);
        node1.updateStrength(id2, newStrength);
        updateMaxStrength(newStrength);
    }

    /**
     * Updates the maximum strength value in the graph.
     * If the provided strength is greater than the current maximum strength,
     * the maximum strength is updated to the provided value.
     *
     * @param strength The new strength value to compare with the current maximum strength.
     *                 Must be a non-negative value.
     */
    private void updateMaxStrength(float strength) {
        if (strength > maxStrength) {
            maxStrength = strength;
        }
    }

    /**
     * Normalizes the strength of all edges in the graph by dividing each edge's strength by the maximum strength.
     * This method iterates through all nodes in the graph, retrieves their neighbors, and updates the strength of each edge.
     * The normalized strength is calculated as the edge's original strength divided by the maximum strength in the graph.
     * The normalized strength is then set back to the edge.
     */
    private void normalizeEdgeStrengths() {
        for (Node<T> node : getAllNodes()) {
            for (Edge<T> edge : node.getNeighbors().values()) {
                float normalizedStrength = edge.getStrength() / maxStrength;
                edge.setStrength(normalizedStrength);
            }
        }
    }

    /**
     * Removes an edge between two nodes in the graph.
     *
     * @param id1 The unique identifier of the first node. Cannot be null or empty.
     * @param id2 The unique identifier of the second node. Cannot be null or empty.
     *
     * @throws NodeNotFoundException If either of the provided IDs does not correspond to an existing, non-deleted node.
     * @throws IllegalArgumentException If the provided IDs are null or empty.
     */
    public void removeEdge(String id1, String id2) {
        if (id1 == null || id1.isEmpty() || id2 == null || id2.isEmpty()) {
            throw new IllegalArgumentException("Node IDs cannot be null or empty");
        }
    
        Node<T> node1 = getNode(id1);
        node1.removeNeighbor(id2);
    }

    /**
     * Checks if an edge exists between two nodes in the graph.
     *
     * @param id1 The unique identifier of the first node. Cannot be null or empty.
     * @param id2 The unique identifier of the second node. Cannot be null or empty.
     *
     * @return {@code true} if an edge exists between the two nodes, {@code false} otherwise.
     *
     * @throws NodeNotFoundException If either of the provided IDs does not correspond to an existing, non-deleted node.
     * @throws IllegalArgumentException If the provided IDs are null or empty.
     */
    public boolean edgeExists(String id1, String id2) {
        Node<T> node1 = getNode(id1);
        return node1.getNeighbors().containsKey(id2);
    }

    /**
     * Retrieves all nodes in the graph.
     *
     * @return A collection of all nodes in the graph. The collection is unmodifiable and does not contain any deleted nodes.
     *
     * @see Node
     */
    public Collection<Node<T>> getAllNodes() {
        return nodes.values();
    }

    /**
     * Retrieves all edges in the graph.
     *
     * <p>This method iterates through all nodes in the graph, retrieves their neighbors,
     * and collects all unique edges. The collected edges are stored in a list and returned.
     * The method ensures that each edge is only included once, even if it exists between
     * the same pair of nodes in both directions.
     *
     * @return A collection of all edges in the graph. The collection is unmodifiable.
     *
     * @see Edge
     */
    public Collection<Edge<T>> getAllEdges() {
        Set<String> visitedEdges = new HashSet<>();
        List<Edge<T>> edges = new ArrayList<>();
        for (Node<T> node : nodes.values()) {
            for (Edge<T> edge : node.getNeighbors().values()) {
                String edgeKey = node.getId() + "-" + edge.getTarget().getId();
                if (!visitedEdges.contains(edgeKey)) {
                    edges.add(edge);
                    visitedEdges.add(edgeKey);
                }
            }
        }
        return edges;
    }

    /**
     * Retrieves the total number of nodes in the graph.
     *
     * @return The total number of nodes in the graph.
     */
    public int getTotalNodes() {
        return nodes.size();
    }
    
    /**
     * Retrieves the total number of edges in the graph.
     * @return The total number of edges in the graph.
     */
    public int getTotalEdges() {
        return getAllEdges().size();
    }

    /**
     * Retrieves the top nodes with the highest number of connections in the graph.
     *
     * @param limit The maximum number of top nodes to retrieve. If the graph has fewer nodes than the specified limit,
     *              all nodes will be returned.
     *
     * @return A list of nodes, sorted in descending order based on the number of connections (neighbors).
     *         The list contains up to the specified limit of nodes.
     *
     * @throws IllegalArgumentException If the provided limit is less than 1.
     */
    public List<Node<T>> getTopNodes(int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("Limit must be greater than or equal to 1");
        }
    
        List<Node<T>> topNodes = new ArrayList<>(getAllNodes());
        topNodes.sort((n1, n2) -> Integer.compare(n2.getNeighbors().size(), n1.getNeighbors().size()));
        return topNodes.subList(0, Math.min(limit, topNodes.size()));
    }


    
    /**
    * Displays detailed information about a node in the graph.
     *
     * <p>This method retrieves the node with the given ID from the graph and prints its details,
     * including its ID, data, tag, sentence structures, and neighbors. If the node is marked as deleted,
     * a message indicating this will be printed instead. If the node is not found, a NodeNotFoundException
     * will be caught and its message will be printed.
     *
     * @param id The unique identifier of the node to display. Cannot be null or empty.
     *
     * @throws IllegalArgumentException If the provided ID is null or empty.
     * @throws NodeNotFoundException If the provided ID does not correspond to an existing, non-deleted node.
     */
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

}


/**
 * Custom exception class to be thrown when a node is not found or marked as deleted.
 */
class NodeNotFoundException extends RuntimeException {

    /**
     * Constructs a new instance of the NodeNotFoundException class with the specified error message.
     *
     * @param message The error message indicating the reason for the exception.
     */
    public NodeNotFoundException(String message) {
        super(message);
        System.out.println("Node not found or marked as deleted: " + message);
    }
}