import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Day6_InfiniteSpawningFish {
    public static void main(String[] args) throws Exception {
        File taskFile = new File("/Users/vzikratyi/Test/6.txt");
        try (Reader r = new FileReader(taskFile);
             BufferedReader br = new BufferedReader(r)) {

//            String[] lines = br.lines().toArray(String[]::new);

            int[] fishes = Arrays.stream(br.readLine().split(",")).mapToInt(Integer::parseInt).toArray();

            long startTs = System.nanoTime();
            long totalFish = fishSpawned(fishes, 256);
            long endTs = System.nanoTime();

            System.out.println(totalFish + ", time took - " + Duration.ofNanos(endTs - startTs).toMillis());
        }
    }

    private static final int TIME_TO_SPAWN = 7;
    private static final int INITIAL_TIME_TO_SPAWN = 9;

    private static long fishSpawned(int[] initialFishes, int days) {
        Map<Integer, Long> calculatedFishSpawned = createCalculatedFishSpawnedDictionary(days);

        long totalFish = 0;
        for (int daysToSpawn : initialFishes) {
            int daysAfterBirth = (days - daysToSpawn) + INITIAL_TIME_TO_SPAWN;
            totalFish += calculatedFishSpawned.get(daysAfterBirth) + 1;
        }

        return totalFish;
    }

    private static Map<Integer, Long> createCalculatedFishSpawnedDictionary(int days) {
        Map<Integer, Long> calculatedFishSpawned = new HashMap<>();
        for (int i = 0; i < days + INITIAL_TIME_TO_SPAWN; i++) {
            long fishSpawned = 0;
            for (int j = i - INITIAL_TIME_TO_SPAWN; j > 0; j -= TIME_TO_SPAWN) {
                fishSpawned += calculatedFishSpawned.get(j) + 1;
            }
            calculatedFishSpawned.put(i, fishSpawned);
        }
        return calculatedFishSpawned;
    }
}
