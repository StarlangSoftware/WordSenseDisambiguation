package AutoProcessor.ParseTree;

import AnnotatedSentence.ViewLayerType;
import AnnotatedTree.ParseNodeDrawable;
import AnnotatedTree.ParseTreeDrawable;
import AnnotatedTree.Processor.Condition.IsTurkishLeafNode;
import AnnotatedTree.Processor.NodeDrawableCollector;
import MorphologicalAnalysis.FsmMorphologicalAnalyzer;
import WordNet.*;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class Lesk extends TreeAutoSemantic {

    private final WordNet turkishWordNet;
    private final FsmMorphologicalAnalyzer fsm;

    public Lesk(WordNet turkishWordNet, FsmMorphologicalAnalyzer fsm){
        this.turkishWordNet = turkishWordNet;
        this.fsm = fsm;
    }

    private int intersection(SynSet synSet, ArrayList<ParseNodeDrawable> leafList){
        String[] words1;
        if (synSet.getExample() != null){
            words1 = (synSet.getLongDefinition() + " " + synSet.getExample()).split(" ");
        } else {
            words1 = synSet.getLongDefinition().split(" ");
        }
        String[] words2 = new String[leafList.size()];
        for (int i = 0; i < leafList.size(); i++){
            words2[i] = leafList.get(i).getLayerData(ViewLayerType.TURKISH_WORD);
        }
        int count = 0;
        for (String word1 : words1){
            for (String word2 : words2){
                if (word1.toLowerCase(new Locale("tr")).equals(word2.toLowerCase(new Locale("tr")))){
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    protected boolean autoLabelSingleSemantics(ParseTreeDrawable parseTree) {
        Random random = new Random(1);
        NodeDrawableCollector nodeDrawableCollector = new NodeDrawableCollector((ParseNodeDrawable) parseTree.getRoot(), new IsTurkishLeafNode());
        ArrayList<ParseNodeDrawable> leafList = nodeDrawableCollector.collect();
        boolean done = false;
        for (int i = 0; i < leafList.size(); i++){
            ArrayList<SynSet> synSets = getCandidateSynSets(turkishWordNet, fsm, leafList, i);
            int maxIntersection = -1;
            for (SynSet synSet : synSets) {
                int intersectionCount = intersection(synSet, leafList);
                if (intersectionCount > maxIntersection) {
                    maxIntersection = intersectionCount;
                }
            }
            ArrayList<SynSet> maxSynSets = new ArrayList<>();
            for (SynSet synSet : synSets) {
                if (intersection(synSet, leafList) == maxIntersection) {
                    maxSynSets.add(synSet);
                }
            }
            if (!maxSynSets.isEmpty()){
                leafList.get(i).getLayerInfo().setLayerData(ViewLayerType.SEMANTICS, maxSynSets.get(random.nextInt(maxSynSets.size())).getId());
                done = true;
            }
        }
        return done;
    }

}
