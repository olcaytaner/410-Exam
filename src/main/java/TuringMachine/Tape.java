package TuringMachine;

import java.util.ArrayList;
import java.util.List;

public class Tape {
    private final List<Character> tape;
    private int headPosition;
    private static final char BLANK = '_';

    public Tape() {
        tape = new ArrayList<>();
        headPosition = 0;
    }

    public void initialize(String input) {
        tape.clear();
        for (char ch : input.toCharArray()) {
            tape.add(ch);
        }
        headPosition = 0;
    }

    public char read() {
        ensureWithinBounds();
        return tape.get(headPosition);
    }

    public void write(char symbol) {
        ensureWithinBounds();
        tape.set(headPosition, symbol);
    }

    public void move(Direction direction) {
        switch (direction) {
            case LEFT:
                headPosition--;
                break;
            case RIGHT:
                headPosition++;
                break;
            default:
                throw new IllegalArgumentException("Invalid direction: " + direction);
        }
    }

    private void ensureWithinBounds() {
        if (headPosition < 0) {
            tape.add(0, BLANK);
            headPosition = 0;
        } else if (headPosition >= tape.size()) {
            tape.add(BLANK);
        }
    }

    public void clear() {
        tape.clear();
        headPosition = 0;
    }

    public void printTape() {
        for (int i = 0; i < tape.size(); i++) {
            if (i == headPosition) System.out.print("[");
            System.out.print(tape.get(i));
            if (i == headPosition) System.out.print("]");
        }
        System.out.println();
    }
}
