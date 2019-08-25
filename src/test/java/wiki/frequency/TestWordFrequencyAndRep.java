package wiki.frequency;

import com.google.gson.Gson;
import org.junit.Test;
import wiki.data.TestRelationsBuilderAndPageParser;

public class TestWordFrequencyAndRep {
    private Gson gson = new Gson();

    @Test
    public void testCountLineFrequency() {
        WordFrequencyAndRepresentation wfar = new WordFrequencyAndRepresentation();
        String text = TestRelationsBuilderAndPageParser.getFileJsonContant("nlp_wiki_test_text.json");
        wfar.countDocFrequency(text);

        System.out.println(gson.toJson(wfar.getDocWordsFrequency()));
    }
}
