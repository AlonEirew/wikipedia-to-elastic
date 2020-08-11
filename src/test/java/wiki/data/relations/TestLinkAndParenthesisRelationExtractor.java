package wiki.data.relations;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import wiki.TestUtils;
import wiki.utils.LangConfiguration;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestLinkAndParenthesisRelationExtractor {

    @BeforeAll
    static void initTest() throws FileNotFoundException {
        LangConfiguration langConfig = TestUtils.getLangConfig("en");
        LinkAndParenthesisRelationExtractor.initResources(langConfig);
    }

    @Test
    public void textExtractLinksAndParenthesis() throws Exception {
        LinkAndParenthesisRelationExtractor linkParenthExtractor1 = new LinkAndParenthesisRelationExtractor();

        linkParenthExtractor1.extract("[[Artificial intelligence|Machine Learning]]");
        assertTrue(linkParenthExtractor1.getLinks().contains("Artificial intelligence"));
        assertTrue(linkParenthExtractor1.getLinks().contains("Machine Learning"));

        LinkAndParenthesisRelationExtractor linkParenthExtractor2 = new LinkAndParenthesisRelationExtractor();
        linkParenthExtractor2.extract("[[Artificial intelligence (Machine Learning)]]");
        assertTrue(linkParenthExtractor2.getLinks().contains("Artificial intelligence"));
        assertFalse(linkParenthExtractor2.getLinks().contains("Machine Learning"));
        assertTrue(linkParenthExtractor2.getTitleParenthesis().contains("Machine Learning"));

        LinkAndParenthesisRelationExtractor linkParenthExtractor3 = new LinkAndParenthesisRelationExtractor();
        linkParenthExtractor3.extract("[[Artificial intelligence(Machine Learning)]]");
        assertTrue(linkParenthExtractor3.getLinks().contains("Artificial intelligence"));
        assertFalse(linkParenthExtractor3.getLinks().contains("Machine Learning"));
        assertTrue(linkParenthExtractor3.getTitleParenthesis().contains("Machine Learning"));
    }
}
