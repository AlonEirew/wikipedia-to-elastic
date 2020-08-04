package wiki.frequency;

import com.google.gson.Gson;
import org.junit.Test;
import wiki.WikiToElasticMain;
import wiki.data.TestRelationsBuilderAndPageParser;
import wiki.utils.LangConfiguration;
import wiki.utils.WikiToElasticConfiguration;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Objects;

public class TestWordFrequencyAndRep {
    private static final Gson GSON = new Gson();

    @Test
    public void testCountLineFrequency() throws FileNotFoundException {
        String testConfig = Objects.requireNonNull(WikiToElasticMain.class.getClassLoader().getResource("test_config.json")).getFile();
        WikiToElasticConfiguration config = GSON.fromJson(new FileReader(testConfig), WikiToElasticConfiguration.class);
        String langConfigFile = Objects.requireNonNull(WikiToElasticMain.class.getClassLoader().getResource("lang/" + config.getLang() + ".json")).getFile();
        LangConfiguration langConfiguration = GSON.fromJson(new FileReader(langConfigFile), LangConfiguration.class);

        WordFrequencyAndRepresentation wfar = new WordFrequencyAndRepresentation(langConfiguration, config.getLang());
        String text = TestRelationsBuilderAndPageParser.getFileJsonContant("nlp_wiki_test_text.json");
        wfar.countDocFrequency(text);

        System.out.println(GSON.toJson(wfar.getDocWordsFrequency()));
    }
}
