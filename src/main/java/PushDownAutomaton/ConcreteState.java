package PushDownAutomaton;

import java.util.Objects;

public class ConcreteState implements State {
    private String name;
    private boolean isStart;
    private boolean isAccept;

    public ConcreteState(String name) {
        this.name = name;
        this.isStart = false;
        this.isAccept = false;
    }

    public ConcreteState(String name, boolean isStart, boolean isAccept) {
        this.name = name;
        this.isStart = isStart;
        this.isAccept = isAccept;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isStart() {
        return isStart;
    }

    @Override
    public boolean isAccept() {
        return isAccept;
    }

    public void setAsStart(boolean isStart) {
        this.isStart = isStart;
    }

    public void setAsAccept(boolean isAccept) {
        this.isAccept = isAccept;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConcreteState that = (ConcreteState) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}