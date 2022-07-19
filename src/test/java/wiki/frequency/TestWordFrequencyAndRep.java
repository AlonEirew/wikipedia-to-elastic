package wiki.frequency;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import wiki.TestUtils;
import wiki.WikiToElasticMain;
import wiki.config.MainConfiguration;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Objects;

public class TestWordFrequencyAndRep {
    private static final Gson GSON = new Gson();

    @Test
    public void testCountLineFrequency() throws FileNotFoundException {
        String testConfig = Objects.requireNonNull(WikiToElasticMain.class.getClassLoader().getResource("test_config.json")).getFile();
        MainConfiguration config = GSON.fromJson(new FileReader(testConfig), MainConfiguration.class);

        WordFrequencyAndRepresentation.initResources(config.getLang());
        WordFrequencyAndRepresentation wfar = new WordFrequencyAndRepresentation();
        String text = TestUtils.getFileJsonContent("nlp_wiki_test_text.json");
        wfar.countDocFrequency(text);

        System.out.println(GSON.toJson(wfar.getDocWordsFrequency()));
    }
}
