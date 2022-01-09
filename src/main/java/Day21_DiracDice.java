import lombok.EqualsAndHashCode;
import lombok.Value;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

public class Day21_DiracDice {
    private static final int WINNING_SCORE = 21;

    public static void main(String[] args) throws Exception {
        File taskFile = new File("/Users/vzikratyi/Test/21.txt");
        try (Reader r = new FileReader(taskFile);
             BufferedReader br = new BufferedReader(r)) {

            List<String> lines = br.lines().collect(Collectors.toList());

            Play play = new Play(
                    new PlayersState(PlayerState.of(lines.get(0)), PlayerState.of(lines.get(1)), true)
            );

            System.out.println(play.winnerWinsCount());
        }
    }

    private static class Play {
        final Track track = new Track();
        final QuantumDie die = new QuantumDie();
        final Map<PlayersState, WinningStat> winningStatDictionary = new HashMap<>();

        WinningStat winningStat;

        final PlayersState initialState;

        public Play(PlayersState initialState) {
            this.initialState = initialState;
            calculateWinningStatDictionary();
            this.winningStat = winningStatDictionary.get(initialState);
        }

        private void calculateWinningStatDictionary() {
            for (int activePlayerScore = WINNING_SCORE - 1; activePlayerScore >= 0; activePlayerScore--) {
                for (int activePlayerPosition = 0; activePlayerPosition < track.positions.length; activePlayerPosition++) {
                    for (int passivePlayerScore = WINNING_SCORE - 1; passivePlayerScore >= 0; passivePlayerScore--) {
                        for (int passivePlayerPosition = 0; passivePlayerPosition < track.positions.length; passivePlayerPosition++) {
                            PlayersState activeFirstPlayer = new PlayersState(
                                    new PlayerState(track, activePlayerPosition, activePlayerScore),
                                    new PlayerState(track, passivePlayerPosition, passivePlayerScore),
                                    true
                            );
                            WinningStat activeFirstPlayerWinningStat = calculateWinningStat(activeFirstPlayer);
                            winningStatDictionary.put(activeFirstPlayer, activeFirstPlayerWinningStat);
                            PlayersState activeSecondPlayer = new PlayersState(
                                    new PlayerState(track, passivePlayerPosition, passivePlayerScore),
                                    new PlayerState(track, activePlayerPosition, activePlayerScore),
                                    false
                            );
                            WinningStat activeSecondPlayerWinningStat = calculateWinningStat(activeSecondPlayer);
                            winningStatDictionary.put(activeSecondPlayer, activeSecondPlayerWinningStat);
                        }
                    }
                }
            }
        }

        private WinningStat calculateWinningStat(PlayersState playerState) {
            WinningStat winningStat = new WinningStat(0, 0);
            for (Integer possible3RollValue : die.possible3RollValues) {
                PlayersState newPlayerState = playerState.progress(possible3RollValue);
                if (newPlayerState.hasWinner()) {
                    winningStat = winningStat.add(new WinningStat(newPlayerState.first.won() ? 1 : 0, newPlayerState.second.won() ? 1 : 0));
                } else {
                    WinningStat storedWinningStat = winningStatDictionary.get(newPlayerState);
                    if (storedWinningStat == null) {
                        storedWinningStat = calculateWinningStat(newPlayerState);
                    }
                    winningStat = winningStat.add(storedWinningStat);
                }
            }
            return winningStat;
        }

        public long winnerWinsCount() {
            return Math.max(winningStat.firstWon, winningStat.secondWon);
        }
    }

    @Value
    private static class WinningStat {
        long firstWon;
        long secondWon;

        public WinningStat add(WinningStat winningStat) {
            return new WinningStat(firstWon + winningStat.firstWon, secondWon + winningStat.secondWon);
        }
    }

    @Value
    private static class Track {
        int[] positions = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        int move(int startPosition, int steps) {
            return (startPosition + steps) % positions.length;
        }

        int score(int position) {
            return positions[position];
        }
    }

    @Value
    private static class PlayersState {
        PlayerState first;
        PlayerState second;
        boolean isFirstNextTurn;

        public PlayersState progress(int steps) {
            if (isFirstNextTurn) {
                return new PlayersState(first.progress(steps), second, false);
            } else {
                return new PlayersState(first, second.progress(steps), true);
            }
        }

        public boolean hasWinner() {
            return first.won() || second.won();
        }
    }

    @Value
    @EqualsAndHashCode(exclude = "track")
    private static class PlayerState {
        public static PlayerState of(String stateStr) {
            int id = Integer.parseInt(stateStr.substring("Player ".length(), stateStr.indexOf(" starting position")));
            int startPosition = Integer.parseInt(stateStr.substring(stateStr.indexOf("starting position: ") + "starting position: ".length())) - 1;
            return new PlayerState(new Track(), startPosition, 0);
        }

        Track track;
        int position;
        int score;

        public PlayerState progress(int steps) {
            int newPosition = track.move(position, steps);
            return new PlayerState(track, newPosition, score + track.score(newPosition));
        }

        public boolean won() {
            return score >= WINNING_SCORE;
        }

        @Override
        public String toString() {
            return "{" +
                    "pos=" + position +
                    ", score=" + score +
                    '}';
        }
    }

    private static class QuantumDie {
        private final int[] possibleValues = new int[]{1, 2, 3};

        private final List<Integer> possible3RollValues;

        public QuantumDie() {
            List<Integer> dieResults = new ArrayList<>();
            for (int i = 0; i < possibleValues.length; i++) {
                for (int j = 0; j < possibleValues.length; j++) {
                    for (int k = 0; k < possibleValues.length; k++) {
                        int dieResult = possibleValues[i] + possibleValues[j] + possibleValues[k];
                        dieResults.add(dieResult);
                    }
                }
            }
            possible3RollValues = dieResults;
        }
    }

    private static class Die {
        int timesRolled = 0;
        int nextValue = 1;

        int roll() {
            timesRolled++;
            int value = nextValue;
            nextValue = value == 100 ? 1 : value + 1;
            return value;
        }

        int timesRolled() {
            return timesRolled;
        }
    }
}
