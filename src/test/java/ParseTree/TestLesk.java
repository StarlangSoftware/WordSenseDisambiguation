package ParseTree;

import AnnotatedSentence.ViewLayerType;
import AnnotatedTree.ParseNodeDrawable;
import AnnotatedTree.ParseTreeDrawable;
import AnnotatedTree.Processor.Condition.IsTurkishLeafNode;
import AnnotatedTree.Processor.NodeDrawableCollector;
import AnnotatedTree.TreeBankDrawable;
import AutoProcessor.ParseTree.Lesk;
import MorphologicalAnalysis.FsmMorphologicalAnalyzer;
import WordNet.WordNet;
import org.junit.Before;

import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class TestLesk {

    FsmMorphologicalAnalyzer fsm;
    WordNet wordNet;

    @Before
    public void setUp() {
        fsm = new FsmMorphologicalAnalyzer();
        wordNet = new WordNet();
    }

    @org.junit.Test
    public void testAccuracy() {
        int correct = 0, total = 0;
        Lesk lesk = new Lesk(wordNet, fsm);
        TreeBankDrawable treeBank1 = new TreeBankDrawable(new File("new-trees"));
        TreeBankDrawable treeBank2 = new TreeBankDrawable(new File("old-trees"));
        for (int i = 0; i < treeBank1.size(); i++){
            ParseTreeDrawable parseTree1 = treeBank1.get(i);
            ParseTreeDrawable parseTree2 = treeBank2.get(i);
            lesk.autoSemantic(parseTree1);
            NodeDrawableCollector nodeDrawableCollector1 = new NodeDrawableCollector((ParseNodeDrawable) parseTree1.getRoot(), new IsTurkishLeafNode());
            ArrayList<ParseNodeDrawable> leafList1 = nodeDrawableCollector1.collect();
            NodeDrawableCollector nodeDrawableCollector2 = new NodeDrawableCollector((ParseNodeDrawable) parseTree2.getRoot(), new IsTurkishLeafNode());
            ArrayList<ParseNodeDrawable> leafList2 = nodeDrawableCollector2.collect();
            for (int j = 0; j < leafList1.size(); j++){
                total++;
                ParseNodeDrawable parseNode1 = leafList1.get(j);
                ParseNodeDrawable parseNode2 = leafList2.get(j);
                if (parseNode1.getLayerData(ViewLayerType.SEMANTICS) != null && parseNode1.getLayerData(ViewLayerType.SEMANTICS).equals(parseNode2.getLayerData(ViewLayerType.SEMANTICS))){
                    correct++;
                }
            }
        }
        assertEquals(475, total);
        assertEquals(240, correct);
    }

}
