package PushDownAutomaton;

/**
 * A custom implementation of a stack data structure using a singly linked list of {@link Node}s.
 * This class is specifically used for the PDA simulation.
 */
public class Stack {
    private Node top;

    /**
     * Constructs an empty stack.
     */
    public Stack() {
        top = null;
    }

    /**
     * Checks if the stack is empty.
     * @return true if the stack contains no nodes, false otherwise.
     */
    public boolean isEmpty() {
        return top == null;
    }

    /**
     * Returns the top node of the stack without removing it.
     * @return The top node, or null if the stack is empty.
     */
    public Node peek() {
        return top;
    }

    /**
     * Pushes a new node onto the top of the stack.
     * @param node The node to be added.
     */
    public void push(Node node) {
        node.setNext(top);
        top = node;
    }

    /**
     * Removes and returns the top node from the stack.
     * @return The node that was at the top of the stack. Returns null if the stack is empty.
     */
    public Node pop() {
        if (isEmpty()) {
            return null;
        }
        Node topNode = top;
        top = top.next;
        return topNode;
    }
}