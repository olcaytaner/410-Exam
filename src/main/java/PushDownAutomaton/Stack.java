package PushDownAutomaton;

/**
 * A custom implementation of a stack data structure using a singly linked list of {@link Node}s.
 * This class can be useful in other parts of the project; the new PDA.execute()
 * uses a String-based stack representation for performance.
 */
public class Stack {
    private Node top;

    public Stack() {
        top = null;
    }

    public boolean isEmpty() {
        return top == null;
    }

    public Node peek() {
        return top;
    }

    public void push(Node node) {
        node.setNext(top);
        top = node;
    }

    public Node pop() {
        if (isEmpty()) {
            return null;
        }
        Node topNode = top;
        top = top.next;
        return topNode;
    }

    /**
     * Creates and returns a deep copy of this stack.
     * @return A new Stack instance with the same contents.
     */
    public Stack clone() {
        Stack newStack = new Stack();
        if (this.isEmpty()) {
            return newStack;
        }

        // Create an array to store elements in order
        java.util.List<Character> elements = new java.util.ArrayList<>();
        Node current = this.top;
        while (current != null) {
            elements.add(current.getData());
            current = current.getNext();
        }

        // Push elements back in reverse order to maintain stack order
        for (int i = elements.size() - 1; i >= 0; i--) {
            newStack.push(new Node(elements.get(i)));
        }

        return newStack;
    }

    /**
     * Provides a string representation of the stack's contents from top to bottom.
     * @return A string representing the stack.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Node current = top;
        while (current != null) {
            sb.append(current.getData());
            current = current.getNext();
        }
        return sb.toString();
    }
}
