import lombok.Value;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public class Day25_MovingCucumbers {
    public static void main(String[] args) throws Exception {
        File taskFile = new File("/Users/vzikratyi/Test/25.txt");
        try (Reader r = new FileReader(taskFile);
             BufferedReader br = new BufferedReader(r)) {

            String[] lines = br.lines().toArray(String[]::new);

            char[][] board = new char[lines.length][lines.length];
            for (int i = 0; i < lines.length; i++) {
                char[] row = lines[i].toCharArray();
                board[i] = row;
            }

            CucumberBoard cucumberBoard = new CucumberBoard(board);

            cucumberBoard.waitTillStop();
            cucumberBoard.print();
            System.out.println(cucumberBoard.getStep());
        }
    }

    private static class CucumberBoard {
        private final Cucumber[][] board;
        private final boolean[][] moveBoard;

        private int step;

        private CucumberBoard(char[][] inputBoard) {
            this.board = new Cucumber[inputBoard.length][inputBoard[0].length];
            this.moveBoard = new boolean[inputBoard.length][inputBoard[0].length];
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[0].length; j++) {
                    this.board[i][j] = Cucumber.of(inputBoard[i][j]);
                }
            }
        }

        public int getStep() {
            return step;
        }

        public void waitTillStop() {
            boolean anyCucumberMoved;
            do {
                anyCucumberMoved = false;
                for (int i = 0; i < board.length; i++) {
                    for (int j = 0; j < board[0].length; j++) {
                        Cucumber cucumber = this.board[i][j];
                        if (cucumber == null) continue;
                        if (cucumber.direction == Direction.RIGHT) {
                            moveBoard[i][j] = canMoveRight(i, j);
                        }
                    }
                }
                for (int i = 0; i < board.length; i++) {
                    for (int j = 0; j < board[0].length; j++) {
                        if (moveBoard[i][j]) {
                            moveRight(i, j);
                            moveBoard[i][j] = false;
                            anyCucumberMoved = true;
                        }
                    }
                }
                for (int j = 0; j < board[0].length; j++) {
                    for (int i = 0; i < board.length; i++) {
                        Cucumber cucumber = this.board[i][j];
                        if (cucumber == null) continue;
                        if (cucumber.direction == Direction.DOWN) {
                            moveBoard[i][j] = canMoveDown(i, j);
                        }
                    }
                }
                for (int i = 0; i < board.length; i++) {
                    for (int j = 0; j < board[0].length; j++) {
                        if (moveBoard[i][j]) {
                            moveDown(i, j);
                            moveBoard[i][j] = false;
                            anyCucumberMoved = true;
                        }
                    }
                }
                step++;
                if (step <= 5 || step % 10 == 0 || step >= 55) {
//                    this.print();
                }
            } while (anyCucumberMoved);

        }

        private boolean canMoveRight(int i, int j) {
            int nextJ = j == board[0].length - 1 ? 0 : j + 1;
            return board[i][nextJ] == null;
        }

        private void moveRight(int i, int j) {
            int nextJ = j == board[0].length - 1 ? 0 : j + 1;
            move(i, j, i, nextJ);
        }

        private boolean canMoveDown(int i, int j) {
            int nextI = i == board.length - 1 ? 0 : i + 1;
            return board[nextI][j] == null;
        }

        private void moveDown(int i, int j) {
            int nextI = i == board.length - 1 ? 0 : i + 1;
            move(i, j, nextI, j);
        }

        private void move(int fromI, int fromJ, int toI, int toJ) {
            board[toI][toJ] = board[fromI][fromJ];
            board[fromI][fromJ] = null;
        }

        public void print() {
            System.out.println();
            System.out.println("After "+step+" step:");
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[0].length; j++) {
                    char c = board[i][j] == null ? '.' : board[i][j].direction.representation;
                    System.out.print(c);
                }
                System.out.println();
            }
        }
    }

    @Value
    private static class Cucumber {
        Direction direction;

        public static Cucumber of(char c) {
            switch (c) {
                case '>':
                    return new Cucumber(Direction.RIGHT);
                case 'v':
                    return new Cucumber(Direction.DOWN);
                default:
                    return null;
            }
        }
    }

    private enum Direction {
        RIGHT('>'), DOWN('v');

        private char representation;

        Direction(char representation) {
            this.representation = representation;
        }
    }
}
