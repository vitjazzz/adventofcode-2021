import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day14_PolymersCreation {
    public static void main(String[] args) throws Exception {
        File taskFile = new File("/Users/vzikratyi/Test/14.txt");
        try (Reader r = new FileReader(taskFile);
             BufferedReader br = new BufferedReader(r)) {

            List<String> lines = br.lines().collect(Collectors.toList());
            String initialTemplate = lines.get(0);

            Polymer polymer = new Polymer(initialTemplate, lines.subList(1, lines.size()), 40);

            System.out.println(polymer.getResult());
        }
    }

    private static class Polymer {
        private final Map<Integer, Map<List<String>, DistinctElementsCounter>> elementsCountDictionary = new HashMap<>();

        private final Map<List<String>, String> initialPairInsertionRules;
        private final List<String> initialPolymer;
        private final int maxSteps;

        public Polymer(String initialPolymerStr, Collection<String> pairInsertionRulesCollection, int maxSteps) {
            this.initialPolymer = Arrays.stream(initialPolymerStr.split("")).collect(Collectors.toCollection(LinkedList::new));
            this.initialPairInsertionRules = pairInsertionRulesCollection.stream()
                    .filter(Predicate.not(String::isBlank))
                    .map(str -> str.split(" -> "))
                    .collect(Collectors.toMap(
                            elements -> Arrays.stream(elements[0].split("")).collect(Collectors.toList()),
                            elements -> elements[1]));
            this.maxSteps = maxSteps;
        }

        public long getResult() {
            DistinctElementsCounter elementsCounter = DistinctElementsCounter.of(initialPolymer);
            for (int i = 1; i < initialPolymer.size(); i++) {
                DistinctElementsCounter foundElementCounter = getElementsCounter(0, initialPolymer.get(i - 1), initialPolymer.get(i));
                elementsCounter = elementsCounter.merge(foundElementCounter);
            }

            long[] quantities = elementsCounter.elementsCount.values().stream()
                    .mapToLong(l -> l)
                    .sorted()
                    .toArray();
            return quantities[quantities.length - 1] - quantities[0];
        }

        private DistinctElementsCounter getElementsCounter(int step, String left, String right) {
            String generatedElement = initialPairInsertionRules.get(List.of(left, right));
            if (generatedElement == null) {
                return DistinctElementsCounter.EMPTY;
            }
            if (step == maxSteps - 1) {
                return DistinctElementsCounter.of(generatedElement);
            }
            Map<List<String>, DistinctElementsCounter> stepDictionary = elementsCountDictionary.computeIfAbsent(step, ignored -> new HashMap<>());
            DistinctElementsCounter leftPair = stepDictionary.computeIfAbsent(List.of(left, generatedElement), ignored ->
                    getElementsCounter(step + 1, left, generatedElement));
            DistinctElementsCounter rightPair = stepDictionary.computeIfAbsent(List.of(generatedElement, right), ignored ->
                    getElementsCounter(step + 1, generatedElement, right));

            return leftPair.merge(rightPair).addElement(generatedElement);
        }
    }

    private static class DistinctElementsCounter {
        public static final DistinctElementsCounter EMPTY = new DistinctElementsCounter(Map.of());

        public static DistinctElementsCounter of(String element) {
            return new DistinctElementsCounter(Map.of(element, 1L));
        }

        public static DistinctElementsCounter of(List<String> elements) {
            Map<String, Long> elementsCount = new HashMap<>();
            for (String element : elements) {
                elementsCount.merge(element, 1L, Long::sum);
            }
            return new DistinctElementsCounter(elementsCount);
        }

        private final Map<String, Long> elementsCount;

        private DistinctElementsCounter(Map<String, Long> elementsCount) {
            this.elementsCount = elementsCount;
        }

        public DistinctElementsCounter merge(DistinctElementsCounter other) {
            Map<String, Long> newElementsCount = Stream.of(this.elementsCount, other.elementsCount)
                    .flatMap(map -> map.entrySet().stream())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            Long::sum));
            return new DistinctElementsCounter(newElementsCount);
        }


        public DistinctElementsCounter addElement(String element) {
            HashMap<String, Long> newElementsCount = new HashMap<>(elementsCount);
            newElementsCount.merge(element, 1L, Long::sum);
            return new DistinctElementsCounter(newElementsCount);
        }
    }
}
