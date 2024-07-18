import org.apache.tika.exception.TikaException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Graph<String> graph = new Graph<>();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose an option: 1) Load existing graph 2) Create new graph");
        int option = scanner.nextInt();
        scanner.nextLine(); // consume newline

        if (option == 1) {
            System.out.println("Enter the file path for the saved graph:");
            String savedGraphPath = scanner.nextLine();
            try {
                graph = Graph.loadGraph(savedGraphPath);
                System.out.println("Graph loaded successfully.");
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Failed to load graph: " + e.getMessage());
                return;
            }
        } else if (option == 2) {
            System.out.println("Enter the file path (text or pdf):");
            String filePath = scanner.nextLine();
            Graph<String> g = graph;
            // Process the file based on its type
            if (filePath.endsWith(".pdf")) {
                GraphHelper.measureTime("Process PDF File", () -> {
                    try {
                        GraphHelper.processPDF(g, filePath);
                    } catch (IOException | TikaException e) {
                        System.out.println("IOException occurred: " + e.getMessage());
                    }
                });
            } else if (filePath.endsWith(".txt")) {
                GraphHelper.measureTime("Process Text File", () -> {
                    try {
                        GraphHelper.processTextFile(g, filePath);
                    } catch (IOException e) {
                        System.out.println("IOException occurred: " + e.getMessage());
                    }
                });
            } else {
                System.out.println("Unsupported file type.");
                return;
            }
        } else {
            System.out.println("Invalid option.");
            return;
        }

        String command;

        // Interactive loop to search for nodes and get neighbors
        while (true) {
            Graph<String> c = graph;
            System.out.println("Enter command (search <word>, neighbors <word>, prev <word>, next <word>, prevall <word>, nextall <word>, structures <word>, save, exit):");
            command = scanner.nextLine();
            String[] parts = command.split(" ");

            if (parts.length == 2) {
                String word = parts[1];
                switch (parts[0]) {
                    case "search":
                        GraphHelper.measureTime("Search Node", () -> {
                            try {
                                c.displayNode(word);
                            } catch (Exception e) {
                                System.out.println("Error: " + e.getMessage());
                            }
                        });
                        break;
                    case "neighbors":
                        GraphHelper.measureTime("Get Neighbors", () -> {
                            try {
                                Node<String> node = c.getNode(word);
                                List<Edge<String>> neighbors = new ArrayList<>(node.getNeighbors().values());
                                neighbors.sort((e1, e2) -> Float.compare(e2.getStrength(), e1.getStrength()));
                                System.out.println("Neighbors of " + word + ":");
                                for (Edge<String> edge : neighbors) {
                                    System.out.printf("%s (Strength: %.2f, Tag: %s)%n", edge.getTarget().getId(), edge.getStrength(), edge.getTarget().getTag());
                                }
                            } catch (Exception e) {
                                System.out.println("Error: " + e.getMessage());
                            }
                        });
                        break;
                    case "prev":
                        GraphHelper.measureTime("Get Previous Neighbors", () -> {
                            try {
                                List<Node<String>> prevNeighbors = c.getTopPreviousNeighbors(word, 3);
                                System.out.println("Previous neighbors of " + word + ":");
                                for (Node<String> neighbor : prevNeighbors) {
                                    System.out.printf("%s (Tag: %s)%n", neighbor.getId(), neighbor.getTag());
                                }
                            } catch (Exception e) {
                                System.out.println("Error: " + e.getMessage());
                            }
                        });
                        break;
                    case "next":
                        GraphHelper.measureTime("Get Next Neighbors", () -> {
                            try {
                                List<Node<String>> nextNeighbors = c.getTopNextNeighbors(word, 3);
                                System.out.println("Next neighbors of " + word + ":");
                                for (Node<String> neighbor : nextNeighbors) {
                                    System.out.printf("%s (Tag: %s)%n", neighbor.getId(), neighbor.getTag());
                                }
                            } catch (Exception e) {
                                System.out.println("Error: " + e.getMessage());
                            }
                        });
                        break;
                    case "prevall":
                        GraphHelper.measureTime("Get All Previous Neighbors", () -> {
                            try {
                                List<Node<String>> prevNeighbors = c.getPreviousNeighbors(word);
                                System.out.println("All previous neighbors of " + word + ":");
                                for (Node<String> neighbor : prevNeighbors) {
                                    System.out.printf("%s (Tag: %s)%n", neighbor.getId(), neighbor.getTag());
                                }
                            } catch (Exception e) {
                                System.out.println("Error: " + e.getMessage());
                            }
                        });
                        break;
                    case "nextall":
                        GraphHelper.measureTime("Get All Next Neighbors", () -> {
                            try {
                                List<Node<String>> nextNeighbors = c.getNextNeighbors(word);
                                System.out.println("All next neighbors of " + word + ":");
                                for (Node<String> neighbor : nextNeighbors) {
                                    System.out.printf("%s (Tag: %s)%n", neighbor.getId(), neighbor.getTag());
                                }
                            } catch (Exception e) {
                                System.out.println("Error: " + e.getMessage());
                            }
                        });
                        break;
                    case "structures":
                        GraphHelper.measureTime("Get Sentence Structures", () -> {
                            try {
                                Node<String> node = c.getNode(word);
                                System.out.println("Sentence structures for " + word + ":");
                                for (Map.Entry<String, Integer> entry : node.getSentenceStructures().entrySet()) {
                                    System.out.printf("%s (Count: %d)%n", entry.getKey(), entry.getValue());
                                }
                            } catch (Exception e) {
                                System.out.println("Error: " + e.getMessage());
                            }
                        });
                        break;
                    default:
                        System.out.println("Unknown command.");
                }
            } else if ("save".equals(parts[0])) {
                System.out.println("Enter the file path to save the graph:");
                String savePath = scanner.nextLine();
                try {
                    graph.saveGraph(savePath);
                    System.out.println("Graph saved successfully.");
                } catch (IOException e) {
                    System.out.println("Failed to save graph: " + e.getMessage());
                }
            } else if ("exit".equals(parts[0])) {
                break;
            } else {
                System.out.println("Invalid command.");
            }
        }

        scanner.close();
    }
}
