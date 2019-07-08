package wiki.test;

import edu.stanford.nlp.simple.Sentence;
import wiki.utils.WikiPageParser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class WordFrequencyAndRepresentation {

    private static final double FREQUANCY_THREASH = 0.005;

    private Map<String, WordFrequency> docWordsFrequency = new HashMap<>();
    private AtomicInteger docWordsCount = new AtomicInteger(0);

    public void countDocFrequency(String docString) {
        String splittedText[] = docString.split("\n");
        for(String line : splittedText) {
            this.countLineFrequency(line);
        }

        double fcount = docWordsCount.get();
        Iterator<WordFrequency> wordIter = docWordsFrequency.values().iterator();
        while(wordIter.hasNext()) {
            WordFrequency wf = wordIter.next();
            double wfWordCount = wf.getWordDocCount();
            double frequencyInDoc = wfWordCount / fcount;
            if(frequencyInDoc < FREQUANCY_THREASH) {
                wordIter.remove();
            } else {
                wf.setFrequencyInDoc(frequencyInDoc);
            }
        }
    }

    private void countLineFrequency(String line) {
        String cleanText = line.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase().trim();
        String[] cleanTextSplit = cleanText.split(" ");
        for (String token : cleanTextSplit) {
            if (!token.isEmpty() && !WikiPageParser.STOP_WORDS.contains(token)) {
                String tokenLemma = new Sentence(token).lemma(0);
                if(docWordsFrequency.containsKey(token)) {
                    docWordsFrequency.get(token).incrementWordCount();
                } else {
                    WordFrequency wf = new WordFrequency(token, tokenLemma);
                    this.docWordsFrequency.put(token, wf);
                    docWordsCount.getAndIncrement();
                }
            }
        }
    }

    public Map<String, WordFrequency> getDocWordsFrequency() {
        return docWordsFrequency;
    }
}
