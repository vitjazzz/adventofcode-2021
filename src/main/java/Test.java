import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Test {
    public static void main(String[] args) throws Exception {
        File taskFile = new File("/Users/vzikratyi/Test/10.txt");
        try (Reader r = new FileReader(taskFile);
             BufferedReader br = new BufferedReader(r)) {

            List<String> lines = br.lines().collect(Collectors.toList());

            BracesManager bracesManager = new BracesManager(lines);

            System.out.println(bracesManager.getCompletionScore());
        }
    }

    private static class BracesManager {
        private static final Map<String, Integer> ERROR_SCORES = Map.of(
                ")", 3,
                "]", 57,
                "}", 1197,
                ">", 25137
        );
        private static final Map<String, Integer> COMPLETION_SCORES = Map.of(
                ")", 1,
                "]", 2,
                "}", 3,
                ">", 4
        );

        private static final Set<String> OPENING_BRACES = Set.of(
                "(", "[", "{", "<"
        );

        private static final Map<String, String> MATCHING_BRACES = Map.of(
                "(", ")",
                "[", "]",
                "{", "}",
                "<", ">"
        );


        private final List<String> foundErrorBraces = new ArrayList<>();
        private final List<List<String>> completedLines = new ArrayList<>();

        public BracesManager(Collection<String> lines) {
            lines.stream()
                    .filter(Predicate.not(String::isBlank))
                    .forEach(this::validateBraces);
        }

        private void validateBraces(String line) {
            Stack<String> openingBraces = new Stack<>();
            for (String brace : line.split("")) {
                if (OPENING_BRACES.contains(brace)) {
                    openingBraces.push(brace);
                } else {
                    if (openingBraces.isEmpty() || !MATCHING_BRACES.get(openingBraces.pop()).equals(brace)) {
                        foundErrorBraces.add(brace);
                        return;
                    }
                }
            }
            if (!openingBraces.isEmpty()) {
                List<String> completedClosingBraces = openingBraces.stream()
                        .map(MATCHING_BRACES::get)
                        .collect(Collectors.toList());
                Collections.reverse(completedClosingBraces);
                completedLines.add(completedClosingBraces);
            }
        }

        public int getErrorScore() {
            return foundErrorBraces.stream().mapToInt(ERROR_SCORES::get).sum();
        }

        public long getCompletionScore() {
            long[] scores = completedLines.stream()
                    .mapToLong(braces -> {
                        long score = 0;
                        for (String brace : braces) {
                            score = score * 5 + COMPLETION_SCORES.get(brace);
                        }
                        return score;
                    })
                    .sorted()
                    .toArray();
            return scores[scores.length / 2];
        }

    }
}
