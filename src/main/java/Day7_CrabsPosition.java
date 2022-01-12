import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Arrays;

public class Day7_CrabsPosition {
    public static void main(String[] args) throws Exception {
        File taskFile = new File("/Users/vzikratyi/Test/7.txt");
        try (Reader r = new FileReader(taskFile);
             BufferedReader br = new BufferedReader(r)) {

            int[] crabsPositions = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();

            Arrays.sort(crabsPositions);

            int totalPositions = 0;
            for (int crabPosition : crabsPositions) {
                totalPositions += crabPosition;
            }
            int averagePosition = (int) Math.round((double) totalPositions / crabsPositions.length);

            int fuel = 0;
            for (int crabPosition : crabsPositions) {
                fuel += calculateFuelCost(Math.abs(crabPosition - averagePosition));
            }
            System.out.println("Position - " + averagePosition + ", fuel - " + fuel +
                    ", avg double - " + (double) totalPositions / crabsPositions.length);
        }
    }

    private static int calculateFuelCost(int distance) {
        int fuel = 0;
        for (int i = 1; i <= distance; i++) {
            fuel += i;
        }
        return fuel;
    }

}
