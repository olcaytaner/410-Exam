package RegularExpression.SyntaxTree;

/**
 * Encapsulates syntax tree nodes that take 2 operands.
 * <p>
 *     For example, in the regex <code>1{@value RegularExpression.SyntaxTree.RegexOperator#OR}2</code>,
 *     the <code>'{@value RegularExpression.SyntaxTree.RegexOperator#OR}'</code> operator would be the
 *     {@code BinaryNode}, with
 *     <code>'1'</code> as the left child and <code>'2'</code> as the right child.
 * </p>
 */
public abstract class BinaryNode extends SyntaxTreeNode {
    public SyntaxTreeNode leftChild;
    public SyntaxTreeNode rightChild;

    public BinaryNode(SyntaxTreeNode l, SyntaxTreeNode r, char sym) {
        super(sym);
        leftChild = l;
        rightChild = r;
    }
}
