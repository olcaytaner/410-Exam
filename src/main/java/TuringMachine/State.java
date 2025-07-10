package TuringMachine;

public class State {
    private final String name;
    private final boolean isAccept;
    private final boolean isReject;

    public State(String name, boolean isAccept, boolean isReject) {
        this.name = name;
        this.isAccept = isAccept;
        this.isReject = isReject;
    }

    public String getName() {
        return name;
    }

    public boolean isAccept() {
        return isAccept;
    }

    public boolean isReject() {
        return isReject;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        State state = (State) obj;
        return name.equals(state.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
