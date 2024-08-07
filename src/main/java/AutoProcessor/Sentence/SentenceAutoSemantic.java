package AutoProcessor.Sentence;

import AnnotatedSentence.*;
import AutoProcessor.AutoSemantic;
import MorphologicalAnalysis.FsmMorphologicalAnalyzer;
import WordNet.SynSet;
import WordNet.WordNet;

import java.util.ArrayList;

public abstract class SentenceAutoSemantic extends AutoSemantic {

    /**
     * The method should set the senses of all words, for which there is only one possible sense.
     * @param sentence The sentence for which word sense disambiguation will be determined automatically.
     */
    protected abstract boolean autoLabelSingleSemantics(AnnotatedSentence sentence);

    /**
     * The method constructs all possible senses for the word at position index in the given sentence. The method checks
     * the previous two words and the current word; the previous, current and next word, current and the next
     * two words to add three word multiword sense (that occurs in the Turkish wordnet) to the result list. The
     * method then check the previous word and current word; current word and the next word to add a two word multiword
     * sense to the result list. Lastly, the method adds all possible senses of the current word to the result list.
     * @param wordNet Turkish wordnet
     * @param fsm Turkish morphological analyzer
     * @param sentence Sentence to be semantically disambiguated.
     * @param index Position of the word to be disambiguated.
     * @return All possible senses for the word at position index in the given sentence.
     */
    protected ArrayList<SynSet> getCandidateSynSets(WordNet wordNet, FsmMorphologicalAnalyzer fsm, AnnotatedSentence sentence, int index){
        AnnotatedWord twoPrevious = null, previous = null, current, twoNext = null, next = null;
        ArrayList<SynSet> synSets;
        current = (AnnotatedWord) sentence.getWord(index);
        if (index > 1){
            twoPrevious = (AnnotatedWord) sentence.getWord(index - 2);
        }
        if (index > 0){
            previous = (AnnotatedWord) sentence.getWord(index - 1);
        }
        if (index != sentence.wordCount() - 1){
            next = (AnnotatedWord) sentence.getWord(index + 1);
        }
        if (index < sentence.wordCount() - 2){
            twoNext = (AnnotatedWord) sentence.getWord(index + 2);
        }
        synSets = wordNet.constructSynSets(current.getParse().getWord().getName(),
                current.getParse(), current.getMetamorphicParse(), fsm);
        if (twoPrevious != null && twoPrevious.getParse() != null && previous.getParse() != null){
            synSets.addAll(wordNet.constructIdiomSynSets(twoPrevious.getParse(), previous.getParse(), current.getParse(),
                    twoPrevious.getMetamorphicParse(), previous.getMetamorphicParse(), current.getMetamorphicParse(), fsm));
        }
        if (previous != null && previous.getParse() != null && next != null && next.getParse() != null){
            synSets.addAll(wordNet.constructIdiomSynSets(previous.getParse(), current.getParse(), next.getParse(),
                    previous.getMetamorphicParse(), current.getMetamorphicParse(), next.getMetamorphicParse(), fsm));
        }
        if (next != null && next.getParse() != null && twoNext != null && twoNext.getParse() != null){
            synSets.addAll(wordNet.constructIdiomSynSets(current.getParse(), next.getParse(), twoNext.getParse(),
                    current.getMetamorphicParse(), next.getMetamorphicParse(), twoNext.getMetamorphicParse(), fsm));
        }
        if (previous != null && previous.getParse() != null){
            synSets.addAll(wordNet.constructIdiomSynSets(previous.getParse(), current.getParse(),
                    previous.getMetamorphicParse(), current.getMetamorphicParse(), fsm));
        }
        if (next != null && next.getParse() != null){
            synSets.addAll(wordNet.constructIdiomSynSets(current.getParse(), next.getParse(),
                    current.getMetamorphicParse(), next.getMetamorphicParse(), fsm));
        }
        return synSets;
    }

    /**
     * The method tries to semantic annotate as many words in the sentence as possible.
     * @param sentence Sentence to be semantically disambiguated.
     */
    public void autoSemantic(AnnotatedSentence sentence){
        if (autoLabelSingleSemantics(sentence)){
            sentence.save();
        }
    }

}
