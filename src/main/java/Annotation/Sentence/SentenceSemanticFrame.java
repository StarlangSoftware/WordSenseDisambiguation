package Annotation.Sentence;

import AnnotatedSentence.AnnotatedCorpus;
import AnnotatedSentence.AnnotatedSentence;
import AnnotatedSentence.AnnotatedWord;
import AutoProcessor.Sentence.TurkishSentenceAutoSemantic;
import DataCollector.ParseTree.TreeEditorPanel;
import DataCollector.Sentence.SentenceAnnotatorFrame;
import DataCollector.Sentence.SentenceAnnotatorPanel;
import MorphologicalAnalysis.FsmMorphologicalAnalyzer;
import WordNet.WordNet;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class SentenceSemanticFrame extends SentenceAnnotatorFrame {
    private final JCheckBox autoSemanticDetectionOption;
    private FsmMorphologicalAnalyzer fsm;
    private WordNet wordNet;
    private final HashMap<String, HashSet<String>> exampleSentences;

    /**
     * Constructor of the sense disambiguation frame for annotated sentence. It reads the annotated sentence
     * corpus and adds automatic sense disambiguation button. It also adds itemUpdateDictionary button whose
     * purpose is to use alternative domain wordnet for sense disambiguation. It also adds itemShowUnannotated button
     * whose purpose is to show the partially annotated files for sense disambiguation. It also creates exampleSentences.
     * exampleSentences will be used to show the user how other sentences with that word was annotated.
     * @param fsm Morphological analyzer
     * @param wordNet Turkish wordnet
     */
    public SentenceSemanticFrame(final FsmMorphologicalAnalyzer fsm, final WordNet wordNet) {
        super();
        exampleSentences = new HashMap<>();
        this.fsm = fsm;
        this.wordNet = wordNet;
        AnnotatedCorpus corpus;
        String subFolder = "false";
        String domainPrefix = "";
        Properties properties = new Properties();
        try {
            properties.load(Files.newInputStream(new File("config.properties").toPath()));
            domainPrefix = properties.getProperty("domainPrefix");
            if (properties.containsKey("subFolder")){
                subFolder = properties.getProperty("subFolder");
            }
        } catch (IOException ignored) {
        }
        corpus = readCorpus(subFolder);
        for (int i = 0; i < corpus.sentenceCount(); i++) {
            AnnotatedSentence sentence = (AnnotatedSentence) corpus.getSentence(i);
            for (int j = 0; j < sentence.wordCount(); j++) {
                AnnotatedWord word = (AnnotatedWord) sentence.getWord(j);
                String semantic = word.getSemantic();
                if (semantic != null) {
                    HashSet<String> sentences;
                    if (exampleSentences.containsKey(semantic)) {
                        sentences = exampleSentences.get(semantic);
                    } else {
                        sentences = new HashSet<>();
                    }
                    if (sentences.size() < 20) {
                        sentences.add(sentence.toWords());
                    }
                    exampleSentences.put(semantic, sentences);
                }
            }
        }
        JMenuItem itemUpdateDictionary = addMenuItem(projectMenu, "Update Wordnet", KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_MASK));
        String finalDomainPrefix = domainPrefix;
        itemUpdateDictionary.addActionListener(e -> {
            String domainWordNetFileName = finalDomainPrefix + "_wordnet.xml";
            String domainDictionaryFileName = finalDomainPrefix + "_dictionary.txt";
            this.fsm = new FsmMorphologicalAnalyzer(domainDictionaryFileName);
            this.wordNet = new WordNet(domainWordNetFileName, new Locale("tr"));
            TurkishSentenceAutoSemantic turkishSentenceAutoSemantic = new TurkishSentenceAutoSemantic(this.wordNet, this.fsm);
            for (int i = 0; i < projectPane.getTabCount(); i++) {
                SentenceSemanticPanel current = (SentenceSemanticPanel) ((JScrollPane) projectPane.getComponentAt(i)).getViewport().getView();
                current.setFsm(this.fsm);
                current.setWordnet(this.wordNet);
                current.setTurkishSentenceAutoSemantic(turkishSentenceAutoSemantic);
            }
        });
        JMenuItem itemAutoAnnotate = addMenuItem(projectMenu, "Annotate Every Word With Last Sense", KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK));
        itemAutoAnnotate.addActionListener(e -> {
            SentenceSemanticPanel current;
            int wordCount = 0, fileCount = 0;
            current = (SentenceSemanticPanel) ((JScrollPane) projectPane.getSelectedComponent()).getViewport().getView();
            AnnotatedWord clickedWord = current.getClickedWord();
            for (int i = 0; i < corpus.sentenceCount(); i++) {
                AnnotatedSentence sentence = (AnnotatedSentence) corpus.getSentence(i);
                boolean modified = false;
                for (int j = 0; j < sentence.wordCount(); j++) {
                    AnnotatedWord word = (AnnotatedWord) sentence.getWord(j);
                    String semantic = word.getSemantic();
                    if (word.getName() != null && word.getName().equals(clickedWord.getName()) && semantic == null) {
                        wordCount++;
                        modified = true;
                    }
                }
                if (modified) {
                    fileCount++;
                }
            }
            int result = JOptionPane.showConfirmDialog(null,
                    wordCount + " words in " + fileCount + " files with text (" + clickedWord.getName() + ") will be modified. Are you sure?",
                    "",
                    JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                for (int i = 0; i < corpus.sentenceCount(); i++) {
                    AnnotatedSentence sentence = (AnnotatedSentence) corpus.getSentence(i);
                    boolean modified = false;
                    for (int j = 0; j < sentence.wordCount(); j++) {
                        AnnotatedWord word = (AnnotatedWord) sentence.getWord(j);
                        String semantic = word.getSemantic();
                        if (word.getName() != null && word.getName().equals(clickedWord.getName()) && semantic == null) {
                            word.setSemantic(clickedWord.getSemantic());
                            modified = true;
                        }
                    }
                    if (modified) {
                        sentence.save();
                    }
                }
            }
        });
        JMenuItem itemShowUnannotated = addMenuItem(projectMenu, "Show Unannotated Files", KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_MASK));
        itemShowUnannotated.addActionListener(e -> {
            int count = 0;
            String result1 = JOptionPane.showInputDialog(null, "How many sentences you want to see:", "",
                    JOptionPane.PLAIN_MESSAGE);
            int numberOfSentences = Integer.parseInt(result1);
            String result2 = JOptionPane.showInputDialog(null, "How many words does not have semantic annotation in the sentence:", "",
                    JOptionPane.PLAIN_MESSAGE);
            int missingAnnotations = Integer.parseInt(result2);
            for (int i = 0; i < corpus.sentenceCount(); i++) {
                int missingCount = 0;
                AnnotatedSentence sentence = (AnnotatedSentence) corpus.getSentence(i);
                for (int j = 0; j < sentence.wordCount(); j++) {
                    AnnotatedWord word = (AnnotatedWord) sentence.getWord(j);
                    String semantic = word.getSemantic();
                    if (word.getName() != null && semantic == null) {
                        missingCount++;
                    }
                }
                if (missingCount == missingAnnotations) {
                    SentenceAnnotatorPanel annotatorPanel = generatePanel(sentence.getFile().getParent(), sentence.getFileName());
                    addPanelToFrame(annotatorPanel, sentence.getFileName());
                    count++;
                    if (count == numberOfSentences) {
                        return;
                    }
                }
            }
        });
        JMenuItem itemViewAnnotated = addMenuItem(projectMenu, "View Annotations", KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        itemViewAnnotated.addActionListener(e -> new ViewSentenceSemanticAnnotationFrame(corpus, this.wordNet, wordNet, this));
        autoSemanticDetectionOption = new JCheckBox("Auto Semantic Detection", false);
        toolBar.add(autoSemanticDetectionOption);
        JOptionPane.showMessageDialog(this, "WordNet, dictionary, and annotated corpus are loaded!", "Semantic Annotation", JOptionPane.INFORMATION_MESSAGE);
    }

    public SentenceAnnotatorPanel generatePanel(String currentPath, String rawFileName) {
        return new SentenceSemanticPanel(currentPath, rawFileName, fsm, wordNet, exampleSentences);
    }

    /**
     * The next method takes an int count as input and moves forward along the SentenceSemanticPanels as much as the
     * count. If the autoSemanticDetectionOption is selected, it automatically assigns semantic tags to words.
     * @param count Integer count is used to move forward.
     */
    public void next(int count) {
        super.next(count);
        SentenceSemanticPanel current;
        current = (SentenceSemanticPanel) ((JScrollPane) projectPane.getSelectedComponent()).getViewport().getView();
        if (autoSemanticDetectionOption.isSelected()) {
            current.autoDetect();
        }
    }

    /**
     * The previous method takes an int count as input and moves backward along the SentenceSemanticPanels as much as
     * the count. If the autoSemanticDetectionOption is selected, it automatically assigns semantic tags to words.
     * @param count Integer count is used to move backward.
     */
    public void previous(int count) {
        super.previous(count);
        SentenceSemanticPanel current;
        current = (SentenceSemanticPanel) ((JScrollPane) projectPane.getSelectedComponent()).getViewport().getView();
        if (autoSemanticDetectionOption.isSelected()) {
            current.autoDetect();
        }
    }

}
