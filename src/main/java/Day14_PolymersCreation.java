import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Day14_PolymersCreation {
    public static void main(String[] args) throws Exception {
        File taskFile = new File("/Users/vzikratyi/Test/14.txt");
        try (Reader r = new FileReader(taskFile);
             BufferedReader br = new BufferedReader(r)) {

            List<String> lines = br.lines().collect(Collectors.toList());
            String initialTemplate = lines.get(0);

            Polymer polymer = new Polymer(initialTemplate, lines.subList(1, lines.size()));

            polymer.simulateSteps(20);

            System.out.println(polymer.getSize());
            System.out.println(polymer.getResult());
        }
    }

    private static class Polymer {
        private final Map<List<String>, String> pairInsertionRules;
        private final LinkedList<String> polymer;

        public Polymer(String initialPolymer, Collection<String> pairInsertionRulesCollection) {
            this.polymer = Arrays.stream(initialPolymer.split("")).collect(Collectors.toCollection(LinkedList::new));
            this.pairInsertionRules = pairInsertionRulesCollection.stream()
                    .filter(Predicate.not(String::isBlank))
                    .map(str -> str.split(" -> "))
                    .collect(Collectors.toMap(
                            elements -> Arrays.stream(elements[0].split("")).collect(Collectors.toList()),
                            elements -> elements[1]));
        }

        public void simulateSteps(int steps) {
            for (int i = 0; i < steps; i++) {
                simulateStep();
            }
        }

        private void simulateStep() {
            ListIterator<String> iterator = polymer.listIterator();
            while (true) {
                String element = iterator.next();
                if (!iterator.hasNext()) {
                    return;
                }
                String nextElement = iterator.next();
                iterator.previous();
                String createdElement = pairInsertionRules.get(List.of(element, nextElement));
                if (createdElement != null) {
                    iterator.add(createdElement);
                }
            }
        }

        public long getResult() {
            Map<String, AtomicLong> elementQuantities = new HashMap<>();
            for (String element : polymer) {
                elementQuantities.computeIfAbsent(element, ignored -> new AtomicLong()).incrementAndGet();
            }
            long[] quantities = elementQuantities.values().stream()
                    .mapToLong(AtomicLong::get)
                    .sorted()
                    .toArray();
            return quantities[quantities.length - 1] - quantities[0];
        }

        public int getSize() {
            return polymer.size();
        }
    }
}
