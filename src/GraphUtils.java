import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import org.graphviz.Graphviz;

/**
 * Utility class for graph operations.
 * This class provides methods for common graph operations and can be extended as needed.
 */
class GraphUtils {



    /**
     * Saves the graph to a DOT file.
     *
     * @param graph The graph to save.
     * @param filePath The path where the DOT file will be saved.
     * @param <T> The type of data stored in the graph's nodes.
     * @throws IOException If there's an error writing to the file.
     */
    public static <T> void saveGraphToDOT(Graph<T> graph, String filePath) throws IOException {
        StringBuilder dot = new StringBuilder();
        dot.append("digraph G {\n");

        for (Node<T> node : graph.getAllNodes()) {
            dot.append("    ").append(node.getId()).append(" [label=\"").append(node.getTag()).append("\"];\n");
        }

        for (Node<T> node : graph.getAllNodes()) {
            for (Edge<T> edge : node.getNeighbors().values()) {
                dot.append("    ").append(node.getId()).append(" -> ").append(edge.getTargetId()).append(" [label=\"").append(edge.getStrength()).append("\"];\n");
            }
        }

        dot.append("}");

        try (Writer writer = new FileWriter(filePath)) {
            writer.write(dot.toString());
        }
    }

    /**
     * Generates an SVG image from a DOT file.
     *
     * @param dotFilePath The path of the DOT file.
     * @param svgFilePath The path where the SVG file will be saved.
     * @throws IOException If there's an error generating the SVG image.
     */
    public static void generateSVGFromDOT(String dotFilePath, String svgFilePath) throws IOException {
        Graphviz graphviz = new Graphviz();
        graphviz.addDotFile(dotFilePath);
        graphviz.executeFormat("svg", "png");
        try (Writer writer = new FileWriter(svgFilePath)) {
            writer.write(graphviz.getDotSource());
        }
    }


    /**
     * Saves the graph to a CSV file.
     * The CSV will have two sections: Nodes and Edges.
     *
     * @param graph The graph to save.
     * @param filePath The path where the CSV file will be saved.
     * @param <T> The type of data stored in the graph's nodes.
     * @throws IOException If there's an error writing to the file.
     */
    public static <T> void saveGraphToCSV(Graph<T> graph, String filePath) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write nodes
            writer.println("Nodes");
            writer.println("ID,Data,Tag");
            for (Node<T> node : graph.getAllNodes()) {
                writer.printf("%s,%s,%s%n",
                        escapeCSV(node.getId()),
                        escapeCSV(node.getData().toString()),
                        escapeCSV(node.getTag()));
            }

            // Write edges
            writer.println("\nEdges");
            writer.println("SourceID,TargetID,Strength");
            for (Node<T> node : graph.getAllNodes()) {
                for (Edge<T> edge : node.getNeighbors().values()) {
                    writer.printf("%s,%s,%.2f%n",
                            escapeCSV(node.getId()),
                            escapeCSV(edge.getTargetId()),
                            edge.getStrength());
                }
            }
        }
    }

    /**
     * Loads a graph from a CSV file.
     *
     * @param filePath The path of the CSV file.
     * @param dataDeserializer A function to convert the CSV string data to type T.
     * @param <T> The type of data stored in the graph's nodes.
     * @return The loaded Graph object.
     * @throws IOException If there's an error reading from the file.
     */
    public static <T> Graph<T> loadGraphFromCSV(String filePath, Function<String, T> dataDeserializer) throws IOException {
        Graph<T> graph = new Graph<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean readingNodes = false;
            boolean readingEdges = false;

            while ((line = reader.readLine()) != null) {
                if (line.trim().equalsIgnoreCase("Nodes")) {
                    readingNodes = true;
                    readingEdges = false;
                    reader.readLine(); // Skip header
                    continue;
                } else if (line.trim().equalsIgnoreCase("Edges")) {
                    readingNodes = false;
                    readingEdges = true;
                    reader.readLine(); // Skip header
                    continue;
                }

                String[] parts = line.split(",");
                if (readingNodes && parts.length >= 3) {
                    String id = unescapeCSV(parts[0]);
                    T data = dataDeserializer.apply(unescapeCSV(parts[1]));
                    String tag = unescapeCSV(parts[2]);
                    graph.addNode(id, data);
                    graph.getNode(id).setTag(tag);
                } else if (readingEdges && parts.length >= 3) {
                    String sourceId = unescapeCSV(parts[0]);
                    String targetId = unescapeCSV(parts[1]);
                    float strength = Float.parseFloat(parts[2]);
                    graph.addEdge(sourceId, targetId, strength);
                }
            }
        }
        return graph;
    }

    /**
     * Saves the graph to a JSON file.
     *
     * @param graph The graph to save.
     * @param filePath The path where the JSON file will be saved.
     * @param <T> The type of data stored in the graph's nodes.
     * @throws IOException If there's an error writing to the file.
     */
    public static <T> void saveGraphToJSON(Graph<T> graph, String filePath) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Map<String, Object> graphMap = new HashMap<>();

        List<Map<String, Object>> nodes = new ArrayList<>();
        for (Node<T> node : graph.getAllNodes()) {
            Map<String, Object> nodeMap = new HashMap<>();
            nodeMap.put("id", node.getId());
            nodeMap.put("data", node.getData());
            nodeMap.put("tag", node.getTag());

            List<Map<String, Object>> neighbors = new ArrayList<>();
            for (Edge<T> edge : node.getNeighbors().values()) {
                Map<String, Object> neighborMap = new HashMap<>();
                neighborMap.put("id", edge.getTargetId());
                neighborMap.put("strength", edge.getStrength());
                neighbors.add(neighborMap);
            }
            nodeMap.put("neighbors", neighbors);

            nodes.add(nodeMap);
        }
        graphMap.put("nodes", nodes);

        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(graphMap, writer);
        }
    }

    /**
     * Loads a graph from a JSON file.
     *
     * @param filePath The path of the JSON file.
     * @param dataDeserializer A function to convert the JSON object to type T.
     * @param <T> The type of data stored in the graph's nodes.
     * @return The loaded Graph object.
     * @throws IOException If there's an error reading from the file.
     */
    public static <T> Graph<T> loadGraphFromJSON(String filePath, Function<Object, T> dataDeserializer) throws IOException {
        Gson gson = new Gson();
        Graph<T> graph = new Graph<>();

        try (Reader reader = new FileReader(filePath)) {
            Map<String, List<Map<String, Object>>> graphMap = gson.fromJson(reader,
                    new TypeToken<Map<String, List<Map<String, Object>>>>(){}.getType());

            List<Map<String, Object>> nodes = graphMap.get("nodes");
            for (Map<String, Object> nodeMap : nodes) {
                String id = (String) nodeMap.get("id");
                T data = dataDeserializer.apply(nodeMap.get("data"));
                String tag = (String) nodeMap.get("tag");

                graph.addNode(id, data);
                graph.getNode(id).setTag(tag);

                List<Map<String, Object>> neighbors = (List<Map<String, Object>>) nodeMap.get("neighbors");
                for (Map<String, Object> neighborMap : neighbors) {
                    String neighborId = (String) neighborMap.get("id");
                    double strength = ((Number) neighborMap.get("strength")).doubleValue();
                    graph.addEdge(id, neighborId, (float) strength);
                }
            }
        }

        return graph;
    }

    /**
 * Escapes a string for use in a CSV file.
 * This method wraps the input string in double quotes and replaces any existing double quotes with two double quotes.
 *
 * @param input The string to escape.
 * @return The escaped string. If the input is null, an empty string is returned.
 */
private static String escapeCSV(String input) {
    if (input == null) {
        return "";
    }
    return "\"" + input.replace("\"", "\"\"") + "\"";
}

    /**
 * Unescapes a string that was previously escaped for use in a CSV file.
 * This method removes the double quotes from the start and end of the input string,
 * and replaces any existing double quotes with a single double quote.
 *
 * @param input The string to unescape.
 * @return The unescaped string. If the input is null or its length is less than 2, the input is returned as is.
 */
private static String unescapeCSV(String input) {
    if (input == null || input.length() < 2) {
        return input;
    }
    if (input.startsWith("\"") && input.endsWith("\"")) {
        return input.substring(1, input.length() - 1).replace("\"\"", "\"");
    }
    return input;
}

    /**
     * Performs a breadth-first search (BFS) traversal of the graph.
     *
     * @param start The starting node for the BFS.
     * @param <T>   The type of data stored in the nodes.
     * @return A list of node IDs in BFS order.
     */
    public static <T> List<String> bfsTraversal(Node<T> start) {
        List<String> visited = new ArrayList<>();
        Queue<Node<T>> queue = new LinkedList<>();
        Set<String> visitedSet = new HashSet<>();

        queue.offer(start);
        visitedSet.add(start.getId());

        while (!queue.isEmpty()) {
            Node<T> current = queue.poll();
            visited.add(current.getId());

            for (Edge<T> edge : current.getNeighbors().values()) {
                String neighborId = edge.getTargetId();
                if (!visitedSet.contains(neighborId)) {
                    visitedSet.add(neighborId);
                    queue.offer(new Node<>(neighborId, null)); // Placeholder node, actual data retrieval would depend on your graph implementation
                }
            }
        }

        return visited;
    }

    /**
     * Calculates the shortest path between two nodes using Dijkstra's algorithm.
     * This implementation assumes that the graph is stored in some accessible structure.
     *
     * @param start    The starting node.
     * @param end      The ending node.
     * @param getNode  A function to retrieve a node by its ID.
     * @param <T>      The type of data stored in the nodes.
     * @return A list of node IDs representing the shortest path, or an empty list if no path exists.
     */
    public static <T> List<String> shortestPath(Node<T> start, Node<T> end,
                                                Function<String, Node<T>> getNode) {
        Map<String, String> predecessors = new HashMap<>();
        Map<String, Float> distances = new HashMap<>();
        PriorityQueue<String> queue = new PriorityQueue<>(
                Comparator.comparingDouble(distances::get));

        distances.put(start.getId(), 0f);
        queue.offer(start.getId());

        while (!queue.isEmpty()) {
            String currentId = queue.poll();
            if (currentId.equals(end.getId())) {
                break;
            }

            Node<T> current = getNode.apply(currentId);
            for (Edge<T> edge : current.getNeighbors().values()) {
                String neighborId = edge.getTargetId();
                float newDist = distances.get(currentId) + edge.getStrength();
                if (!distances.containsKey(neighborId) || newDist < distances.get(neighborId)) {
                    distances.put(neighborId, newDist);
                    predecessors.put(neighborId, currentId);
                    queue.offer(neighborId);
                }
            }
        }

        if (!predecessors.containsKey(end.getId())) {
            return Collections.emptyList();
        }

        LinkedList<String> path = new LinkedList<>();
        for (String at = end.getId(); at != null; at = predecessors.get(at)) {
            path.addFirst(at);
        }
        return path;
    }
}