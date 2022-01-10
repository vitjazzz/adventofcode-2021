import lombok.Value;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Day22_ReactorReboot {
    private static final int COORDINATES_LIMIT = 50;

    public static void main(String[] args) throws Exception {
        File taskFile = new File("/Users/vzikratyi/Test/22.txt");
        try (Reader r = new FileReader(taskFile);
             BufferedReader br = new BufferedReader(r)) {

            List<String> lines = br.lines().collect(Collectors.toList());

            List<RebootRule> rebootRules = lines.stream()
                    .map(RebootRule::of)
                    .filter(rule -> Objects.nonNull(rule.cuboid))
                    .collect(Collectors.toList());

            RebootProcessor rebootProcessor = new RebootProcessor(rebootRules);

            System.out.println(rebootProcessor.enabledCubes());
        }
    }

    private static class RebootProcessor {
        private Set<Cuboid> enabledCuboids = new HashSet<>();

        public RebootProcessor(List<RebootRule> rebootRules) {
            for (RebootRule rebootRule : rebootRules) {
                processRule(rebootRule);
            }
        }

        private void processRule(RebootRule rebootRule) {
            if (rebootRule.operationType == OperationType.ON) {
                enableCubes(rebootRule);
            } else {
                disableCubes(rebootRule);
            }
        }

        private void disableCubes(RebootRule rebootRule) {
            Set<Cuboid> updatedEnabledCuboids = new HashSet<>();
            for (Cuboid enabledCuboid : enabledCuboids) {
                updatedEnabledCuboids.addAll(enabledCuboid.subtract(rebootRule.cuboid));
            }
            enabledCuboids = updatedEnabledCuboids;
        }

        private void enableCubes(RebootRule rebootRule) {
            List<Cuboid> newEnabledCuboids = List.of(rebootRule.cuboid);
            for (Cuboid enabledCuboid : enabledCuboids) {
                newEnabledCuboids = newEnabledCuboids(enabledCuboid, newEnabledCuboids);
            }
            enabledCuboids.addAll(newEnabledCuboids);
        }

        private List<Cuboid> newEnabledCuboids(Cuboid alreadyEnabledCuboid, List<Cuboid> cuboids) {
            List<Cuboid> newEnabledCuboids = new ArrayList<>();
            for (Cuboid cuboid : cuboids) {
                newEnabledCuboids.addAll(cuboid.subtract(alreadyEnabledCuboid));
            }
            return newEnabledCuboids;
        }

        public long enabledCubes() {
            long enabledCubes = 0;
            for (Cuboid enabledCuboid : enabledCuboids) {
                enabledCubes += enabledCuboid.volume();
            }
            return enabledCubes;
        }
    }

    @Value
    private static class RebootRule {
        public static RebootRule of(String rebootRuleStr) {
            OperationType operationType = OperationType.valueOf(rebootRuleStr.substring(0, rebootRuleStr.indexOf(" ")).toUpperCase());
            Cuboid cuboid = Cuboid.of(rebootRuleStr.substring(rebootRuleStr.indexOf("x")));
//            cuboid = cuboid.trim(COORDINATES_LIMIT);
            return new RebootRule(operationType, cuboid);
        }

        OperationType operationType;
        Cuboid cuboid;
    }

    private enum OperationType {
        ON, OFF
    }

    @Value
    private static class Cuboid {
        private static Cuboid of(String cuboidStr) {
            List<int[]> coordinates = Arrays.stream(cuboidStr.split(","))
                    .map(str -> str.substring(2))
                    .map(str -> str.split("\\.\\."))
                    .map(coordinatesStrArr -> new int[]{Integer.parseInt(coordinatesStrArr[0]), Integer.parseInt(coordinatesStrArr[1])})
                    .collect(Collectors.toList());
            int[] xCoordinates = coordinates.get(0);
            int[] yCoordinates = coordinates.get(1);
            int[] zCoordinates = coordinates.get(2);
            return new Cuboid(xCoordinates[0], xCoordinates[1], yCoordinates[0], yCoordinates[1], zCoordinates[0], zCoordinates[1]);
        }

        int x1;
        int x2;
        int y1;
        int y2;
        int z1;
        int z2;

        public Cuboid trim(int limit) {
            if (x2 <= -limit || x1 >= limit
                    || y2 <= -limit || y1 >= limit
                    || z2 <= -limit || z1 >= limit) {
                return null;
            }
            return new Cuboid(
                    Math.max(x1, -limit), Math.min(x2, limit),
                    Math.max(y1, -limit), Math.min(y2, limit),
                    Math.max(z1, -limit), Math.min(z2, limit)
            );
        }

        public long volume() {
            return (long) (Math.abs(x2 - x1) + 1) *
                    (Math.abs(y2 - y1) + 1) *
                    (Math.abs(z2 - z1) + 1);
        }

        public List<Cuboid> subtract(Cuboid cuboid) {
            if (x2 < cuboid.x1 || x1 > cuboid.x2
                    || y2 < cuboid.y1 || y1 > cuboid.y2
                    || z2 < cuboid.z1 || z1 > cuboid.z2) {
                return List.of(this);
            }
            List<Cuboid> afterSubtraction = new ArrayList<>();
            if (x1 < cuboid.x1) {
                afterSubtraction.add(new Cuboid(x1, cuboid.x1 - 1, y1, y2, z1, z2));
            }
            if (x2 > cuboid.x2) {
                afterSubtraction.add(new Cuboid(cuboid.x2 + 1, x2, y1, y2, z1, z2));
            }
            if (y1 < cuboid.y1) {
                afterSubtraction.add(new Cuboid(Math.max(x1, cuboid.x1), Math.min(x2, cuboid.x2), y1, cuboid.y1 - 1, z1, z2));
            }
            if (y2 > cuboid.y2) {
                afterSubtraction.add(new Cuboid(Math.max(x1, cuboid.x1), Math.min(x2, cuboid.x2), cuboid.y2 + 1, y2, z1, z2));
            }
            if (z1 < cuboid.z1) {
                afterSubtraction.add(new Cuboid(Math.max(x1, cuboid.x1), Math.min(x2, cuboid.x2),
                        Math.max(y1, cuboid.y1), Math.min(y2, cuboid.y2),
                        z1, cuboid.z1 - 1));
            }
            if (z2 > cuboid.z2) {
                afterSubtraction.add(new Cuboid(Math.max(x1, cuboid.x1), Math.min(x2, cuboid.x2),
                        Math.max(y1, cuboid.y1), Math.min(y2, cuboid.y2),
                        cuboid.z2 + 1, z2));
            }

            return afterSubtraction;
        }
    }
}
