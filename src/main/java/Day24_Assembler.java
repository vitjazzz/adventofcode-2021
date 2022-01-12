import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Day24_Assembler {
    public static void main(String[] args) throws Exception {
        File taskFile = new File("/Users/vzikratyi/Test/24.txt");
        try (Reader r = new FileReader(taskFile);
             BufferedReader br = new BufferedReader(r)) {

            List<Command> commands = br.lines().map(Command::of).collect(Collectors.toList());
            int groupCounter = 1;

            Equation zEquation = Lit.ZERO;
            int lastValue = -1;
            int position = -1;
            for (Command command : commands) {
                if (Objects.equals(command.name, "inp")) {
                    groupCounter = 1;
                    position++;
                } else if (groupCounter++ == 15) {
                    zEquation = new Add(
                            new Mul(zEquation, new Lit(26)),
                            new Add(new Var("i[" + position + "]"), new Lit(Integer.parseInt(command.args[1])))
                    );
                }
            }
            System.out.println(zEquation);
        }
    }

    private static void madness(BufferedReader br) {
        List<Command> commands = br.lines().map(Command::of).collect(Collectors.toList());
        int inputSize = (int) commands.stream().map(Command::getName).filter(name -> name.equals("inp")).count();

        ArrayList<Command> reversedCommands = new ArrayList<>(commands);
        Collections.reverse(reversedCommands);

        Equation expectedEquation = new Eql(new Var("z"), Lit.ZERO);
        long start = System.currentTimeMillis();
        CommandConverter commandConverter = new CommandConverter(inputSize);
        for (Command command : reversedCommands) {
            expectedEquation = commandConverter.convert(expectedEquation, command);
        }
        long end = System.currentTimeMillis();
        System.out.println("Convertion took " + (end - start));
        expectedEquation = expectedEquation.replace("x", Lit.ZERO)
                .replace("y", Lit.ZERO)
                .replace("z", Lit.ZERO)
                .replace("w", Lit.ZERO)
                .simplify();


        Assembler assembler = new Assembler(new Input(new int[]{9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9}));
        for (Command command : commands) {
            assembler.handleCommand(command);
        }

        System.out.println(assembler.state);
    }

    @RequiredArgsConstructor
    private static class Assembler {
        private State state = new State();

        private final Input input;

        public void handleCommand(Command command) {
            switch (command.name) {
                case "inp":
                    state = state.write(command.args[0], input.read());
                    break;
                case "add":
                    state = state.write(command.args[0], state.read(command.args[0]) + getSecondArgValue(command.args[1]));
                    break;
                case "mul":
                    state = state.write(command.args[0], state.read(command.args[0]) * getSecondArgValue(command.args[1]));
                    break;
                case "div":
                    if (getSecondArgValue(command.args[1]) == 0) {
                        throw new IllegalStateException("Cannot divide by null");
                    }
                    state = state.write(command.args[0], state.read(command.args[0]) / getSecondArgValue(command.args[1]));
                    break;
                case "mod":
                    if (getSecondArgValue(command.args[1]) < 0) {
                        throw new IllegalStateException("Cannot mod by negative");
                    }
                    state = state.write(command.args[0], state.read(command.args[0]) % getSecondArgValue(command.args[1]));
                    break;
                case "eql":
                    state = state.write(command.args[0], (state.read(command.args[0]) == getSecondArgValue(command.args[1])) ? 1 : 0);
                    break;
            }
        }

        private int getSecondArgValue(String arg) {
            if (Util.isNumeric(arg)) {
                return Integer.parseInt(arg);
            } else {
                return state.read(arg);
            }
        }
    }

    @AllArgsConstructor
    private static class CommandConverter {
        int inputSize;
        public Equation convert(Equation equation, Command command) {
            equation = equation.simplify();
            switch (command.name) {
                case "inp":
                    return equation.replace(command.args[0], new Var("i[" + --inputSize + "]"));
                case "add":
                    return equation.replace(command.args[0], new Add(new Var(command.args[0]), getSecondArgEquation(command.args[1])));
                case "mul":
                    return equation.replace(command.args[0], new Mul(new Var(command.args[0]), getSecondArgEquation(command.args[1])));
                case "div":
                    return equation.replace(command.args[0], new Div(new Var(command.args[0]), getSecondArgEquation(command.args[1])));
                case "mod":
                    return equation.replace(command.args[0], new Mod(new Var(command.args[0]), getSecondArgEquation(command.args[1])));
                case "eql":
                    return equation.replace(command.args[0], new Eql(new Var(command.args[0]), getSecondArgEquation(command.args[1])));
                default:
                    throw new IllegalStateException();
            }
        }

        private Equation getSecondArgEquation(String arg) {
            if (Util.isNumeric(arg)) {
                return new Lit(Integer.parseInt(arg));
            } else {
                return new Var(arg);
            }
        }

    }

    private interface Equation {
        Equation replace(String var, Equation equation);
        boolean isZero();
        Equation simplify();
    }

    @Value
    private static class Var implements Equation {
        String var;

        @Override
        public String toString() { return var; }

        @Override
        public Equation replace(String var, Equation equation) {
            return this.var.equals(var) ? equation : this;
        }

        @Override
        public boolean isZero() {
            return false;
        }

        @Override
        public Equation simplify() {
            return this;
        }

        public boolean isInput() {
            return !(var.equals("x") || var.equals("y") || var.equals("z") || var.equals("w"));
        }
    }

    @Value
    private static class Lit implements Equation {
        public static Lit ZERO = new Lit(0);
        int val;

        @Override
        public String toString() {
            return String.valueOf(val);
        }

        @Override
        public Equation replace(String var, Equation equation) {
            return this;
        }

        @Override
        public boolean isZero() {
            return val == 0;
        }

        @Override
        public Equation simplify() {
            return this;
        }

        public boolean couldBeInput() {
            return val > 0 && val < 10;
        }
    }

    @Value
    private static class Add implements Equation {
        Equation first;
        Equation second;

        @Override
        public String toString() {
            return "(" + first + " + " + second + ")";
        }

        @Override
        public Equation replace(String var, Equation equation) {
            return new Add(first.replace(var, equation), second.replace(var, equation));
        }

        @Override
        public boolean isZero() {
            return false;
        }

        @Override
        public Equation simplify() {
            Add simplified = new Add(first.simplify(), second.simplify());
            if (simplified.first.isZero() && simplified.second.isZero()) { return Lit.ZERO; };
            if (simplified.first.isZero()) { return simplified.second; };
            if (simplified.second.isZero()) { return simplified.first; };
            if (simplified.first instanceof Lit
                    && simplified.second instanceof Lit) {
                int result = ((Lit) simplified.first).val + ((Lit) simplified.second).val;
                return new Lit(result);
            }
            return simplified;
        }
    }

    @Value
    private static class Mul implements Equation {
        Equation first;
        Equation second;

        @Override
        public String toString() {
            return "(" + first + " * " + second + ")";
        }

        @Override
        public Equation replace(String var, Equation equation) {
            return new Mul(first.replace(var, equation), second.replace(var, equation));
        }

        @Override
        public boolean isZero() {
            return first.isZero() || second.isZero();
        }

        @Override
        public Equation simplify() {
            Mul simplified = new Mul(first.simplify(), second.simplify());
            if (simplified.first.isZero() || simplified.second.isZero()) { return Lit.ZERO; };
            if (simplified.first instanceof Lit
                    && simplified.second instanceof Lit) {
                int result = ((Lit) simplified.first).val * ((Lit) simplified.second).val;
                return new Lit(result);
            }
            return simplified;
        }
    }

    @Value
    private static class Div implements Equation {
        Equation first;
        Equation second;

        @Override
        public String toString() {
            return "(" + first + " / " + second + ")";
        }

        @Override
        public Equation replace(String var, Equation equation) {
            return new Div(first.replace(var, equation), second.replace(var, equation));
        }

        @Override
        public boolean isZero() {
            return first.isZero();
        }

        @Override
        public Equation simplify() {
            Div simplified = new Div(first.simplify(), second.simplify());
            if (simplified.first.isZero()) {return Lit.ZERO;}
            if (simplified.second instanceof Lit
                    && ((Lit) simplified.second).val == 1) {return simplified.first;}
            if (simplified.first instanceof Lit
                    && simplified.second instanceof Lit) {
                int result = ((Lit) simplified.first).val / ((Lit) simplified.second).val;
                return new Lit(result);
            }
            return simplified;
        }
    }

    @Value
    private static class Mod implements Equation {
        Equation first;
        Equation second;

        @Override
        public String toString() {
            return "(" + first + " % " + second + ")";
        }

        @Override
        public Equation replace(String var, Equation equation) {
            return new Mod(first.replace(var, equation), second.replace(var, equation));
        }

        @Override
        public boolean isZero() {
            return false;
        }

        @Override
        public Equation simplify() {
            Mod simplified = new Mod(first.simplify(), second.simplify());
            if (simplified.first.isZero()) {return Lit.ZERO;}
            if (simplified.first instanceof Lit
                    && simplified.second instanceof Lit) {
                int result = ((Lit) simplified.first).val % ((Lit) simplified.second).val;
                return new Lit(result);
            }
            return simplified;
        }
    }

    @Value
    private static class Eql implements Equation {
        Equation first;
        Equation second;

        @Override
        public String toString() {
            return "(" + first + " == " + second + " ? 1 : 0)";
        }

        @Override
        public Equation replace(String var, Equation equation) {
            return new Eql(first.replace(var, equation), second.replace(var, equation));
        }

        @Override
        public boolean isZero() {
            return false;
        }

        @Override
        public Equation simplify() {
            Eql simplified = new Eql(first.simplify(), second.simplify());
            if (simplified.first.isZero() && simplified.second.isZero()) {return new Lit(1);}
            if (simplified.first instanceof Var
                    && ((Var) simplified.first).isInput()
                    && simplified.second instanceof Lit
                    && !((Lit) simplified.second).couldBeInput()) {
                return Lit.ZERO;
            }
            if (simplified.second instanceof Var
                    && ((Var) simplified.second).isInput()
                    && simplified.first instanceof Lit
                    && !((Lit) simplified.first).couldBeInput()) {
                return Lit.ZERO;
            }
            if (simplified.first instanceof Lit
                    && simplified.second instanceof Lit) {
                return simplified.first.equals(simplified.second) ? new Lit(1) : Lit.ZERO;
            }
            return simplified;
        }
    }

    @Value
    @AllArgsConstructor
    private static class State {
        Map<String, Integer> registries;

        public State() {
            registries = Map.of("x", 0, "y", 0, "z", 0, "w", 0);
        }

        public Integer read(String registry) {
            return registries.get(registry);
        }

        public State write(String registry, int value) {
            Map<String, Integer> newRegistries = new HashMap<>(registries);
            newRegistries.put(registry, value);
            return new State(newRegistries);
        }

        public boolean isValid() {
            return registries.get("z") == 0;
        }
    }

    @Value
    private static class Command {
        String name;
        String[] args;

        public static Command of(String commandStr) {
            String name = commandStr.substring(0, commandStr.indexOf(" "));
            String argsStr = commandStr.substring(name.length() + 1);
            return new Command(name, argsStr.split(" "));
        }
    }

    @RequiredArgsConstructor
    private static class Input {
        final int[] data;
        int position;

        public int read() {
            return data[position++];
        }
    }

    private static class Util {
        private static final Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

        public static boolean isNumeric(String strNum) {
            if (strNum == null) {
                return false;
            }
            return pattern.matcher(strNum).matches();
        }
    }
}
