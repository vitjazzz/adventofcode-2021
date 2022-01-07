import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class Day16_Calculator {
    public static void main(String[] args) throws Exception {
        File taskFile = new File("/Users/vzikratyi/Test/16.txt");
        try (Reader r = new FileReader(taskFile);
             BufferedReader br = new BufferedReader(r)) {

            String hexLine = br.lines().findFirst().get();
            String binaryLine = new BigInteger("1" + hexLine, 16).toString(2).substring(1);

            Message message = new Message(binaryLine.toCharArray());
            MessageParser messageParser = new MessageParser();
            Expression expression = messageParser.parseMessage(message);
            System.out.println(expression.calculate());
        }
    }

    private static class Message {
        private final char[] bits;
        private int pointer = 0;

        private Message(char[] messageBits) {
            this.bits = messageBits;
        }

        public int getVersion() {
            int version = Integer.parseInt(String.copyValueOf(bits, pointer, 3), 2);
            pointer += 3;
            return version;
        }

        public int getTypeId() {
            int packetTypeInt = Integer.parseInt(String.copyValueOf(bits, pointer, 3), 2);
            pointer += 3;
            return packetTypeInt;
        }

        public char[] getBits(int size) {
            char[] returnBits = new char[size];
            System.arraycopy(bits, pointer, returnBits, 0, size);
            pointer += size;
            return returnBits;
        }

        public boolean isEmpty() {
            for (int i = pointer; i < bits.length; i++) {
                if (bits[i] != '0') {
                    return false;
                }
            }
            return true;
        }
    }

    private interface Expression {
        long calculate();

        int getVersionSum();

        int getPacketSize();
    }

    @Value
    private static class Operator implements Expression {
        List<Expression> operands = new ArrayList<>();
        OperatorLengthLeftCalculator lengthCalculator;
        int version;
        int type;

        @Override
        public long calculate() {
            switch (type) {
                case 0:
                    return operands.stream().mapToLong(Expression::calculate).sum();
                case 1:
                    return operands.stream().mapToLong(Expression::calculate).reduce(1, (prev, element) -> prev * element);
                case 2:
                    return operands.stream().mapToLong(Expression::calculate).min().orElseThrow();
                case 3:
                    return operands.stream().mapToLong(Expression::calculate).max().orElseThrow();
                case 5:
                    return operands.get(0).calculate() > operands.get(1).calculate() ? 1 : 0;
                case 6:
                    return operands.get(0).calculate() < operands.get(1).calculate() ? 1 : 0;
                case 7:
                    return operands.get(0).calculate() == operands.get(1).calculate() ? 1 : 0;
                default:
                    throw new IllegalStateException("Unknown operator type");
            }
        }

        @Override
        public int getVersionSum() {
            return version + operands.stream().mapToInt(Expression::getVersionSum).sum();
        }

        @Override
        public int getPacketSize() {
            return 7 + lengthCalculator.getLengthFieldLength() + operands.stream().mapToInt(Expression::getPacketSize).sum();
        }

        public void addOperand(Expression expression) {
            lengthCalculator.decreaseLengthLeft(expression);
            operands.add(expression);
        }

        public boolean isComplete() {
            return lengthCalculator.isComplete();
        }
    }

    @Value
    private static class Literal implements Expression {
        long value;
        int version;
        int packetSize;

        @Override
        public long calculate() {
            return value;
        }

        @Override
        public int getVersionSum() {
            return version;
        }

        @Override
        public int getPacketSize() {
            return packetSize;
        }
    }

    private static class MessageParser {
        public Expression parseMessage(Message message) {
            Queue<Expression> expressions = new LinkedList<>();
            while (!message.isEmpty()) {
                int version = message.getVersion();
                int typeId = message.getTypeId();
                Expression expression = typeId == 4 ? parseLiteral(version, message) : parseOperator(version, typeId, message);
                expressions.add(expression);
            }

            return buildFullExpression(expressions);
        }

        private Operator parseOperator(int version, int typeId, Message message) {
            char[] lengthBit = message.getBits(1);
            OperatorLengthLeftType lengthType = lengthBit[0] == '0'
                    ? OperatorLengthLeftType.LENGTH_IN_BITS
                    : OperatorLengthLeftType.LENGTH_IN_PACKETS;
            int length = lengthType == OperatorLengthLeftType.LENGTH_IN_BITS
                    ? Integer.parseInt(String.copyValueOf(message.getBits(15)), 2)
                    : Integer.parseInt(String.copyValueOf(message.getBits(11)), 2);
            OperatorLengthLeftCalculator lengthCalculator = new OperatorLengthLeftCalculator(lengthType, length);
            return new Operator(lengthCalculator, version, typeId);
        }

        private Literal parseLiteral(int version, Message message) {
            StringBuilder binaryValue = new StringBuilder();
            boolean isLastGroup;
            int payloadSize = 0;
            do {
                char[] group = message.getBits(5);
                isLastGroup = group[0] == '0';
                binaryValue.append(String.copyValueOf(group, 1, 4));
                payloadSize += group.length;

            } while (!isLastGroup);
            return new Literal(Long.parseLong(binaryValue.toString(), 2), version, payloadSize + 6);
        }

        private Expression buildFullExpression(Queue<Expression> expressions) {
            Expression firstExpression = expressions.poll();
            if (expressions.isEmpty()) {
                return firstExpression;
            }
            Operator lastOperator = null;
            Stack<Operator> operators = new Stack<>();
            operators.push((Operator) firstExpression);
            while (!expressions.isEmpty()) {
                Operator operator = operators.peek();
                Expression expression = expressions.poll();
                if (expression instanceof Operator) {
                    operators.push((Operator) expression);
                } else {
                    operator.addOperand(expression);
                    lastOperator = finishOperators(operators);
                }
            }
            return lastOperator;
        }

        private Operator finishOperators(Stack<Operator> operators) {
            while (operators.peek().isComplete()) {
                Operator lastOperator = operators.pop();
                if (operators.isEmpty()) {
                    return lastOperator;
                } else {
                    operators.peek().addOperand(lastOperator);
                }
            }
            return null;
        }
    }

    @AllArgsConstructor
    private static class OperatorLengthLeftCalculator {
        private final OperatorLengthLeftType type;
        private int lengthLeft;

        public void decreaseLengthLeft(Expression expression) {
            if (lengthLeft == 0) {
                throw new IllegalStateException("Length is already zero");
            }
            switch (type) {
                case LENGTH_IN_BITS:
                    lengthLeft -= expression.getPacketSize();
                    break;
                case LENGTH_IN_PACKETS:
                    lengthLeft -= 1;
                    break;
            }
        }

        public boolean isComplete() {
            return lengthLeft == 0;
        }

        public int getLengthFieldLength() {
            return type == OperatorLengthLeftType.LENGTH_IN_BITS ? 15 : 11;
        }
    }

    private enum OperatorLengthLeftType {
        LENGTH_IN_BITS, LENGTH_IN_PACKETS;
    }
}
