package SyntaxTree;

public abstract class BinaryNode extends SyntaxTreeNode {
    public SyntaxTreeNode leftChild;
    public SyntaxTreeNode rightChild;

    public BinaryNode(SyntaxTreeNode l, SyntaxTreeNode r, char sym) {
        super(sym);
        leftChild = l;
        rightChild = r;
    }
}
