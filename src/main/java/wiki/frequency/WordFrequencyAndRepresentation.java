package wiki.frequency;

import edu.stanford.nlp.simple.Sentence;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wiki.utils.LangConfiguration;
import wiki.utils.WikiPageParser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WordFrequencyAndRepresentation {
    private final static Logger LOGGER = LogManager.getLogger(WikiPageParser.class);

    private static final double FREQUANCY_THREASH = 0.005;

    private final Map<String, WordFrequency> docWordsFrequency = new HashMap<>();
    private final AtomicInteger docWordsCount = new AtomicInteger(0);

    public static List<String> STOP_WORDS;

    public static void initResources(String lang) {
        if(STOP_WORDS == null) {
            InputStream stopWordsFile = Objects.requireNonNull(WikiPageParser.class.getClassLoader().getResourceAsStream("stop_words/" + lang + ".txt"));
            try {
                STOP_WORDS = IOUtils.readLines(stopWordsFile, StandardCharsets.UTF_8);
            } catch (IOException e) {
                LOGGER.error("failed to load STOP_WORDS", e);
            }
        }
    }

    public void countDocFrequency(String docString) {
        String[] splittedText = docString.split("\n");
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
            if (!token.isEmpty() && !STOP_WORDS.contains(token)) {
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
