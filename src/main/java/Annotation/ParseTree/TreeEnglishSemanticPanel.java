package Annotation.ParseTree;

import AnnotatedSentence.ViewLayerType;
import AnnotatedTree.*;
import DataCollector.ParseTree.TreeAction.LayerAction;
import DataCollector.ParseTree.TreeLeafEditorPanel;
import Dictionary.Pos;
import ParseTree.ParseNode;
import WordNet.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;

public class TreeEnglishSemanticPanel extends TreeLeafEditorPanel {

    private final WordNet englishWordNet;
    private final WordNet turkishWordNet;
    private final JList list;
    private final DefaultListModel listModel;
    private ArrayList<Integer> translatedSideCandidateList;

    /**
     * Constructor for the sense disambiguation panel for a parse tree in English. It also adds the
     * list selection listener which will update the parse tree according to the selection.
     * @param path The absolute path of the annotated parse tree.
     * @param fileName The raw file name of the annotated parse tree.
     * @param englishWordNet English wordnet
     * @param turkishWordNet Turkish wordnet
     */
    public TreeEnglishSemanticPanel(String path, String fileName, WordNet englishWordNet, WordNet turkishWordNet) {
        super(path, fileName, ViewLayerType.ENGLISH_SEMANTICS, false);
        heightDecrease = 280;
        this.turkishWordNet = turkishWordNet;
        this.englishWordNet = englishWordNet;
        listModel = new DefaultListModel();
        list = new JList(listModel);
        list.setVisible(false);
        list.addListSelectionListener(listSelectionEvent -> {
            if (!listSelectionEvent.getValueIsAdjusting()) {
                if (list.getSelectedIndex() != -1 && previousNode != null) {
                    previousNode.setSelected(false);
                    LayerAction action = new LayerAction(((TreeEnglishSemanticPanel)((JList) listSelectionEvent.getSource()).getParent().getParent().getParent()), previousNode.getLayerInfo(), ((SynSet) list.getSelectedValue()).getId(), ViewLayerType.ENGLISH_SEMANTICS);
                    actionList.add(action);
                    action.execute();
                    list.setVisible(false);
                    pane.setVisible(false);
                    isEditing = false;
                    repaint();
                }
            }
        });
        list.setCellRenderer(new ListRenderer());
        list.setFocusTraversalKeysEnabled(false);
        pane = new JScrollPane(list);
        add(pane);
        pane.setFocusTraversalKeysEnabled(false);
        setFocusable(false);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    private class ListRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (translatedSideCandidateList.contains(index)){
                if (isSelected){
                    setBackground(Color.BLUE);
                } else {
                    setBackground(Color.RED);
                }
            }
            return this;
        }
    }

    /**
     * Fills the JList that contains all English sense id's for the given word. Depending on the symbol on the parent
     * node, that is, the pos tag of the English word, getSynSetsWithPossiblyModifiedLiteral is called to get
     * possible candidate root word list. For multiword expression possibility in English, the next and previous
     * siblings in the English tree are combined as two idiom (multiword expression) candidates.
     * @param node Selected node for which options will be displayed.
     */
    public void populateLeaf(ParseNodeDrawable node){
        SynSet selected = null;
        ArrayList<SynSet> synSets, synonymList = null;
        int selectedIndex = -1;
        if (previousNode != null){
            previousNode.setSelected(false);
        }
        previousNode = node;
        translatedSideCandidateList = new ArrayList<>();
        listModel.clear();
        String englishWord = node.getLayerData(ViewLayerType.ENGLISH_WORD);
        String englishSemantics = node.getLayerData(ViewLayerType.ENGLISH_SEMANTICS);
        String turkishSemantics = node.getLayerData(ViewLayerType.SEMANTICS);
        if (englishSemantics != null){
            selected = englishWordNet.getSynSetWithId(englishSemantics);
        }
        if (turkishSemantics != null){
            SynSet turkish = turkishWordNet.getSynSetWithId(turkishSemantics);
            if (turkish != null){
                synonymList = turkish.getInterlingual(englishWordNet);
            }
        }
        if (node.getParent().getData().getName().startsWith("V")){
            synSets = englishWordNet.getSynSetsWithPossiblyModifiedLiteral(englishWord, Pos.VERB);
        } else {
            if (node.getParent().getData().getName().startsWith("N")){
                synSets = englishWordNet.getSynSetsWithPossiblyModifiedLiteral(englishWord, Pos.NOUN);
            } else {
                if (node.getParent().getData().getName().startsWith("ADJ") || node.getParent().getData().getName().startsWith("JJ")){
                    synSets = englishWordNet.getSynSetsWithPossiblyModifiedLiteral(englishWord, Pos.ADJECTIVE);
                } else {
                    if (node.getParent().getData().getName().startsWith("RB")){
                        synSets = englishWordNet.getSynSetsWithPossiblyModifiedLiteral(englishWord, Pos.ADVERB);
                    } else {
                        synSets = englishWordNet.getSynSetsWithLiteral(englishWord);
                    }
                }
            }
        }
        ParseTreeDrawable englishTree = new ParseTreeDrawable(englishPath, currentTree.getFileDescription());
        HashMap<ParseNode, ParseNodeDrawable> mapping = englishTree.mapTree(currentTree);
        for (ParseNode parseNode : mapping.keySet()){
            if (mapping.get(parseNode).equals(node)){
                ParseNode previousNode = englishTree.previousLeafNode(parseNode);
                if (previousNode != null){
                    ParseNodeDrawable previous = mapping.get(previousNode);
                    if (previous != null){
                        ArrayList<String> modifiedLiterals = englishWordNet.getLiteralsWithPossibleModifiedLiteral(previous.getLayerData(ViewLayerType.ENGLISH_WORD));
                        for (String modifiedLiteral : modifiedLiterals){
                            synSets.addAll(englishWordNet.getSynSetsWithLiteral(modifiedLiteral + " " + englishWord));
                        }
                    }
                }
                ParseNode nextNode = englishTree.nextLeafNode(parseNode);
                if (nextNode != null){
                    ParseNodeDrawable next = mapping.get(nextNode);
                    if (next != null){
                        ArrayList<String> modifiedLiterals = englishWordNet.getLiteralsWithPossibleModifiedLiteral(englishWord);
                        for (String modifiedLiteral : modifiedLiterals){
                            synSets.addAll(englishWordNet.getSynSetsWithLiteral(modifiedLiteral + " " + next.getLayerData(ViewLayerType.ENGLISH_WORD)));
                        }
                    }
                }
                break;
            }
        }
        for (int i = 0; i < synSets.size(); i++){
            listModel.addElement(synSets.get(i));
            if (synSets.get(i).equals(selected)){
                selectedIndex = i;
            }
        }
        if (synonymList != null){
            for (int i = 0; i < synSets.size(); i++){
                if (synonymList.contains(synSets.get(i))){
                    translatedSideCandidateList.add(i);
                }
            }
        }
        if (selectedIndex != -1){
            list.setValueIsAdjusting(true);
            list.setSelectedIndex(selectedIndex);
        }
        list.setVisible(true);
        pane.setVisible(true);
        pane.getVerticalScrollBar().setValue(0);
        pane.setBounds(node.getArea().getX() - 5, node.getArea().getY() + 30, 300, 30 + Math.max(3, Math.min(15, listModel.getSize() + 1)) * 18);
        this.repaint();
        isEditing = true;
    }

    /**
     * When the use control clicks a node, the sense annotation is cleared.
     * @param mouseEvent Mouse click event to handle.
     */
    public void mouseClicked(MouseEvent mouseEvent) {
        ParseNodeDrawable node = currentTree.getLeafNodeAt(mouseEvent.getX(), mouseEvent.getY());
        if (node != null){
            if (mouseEvent.isControlDown()){
                node.getLayerInfo().englishSemanticClear();
                save();
            } else {
                populateLeaf(node);
            }
        }
    }

    /**
     * The size of the string displayed. If it is a leaf node, it returns the maximum size of the sense id's
     * of word(s) in the leaf node. Otherwise, it returns the size of the symbol in the node.
     * @param parseNode Parse node
     * @param g Graphics on which tree will be drawn.
     * @return Size of the string displayed.
     */
    protected int getStringSize(ParseNodeDrawable parseNode, Graphics g) {
        if (parseNode.numberOfChildren() == 0) {
            String layerData = parseNode.getLayerData(ViewLayerType.ENGLISH_SEMANTICS);
            if (layerData != null){
                return g.getFontMetrics().stringWidth(layerData.substring(6, 14));
            } else {
                return g.getFontMetrics().stringWidth(parseNode.getData().getName());
            }
        } else {
            return g.getFontMetrics().stringWidth(parseNode.getData().getName());
        }
    }

    /**
     * If the node is a leaf node, it draws the word and its sense id. Otherwise, it draws the node symbol.
     * @param parseNode Parse Node
     * @param g Graphics on which symbol is drawn.
     * @param x x coordinate
     * @param y y coordinate
     */
    protected void drawString(ParseNodeDrawable parseNode, Graphics g, int x, int y){
        if (parseNode.numberOfChildren() == 0){
            g.drawString(parseNode.getLayerData(ViewLayerType.ENGLISH_WORD), x, y);
            g.setColor(Color.RED);
            String layerData = parseNode.getLayerData(ViewLayerType.ENGLISH_SEMANTICS);
            if (layerData != null){
                g.drawString(layerData.substring(6, 14), x, y + 20);
            }
        } else {
            g.drawString(parseNode.getData().getName(), x, y);
        }
    }

    /**
     * Sets the size of the enclosing area of the parse node (for selecting, editing etc.).
     * @param parseNode Parse Node
     * @param x x coordinate of the center of the node.
     * @param y y coordinate of the center of the node.
     * @param stringSize Size of the string in terms of pixels.
     */
    protected void setArea(ParseNodeDrawable parseNode, int x, int y, int stringSize){
        if (parseNode.numberOfChildren() == 0){
            parseNode.setArea(x - 5, y - 15, stringSize + 10, 40);
        } else {
            parseNode.setArea(x - 5, y - 15, stringSize + 10, 20);
        }
    }

}
