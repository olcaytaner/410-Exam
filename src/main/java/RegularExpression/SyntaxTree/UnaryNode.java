package RegularExpression.SyntaxTree;

import static RegularExpression.SyntaxTree.RegexOperator.*;

public abstract class UnaryNode extends SyntaxTreeNode {
    public SyntaxTreeNode child;

    public UnaryNode(SyntaxTreeNode child) {
        super(STAR);
        this.child = child;
    }
}
