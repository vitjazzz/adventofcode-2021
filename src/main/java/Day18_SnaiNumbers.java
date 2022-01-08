import lombok.Value;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Day18_SnaiNumbers {
    public static void main(String[] args) throws Exception {
        File taskFile = new File("/Users/vzikratyi/Test/18.txt");
        try (Reader r = new FileReader(taskFile);
             BufferedReader br = new BufferedReader(r)) {

            SnailFactory snailFactory = new SnailFactory();
            List<SnailNumber> snailNumbers = br.lines().map(snailFactory::create).collect(Collectors.toList());

            long maxMagnitude = 0;
            for (int i = 0; i < snailNumbers.size(); i++) {
                for (int j = 0; j < snailNumbers.size(); j++) {
                    if (i == j) {
                        continue;
                    }
                    long magnitude = new PairSnailNumber(snailNumbers.get(i), snailNumbers.get(j))
                            .reduce()
                            .calculateMagnitude();
                    maxMagnitude = Math.max(maxMagnitude, magnitude);
                }
            }

            System.out.println(maxMagnitude);
        }
    }

    private static class SnailFactory {
        public SnailNumber create(String snailNumberStr) {
            if (snailNumberStr.startsWith("[")) {
                int middleCommaIndex = findMiddleCommaIndex(snailNumberStr);
                String leftSnailNumberStr = snailNumberStr.substring(1, middleCommaIndex);
                String rightSnailNumberStr = snailNumberStr.substring(middleCommaIndex + 1, snailNumberStr.length() - 1);
                return new PairSnailNumber(create(leftSnailNumberStr), create(rightSnailNumberStr));
            } else {
                return new RegularSnailNumber(Integer.parseInt(snailNumberStr));
            }
        }

        private int findMiddleCommaIndex(String snailNumberStr) {
            int requiredCommaOrder = 0;
            int foundCommas = 0;
            char[] snailNumberChars = snailNumberStr.toCharArray();
            for (int i = 0; i < snailNumberChars.length; i++) {
                if (snailNumberChars[i] == '[') {
                    requiredCommaOrder++;
                }
                if (snailNumberChars[i] == ',') {
                    foundCommas++;
                    if (foundCommas == requiredCommaOrder) {
                        return i;
                    }
                }
            }
            return -1;
        }
    }

    private interface SnailNumber {
        long calculateMagnitude();

        SnailNumber reduce();

        SnailNumber addRightValue(int value);

        SnailNumber addLeftValue(int value);
    }

    @Value
    private static class RegularSnailNumber implements SnailNumber {
        int value;

        @Override
        public long calculateMagnitude() {
            return value;
        }

        @Override
        public SnailNumber reduce() {
            return this;
        }

        @Override
        public SnailNumber addRightValue(int value) {
            return new RegularSnailNumber(this.value + value);
        }

        @Override
        public SnailNumber addLeftValue(int value) {
            return new RegularSnailNumber(this.value + value);
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    @Value
    private static class PairSnailNumber implements SnailNumber {
        SnailNumber left;
        SnailNumber right;

        @Override
        public long calculateMagnitude() {
            return 3 * left.calculateMagnitude() + 2 * right.calculateMagnitude();
        }

        @Override
        public SnailNumber reduce() {
            boolean wasReduced;
            SnailNumber reducedSnailNumber = this;
            do {
                wasReduced = false;
                SnailNumber explodedSnailNumber = explode(reducedSnailNumber);
                if (!explodedSnailNumber.equals(reducedSnailNumber)) {
                    wasReduced = true;
                    reducedSnailNumber = explodedSnailNumber;
                    continue;
                }
                SnailNumber splitSnailNumber = split(reducedSnailNumber);
                if (!splitSnailNumber.equals(reducedSnailNumber)) {
                    wasReduced = true;
                    reducedSnailNumber = splitSnailNumber;
                }
            } while (wasReduced);
            return reducedSnailNumber;
        }

        @Override
        public SnailNumber addRightValue(int value) {
            return new PairSnailNumber(left, right.addRightValue(value));
        }

        @Override
        public SnailNumber addLeftValue(int value) {
            return new PairSnailNumber(left.addLeftValue(value), right);
        }

        @Override
        public String toString() {
            return "[" + left.toString() + "," + right.toString() + "]";
        }

        private SnailNumber explode(SnailNumber snailNumber) {
            if (snailNumber instanceof RegularSnailNumber) {
                return snailNumber;
            } else {
                return explode((PairSnailNumber) snailNumber, 0).snailNumber;
            }
        }

        private ExplodedSnailNumber explode(PairSnailNumber snailNumber, int recursionLevel) {
            if (recursionLevel < 4) {
                if (snailNumber.left instanceof PairSnailNumber) {
                    ExplodedSnailNumber explodedLeftSnailNumber = explode((PairSnailNumber) snailNumber.left, recursionLevel + 1);
                    if (explodedLeftSnailNumber.exploded) {
                        SnailNumber newLeftSnailNumber = explodedLeftSnailNumber.snailNumber == null
                                ? new RegularSnailNumber(0)
                                : explodedLeftSnailNumber.snailNumber;
                        SnailNumber newRightSnailNumber = explodedLeftSnailNumber.rightNumber != null
                                ? snailNumber.right.addLeftValue(explodedLeftSnailNumber.rightNumber)
                                : snailNumber.right;
                        return new ExplodedSnailNumber(new PairSnailNumber(newLeftSnailNumber, newRightSnailNumber),
                                explodedLeftSnailNumber.leftNumber, null, true);
                    }
                }
                if (snailNumber.right instanceof PairSnailNumber) {
                    ExplodedSnailNumber explodedRightSnailNumber = explode((PairSnailNumber) snailNumber.right, recursionLevel + 1);
                    if (explodedRightSnailNumber.exploded) {
                        SnailNumber newRightSnailNumber = explodedRightSnailNumber.snailNumber == null
                                ? new RegularSnailNumber(0)
                                : explodedRightSnailNumber.snailNumber;
                        SnailNumber newLeftSnailNumber = explodedRightSnailNumber.leftNumber != null
                                ? snailNumber.left.addRightValue(explodedRightSnailNumber.leftNumber)
                                : snailNumber.left;
                        return new ExplodedSnailNumber(new PairSnailNumber(newLeftSnailNumber, newRightSnailNumber),
                                null, explodedRightSnailNumber.rightNumber, true);
                    }
                }
            } else {
                int leftValue = ((RegularSnailNumber) snailNumber.left).value;
                int rightValue = ((RegularSnailNumber) snailNumber.right).value;
                return new ExplodedSnailNumber(null, leftValue, rightValue, true);
            }
            return new ExplodedSnailNumber(snailNumber, null, null, false);
        }

        @Value
        private static class ExplodedSnailNumber {
            SnailNumber snailNumber;
            Integer leftNumber;
            Integer rightNumber;
            boolean exploded;
        }

        private SnailNumber split(SnailNumber snailNumber) {
            return splitRecursive(snailNumber).snailNumber;
        }

        private SplitSnailNumber splitRecursive(SnailNumber snailNumber) {
            if (snailNumber instanceof RegularSnailNumber) {
                int value = ((RegularSnailNumber) snailNumber).value;
                if (value >= 10) {
                    return new SplitSnailNumber(new PairSnailNumber(
                            new RegularSnailNumber((int) Math.floor(((double) value) / 2)),
                            new RegularSnailNumber((int) Math.ceil(((double) value) / 2))
                    ), true);
                } else {
                    return new SplitSnailNumber(snailNumber, false);
                }
            } else {
                PairSnailNumber pairSnailNumber = (PairSnailNumber) snailNumber;
                SplitSnailNumber leftSplit = splitRecursive(pairSnailNumber.left);
                if (leftSplit.split) {
                    return new SplitSnailNumber(new PairSnailNumber(leftSplit.snailNumber, pairSnailNumber.right), true);
                }
                SplitSnailNumber rightSplit = splitRecursive(pairSnailNumber.right);
                return new SplitSnailNumber(new PairSnailNumber(leftSplit.snailNumber, rightSplit.snailNumber), rightSplit.split);
            }
        }

        @Value
        private static class SplitSnailNumber {
            SnailNumber snailNumber;
            boolean split;
        }
    }
}
