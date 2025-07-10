package PushDownAutomaton;

public class Node {
    protected char data;
    protected Node next;

    public Node(char data) {
        this.data = data;
        this.next = null;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public Node getNext() {
        return next;
    }

    public int getData() {
        return data;
    }

    @Override
    public String toString() {
        return "" + data;
    }
}