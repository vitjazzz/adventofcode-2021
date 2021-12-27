import lombok.Value;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Test {
    public static void main(String[] args) throws Exception {
        File taskFile = new File("/Users/vzikratyi/Test/9.txt");
        try (Reader r = new FileReader(taskFile);
             BufferedReader br = new BufferedReader(r)) {

            List<int[]> boardLines = br.lines()
                    .map(str -> str.split(""))
                    .map(numberStrs -> Arrays.stream(numberStrs).mapToInt(Integer::parseInt).toArray())
                    .collect(Collectors.toList());

            int[][] board = null;
            for (int i = 0; i < boardLines.size(); i++) {
                int[] boardLine = boardLines.get(i);
                if (board == null) {
                    board = new int[boardLines.size()][boardLine.length];
                }
                board[i] = boardLine;
            }

            BoardRiskCalculator riskCalculator = new BoardRiskCalculator(board);

            System.out.println(riskCalculator.getTotalRisk());
        }
    }

    private static class BoardRiskCalculator {
        List<Point> lowestPoints = new ArrayList<>();
        private final int[][] board;

        public BoardRiskCalculator(int[][] board) {
            this.board = board;
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[0].length; j++) {
                    if (isLowPoint(i, j, board)) {
                        lowestPoints.add(new Point(i, j));
                    }
                }
            }
        }

        private boolean isLowPoint(int i, int j, int[][] board) {
            int pointHeight = board[i][j];
            return pointHeight < getPointHeight(i - 1, j, board)
                    && pointHeight < getPointHeight(i + 1, j, board)
                    && pointHeight < getPointHeight(i, j - 1, board)
                    && pointHeight < getPointHeight(i, j + 1, board);
        }

        private int getPointHeight(int i, int j, int[][] board) {
            return isPointValid(i, j, board) ? board[i][j] : Integer.MAX_VALUE;
        }

        private boolean isPointValid(int i, int j, int[][] board) {
            return i >= 0 && i < board.length && j >= 0 && j < board[0].length;
        }

        public Integer getTotalRisk() {
            return lowestPoints.stream().mapToInt(point -> board[point.i][point.j]).sum();
        }
    }

    private static class HigherPoints {
        private final int value;
        private final Set<Point> points = new HashSet<>();
        private boolean isPointLowest = false;

        public HigherPoints(int value) {
            this.value = value;
        }

        public void addPoint(int i, int j) {
            points.add(new Point(i, j));
        }

        public void setPointLowest() {
            isPointLowest = true;
        }

        public boolean isPointLowest() {
            return isPointLowest;
        }

        public Set<Point> getPoints() {
            return points;
        }

    }

    private static class BasinManager {
        private final Set<Basin> basins = new HashSet<>();
        private final Basin[][] pointsToBasin;

        public BasinManager(int[][] board) {
            pointsToBasin = new Basin[board.length][board[0].length];
        }

        public void connectPoints(Point first) {

        }

        public int getTopLargestBasins(int amount) {
            return basins.stream()
                    .map(Basin::getPoints)
                    .map(Collection::size)
                    .sorted(Collections.reverseOrder())
                    .limit(amount)
                    .mapToInt(i -> i)
                    .sum()
                    ;
        }
    }

    @Value
    private static class Basin {
        Set<Point> points;
    }

    @Value
    private static class Point {
        int i;
        int j;
    }
}
