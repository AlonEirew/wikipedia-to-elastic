package wiki.data;

import com.google.gson.JsonArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import wiki.TestUtils;
import wiki.utils.LangConfiguration;
import wiki.utils.WikiPageParser;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestWikiPageParser {

    @BeforeAll
    public static void init() throws FileNotFoundException {
        LangConfiguration langConfig = TestUtils.getLangConfig("en");
    }

    @Test
    public void testExtractFirstParagraph() {
        JsonArray fileJsonContant = TestUtils.getFileJsonArray("first_paragraph.json");
        String firstPageParagraph = WikiPageParser.extractFirstPageParagraph(fileJsonContant.get(0).getAsJsonObject().get("text").getAsString());
        String expected = fileJsonContant.get(0).getAsJsonObject().get("expected").getAsString();
        assertEquals(expected, firstPageParagraph);
    }
}
