package NondeterministicFiniteAutomaton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Symbol {

    private char c;

    public Symbol(char c) {
        this.c = c;
    }

    public char getC() {
        return c;
    }

    public List<Warning> validate(){
        List<Warning> warnings = new ArrayList<>();

        if (!(isEpsilon() || Character.isLetter(getC()))){
            warnings.add(new Warning("Symbol is not valid: " + getC(),  Warning.ERROR, -1, -1));
        }
        return warnings;
    }

    public boolean isEpsilon() {
        return c == '_';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Symbol symbol = (Symbol) o;
        return c == symbol.c;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(c);
    }

    @Override
    public String toString() {
        return "Symbol{" +
                "c=" + c +
                "}\n";
    }

    public String prettyPrint() {
        StringBuilder sb = new StringBuilder();
        String s;
        if (isEpsilon()){
            s = "epsilon";
        }else {
            s = String.valueOf(c);
        }
        sb.append("Symbol: ").append(s).append("\n");
        return sb.toString();
    }

}
