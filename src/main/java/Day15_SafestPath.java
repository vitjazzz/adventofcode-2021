import lombok.Value;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class Day15_SafestPath {
    public static void main(String[] args) throws Exception {
        File taskFile = new File("/Users/vzikratyi/Test/15.txt");
        try (Reader r = new FileReader(taskFile);
             BufferedReader br = new BufferedReader(r)) {

            String[] lines = br.lines().toArray(String[]::new);
            int[][] board = new int[lines.length][lines[0].length()];
            for (int i = 0; i < lines.length; i++) {
                board[i] = Arrays.stream(lines[i].split("")).mapToInt(Integer::parseInt).toArray();
            }

            NodeGraph nodeGraph = new NodeGraph(board, 5);
            PathCalculator pathCalculator = new PathCalculator(nodeGraph);

            System.out.println(pathCalculator.getSafestPath());
        }
    }

    private static class PathCalculator {
        private final Map<Node, NodePath> paths = new HashMap<>();
        private final NodeGraph nodeGraph;

        public PathCalculator(NodeGraph nodeGraph) {
            this.nodeGraph = nodeGraph;
            calculateSafestPath();
        }

        private void calculateSafestPath() {
            Queue<Node> nodes = new LinkedList<>();
            nodes.add(nodeGraph.getStart());
            while (!nodes.isEmpty()) {
                Node currentNode = nodes.poll();
                NodePath currentNodePath = paths.getOrDefault(currentNode, new NodePath(0, List.of()));
                List<Node> neighbours = nodeGraph.getNeighbours(currentNode);
                for (Node neighbour : neighbours) {
                    NodePath neighbourPath = paths.getOrDefault(neighbour, NodePath.LONGEST_PATH);
                    if (currentNodePath.pathRisk + neighbour.riskLevel < neighbourPath.getPathRisk()) {
                        nodes.add(neighbour);
                        paths.put(neighbour, currentNodePath.newPath(neighbour));
                    }
                }
            }
        }

        public int getSafestPath() {
            return paths.get(nodeGraph.getEnd()).pathRisk;
        }
    }

    private static class NodeGraph {
        private final Node start;
        private final Node end;
        private final Map<Node, List<Node>> nodeConnections;

        private NodeGraph(int[][] boardPart, int boardMultiplier) {
            int[][] board = createFullBoard(boardPart, boardMultiplier);
            this.nodeConnections = new HashMap<>();
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[0].length; j++) {
                    List<Node> neighbours = Collections.unmodifiableList(getNeighbourNodes(i, j, board));
                    nodeConnections.put(new Node(i, j, board[i][j]), neighbours);
                }
            }
            this.start = new Node(0, 0, board[0][0]);
            this.end = new Node(board.length - 1, board[0].length - 1, board[board.length - 1][board[0].length - 1]);
        }

        private int[][] createFullBoard(int[][] boardPart, int boardMultiplier) {
            int[][] fullBoard = new int[boardPart.length * boardMultiplier][boardPart[0].length * boardMultiplier];
            for (int i = 0; i < fullBoard.length; i++) {
                for (int j = 0; j < fullBoard[0].length; j++) {
                    int iPart = i % boardPart.length;
                    int jPart = j % boardPart[0].length;
                    int increment = i / boardPart.length + j / boardPart.length;
                    int value = boardPart[iPart][jPart] + increment;
                    if (value > 9) {
                        value = (value - 9);
                    }
                    fullBoard[i][j] = value;
                }
            }
            return fullBoard;
        }

        private List<Node> getNeighbourNodes(int i, int j, int[][] board) {
            List<Node> neighbours = new ArrayList<>();
            if (isValidNeighbour(i - 1, j, board)) {
                neighbours.add(new Node(i - 1, j, board[i - 1][j]));
            }
            if (isValidNeighbour(i + 1, j, board)) {
                neighbours.add(new Node(i + 1, j, board[i + 1][j]));
            }
            if (isValidNeighbour(i, j - 1, board)) {
                neighbours.add(new Node(i, j - 1, board[i][j - 1]));
            }
            if (isValidNeighbour(i, j + 1, board)) {
                neighbours.add(new Node(i, j + 1, board[i][j + 1]));
            }
            return neighbours;
        }

        private boolean isValidNeighbour(int i, int j, int[][] board) {
            return i >= 0 && i < board.length && j >= 0 && j < board[0].length
                    && !(i == 0 && j == 0);
        }

        public Node getStart() {
            return start;
        }

        public Node getEnd() {
            return end;
        }

        public List<Node> getNeighbours(Node from) {
            return nodeConnections.get(from);
        }
    }

    @Value
    private static class NodePath {
        public static final NodePath LONGEST_PATH = new NodePath(Integer.MAX_VALUE, List.of());

        int pathRisk;
        List<Node> path;

        public NodePath newPath(Node node) {
            List<Node> tmpPath = new ArrayList<>(path);
            tmpPath.add(node);
            return new NodePath(pathRisk + node.riskLevel, Collections.unmodifiableList(tmpPath));
        }
    }

    @Value
    private static class Node {
        int x;
        int y;
        int riskLevel;
    }
}
