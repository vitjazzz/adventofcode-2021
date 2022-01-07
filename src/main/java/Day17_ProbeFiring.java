import com.google.common.collect.Sets;
import lombok.Value;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Day17_ProbeFiring {
    public static void main(String[] args) throws Exception {
        File taskFile = new File("/Users/vzikratyi/Test/17.txt");
        try (Reader r = new FileReader(taskFile);
             BufferedReader br = new BufferedReader(r)) {

            String line = br.lines().findFirst().get();

            TargetZone targetZone = TargetZone.of(line);
            VelocityCalculator velocityCalculator = new VelocityCalculator(targetZone);

            System.out.println(velocityCalculator.getNumberOfInitialVelocities());
        }
    }

    private static Set<Pair> getExpectedPairs() throws Exception {
        File taskFile = new File("/Users/vzikratyi/Test/17_test_validate.txt");
        try (Reader r = new FileReader(taskFile);
             BufferedReader br = new BufferedReader(r)) {

            return br.lines().flatMap(line -> Arrays.stream(line.trim().split(" ")))
                    .filter(Predicate.not(String::isBlank))
                    .map(pairStr -> pairStr.split(","))
                    .map(pairArr -> new Pair(Integer.parseInt(pairArr[0]), Integer.parseInt(pairArr[1])))
                    .collect(Collectors.toSet());
        }
    }

    private static class VelocityCalculator {
        private final TargetZone targetZone;

        private final Set<Pair> initialVelocities = new HashSet<>();

        private VelocityCalculator(TargetZone targetZone) {
            this.targetZone = targetZone;
            calculateInitialVelocities();
        }

        private void calculateInitialVelocities() {
            Map<Integer, Set<Integer>> yCoordinatesByStep = calculateYCoordinatesByStep();
            int maxStep = yCoordinatesByStep.keySet().stream().mapToInt(i -> i).max().getAsInt();
            Map<Integer, Set<Integer>> xCoordinatesByStep = calculateXCoordinatesByStep(maxStep);

            for (int i = 0; i <= maxStep; i++) {
                for (Integer y : yCoordinatesByStep.getOrDefault(i, Set.of())) {
                    for (Integer x : xCoordinatesByStep.getOrDefault(i, Set.of())) {
                        initialVelocities.add(new Pair(x, y));
                    }
                }
            }
        }

        private Map<Integer, Set<Integer>> calculateYCoordinatesByStep() {
            Map<Integer, Set<Integer>> yCoordinatesByStep = new HashMap<>();
            for (int y = calculateYHighestVelocity(); y >= targetZone.yStart; y--) {
                for (int step = 1;; step++) {
                    if (y >= 0 && step <= (y * 2) + 1) {
                        continue;
                    }
                    int effectiveStep = y >= 0 ? step - ((y * 2) + 1) : step - 1;
                    int yCalculated = Math.negateExact(triangular(Math.abs(y) + effectiveStep) - triangular(y >= 0 ? y : Math.abs(y) - 1));
                    if (targetZone.yContains(yCalculated)) {
                        yCoordinatesByStep.computeIfAbsent(step, ignored -> new HashSet<>()).add(y);
                    } else if (yCalculated < targetZone.yStart) {
                        break;
                    }
                }
            }
            return yCoordinatesByStep;
        }

        private Map<Integer, Set<Integer>> calculateXCoordinatesByStep(int maxStep) {
            Map<Integer, Set<Integer>> xCoordinatesByStep = new HashMap<>();
            for (int x = 0; x <= targetZone.xEnd; x++) {
                for (int step = 1; step <= maxStep; step++) {
                    int xCalculated = triangular(x) - (x < step ? 0 : triangular(x - step));
                    if (targetZone.xContains(xCalculated)) {
                        xCoordinatesByStep.computeIfAbsent(step, ignored -> new HashSet<>()).add(x);
                    } else if (x < step) {
                        break;
                    }
                }
            }
            return xCoordinatesByStep;
        }

        private int triangular(int i) {
            return (i * (i + 1)) / 2;
        }

        public int getNumberOfInitialVelocities() {
            return initialVelocities.size();
        }

        public Set<Pair> getInitialVelocities() {
            return initialVelocities;
        }

        private int calculateYHighestVelocity() {
            return Math.abs(targetZone.yStart) - 1;
        }
    }

    @Value
    private static class TargetZone {
        int xStart;
        int xEnd;
        int yStart;
        int yEnd;

        public static TargetZone of(String zoneStr) {
            String[] coordinates = zoneStr.substring("target area: ".length()).split(", ");
            String[] xCoordinates = coordinates[0].substring("x=".length()).split("\\.\\.");
            int xStart = Integer.parseInt(xCoordinates[0]);
            int xEnd = Integer.parseInt(xCoordinates[1]);
            String[] yCoordinates = coordinates[1].substring("y=".length()).split("\\.\\.");
            int yStart = Integer.parseInt(yCoordinates[0]);
            int yEnd = Integer.parseInt(yCoordinates[1]);
            return new TargetZone(xStart, xEnd, yStart, yEnd);
        }

        public boolean yContains(int y) {
            return y >= yStart && y <= yEnd;
        }

        public boolean xContains(int x) {
            return x >= xStart && x <= xEnd;
        }
    }

    @Value
    private static class Pair {
        int x;
        int y;
    }
}
