package SyntaxTree;

public abstract class UnaryNode extends SyntaxTreeNode {
    public SyntaxTreeNode child;

    public UnaryNode(SyntaxTreeNode child) {
        super('*');
        this.child = child;
    }
}
