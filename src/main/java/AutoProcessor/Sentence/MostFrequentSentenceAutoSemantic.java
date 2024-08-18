package AutoProcessor.Sentence;

import AnnotatedSentence.*;
import MorphologicalAnalysis.FsmMorphologicalAnalyzer;
import WordNet.*;
import java.util.ArrayList;

public class MostFrequentSentenceAutoSemantic extends SentenceAutoSemantic {

    private final WordNet turkishWordNet;
    private final FsmMorphologicalAnalyzer fsm;

    /**
     * Constructor for the {@link MostFrequentSentenceAutoSemantic} class. Gets the Turkish wordnet and Turkish fst based
     * morphological analyzer from the user and sets the corresponding attributes.
     * @param turkishWordNet Turkish wordnet
     * @param fsm Turkish morphological analyzer
     */
    public MostFrequentSentenceAutoSemantic(WordNet turkishWordNet, FsmMorphologicalAnalyzer fsm) {
        this.turkishWordNet = turkishWordNet;
        this.fsm = fsm;
    }

    /**
     * Checks
     * 1. the previous two words and the current word; the previous, current and next word, current and the next
     * two words for a three word multiword expression that occurs in the Turkish wordnet.
     * 2. the previous word and current word; current word and the next word for a two word multiword expression that
     * occurs in the Turkish wordnet.
     * 3. the current word
     * and sets the most frequent sense for that multiword expression or word.
     * @param sentence The sentence for which word sense will be determined automatically.
     * @return True, if at least one word is semantically annotated, false otherwise.
     */
    @Override
    public boolean autoLabelSingleSemantics(AnnotatedSentence sentence) {
        boolean done = false;
        AnnotatedWord twoPrevious = null, previous = null, current, twoNext = null, next = null;
        for (int i = 0; i < sentence.wordCount(); i++) {
            current = (AnnotatedWord) sentence.getWord(i);
            if (i > 1) {
                twoPrevious = (AnnotatedWord) sentence.getWord(i - 2);
            }
            if (i > 0) {
                previous = (AnnotatedWord) sentence.getWord(i - 1);
            }
            if (i != sentence.wordCount() - 1) {
                next = (AnnotatedWord) sentence.getWord(i + 1);
            }
            if (i < sentence.wordCount() - 2) {
                twoNext = (AnnotatedWord) sentence.getWord(i + 2);
            }
            if (current.getSemantic() == null && current.getParse() != null) {
                if (twoPrevious != null && twoPrevious.getParse() != null && previous.getParse() != null) {
                    ArrayList<Literal> literals = turkishWordNet.constructIdiomLiterals(twoPrevious.getParse(), previous.getParse(), current.getParse(), twoPrevious.getMetamorphicParse(), previous.getMetamorphicParse(), current.getMetamorphicParse(), fsm);
                    if (!literals.isEmpty()) {
                        SynSet bestSynset = mostFrequent(literals);
                        if (bestSynset != null){
                            current.setSemantic(bestSynset.getId());
                            done = true;
                            continue;
                        }
                    }
                }
                if (previous != null && previous.getParse() != null && next != null && next.getParse() != null) {
                    ArrayList<Literal> literals = turkishWordNet.constructIdiomLiterals(previous.getParse(), current.getParse(), next.getParse(), previous.getMetamorphicParse(), current.getMetamorphicParse(), next.getMetamorphicParse(), fsm);
                    if (!literals.isEmpty()) {
                        SynSet bestSynset = mostFrequent(literals);
                        if (bestSynset != null) {
                            current.setSemantic(bestSynset.getId());
                            done = true;
                            continue;
                        }
                    }
                }
                if (next != null && next.getParse() != null && twoNext != null && twoNext.getParse() != null) {
                    ArrayList<Literal> literals = turkishWordNet.constructIdiomLiterals(current.getParse(), next.getParse(), twoNext.getParse(), current.getMetamorphicParse(), next.getMetamorphicParse(), twoNext.getMetamorphicParse(), fsm);
                    if (!literals.isEmpty()) {
                        SynSet bestSynset = mostFrequent(literals);
                        if (bestSynset != null) {
                            current.setSemantic(bestSynset.getId());
                            done = true;
                            continue;
                        }
                    }
                }
                if (previous != null && previous.getParse() != null) {
                    ArrayList<Literal> literals = turkishWordNet.constructIdiomLiterals(previous.getParse(), current.getParse(), previous.getMetamorphicParse(), current.getMetamorphicParse(), fsm);
                    if (!literals.isEmpty()) {
                        SynSet bestSynset = mostFrequent(literals);
                        if (bestSynset != null) {
                            current.setSemantic(bestSynset.getId());
                            done = true;
                            continue;
                        }
                    }
                }
                if (current.getSemantic() == null && next != null && next.getParse() != null) {
                    ArrayList<Literal> literals = turkishWordNet.constructIdiomLiterals(current.getParse(), next.getParse(), current.getMetamorphicParse(), next.getMetamorphicParse(), fsm);
                    if (!literals.isEmpty()) {
                        SynSet bestSynset = mostFrequent(literals);
                        if (bestSynset != null) {
                            current.setSemantic(bestSynset.getId());
                            done = true;
                            continue;
                        }
                    }
                }
                ArrayList<Literal> literals = turkishWordNet.constructLiterals(current.getParse().getWord().getName(), current.getParse(), current.getMetamorphicParse(), fsm);
                if (current.getSemantic() == null && !literals.isEmpty()) {
                    SynSet bestSynset = mostFrequent(literals);
                    if (bestSynset != null) {
                        current.setSemantic(bestSynset.getId());
                        done = true;
                    }
                }
            }
        }
        return done;
    }

    /**
     * Determines the synset containing the literal with the lowest sense number.
     * @param literals an ArrayList of Literal objects
     * @return the SynSet containing the literal with the lowest sense number, or null if the input list is empty
     */
    private SynSet mostFrequent(ArrayList<Literal> literals) {
        if (literals.size() == 1) {
            return turkishWordNet.getSynSetWithId(literals.get(0).getSynSetId());
        }
        int minSense = Integer.MAX_VALUE;
        SynSet bestSynset = null;
        for (Literal literal : literals) {
            if(literal.getSense() < minSense) {
                minSense = literal.getSense();
                bestSynset = turkishWordNet.getSynSetWithId(literal.getSynSetId());
            }
        }
        return bestSynset;
    }
}
