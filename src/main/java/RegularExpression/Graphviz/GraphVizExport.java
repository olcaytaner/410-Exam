package RegularExpression.Graphviz;

import RegularExpression.SyntaxTree.BinaryNode;
import RegularExpression.SyntaxTree.SyntaxTreeNode;
import RegularExpression.SyntaxTree.UnaryNode;

import java.io.FileWriter;
import java.io.IOException;


public class GraphVizExport {
    private static int nodeIdx = 0;
    private static final String filePath = "RegularExpression/Graphviz/graphviz.txt";
    private static final StringBuilder nodesBuilder = new StringBuilder();
    private static final StringBuilder edgesBuilder = new StringBuilder();

    private static void dfs1(SyntaxTreeNode u) {
        int curNodeIdx = nodeIdx;

        String label = "node" + curNodeIdx + " [label=" + "\"" + u.sym + "\"]" + "\n";
        nodesBuilder.append(label);

        if (u instanceof UnaryNode) {
            nodeIdx += 1;
            dfs1(((UnaryNode) u).child);
        } else if (u instanceof BinaryNode) {
            nodeIdx += 1;
            dfs1(((BinaryNode) u).leftChild);
            nodeIdx += 1;
            dfs1(((BinaryNode) u).rightChild);
        }
    }

    private static void dfs2(SyntaxTreeNode u) {
        int curNodeIdx = nodeIdx;

        StringBuilder edges = new StringBuilder();
        if (u instanceof UnaryNode) {
            edges.append("node").append(curNodeIdx).append(" -> node").append(nodeIdx + 1).append("\n");
            nodeIdx += 1;
            dfs2(((UnaryNode) u).child);
        } else if (u instanceof BinaryNode) {
            edges.append("node").append(curNodeIdx).append(" -> node").append(nodeIdx + 1).append("\n");
            nodeIdx += 1;
            dfs2(((BinaryNode) u).leftChild);
            edges.append("node").append(curNodeIdx).append(" -> node").append(nodeIdx + 1).append("\n");
            nodeIdx += 1;
            dfs2(((BinaryNode) u).rightChild);
        }
        edgesBuilder.append(edges);
    }

    public static void export(SyntaxTreeNode root) throws IOException {
        dfs1(root);
        nodeIdx = 0;
        dfs2(root);
        StringBuilder res = new StringBuilder("digraph SyntaxTree {").append("\n");
        res.append(nodesBuilder);
        res.append(edgesBuilder);
        res.append("}");
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(new String(res));
        }
    }
}
