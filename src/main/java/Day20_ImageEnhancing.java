import lombok.Value;

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
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day20_ImageEnhancing {
    public static void main(String[] args) throws Exception {
        File taskFile = new File("/Users/vzikratyi/Test/20.txt");
        try (Reader r = new FileReader(taskFile);
             BufferedReader br = new BufferedReader(r)) {

            List<String> lines = br.lines().collect(Collectors.toList());

            EnhancementAlgorithm enhancementAlgorithm = EnhancementAlgorithm.of(lines.get(0));

            ImageGrid image = ImageGrid.of(lines.subList(2, lines.size()), enhancementAlgorithm);

            for (int i = 0; i < 50; i++) {
                image = image.enhance();
            }
            System.out.println();
            image.print();
            System.out.println(image.lightPixels());
        }
    }

    @Value
    private static class ImageGrid {
        boolean isLightEdge;
        boolean[][] grid;
        EnhancementAlgorithm algorithm;

        public static ImageGrid of(List<String> gridLines, EnhancementAlgorithm algorithm) {
            boolean[][] grid = new boolean[gridLines.size()][gridLines.get(0).length()];
            for (int i = 0; i < gridLines.size(); i++) {
                for (int j = 0; j < gridLines.get(0).length(); j++) {
                    grid[i][j] = gridLines.get(i).charAt(j) == '#';
                }
            }
            return new ImageGrid(false, grid, algorithm);
        }

        public ImageGrid enhance() {
            boolean[][] newGrid = new boolean[grid.length + 2][grid[0].length + 2];
            for (int i = 0; i < newGrid.length; i++) {
                for (int j = 0; j < newGrid[0].length; j++) {
                    newGrid[i][j] = calculateEnhancedPixel(i - 1, j - 1);
                }
            }
            boolean isNewImageLightEdge = isLightEdge ? algorithm.isLight(511) : algorithm.isLight(0);
            return new ImageGrid(isNewImageLightEdge, newGrid, algorithm);
        }

        private boolean calculateEnhancedPixel(int i, int j) {
            StringBuilder binaryString = new StringBuilder();
            for (int newI = i - 1; newI <= i + 1; newI++) {
                for (int newJ = j - 1; newJ <= j + 1; newJ++) {
                    binaryString.append(isLight(newI, newJ) ? "1" : "0");
                }
            }
            int position = Integer.parseInt(binaryString.toString(), 2);
            return algorithm.isLight(position);
        }

        private boolean isLight(int i, int j) {
            return isOutsideGrid(i, j) ? isLightEdge : grid[i][j];
        }

        private boolean isOutsideGrid(int i, int j) {
            return i < 0 || i >= grid.length || j < 0 || j >= grid[0].length;
        }

        public int lightPixels() {
            if (isLightEdge) {
                throw new IllegalStateException("It's all light");
            }
            int lightPixels = 0;
            for (int i = 0; i < grid.length; i++) {
                for (int j = 0; j < grid[0].length; j++) {
                    if (grid[i][j]) {
                        lightPixels++;
                    }
                }
            }
            return lightPixels;
        }

        public void print() {
            for (int i = 0; i < grid.length; i++) {
                for (int j = 0; j < grid[0].length; j++) {
                    System.out.print(grid[i][j] ? "#" : ".");
                }
                System.out.println();
            }
        }
    }

    @Value
    private static class EnhancementAlgorithm {
        boolean[] lightConversions;
        
        public static EnhancementAlgorithm of(String enhancementAlgorithmStr) {
            boolean[] lightConversions = new boolean[512];
            String[] lightConversionsStr = enhancementAlgorithmStr.split("");
            for (int i = 0; i < lightConversionsStr.length; i++) {
                lightConversions[i] = lightConversionsStr[i].equals("#");
            }
            return new EnhancementAlgorithm(lightConversions);
        }

        public boolean isLight(int i) {
            return lightConversions[i];
        }
    }
}
