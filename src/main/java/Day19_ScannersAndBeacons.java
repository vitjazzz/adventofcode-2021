import lombok.Value;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Day19_ScannersAndBeacons {
    public static void main(String[] args) throws Exception {
        File taskFile = new File("/Users/vzikratyi/Test/19.txt");
        try (Reader r = new FileReader(taskFile);
             BufferedReader br = new BufferedReader(r)) {

            long start = System.nanoTime();
            List<String> lines = br.lines().collect(Collectors.toList());
            ScannerReportFactory scannerReportFactory = new ScannerReportFactory();

            List<ScannerReport> scannerReports = scannerReportFactory.create(lines);

            BeaconLocationCalculator beaconLocationCalculator = new BeaconLocationCalculator(scannerReports);
            System.out.println(beaconLocationCalculator.findMaxDistance());
            long end = System.nanoTime();
            System.out.println("Time to solve - " + Duration.ofNanos(end - start).toMillis());
        }
    }

    private static class ScannerReportFactory {
        List<ScannerReport> create(List<String> lines) {
            List<ScannerReport> scannerReports = new ArrayList<>();
            int currentScannerId = -1;
            Set<Point> currentPoints = new HashSet<>();
            for (String line : lines) {
                if (line.startsWith("---")) {
                    currentScannerId = Integer.parseInt(line.substring("--- scanner ".length(), line.length() - 4));
                    currentPoints = new HashSet<>();
                } else if (line.isBlank()) {
                    scannerReports.add(new ScannerReport(currentScannerId, Collections.unmodifiableSet(currentPoints)));
                } else {
                    int[] coordinates = Arrays.stream(line.split(",")).mapToInt(Integer::parseInt).toArray();
                    currentPoints.add(new Point(coordinates[0], coordinates[1], coordinates[2]));
                }
            }
            scannerReports.add(new ScannerReport(currentScannerId, Collections.unmodifiableSet(currentPoints)));
            return scannerReports;
        }
    }

    private static class BeaconLocationCalculator {

        int uniqueBeacons = 0;

        Map<Integer, List<PairedScanners>> pairedScannersByScanner = new HashMap<>();
        Map<Integer, ComplexScannerReport> complexScannerReportsMap = new HashMap<>();
        Set<Point> pointsFromRoot;
        Set<Point> relativeScannerCoordinates = new HashSet<>();

        public BeaconLocationCalculator(List<ScannerReport> scannerReports) {
            List<ComplexScannerReport> complexScannerReports = scannerReports.stream()
                    .map(ComplexScannerReport::new)
                    .collect(Collectors.toList());
            this.complexScannerReportsMap = complexScannerReports.stream()
                    .collect(Collectors.toMap(ComplexScannerReport::getScannerId, Function.identity()));
            for (int i = 0; i < complexScannerReports.size(); i++) {
                for (int j = i + 1; j < complexScannerReports.size(); j++) {
                    compareScannerReports(complexScannerReports.get(i), complexScannerReports.get(j));
                }
            }
            Set<Integer> processedScanners = new HashSet<>();
            processedScanners.add(0);
            relativeScannerCoordinates.add(new Point(0, 0, 0));
            this.pointsFromRoot = new HashSet<>();
            Queue<UnprocessedScanner> unprocessedScanners = new LinkedList<>();
            unprocessedScanners.add(new UnprocessedScanner(0, Rotation.BASE_ROTATION, new Point(0, 0, 0)));
            while (!unprocessedScanners.isEmpty()) {
                UnprocessedScanner unprocessedScanner = unprocessedScanners.poll();
                ComplexScannerReport scannerReport = complexScannerReportsMap.get(unprocessedScanner.scannerId);
                Set<Point> currentPointsFromRoot = scannerReport.beaconsCoordinates.stream()
                        .map(unprocessedScanner.rotation::rotate)
                        .map(point -> point.add(unprocessedScanner.distanceFromRoot))
                        .collect(Collectors.toSet());
                pointsFromRoot.addAll(currentPointsFromRoot);
                List<PairedScanners> pairedScanners = pairedScannersByScanner.get(unprocessedScanner.scannerId);
                for (PairedScanners pairedScanner : pairedScanners) {
                    if (processedScanners.contains(pairedScanner.second.scannerId)) {
                        continue;
                    }
                    processedScanners.add(pairedScanner.second.scannerId);
                    UnprocessedScanner newUnprocessedScanner = calculateRelativePosition(unprocessedScanner, pairedScanner.second, pairedScanner.overlappingPoints);
                    relativeScannerCoordinates.add(newUnprocessedScanner.distanceFromRoot);
                    unprocessedScanners.add(newUnprocessedScanner);
                }
            }
            uniqueBeacons = pointsFromRoot.size();
        }

        private void compareScannerReports(ComplexScannerReport first, ComplexScannerReport second) {
            Set<Pair<Pair<Point>>> overlappingPoints = new HashSet<>();
            for (Long quadraticDistance : first.distanceBetweenBeacons.keySet()) {
                Distance secondDistance = second.distanceBetweenBeacons.get(quadraticDistance);
                if (secondDistance != null) {
                    Distance firstDistance = first.distanceBetweenBeacons.get(quadraticDistance);
                    overlappingPoints.add(
                            new Pair<>(
                                    new Pair<>(firstDistance.from, firstDistance.to),
                                    new Pair<>(secondDistance.from, secondDistance.to)
                            )
                    );
                }
            }
            if (overlappingPoints.size() >= 12) {
                pairedScannersByScanner.computeIfAbsent(first.scannerId, ignored -> new ArrayList<>())
                        .add(new PairedScanners(first, second, overlappingPoints));
                Set<Pair<Pair<Point>>> swappedOverlappingPoints = overlappingPoints.stream().map(pair -> new Pair<>(pair.second, pair.first)).collect(Collectors.toSet());
                pairedScannersByScanner.computeIfAbsent(second.scannerId, ignored -> new ArrayList<>())
                        .add(new PairedScanners(second, first, swappedOverlappingPoints));
            }
        }

        private UnprocessedScanner calculateRelativePosition(UnprocessedScanner firstUnprocessed, ComplexScannerReport second, Set<Pair<Pair<Point>>> overlappingPoints) {
            Set<Point> firstOverlappingPoints = overlappingPoints.stream()
                    .map(Pair::getFirst)
                    .flatMap(pair -> List.of(pair.first, pair.second).stream())
                    .collect(Collectors.toSet());
            ComplexScannerReport first = complexScannerReportsMap.get(firstUnprocessed.scannerId);
            List<Pair<Pair<Point>>> testNotCertainPoints = overlappingPoints.stream().limit(4).collect(Collectors.toList());
            Set<Pair<Point>> testPoints = new HashSet<>();
            for (int i = 0; i < 3; i++) {
                Pair<Pair<Point>> pair = testNotCertainPoints.get(i);
                Pair<Point> firstPossiblePoints = pair.first;
                Pair<Point> secondPossiblePoints = pair.second;
                long firstRandomDistance = first.pointConnections.get(firstPossiblePoints.first).entrySet().stream()
                        .filter(entry -> !entry.getKey().equals(firstPossiblePoints.second))
                        .filter(entry -> firstOverlappingPoints.contains(entry.getKey()))
                        .findAny()
                        .get()
                        .getValue()
                        .quadraticDistance;
                if (second.distanceBetweenBeacons.get(firstRandomDistance).contains(secondPossiblePoints.first)) {
                    testPoints.add(new Pair<>(firstPossiblePoints.first, secondPossiblePoints.first));
                    testPoints.add(new Pair<>(firstPossiblePoints.second, secondPossiblePoints.second));
                } else {
                    testPoints.add(new Pair<>(firstPossiblePoints.first, secondPossiblePoints.second));
                    testPoints.add(new Pair<>(firstPossiblePoints.second, secondPossiblePoints.first));
                }
            }

            List<Pair<Point>> rotatedFirstTestPoints = testPoints.stream()
                    .map(pair -> new Pair<>(firstUnprocessed.rotation.rotate(pair.first), pair.second))
                    .collect(Collectors.toList());

            for (Rotation rotation : Rotation.rotations()) {
                List<Pair<Point>> rotatedTestPoints = rotatedFirstTestPoints.stream()
                        .map(pair -> new Pair<>(pair.first, rotation.rotate(pair.second)))
                        .collect(Collectors.toList());
                Pair<Point> testPointPair = rotatedTestPoints.get(0);
                Point possibleScannerCoordinates = testPointPair.first.minus(testPointPair.second);
                boolean rotationMatch = rotatedTestPoints.stream()
                        .allMatch(pair -> pair.first.minus(possibleScannerCoordinates).equals(pair.second));
                if (rotationMatch) {
                    return new UnprocessedScanner(second.scannerId, rotation, firstUnprocessed.distanceFromRoot.add(possibleScannerCoordinates));
                }
            }
            throw new IllegalStateException();
        }

        public int distinctBeacons() {
            return uniqueBeacons;
        }

        public int findMaxDistance() {
            int maxDistance = 0;
            for (Point firstPoint : relativeScannerCoordinates) {
                for (Point secondPoint : relativeScannerCoordinates) {
                    maxDistance = Math.max(maxDistance, firstPoint.manhattanDistance(secondPoint));
                }
            }
            return maxDistance;
        }
    }

    @Value
    private static class UnprocessedScanner {
        int scannerId;
        Rotation rotation;
        Point distanceFromRoot;
    }

    @Value
    private static class Rotation {
        public static int[][] signRotations = new int[][]{
                {1, 1, 1},
                {1, 1, -1},
                {1, -1, 1},
                {-1, 1, 1},
                {-1, -1, 1},
                {-1, 1, -1},
                {1, -1, -1},
                {-1, -1, -1},
        };
        public static char[][] axisRotations = new char[][]{
                {'x', 'y', 'z'},
                {'x', 'z', 'y'},
                {'z', 'y', 'x'},
                {'z', 'x', 'y'},
                {'y', 'x', 'z'},
                {'y', 'z', 'x'},
        };

        public static Rotation BASE_ROTATION = new Rotation(new int[]{1, 1, 1}, new char[]{'x', 'y', 'z'});

        public static List<Rotation> rotations() {
            List<Rotation> rotations = new ArrayList<>();
            for (char[] axisRotation : axisRotations) {
                for (int[] signRotation : signRotations) {
                    rotations.add(new Rotation(signRotation, axisRotation));
                }
            }
            return rotations;
        }

        int[] signRotation;
        char[] axisRotation;

        public Point rotate(Point point) {
            return convertSign(convertAxis(point));
        }

        private Point convertAxis(Point point) {
            if (Arrays.equals(axisRotation, new char[]{'x', 'y', 'z'})) {
                return point;
            } else if (Arrays.equals(axisRotation, new char[]{'x', 'z', 'y'})) {
                return new Point(point.x, point.z, point.y);
            } else if (Arrays.equals(axisRotation, new char[]{'z', 'y', 'x'})) {
                return new Point(point.z, point.y, point.x);
            } else if (Arrays.equals(axisRotation, new char[]{'z', 'x', 'y'})) {
                return new Point(point.z, point.x, point.y);
            } else if (Arrays.equals(axisRotation, new char[]{'y', 'x', 'z'})) {
                return new Point(point.y, point.x, point.z);
            } else if (Arrays.equals(axisRotation, new char[]{'y', 'z', 'x'})) {
                return new Point(point.y, point.z, point.x);
            }
            throw new IllegalStateException();
        }

        private Point convertSign(Point point) {
            return new Point(point.x * signRotation[0], point.y * signRotation[1], point.z * signRotation[2]);
        }
    }

    @Value
    private static class PairedScanners {
        ComplexScannerReport first;
        ComplexScannerReport second;
        Set<Pair<Pair<Point>>> overlappingPoints;
    }

    @Value
    private static class ComplexScannerReport {
        int scannerId;
        Set<Point> beaconsCoordinates;
        Map<Long, Distance> distanceBetweenBeacons = new HashMap<>();
        Map<Point, Map<Point, Distance>> pointConnections = new HashMap<>();

        public ComplexScannerReport(ScannerReport scannerReport) {
            this.scannerId = scannerReport.scannerId;
            this.beaconsCoordinates = scannerReport.beaconsCoordinates;
            List<Point> coordinatesList = new ArrayList<>(scannerReport.beaconsCoordinates);
            for (int i = 0; i < coordinatesList.size(); i++) {
                for (int j = i + 1; j < coordinatesList.size(); j++) {
                    Point from = coordinatesList.get(i);
                    Point to = coordinatesList.get(j);
                    Distance distance = new Distance(from, to);
                    distanceBetweenBeacons.put(distance.quadraticDistance, distance);
                    pointConnections.computeIfAbsent(from, ignored -> new HashMap<>()).put(to, distance);
                    pointConnections.computeIfAbsent(to, ignored -> new HashMap<>()).put(from, distance);
                }
            }
        }
    }

    @Value
    private static class Distance {
        Point from;
        Point to;
        long quadraticDistance;

        public Distance(Point from, Point to) {
            this.from = from;
            this.to = to;
            this.quadraticDistance = (long) (Math.pow(to.x - from.x, 2) + Math.pow(to.y - from.y, 2) + Math.pow(to.z - from.z, 2));
        }

        public boolean contains(Point point) {
            return from.equals(point) || to.equals(point);
        }
    }

    @Value
    private static class ScannerReport {
        int scannerId;
        Set<Point> beaconsCoordinates;
    }

    @Value
    private static class Point {
        int x;
        int y;
        int z;

        public Point add(Point point) {
            return new Point(x + point.x, y + point.y, z + point.z);
        }

        public Point minus(Point point) {
            return new Point(x - point.x, y - point.y, z - point.z);
        }

        public int manhattanDistance(Point point) {
            int distX = Math.abs(x - point.x);
            int distY = Math.abs(y - point.y);
            int distZ = Math.abs(z - point.z);

            return distX + distY + distZ;
        }
    }

    @Value
    private static class Pair<T> {
        T first;
        T second;
    }
}
