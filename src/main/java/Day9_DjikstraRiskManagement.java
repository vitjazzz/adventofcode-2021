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
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Day9_DjikstraRiskManagement {
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

            BasinManager basinManager = new BasinManager(board);

            System.out.println(basinManager.getTopLargestBasins(3));
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

    private static class BasinManager {
        private final Set<Basin> basins = new HashSet<>();
        private final Basin[][] pointsToBasin;
        private final int[][] board;

        public BasinManager(int[][] board) {
            this.board = board;
            pointsToBasin = new Basin[board.length][board[0].length];
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[0].length; j++) {
                    if (board[i][j] == 9) {
                        continue;
                    }
                    Basin surroundingBasin = getSurroundingBasin(i, j);
                    if (surroundingBasin == null) {
                        createBasin(i, j);
                    } else {
                        addPointToBasin(i, j, surroundingBasin);
                        mergeSurroundingBasins(i, j, surroundingBasin);
                    }
                }
            }
        }

        private void addPointToBasin(int i, int j, Basin basin) {
            basin.getPoints().add(new Point(i, j));
            pointsToBasin[i][j] = basin;
        }

        private Basin getSurroundingBasin(int i, int j) {
            Basin topBasin = extractBasin(i - 1, j);
            if (topBasin != null) {
                return topBasin;
            }
            Basin bottomBasin = extractBasin(i + 1, j);
            if (bottomBasin != null) {
                return bottomBasin;
            }
            Basin leftBasin = extractBasin(i, j - 1);
            if (leftBasin != null) {
                return leftBasin;
            }
            return extractBasin(i, j + 1);
        }

        private void createBasin(int i, int j) {
            HashSet<Point> points = new HashSet<>();
            points.add(new Point(i, j));
            Basin basin = new Basin(points);
            basins.add(basin);
            pointsToBasin[i][j] = basin;
        }

        private void mergeSurroundingBasins(int i, int j, Basin surroundingBasin) {
            Basin topBasin = extractBasin(i - 1, j);
            if (topBasin != null && !surroundingBasin.equals(topBasin)) {
                mergeBasins(surroundingBasin, topBasin);
            }
            Basin bottomBasin = extractBasin(i + 1, j);
            if (bottomBasin != null && !surroundingBasin.equals(topBasin)) {
                mergeBasins(surroundingBasin, bottomBasin);
            }
            Basin leftBasin = extractBasin(i, j - 1);
            if (leftBasin != null && !surroundingBasin.equals(leftBasin)) {
                mergeBasins(surroundingBasin, leftBasin);
            }
            Basin rightBasin = extractBasin(i, j + 1);
            if (rightBasin != null && !surroundingBasin.equals(rightBasin)) {
                mergeBasins(surroundingBasin, rightBasin);
            }
        }

        private void mergeBasins(Basin firstBasin, Basin secondBasin) {
            Basin biggestBasin = firstBasin.getPoints().size() > secondBasin.getPoints().size() ? firstBasin : secondBasin;
            Basin lowestBasin = biggestBasin.equals(firstBasin) ? secondBasin : firstBasin;
            basins.remove(lowestBasin);
            for (Point point : lowestBasin.getPoints()) {
                biggestBasin.getPoints().add(point);
                pointsToBasin[point.i][point.j] = biggestBasin;
            }
        }

        public int getTopLargestBasins(int amount) {
            return basins.stream()
                    .map(Basin::getPoints)
                    .map(Collection::size)
                    .sorted(Collections.reverseOrder())
                    .limit(amount)
                    .reduce(1, (a, b) -> a * b)
                    ;
        }

        private Basin extractBasin(int i, int j) {
            return isValid(i, j) ? pointsToBasin[i][j] : null;
        }

        private boolean isValid(int i, int j) {
            return i >= 0 && i < board.length && j >= 0 && j < board[0].length;
        }
    }

    @Value
    private static class Basin {
        UUID id = UUID.randomUUID();

        Set<Point> points;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Basin basin = (Basin) o;
            return Objects.equals(id, basin.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    @Value
    private static class Point {
        int i;
        int j;
    }
}
