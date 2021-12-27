import com.google.common.collect.Sets;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class Day12_CaveMaze {
    public static void main(String[] args) throws Exception {
        File taskFile = new File("/Users/vzikratyi/Test/12.txt");
        try (Reader r = new FileReader(taskFile);
             BufferedReader br = new BufferedReader(r)) {

            String[] lines = br.lines().toArray(String[]::new);

            CaveMaze caveMaze = new CaveMaze(lines);

            System.out.println(caveMaze.calculatePathCount());
        }
    }

    private static final Cave START_CAVE = new Cave("start");
    private static final Cave END_CAVE = new Cave("end");

    private static class CaveMaze {

        private final Map<Cave, Set<Cave>> caveConnections = new HashMap<>();

        public CaveMaze(String[] caveConnectionsArr) {
            for (String caveConnectionsStr : caveConnectionsArr) {
                String[] caves = caveConnectionsStr.split("-");
                Cave first = new Cave(caves[0]);
                Cave second = new Cave(caves[1]);
                caveConnections.computeIfAbsent(first, ignored -> new HashSet<>()).add(second);
                caveConnections.computeIfAbsent(second, ignored -> new HashSet<>()).add(first);
            }
        }

        public int calculatePathCount() {
            int pathsCount = 0;
            Stack<Path> paths = new Stack<>();
            paths.push(new Path());
            while (!paths.isEmpty()) {
                Path path = paths.pop();
                Cave lastCave = path.cavePath.get(path.cavePath.size() - 1);
                if (lastCave.equals(END_CAVE)) {
                    pathsCount++;
                    continue;
                }
                for (Cave neighborCave : caveConnections.get(lastCave)) {
                    if (neighborCave.equals(START_CAVE)) {
                        continue;
                    }
                    if (neighborCave.caveType == CaveType.BIG
                            || !path.visitedCaves.contains(neighborCave)
                            || path.allowedAdditionalSmallCaveVisit > 0) {
                        paths.push(path.createUpdatedPath(neighborCave));
                    }
                }
            }
            return pathsCount;
        }
    }

    private static class Path {
        private final Set<Cave> visitedCaves;
        private final List<Cave> cavePath;
        private final int allowedAdditionalSmallCaveVisit;

        public Path() {
            this.visitedCaves = Set.of(START_CAVE);
            this.cavePath = List.of(START_CAVE);
            this.allowedAdditionalSmallCaveVisit = 1;
        }

        private Path(Set<Cave> visitedCaves, List<Cave> cavePath, int allowedAdditionalSmallCaveVisit) {
            this.visitedCaves = visitedCaves;
            this.cavePath = cavePath;
            this.allowedAdditionalSmallCaveVisit = allowedAdditionalSmallCaveVisit;
        }

        public Path createUpdatedPath(Cave nextCave) {
            boolean additionalVisitOfSmallCave = nextCave.caveType == CaveType.SMALL
                    && visitedCaves.contains(nextCave);
            Set<Cave> newVisitedCaves = Sets.union(visitedCaves, Set.of(nextCave));
            LinkedList<Cave> newCavePath = new LinkedList<>(cavePath);
            newCavePath.addLast(nextCave);
            return new Path(newVisitedCaves, List.copyOf(newCavePath),
                    allowedAdditionalSmallCaveVisit - (additionalVisitOfSmallCave ? 1 : 0));
        }

    }

    @ToString(exclude = "caveType")
    @EqualsAndHashCode(exclude = "caveType")
    private static class Cave {
        private final CaveType caveType;
        private final String name;

        private Cave(String name) {
            this.name = name;
            this.caveType = name.toUpperCase().equals(name) ? CaveType.BIG : CaveType.SMALL;
        }
    }

    private enum CaveType {
        BIG, SMALL
    }
}
