package wiki.utils;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import wiki.WikiToElasticMain;
import wiki.handlers.ArrayPageHandler;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestWikiToElasticUtils {

    private static final Gson GSON = new Gson();
    private static LangConfiguration langConfig;

    @BeforeAll
    public static void initTest() throws FileNotFoundException {
        String testConfig = Objects.requireNonNull(WikiToElasticMain.class.getClassLoader().getResource("test_config.json")).getFile();
        WikiToElasticConfiguration config = GSON.fromJson(new FileReader(testConfig), WikiToElasticConfiguration.class);
        String langConfigFile = Objects.requireNonNull(WikiToElasticMain.class.getClassLoader().getResource("lang/" + config.getLang() + ".json")).getFile();
        langConfig = GSON.fromJson(new FileReader(langConfigFile), LangConfiguration.class);
    }

    @Test
    public void testParseWikipediaXMLdump() throws IOException {
        InputStream inputStream = null;
        try {
            long startTime = System.currentTimeMillis();
            TestWikiToElasticUtils tu = new TestWikiToElasticUtils();
            URL resource = tu.getClass().getClassLoader().getResource("tinywiki-latest-pages-articles.xml.bz2");
            if(resource != null) {
                String tinyWikifile = resource.getFile();
                inputStream = WikiToElasticUtils.openCompressedFileInputStream(tinyWikifile);

                ArrayPageHandler arrayPageHandler = new ArrayPageHandler();
                STAXParser parser = new STAXParser(arrayPageHandler, langConfig);
                parser.parse(inputStream);

                System.out.println("total time took to parse: " + (System.currentTimeMillis() - startTime));
                String output = String.valueOf(arrayPageHandler.getPages().size());
                assertNotNull(output);
                assertFalse(output.isEmpty());
            }
        } finally {
            WikiToElasticUtils.closeCompressedFileInputStream(inputStream);
        }
    }
}
