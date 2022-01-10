import lombok.Value;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

public class Day23_AmphipodMoving {
    private static final int ROOM_SIZE = 4;

    public static void main(String[] args) throws Exception {
        File taskFile = new File("/Users/vzikratyi/Test/23.txt");
        try (Reader r = new FileReader(taskFile);
             BufferedReader br = new BufferedReader(r)) {

            List<String> lines = br.lines().collect(Collectors.toList());
            MoveState initialState = initializeState(lines);
            Stack<MoveState> possibleStates = new Stack<>();
            possibleStates.push(initialState);
            int lowestEnergyCost = Integer.MAX_VALUE;
            while (!possibleStates.isEmpty()) {
                MoveState currentState = possibleStates.pop();
                if (currentState.energyCost >= lowestEnergyCost) {
                    continue;
                }
                if (currentState.isFinished()) {
                    lowestEnergyCost = currentState.energyCost;
                    continue;
                }

                MoveState goToRoomState = tryMoveToRoom(currentState);
                if (goToRoomState != null) {
                    possibleStates.push(goToRoomState);
                    continue;
                }
                List<MoveState> newPossibleStates = calculateNewPossibleStates(currentState);
                for (MoveState newPossibleState : newPossibleStates) {
                    possibleStates.push(newPossibleState);
                }
            }


            System.out.println(lowestEnergyCost);
        }
    }

    private static List<MoveState> calculateNewPossibleStates(MoveState currentState) {
        List<MoveState> possibleStates = new ArrayList<>();
        Burrow burrow = currentState.burrow;
        List<Room> rooms = burrow.rooms;
        Hallway hallway = burrow.hallway;
        for (int roomId = 0; roomId < rooms.size(); roomId++) {
            Room room = rooms.get(roomId);
            if (!room.canLeave(roomId)) {
                continue;
            }
            Amphipod amphipod = room.closestToExit();
            List<Integer> possiblePositions = hallway.possibleLocations(Hallway.roomsPositions[roomId]);
            for (Integer position : possiblePositions) {
                int steps = Math.abs(Hallway.roomsPositions[roomId] - position)
                        + room.stepsToLeave();
                Room newRoom = room.leave();
                ArrayList<Room> newRooms = new ArrayList<>(rooms);
                newRooms.set(roomId, newRoom);
                Hallway newHallway = hallway.moveTo(position, amphipod);
                int newEnergyCost = currentState.energyCost + steps * amphipod.energyCost;
                possibleStates.add(new MoveState(newEnergyCost, new Burrow(newHallway, newRooms)));
            }
        }
        return possibleStates;
    }

    private static MoveState tryMoveToRoom(MoveState currentState) {
        Burrow burrow = currentState.burrow;
        List<Room> rooms = burrow.rooms;
        Hallway hallway = burrow.hallway;
        for (Map.Entry<Integer, Amphipod> hallwayEntry : hallway.amphipods.entrySet()) {
            Amphipod amphipod = hallwayEntry.getValue();
            Room room = rooms.get(amphipod.targetRoom);
            if (room.canEnter(amphipod)
                    && hallway.canMove(hallwayEntry.getKey(), Hallway.roomsPositions[amphipod.targetRoom])) {
                int steps = Math.abs(hallwayEntry.getKey() - Hallway.roomsPositions[amphipod.targetRoom])
                        + room.stepsToEnter();
                Room newRoom = room.enter(amphipod);
                ArrayList<Room> newRooms = new ArrayList<>(rooms);
                newRooms.set(amphipod.targetRoom, newRoom);
                Hallway newHallway = hallway.remove(hallwayEntry.getKey());
                int newEnergyCost = currentState.energyCost + steps * amphipod.energyCost;
                return new MoveState(newEnergyCost, new Burrow(newHallway, newRooms));
            }
        }
        for (int roomId = 0; roomId < rooms.size(); roomId++) {
            Room initialRoom = rooms.get(roomId);
            if (!initialRoom.canLeave(roomId)) {
                continue;
            }
            Amphipod amphipod = initialRoom.closestToExit();
            Room targetRoom = rooms.get(amphipod.targetRoom);
            if (!initialRoom.equals(targetRoom)
                    && targetRoom.canEnter(amphipod)
                    && hallway.canMove(Hallway.roomsPositions[roomId], Hallway.roomsPositions[amphipod.targetRoom])) {
                int steps = Math.abs(Hallway.roomsPositions[roomId] - Hallway.roomsPositions[amphipod.targetRoom])
                        + initialRoom.stepsToLeave()
                        + targetRoom.stepsToEnter();
                Room newInitialRoom = initialRoom.leave();
                Room newTargetRoom = targetRoom.enter(amphipod);
                ArrayList<Room> newRooms = new ArrayList<>(rooms);
                newRooms.set(amphipod.targetRoom, newTargetRoom);
                newRooms.set(roomId, newInitialRoom);
                int newEnergyCost = currentState.energyCost + steps * amphipod.energyCost;
                return new MoveState(newEnergyCost, new Burrow(hallway, newRooms));
            }
        }
        return null;
    }

    private static MoveState initializeState(List<String> lines) {
        int startLine = lines.size() - 2;
        Map<Integer, Stack<Amphipod>> roomStates = new TreeMap<>();
        for (int i = startLine; i >= startLine - 4; i--) {
            String line = lines.get(i);
            int currentRoomId = 0;
            String[] chars = line.split("#");
            for (String typeLetter : chars) {
                Amphipod amphipod = Amphipod.of(typeLetter);
                if (amphipod != null) {
                    roomStates.computeIfAbsent(currentRoomId++, ignored -> new Stack<>()).push(amphipod);
                }
            }
        }

        List<Room> rooms = roomStates.values().stream().map(Room::new).collect(Collectors.toList());
        return new MoveState(0, new Burrow(new Hallway(Map.of()), rooms));
    }

    @Value
    private static class MoveState {
        int energyCost;
        Burrow burrow;

        public boolean isFinished() {
            return burrow.isFinished();
        }
    }

    @Value
    private static class Burrow {
        Hallway hallway;
        List<Room> rooms;

        public boolean isFinished() {
            if (!hallway.amphipods.isEmpty()) {
                return false;
            }
            for (int roomId = 0; roomId < rooms.size(); roomId++) {
                Room room = rooms.get(roomId);
                for (Amphipod amphipod : room.amphipods) {
                    if (amphipod.targetRoom != roomId) {
                        return false;
                    }
                }
            }
            return true;
        }

    }

    @Value
    private static class Hallway {
        private static int[] roomsPositions = new int[]{2, 4, 6, 8};
        private static Set<Integer> roomsPositionsSet = Set.of(2, 4, 6, 8);

        Map<Integer, Amphipod> amphipods;

        boolean canMove(int from, int to) {
            return amphipods.keySet().stream()
                    .filter(position -> position >= Math.min(from, to) && position <= Math.max(from, to))
                    .filter(position -> position != from)
                    .findFirst()
                    .isEmpty();
        }

        public Hallway moveTo(int position, Amphipod amphipod) {
            Map<Integer, Amphipod> newAmphipods = new TreeMap<>(amphipods);
            newAmphipods.put(position, amphipod);
            return new Hallway(newAmphipods);
        }

        public Hallway remove(int position) {
            Map<Integer, Amphipod> newAmphipods = new TreeMap<>(amphipods);
            newAmphipods.remove(position);
            return new Hallway(newAmphipods);
        }

        public List<Integer> possibleLocations(int from) {
            int firstLeftPosition = -1;
            int firstRightPosition = 11;
            for (int position = 0; position < 11; position++) {
                if (amphipods.containsKey(position)) {
                    if (position < from) {
                        firstLeftPosition = position;
                    } else if (firstRightPosition == 11) {
                        firstRightPosition = position;
                    }
                }
            }
            List<Integer> possiblePositions = new ArrayList<>();
            for (int position = firstLeftPosition + 1; position <= firstRightPosition - 1; position++) {
                if (!roomsPositionsSet.contains(position)) {
                    possiblePositions.add(position);
                }
            }
            return possiblePositions;
        }
    }

    @Value
    private static class Room {
        Stack<Amphipod> amphipods;

        public boolean canEnter(Amphipod amphipod) {
            return amphipods.stream().allMatch(type -> type == amphipod);
        }

        public int stepsToEnter() {
            return ROOM_SIZE - amphipods.size();
        }

        public Room enter(Amphipod amphipod) {
            Stack<Amphipod> newAmphipods = new Stack<>();
            newAmphipods.addAll(amphipods);
            newAmphipods.push(amphipod);
            return new Room(newAmphipods);
        }

        public boolean canLeave(int currentRoomId) {
            if (amphipods.isEmpty()) {
                return false;
            }
            return !amphipods.stream().allMatch(amphipod -> amphipod.targetRoom == currentRoomId);
        }

        public Amphipod closestToExit() {
            return amphipods.peek();
        }

        public int stepsToLeave() {
            return (ROOM_SIZE - amphipods.size()) + 1;
        }

        public Room leave() {
            Stack<Amphipod> newAmphipods = new Stack<>();
            newAmphipods.addAll(amphipods);
            newAmphipods.pop();
            return new Room(newAmphipods);
        }
    }

    private enum Amphipod {
        AMBER(1, 0), BRONZE(10, 1), COPPER(100, 2), DESERT(1000, 3);

        private final int energyCost;
        private final int targetRoom;

        Amphipod(int energyCost, int targetRoom) {
            this.energyCost = energyCost;
            this.targetRoom = targetRoom;
        }

        public static Amphipod of(String type) {
            switch (type) {
                case "A":
                    return AMBER;
                case "B":
                    return BRONZE;
                case "C":
                    return COPPER;
                case "D":
                    return DESERT;
                default:
                    return null;
            }
        }
    }

    private static class Printer {
        public void print(MoveState state) {
            System.out.println("\nEnergy cost - " + state.energyCost);
            Burrow burrow = state.burrow;
            printBurrow(burrow);

        }

        private void printBurrow(Burrow burrow) {
            List<ArrayList<Amphipod>> roomsLists = mapToRoomLists(burrow);
            System.out.println("#############");
            printHallway(burrow.hallway);
            printRooms(roomsLists);
            System.out.println();
        }

        private void printRooms(List<ArrayList<Amphipod>> roomsLists) {
            for (int i = 0; i < ROOM_SIZE + 1; i++) {
                if (i == ROOM_SIZE) {
                    System.out.print("  #########");
                    continue;
                }
                if (i == 0) {
                    System.out.print("###");
                } else {
                    System.out.print("  #");
                }
                for (ArrayList<Amphipod> roomsList : roomsLists) {
                    Amphipod amphipod = roomsList.get(i);
                    System.out.print(amphipod == null ? "." : amphipod.toString().substring(0, 1));
                    System.out.print("#");
                }
                if (i == 0) {
                    System.out.print("##");
                }
                System.out.println();
            }
        }

        private void printHallway(Hallway hallway) {
            System.out.print("#");
            for (int i = 0; i < 11; i++) {
                Amphipod amphipod = hallway.amphipods.get(i);
                System.out.print(amphipod == null ? "." : amphipod.toString().substring(0, 1));
            }
            System.out.print("#");
            System.out.println();
        }

        private List<ArrayList<Amphipod>> mapToRoomLists(Burrow burrow) {
            return burrow.rooms.stream().map(room -> room.amphipods)
                    .map(ArrayList::new)
                    .peek(Collections::reverse)
                    .peek(list -> {
                        while (list.size() != ROOM_SIZE) {
                            list.add(0, null);
                        }
                    })
                    .collect(Collectors.toList());
        }
    }
}
