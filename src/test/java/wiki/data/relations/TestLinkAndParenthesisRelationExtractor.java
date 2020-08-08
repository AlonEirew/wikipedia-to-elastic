package wiki.data.relations;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import wiki.TestUtils;
import wiki.data.obj.LinkParenthesisPair;
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
        IRelationsExtractor<LinkParenthesisPair> linkParenthExtractor = new LinkAndParenthesisRelationExtractor();

        LinkParenthesisPair linkParenthesisPair1 = linkParenthExtractor.extract("[[Artificial intelligence|Machine Learning]]");
        assertTrue(linkParenthesisPair1.getLinks().contains("Artificial intelligence"));
        assertTrue(linkParenthesisPair1.getLinks().contains("Machine Learning"));

        LinkParenthesisPair linkParenthesisPair2 = linkParenthExtractor.extract("[[Artificial intelligence (Machine Learning)]]");
        assertTrue(linkParenthesisPair2.getLinks().contains("Artificial intelligence"));
        assertFalse(linkParenthesisPair2.getLinks().contains("Machine Learning"));
        assertTrue(linkParenthesisPair2.getParenthesis().contains("Machine Learning"));

        LinkParenthesisPair linkParenthesisPair3 = linkParenthExtractor.extract("[[Artificial intelligence(Machine Learning)]]");
        assertTrue(linkParenthesisPair3.getLinks().contains("Artificial intelligence"));
        assertFalse(linkParenthesisPair3.getLinks().contains("Machine Learning"));
        assertTrue(linkParenthesisPair3.getParenthesis().contains("Machine Learning"));
    }
}
