import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public class LuminescentOctopus {
    public static void main(String[] args) throws Exception {
        File taskFile = new File("/Users/vzikratyi/Test/11.txt");
        try (Reader r = new FileReader(taskFile);
             BufferedReader br = new BufferedReader(r)) {

            String[] lines = br.lines().toArray(String[]::new);

            int[][] board = new int[lines.length][lines.length];
            for (int i = 0; i < lines.length; i++) {
                int[] row = Arrays.stream(lines[i].split("")).mapToInt(Integer::parseInt).toArray();
                board[i] = row;
            }

            OctopusBoard octopusBoard = new OctopusBoard(board);

            System.out.println(octopusBoard.findFirstTotalFlash());
        }
    }

    private static class OctopusBoard {
        private final Octopus[][] board;

        private OctopusBoard(int[][] inputBoard) {
            this.board = new Octopus[inputBoard.length][inputBoard[0].length];
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[0].length; j++) {
                    this.board[i][j] = new Octopus(inputBoard[i][j], i, j);
                }
            }
        }

        public int calculateTotalFlashes(int steps) {
            int totalFlashes = 0;
            for (int i = 0; i < steps; i++) {
                totalFlashes += simulateStep();
            }
            return totalFlashes;
        }

        public int findFirstTotalFlash() {
            for (int i = 1;; i++) {
                int flashCount = simulateStep();
                if (flashCount == board.length * board[0].length) {
                    return i;
                }
            }
        }

        private int simulateStep() {
            Stack<Octopus> flashedOctopus = new Stack<>();
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[0].length; j++) {
                    board[i][j].flashedThisTurn = false;
                    int newEnergyLevel = board[i][j].energyLevel.incrementAndGet();
                    if (newEnergyLevel == 10) {
                        flashedOctopus.push(board[i][j]);
                    }
                }
            }

            int totalFlashes = 0;
            while (!flashedOctopus.isEmpty()) {
                Octopus octopus = flashedOctopus.pop();
                totalFlashes++;
                octopus.energyLevel.set(0);
                octopus.flashedThisTurn = true;
                for (int i = octopus.i - 1; i <= octopus.i + 1; i++) {
                    for (int j = octopus.j - 1; j <= octopus.j + 1; j++) {
                        if (!isValidPoint(i, j)) {
                            continue;
                        }
                        Octopus surroundingOctopus = board[i][j];
                        if (surroundingOctopus.flashedThisTurn) {
                            continue;
                        }
                        int newEnergyLevel = surroundingOctopus.energyLevel.incrementAndGet();
                        if (newEnergyLevel == 10) {
                            flashedOctopus.push(surroundingOctopus);
                        }
                    }
                }
            }

            return totalFlashes;
        }

        private boolean isValidPoint(int i, int j) {
            return i >= 0 && i < board.length && j >= 0 && j < board[0].length;
        }
    }

    private static class Octopus {
        private boolean flashedThisTurn = false;
        private AtomicInteger energyLevel;
        private int i;
        private int j;

        public Octopus(int energyLevel, int i, int j) {
            this.energyLevel = new AtomicInteger(energyLevel);
            this.i = i;
            this.j = j;
        }
    }
}
