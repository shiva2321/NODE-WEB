import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class GraphUtils<T> {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Saves the graph to a text file.
     *
     * @param filePath The path of the file to save the graph.
     * @throws IOException If an I/O error occurs.
     */
    public void saveGraphToTXT(String filePath, Graph<T> g) throws IOException {
       Map<String, Node<T>> nodes = g.nodes;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Node<T> node : nodes.values()) {
                if (!node.isDeleted()) {
                    writer.write(node.getId() + ":" + node.getData().toString() + "\n");
                    for (Edge<T> edge : node.getNeighbors().values()) {
                        writer.write(" " + edge.getSource().getId() + " -> " + edge.getTarget().getId() + " : " + edge.getStrength() + "\n");
                    }
                }
            }
        }
    }

    /**
     * Loads the graph from a text file.
     *
     * @param filePath The path of the file to load the graph.
     * @throws IOException If an I/O error occurs.
     */
    public void loadGraphFromTXT(String filePath, Graph<T> g, Type dataType) throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
        String line;
        Map<String, Node<T>> tempNodes = new HashMap<>();
        Map<String, Node<T>> nodes = g.nodes;

        while ((line = reader.readLine()) != null) {
            if (line.contains(":")) {
                String[] parts = line.split(":");
                String id = parts[0].trim();
                T data = gson.fromJson(parts[1].trim(), dataType);
                Node<T> node = new Node<>(id, data);
                tempNodes.put(id, node);
                nodes.put(id, node);
            } else if (line.contains("->")) {
                String[] parts = line.split("->");
                String sourceId = parts[0].trim();
                String[] targetParts = parts[1].split(":");
                String targetId = targetParts[0].trim();
                float strength = Float.parseFloat(targetParts[1].trim());

                Node<T> sourceNode = tempNodes.get(sourceId);
                Node<T> targetNode = tempNodes.get(targetId);
                if (sourceNode != null && targetNode != null) {
                    sourceNode.getNeighbors().put(targetId, new Edge<>(sourceNode, targetNode, strength));
                }
            }
        }
    }
}
}
