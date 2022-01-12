import com.google.common.collect.Sets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Day8_Fixing8DigitClock {
    public static void main(String[] args) throws Exception {
        File taskFile = new File("/Users/vzikratyi/Test/8.txt");
        try (Reader r = new FileReader(taskFile);
             BufferedReader br = new BufferedReader(r)) {

            Long result = br.lines()
                    .map(str -> str.split(" \\| "))
                    .mapToLong(strs -> {
                        List<Set<String>> encodedNumbers = Arrays.stream(strs[0].split(" "))
                                .map(str -> Arrays.stream(str.split("")).collect(Collectors.toSet()))
                                .collect(Collectors.toList());
                        Display display = new Display(encodedNumbers);


                        List<Integer> numbers = Arrays.stream(strs[1].split(" "))
                                .map(str -> Arrays.stream(str.split("")).collect(Collectors.toSet()))
                                .map(display::getNumber)
                                .collect(Collectors.toList());
                        long value = 0;
                        long multiplier = 1;
                        for (int i = numbers.size() - 1; i >= 0; i--) {
                            value += numbers.get(i) * multiplier;
                            multiplier *= 10;
                        }
                        return value;
                    })
                    .sum();

            System.out.println(result);
        }
    }

    private static class Display {
        Map<Set<String>, Integer> numberRules = new HashMap<>();

        public Display(Collection<Set<String>> encodedNumbers) {
            Set<Set<String>> unsolvedEncodedNumbers = new HashSet<>(encodedNumbers);

            Set<String> one = unsolvedEncodedNumbers.stream().filter(set -> set.size() == 2).findAny().get();
            unsolvedEncodedNumbers.remove(one);
            numberRules.put(one, 1);

            Set<String> four = unsolvedEncodedNumbers.stream().filter(set -> set.size() == 4).findAny().get();
            unsolvedEncodedNumbers.remove(four);
            numberRules.put(four, 4);

            Set<String> seven = unsolvedEncodedNumbers.stream().filter(set -> set.size() == 3).findAny().get();
            unsolvedEncodedNumbers.remove(seven);
            numberRules.put(seven, 7);

            Set<String> eight = unsolvedEncodedNumbers.stream().filter(set -> set.size() == 7).findAny().get();
            unsolvedEncodedNumbers.remove(eight);
            numberRules.put(eight, 8);

            Set<String> six = unsolvedEncodedNumbers.stream()
                    .filter(set -> set.size() == 6)
                    .filter(set ->
                            !Sets.intersection(one, Sets.difference(eight, set)).isEmpty()
                    )
                    .findAny().get();
            unsolvedEncodedNumbers.remove(six);
            numberRules.put(six, 6);

            Set<String> zero = unsolvedEncodedNumbers.stream()
                    .filter(set -> set.size() == 6)
                    .filter(set ->
                            !Sets.intersection(four, Sets.difference(eight, set)).isEmpty()
                    )
                    .findAny().get();
            unsolvedEncodedNumbers.remove(zero);
            numberRules.put(zero, 0);

            Set<String> nine = unsolvedEncodedNumbers.stream()
                    .filter(set -> set.size() == 6)
                    .findAny().get();
            unsolvedEncodedNumbers.remove(nine);
            numberRules.put(nine, 9);

            String topRight = Sets.difference(eight, six).stream().findAny().get();
            Set<String> five = unsolvedEncodedNumbers.stream()
                    .filter(set -> set.size() == 5)
                    .filter(set -> Sets.difference(nine, set).equals(Set.of(topRight)))
                    .findAny().get();
            unsolvedEncodedNumbers.remove(five);
            numberRules.put(five, 5);

            String bottomRight = Sets.difference(one, Set.of(topRight)).stream().findAny().get();
            Set<String> three = unsolvedEncodedNumbers.stream()
                    .filter(set -> set.size() == 5)
                    .filter(set -> Sets.intersection(Set.of(bottomRight, topRight), set).size() == 2)
                    .findAny().get();
            unsolvedEncodedNumbers.remove(three);
            numberRules.put(three, 3);

            Set<String> two = unsolvedEncodedNumbers.stream()
                    .findAny().get();
            unsolvedEncodedNumbers.remove(two);
            numberRules.put(two, 2);
        }

        public Integer getNumber(Set<String> encoded) {
            return numberRules.get(encoded);
        }

    }
    /*
        th = 7 - 1;
        bh = 9 - (7 + 4);
        mh = 3 - (7 + bh);
        tl = (4 - 1) - mh;
        bl = 0 - bh - tl - 7;
        br = 5 - tl - th - mh - bh;
        tr = 1 - br;

        1 = 2 sides;
        7 = 3 sides;
        4 = 4 sides;
        2 = 5 sides;
        3 = 5 sides;
        5 = 5 sides;
        6 = 6 sides;
        9 = 6 sides;
        0 = 6 sides;
        8 = 7 sides;
     */
}
