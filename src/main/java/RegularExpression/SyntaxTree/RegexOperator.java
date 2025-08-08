package RegularExpression.SyntaxTree;

/**
 * Contains constants representing regex operators used in the syntax tree.
 * <p>
 * These operator symbols (<code>{@value #OR}</code>, <code>{@value #CONCAT}</code>, <code>{@value #STAR}</code>)
 * may be changed freely.
 * </p>
 */
public class RegexOperator {
    public static final char OR = 'u';
    public static final char CONCAT = '.';
    public static final char STAR = '*';
}
