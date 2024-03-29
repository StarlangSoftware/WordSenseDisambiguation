package Annotation.Sentence;

import WordNet.SynSet;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;

public class SemanticExampleTreeCellRenderer extends DefaultTreeCellRenderer {
    private final HashMap<String, HashSet<String>> exampleSentences;

    public SemanticExampleTreeCellRenderer(HashMap<String, HashSet<String>> exampleSentences){
        this.exampleSentences = exampleSentences;
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean exp, boolean leaf, int row, boolean hasFocus) {
        Component cell = super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, hasFocus);
        DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) value;
        if (currentNode.getUserObject() instanceof SynSet){
            SynSet synSet = (SynSet)currentNode.getUserObject();
            if (exampleSentences.containsKey(synSet.getId())){
                StringBuilder examples = new StringBuilder("<html>");
                for (String example: exampleSentences.get(synSet.getId())){
                    examples.append(example).append("<br>");
                }
                examples.append("</html>");
                ((JComponent) cell).setToolTipText(examples.toString());
            } else {
                if (synSet.getExample() != null){
                    ((JComponent) cell).setToolTipText(synSet.getExample());
                } else {
                    ((JComponent) cell).setToolTipText("");
                }
            }
        } else {
            ((JComponent) cell).setToolTipText("");
        }
        return this;
    }
}
