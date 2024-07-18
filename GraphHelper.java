import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;

class GraphHelper {
    public static void processTextFile(Graph<String> graph, String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        processLines(graph, lines);
    }

    public static void processPDF(Graph<String> graph, String filePath) throws IOException, TikaException {
        Tika tika = new Tika();
        String text = tika.parseToString(Paths.get(filePath).toFile());
        List<String> lines = List.of(text.split("\n"));
        processLines(graph, lines);
    }

    private static void processLines(Graph<String> graph, List<String> lines) {
        Pattern pattern = Pattern.compile("\\w+|[\\p{Punct}]");
        List<String> sentenceStructureKeywords = Arrays.asList("Subject", "Verb", "Object", "Complement", "Adjunct", "Indirect Object", "Direct Object");

        String previousNodeId = null;
        List<String> currentSentence = new ArrayList<>();

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                String currentNodeId = matcher.group();
                if (!currentNodeId.isEmpty()) {
                    currentSentence.add(currentNodeId);
                    graph.addNode(currentNodeId, currentNodeId);
                    tagNode(graph.getNode(currentNodeId));

                    if (previousNodeId != null) {
                        if (graph.edgeExists(previousNodeId, currentNodeId)) {
                            float currentStrength = graph.getNode(previousNodeId).getNeighbors().get(currentNodeId).getStrength();
                            float newStrength = currentStrength + 0.01f; // Increase by a small amount
                            graph.updateEdgeStrength(previousNodeId, currentNodeId, newStrength);
                        } else {
                            graph.addEdge(previousNodeId, currentNodeId, 0.01f); // Start with a small strength
                        }
                    }
                    previousNodeId = currentNodeId;
                }

                // Check for end of sentence
                if (currentNodeId.matches("[.!?]")) {
                    processSentence(graph, currentSentence, sentenceStructureKeywords);
                    currentSentence.clear();
                }
            }
        }

        // Process any remaining sentence
        if (!currentSentence.isEmpty()) {
            processSentence(graph, currentSentence, sentenceStructureKeywords);
        }

        System.out.println("Total number of nodes: " + graph.getAllNodes().size());
        System.out.println("Total number of edges: " + graph.getAllEdges().size());
    }

    private static void processSentence(Graph<String> graph, List<String> sentence, List<String> keywords) {
        String sentenceStructure = identifySentenceStructure(sentence, keywords);
        if (!sentenceStructure.isEmpty()) {
            for (String nodeId : sentence) {
                Node<String> node = graph.getNode(nodeId);
                node.addSentenceStructure(sentenceStructure);
            }
        }
    }

    private static String identifySentenceStructure(List<String> sentence, List<String> keywords) {
        StringBuilder structure = new StringBuilder();
        for (String word : sentence) {
            String component = identifySentenceComponent(word);
            if (component != null && !structure.toString().contains(component)) {
                structure.append(component).append(" + ");
            }
        }
        // Remove the last " + " if it exists
        if (structure.length() > 3) {
            structure.setLength(structure.length() - 3);
        }
        return structure.toString();
    }

    private static String identifySentenceComponent(String word) {
        String tag = determineWordTag(word);
        return switch (tag) {
            case "Pronoun", "Noun" -> "Subject";
            case "Verb" -> "Verb";
            case "Preposition" -> "Adjunct";
            case "Article" -> null; // Articles don't contribute to the main structure
            default -> "Object"; // Default to Object for unidentified components
        };
    }

    private static void tagNode(Node<String> node) {
        String id = node.getId();
        if (id.matches("\\d+")) {
            node.setTag("Number");
        } else if (id.matches("\\p{Punct}")) {
            node.setTag("Symbol");
        } else {
            node.setTag(determineWordTag(id)); // Use heuristic or POS tagger for words
        }
    }

    private static String determineWordTag(String word) {
        if (word.matches("^(?i:he|she|it|they|we|you|i)$")) {
            return "Pronoun";
        } else if (word.matches("^(?i:a|an|the)$")) {
            return "Article";
        } else if (word.matches("^(?i:and|but|or|nor|for|so|yet)$")) {
            return "Conjunction";
        } else if (word.matches("^(?i:is|are|was|were|be|being|been|have|has|had|do|does|did|will|would|shall|should|may|might|must|can|could)$")) {
            return "Verb";
        } else if (word.matches("^(?i:to|from|with|at|by|in|on|of|for|about|as|into|like|through|after|over|between|out|against|during|without|before|under|around|among)$")) {
            return "Preposition";
        } else if (word.matches(".*ing$|.*ed$|.*s$")) {
            return "Verb";
        } else {
            return "Noun"; // Default to noun for simplicity
        }
    }

    public static void measureTime(String taskName, Runnable task) {
        long startTime = System.nanoTime();
        task.run();
        long endTime = System.nanoTime();
        long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        System.out.printf("%s took %d milliseconds.%n", taskName, duration);
    }
}
