package wiki.frequency;

import java.util.concurrent.atomic.AtomicInteger;

public class WordFrequency {
    private String originalWord;
    private String lemma;
    private final AtomicInteger wordDocCount;
    private double frequencyInDoc;

    public WordFrequency(String originalWord, String lemma) {
        this.originalWord = originalWord;
        this.lemma = lemma;
        wordDocCount = new AtomicInteger(1);
    }

    public String getOriginalWord() {
        return originalWord;
    }

    public void setOriginalWord(String originalWord) {
        this.originalWord = originalWord;
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public void incrementWordCount() {
        wordDocCount.getAndIncrement();
    }

    public int getWordDocCount() {
        return wordDocCount.get();
    }

    public void setFrequencyInDoc(double frequencyInDoc) {
        this.frequencyInDoc = frequencyInDoc;
    }

    public double getFrequencyInDoc() {
        return frequencyInDoc;
    }
}
