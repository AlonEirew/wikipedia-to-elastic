package wiki.data;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import wiki.TestUtils;
import wiki.WikiToElasticMain;
import wiki.data.obj.BeCompRelationResult;
import wiki.data.relations.BeCompRelationExtractor;
import wiki.data.relations.ExtractorsManager;
import wiki.data.relations.IRelationsExtractor;
import wiki.utils.LangConfiguration;
import wiki.utils.WikiPageParser;
import wiki.utils.WikiToElasticConfiguration;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Objects;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestRelationsBuilderAndPageParser {

    @BeforeAll
    public static void initTests() throws FileNotFoundException {
        String testConfig = Objects.requireNonNull(WikiToElasticMain.class.getClassLoader().getResource("test_config.json")).getFile();
        WikiToElasticConfiguration config = TestUtils.GSON.fromJson(new FileReader(testConfig), WikiToElasticConfiguration.class);
        String langConfigFile = Objects.requireNonNull(WikiToElasticMain.class.getClassLoader().getResource("lang/" + config.getLang() + ".json")).getFile();
        LangConfiguration langConfiguration = TestUtils.GSON.fromJson(new FileReader(langConfigFile), LangConfiguration.class);

        ExtractorsManager.initExtractors(config, langConfiguration);
    }

    @Test
    public void testPageAndRelationExtractPageLinks() throws Exception {
        String text = TestUtils.getFileJsonContent("nlp_wiki_test_text.json");
        WikiParsedPageRelations jsonResult = TestUtils.getFileRelationContent("nlp_relation_result.json");
        WikiParsedPageRelationsBuilder builder = new WikiParsedPageRelationsBuilder();
        WikiParsedPageRelations wikiParsedPageRelations = builder.buildFromText(text);
//        writeResultToFile("/Users/aeirew/workspace/WikipediaToElastic/src/frequency/resources/nlp_relation_result.json", wikiParsedPageRelations);
        assertEquals(jsonResult.getCategories(), wikiParsedPageRelations.getCategories());
    }

    @Test
    public void testBeSingleSent() throws Exception {
        IRelationsExtractor<BeCompRelationResult> beCompExtractor = new BeCompRelationExtractor();

        String text = "'''Alabama''' is a [[U.S. state|state]] in the [[Southern United States|southeastern region]] of the [[United States]]. It is bordered by [[Tennessee]] to the north, [[Georgia (U.S. state)|Georgia]] to the east, [[Florida]] and the [[Gulf of Mexico]] to the south, and [[Mississippi]] to the west. Alabama is the [[List of U.S. states and territories by area|30th largest by area]] and the [[List of U.S. states and territories by population|24th-most populous]] of the [[List of U.S. states|U.S. states]]. With a total of {{convert|1500|mi|km}} of [[inland waterway]]s, Alabama has among the most of any state.<ref>{{cite web|title=Alabama Transportation Overview|url=https://www.edpa.org/wp-content/uploads/Alabama-Transportation-Overview-1.pdf|publisher=Economic Development Partnership of Alabama|accessdate=21 January 2017}}</ref>\n";
        String firstPar = WikiPageParser.extractFirstPageParagraph(text);
        final BeCompRelationResult jimResultSet = beCompExtractor.extract(firstPar).getResult();
        System.out.println(Arrays.toString(jimResultSet.getBeCompRelations().toArray()));
    }

    @Test
    public void testPageAndRelationExtractDisambiguation() throws Exception {
        String text = TestUtils.getFileJsonContent("nlp_disambig_wiki_test_text.json");
        WikiParsedPageRelations jsonResult = TestUtils.getFileRelationContent("nlp_disambig_relation_result.json");
        WikiParsedPageRelationsBuilder builder = new WikiParsedPageRelationsBuilder();
        WikiParsedPageRelations wikiParsedPageRelations = builder.buildFromText(text);
//        writeResultToFile("/Users/aeirew/workspace/WikipediaToElastic/src/frequency/resources/nlp_disambig_relation_result.json", wikiParsedPageRelations);
        assertEquals(new TreeSet<>(wikiParsedPageRelations.getDisambiguationLinks()), new TreeSet<>(jsonResult.getDisambiguationLinks()));

    }
}
