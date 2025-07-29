package PushDownAutomaton;

/**
 * A node for a singly linked list, used by the custom {@link Stack} class.
 * It holds a character data payload.
 */
public class Node {
    protected char data;
    protected Node next;

    /**
     * Constructs a node with the given character data.
     * @param data The character to be stored in the node.
     */
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

    public char getData() {
        return data;
    }

    @Override
    public String toString() {
        return "" + data;
    }
}