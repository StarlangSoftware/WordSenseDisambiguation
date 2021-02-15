package Sentence;

import AnnotatedSentence.*;
import AutoProcessor.Sentence.MostFrequentSentenceAutoSemantic;
import MorphologicalAnalysis.FsmMorphologicalAnalyzer;
import WordNet.WordNet;
import org.junit.Before;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class TestMostFrequent {

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
        MostFrequentSentenceAutoSemantic mostFrequent = new MostFrequentSentenceAutoSemantic(wordNet, fsm);
        AnnotatedCorpus corpus1 = new AnnotatedCorpus(new File("new-sentences"));
        AnnotatedCorpus corpus2 = new AnnotatedCorpus(new File("old-sentences"));
        for (int i = 0; i < corpus1.sentenceCount(); i++){
            AnnotatedSentence sentence1 = (AnnotatedSentence) corpus1.getSentence(i);
            mostFrequent.autoSemantic(sentence1);
            AnnotatedSentence sentence2 = (AnnotatedSentence) corpus2.getSentence(i);
            for (int j = 0; j < sentence1.wordCount(); j++){
                total++;
                AnnotatedWord word1 = (AnnotatedWord) sentence1.getWord(j);
                AnnotatedWord word2 = (AnnotatedWord) sentence2.getWord(j);
                if (word1.getSemantic() != null && word1.getSemantic().equals(word2.getSemantic())){
                    correct++;
                }
            }
        }
        assertEquals(549, total);
        assertEquals(284, correct);
    }

}
