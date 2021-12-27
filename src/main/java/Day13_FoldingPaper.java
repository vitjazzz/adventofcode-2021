import com.google.common.collect.Sets;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Day13_FoldingPaper {
    public static void main(String[] args) throws Exception {
        File taskFile = new File("/Users/vzikratyi/Test/13.txt");
        try (Reader r = new FileReader(taskFile);
             BufferedReader br = new BufferedReader(r)) {

            List<String> lines = br.lines().collect(Collectors.toList());

            Board board = new Board(lines);

            List<FoldingRule> foldingRules = lines.stream()
                    .filter(str -> str.contains("fold along "))
                    .map(str -> str.substring("fold along ".length()))
                    .map(str -> str.split("="))
                    .map(rule -> new FoldingRule(rule[0], Integer.parseInt(rule[1])))
                    .collect(Collectors.toList());

            for (FoldingRule foldingRule : foldingRules) {
                board.fold(foldingRule);
            }

            board.visualizeBoard();
        }
    }

    private static class Board {
        private final Set<Point> points;

        public Board(Collection<String> boardConfig) {
            this.points = boardConfig.stream()
                    .filter(Predicate.not(String::isBlank))
                    .filter(str -> !str.contains("fold"))
                    .map(str -> Arrays.stream(str.split(",")).mapToInt(Integer::parseInt).toArray())
                    .map(coordinates -> new Point(coordinates[0], coordinates[1]))
                    .collect(Collectors.toSet());


        }


        public int getPointsCount() {
            return points.size();
        }

        public void fold(FoldingRule foldingRule) {
            Collection<Point> discardedPoints = points.stream()
                    .filter(foldingRule::isOnLine)
                    .collect(Collectors.toList());
            discardedPoints.forEach(points::remove);

            Collection<Point> pointsToFold = points.stream()
                    .filter(foldingRule::isFolded)
                    .collect(Collectors.toList());
            pointsToFold.forEach(points::remove);

            Collection<Point> pointsAfterFold = pointsToFold.stream()
                    .map(foldingRule::afterFold)
                    .collect(Collectors.toList());
            points.addAll(pointsAfterFold);
        }

        public void visualizeBoard() {
            int maxX = points.stream().mapToInt(Point::getX).max().getAsInt();
            int maxY = points.stream().mapToInt(Point::getY).max().getAsInt();
            for (int y = 0; y <= maxY; y++) {
                for (int x = 0; x <= maxX; x++) {
                    System.out.print(points.contains(new Point(x, y)) ? "#" : ".");
                }
                System.out.println();
            }
        }
    }

    @Value
    private static class Point {
        int x;
        int y;
    }

    private static class FoldingRule {
        private final LineType type;
        private final int coordinate;

        public FoldingRule(String coordinateName, int coordinate) {
            this.type = coordinateName.equals("x") ? LineType.VERTICAL : LineType.HORIZONTAL;
            this.coordinate = coordinate;
        }

        public boolean isOnLine(Point point) {
            if (type == LineType.HORIZONTAL) {
                return point.y == coordinate;
            } else {
                return point.x == coordinate;
            }
        }

        public boolean isFolded(Point point) {
            if (type == LineType.HORIZONTAL) {
                return point.y > coordinate;
            } else {
                return point.x > coordinate;
            }
        }

        public Point afterFold(Point point) {
            if (type == LineType.HORIZONTAL) {
                int newY = coordinate - (point.y - coordinate);
                return new Point(point.x, newY);
            } else {
                int newX = coordinate - (point.x - coordinate);
                return new Point(newX, point.y);
            }
        }
    }

    private enum LineType {
        HORIZONTAL, VERTICAL;
    }
}
