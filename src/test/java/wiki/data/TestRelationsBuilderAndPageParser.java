package wiki.data;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import wiki.data.obj.BeCompRelationResult;
import wiki.data.obj.LinkParenthesisPair;
import wiki.data.relations.BeCompRelationExtractor;
import wiki.data.relations.CategoryRelationExtractor;
import wiki.data.relations.IRelationsExtractor;
import wiki.data.relations.LinkAndParenthesisRelationExtractor;
import wiki.utils.WikiPageParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class TestRelationsBuilderAndPageParser {

    private static Gson gson = new Gson();

    @BeforeClass
    public static void initTests() {
        WikiPageParser.initResources();
        BeCompRelationExtractor.initResources();
    }

    @Test
    public void testPageAndRelationExtractPageLinks() {
        String text = getFileJsonContant("nlp_wiki_test_text.json");
        WikiParsedPageRelations jsonResult = getFileRelationContant("nlp_relation_result.json");
        WikiParsedPageRelationsBuilder builder = new WikiParsedPageRelationsBuilder();
        WikiParsedPageRelations wikiParsedPageRelations = builder.buildFromWikipediaPageText(text);
//        writeResultToFile("/Users/aeirew/workspace/WikipediaToElastic/src/frequency/resources/nlp_relation_result.json", wikiParsedPageRelations);
        Assert.assertEquals(jsonResult.getCategories(), wikiParsedPageRelations.getCategories());
        Assert.assertEquals(jsonResult.getCategoriesNorm(), wikiParsedPageRelations.getCategoriesNorm());
    }

    @Test
    public void testPageAndRelationExtractDisambiguation() {
        String text = getFileJsonContant("nlp_disambig_wiki_test_text.json");
        WikiParsedPageRelations jsonResult = getFileRelationContant("nlp_disambig_relation_result.json");
        WikiParsedPageRelationsBuilder builder = new WikiParsedPageRelationsBuilder();
        WikiParsedPageRelations wikiParsedPageRelations = builder.buildFromWikipediaPageText(text);
//        writeResultToFile("/Users/aeirew/workspace/WikipediaToElastic/src/frequency/resources/nlp_disambig_relation_result.json", wikiParsedPageRelations);
        Assert.assertEquals(jsonResult.getDisambiguationLinks(), wikiParsedPageRelations.getDisambiguationLinks());
        Assert.assertEquals(jsonResult.getDisambiguationLinksNorm(), wikiParsedPageRelations.getDisambiguationLinksNorm());
    }

    @Test
    public void textExtractCategories() {
        IRelationsExtractor<Set<String>> categoryExtract = new CategoryRelationExtractor();
        Set<String> categories = categoryExtract.extract("[[Category:Artificial intelligence]]");
        Assert.assertTrue(categories.contains("Artificial intelligence"));
    }

    @Test
    public void textExtractDisCategory() {
        IRelationsExtractor<Set<String>> categoryExtract = new CategoryRelationExtractor();
        Set<String> categories = categoryExtract.extract("{{disambiguation}}");
        Assert.assertTrue(categories.contains("disambiguation"));
    }

    @Test
    public void textExtractLinksAndParenthesis() {
        IRelationsExtractor<LinkParenthesisPair> linkParenthExtractor = new LinkAndParenthesisRelationExtractor();

        LinkParenthesisPair linkParenthesisPair1 = linkParenthExtractor.extract("[[Artificial intelligence|Machine Learning]]");
        Assert.assertTrue(linkParenthesisPair1.getLinks().contains("Artificial intelligence"));
        Assert.assertTrue(linkParenthesisPair1.getLinks().contains("Machine Learning"));

        LinkParenthesisPair linkParenthesisPair2 = linkParenthExtractor.extract("[[Artificial intelligence (Machine Learning)]]");
        Assert.assertTrue(linkParenthesisPair2.getLinks().contains("Artificial intelligence"));
        Assert.assertFalse(linkParenthesisPair2.getLinks().contains("Machine Learning"));
        Assert.assertTrue(linkParenthesisPair2.getParenthesis().contains("Machine Learning"));

        LinkParenthesisPair linkParenthesisPair3 = linkParenthExtractor.extract("[[Artificial intelligence(Machine Learning)]]");
        Assert.assertTrue(linkParenthesisPair3.getLinks().contains("Artificial intelligence"));
        Assert.assertFalse(linkParenthesisPair3.getLinks().contains("Machine Learning"));
        Assert.assertTrue(linkParenthesisPair3.getParenthesis().contains("Machine Learning"));
    }

    @Test
    public void testNormalizeString() {
        String result = WikiPageParser.normalizeString("The New Orleans Saints");
        Assert.assertEquals("new orlean saint", result);
    }

    @Test
    public void testExtractBeComp() {
        IRelationsExtractor<BeCompRelationResult> beCompExtractor = new BeCompRelationExtractor();

        final List<String> textsLists = getFileJsonListContant("is_a_wiki_pages_test.json");

        String[] jimIsA = {"Carrey", "Eugene Carrey", "James Eugene Carrey", "James Carrey", "Canadian-American actor",
                "actor", "comedian", "impressionist", "producer", "screenwriter"};
        List<String> jimList = new ArrayList<>(Arrays.asList(jimIsA));
        final String firstPageParagraph0 = WikiPageParser.extractFirstPageParagraph(textsLists.get(0));
        final BeCompRelationResult jimResultSet = beCompExtractor.extract(firstPageParagraph0);
        Assert.assertTrue(jimResultSet.getBeCompRelations().containsAll(jimList));
        Assert.assertTrue(jimList.containsAll(jimResultSet.getBeCompRelations()));

        String[] ellenIsA = {"Ellen Lee DeGeneres", "DeGeneres", "Lee DeGeneres", "Ellen DeGeneres", "American comedian", "activist",
                "actress", "comedian", "host", "LGBT activist", "producer", "television host", "writer"};
        List<String> ellenList = new ArrayList<>(Arrays.asList(ellenIsA));
        final String firstPageParagraph1 = WikiPageParser.extractFirstPageParagraph(textsLists.get(1));
        final BeCompRelationResult elenResultSet = beCompExtractor.extract(firstPageParagraph1);
        Assert.assertTrue(elenResultSet.getBeCompRelations().containsAll(ellenList));
        Assert.assertTrue(ellenList.containsAll(elenResultSet.getBeCompRelations()));

        String[] nlpIsA = {"area", "computer science", "artificial intelligence", "intelligence", "Natural-language processing", "processing"};
        List<String> nlpList = new ArrayList<>(Arrays.asList(nlpIsA));
        final String firstPageParagraph2 = WikiPageParser.extractFirstPageParagraph(textsLists.get(2));
        final BeCompRelationResult nlpResultSet = beCompExtractor.extract(firstPageParagraph2);
        Assert.assertTrue(nlpResultSet.getBeCompRelations().containsAll(nlpList));
        Assert.assertTrue(nlpList.containsAll(nlpResultSet.getBeCompRelations()));

        String[] ibmIsA = {"multinational American technology company", "company", "technology company", "IBM"};
        List<String> ibmList = new ArrayList<>(Arrays.asList(ibmIsA));
        final String firstPageParagraph3 = WikiPageParser.extractFirstPageParagraph(textsLists.get(3));
        final BeCompRelationResult imbResultSet = beCompExtractor.extract(firstPageParagraph3);
        Assert.assertTrue(imbResultSet.getBeCompRelations().containsAll(ibmList));
        Assert.assertTrue(ibmList.containsAll(imbResultSet.getBeCompRelations()));

        String[] nyIsA = {"New York", "City"};
        List<String> nyList = new ArrayList<>(Arrays.asList(nyIsA));
        final String firstPageParagraph4 = WikiPageParser.extractFirstPageParagraph(textsLists.get(4));
        final BeCompRelationResult nyResultSet = beCompExtractor.extract(firstPageParagraph4);
        Assert.assertTrue(nyResultSet.getBeCompRelations().containsAll(nyList));
        Assert.assertTrue(nyList.containsAll(nyResultSet.getBeCompRelations()));

        String[] abbaIsA = {"pop group", "Swedish pop group", "group", "ABBA"};
        List<String> abbaList = new ArrayList<>(Arrays.asList(abbaIsA));
        final String firstPageParagraph5 = WikiPageParser.extractFirstPageParagraph(textsLists.get(5));
        final BeCompRelationResult abbaResultSet = beCompExtractor.extract(firstPageParagraph5);
        Assert.assertTrue(abbaResultSet.getBeCompRelations().containsAll(abbaList));
        Assert.assertTrue(abbaList.containsAll(abbaResultSet.getBeCompRelations()));
    }

    @Test
    public void testBeSingleSent() {
        IRelationsExtractor<BeCompRelationResult> beCompExtractor = new BeCompRelationExtractor();

        String text = "'''Alabama''' is a [[U.S. state|state]] in the [[Southern United States|southeastern region]] of the [[United States]]. It is bordered by [[Tennessee]] to the north, [[Georgia (U.S. state)|Georgia]] to the east, [[Florida]] and the [[Gulf of Mexico]] to the south, and [[Mississippi]] to the west. Alabama is the [[List of U.S. states and territories by area|30th largest by area]] and the [[List of U.S. states and territories by population|24th-most populous]] of the [[List of U.S. states|U.S. states]]. With a total of {{convert|1500|mi|km}} of [[inland waterway]]s, Alabama has among the most of any state.<ref>{{cite web|title=Alabama Transportation Overview|url=https://www.edpa.org/wp-content/uploads/Alabama-Transportation-Overview-1.pdf|publisher=Economic Development Partnership of Alabama|accessdate=21 January 2017}}</ref>\n";
        String firstPar = WikiPageParser.extractFirstPageParagraph(text);
        final BeCompRelationResult jimResultSet = beCompExtractor.extract(firstPar);
        System.out.println(jimResultSet.getBeCompRelations().toArray());
    }

    public static String getFileJsonContant(String fileName) {
        InputStream inputStreamNlp = TestRelationsBuilderAndPageParser.class.getClassLoader().getResourceAsStream(fileName);
        JsonObject inputJsonNlp = gson.fromJson(new InputStreamReader(inputStreamNlp), JsonObject.class);
        String text = inputJsonNlp.get("text").getAsString();
        return text;
    }

    public static List<String> getFileJsonListContant(String fileName) {
        List<String> resultList = new ArrayList<>();
        InputStream inputStreamNlp = TestRelationsBuilderAndPageParser.class.getClassLoader().getResourceAsStream(fileName);
        JsonArray inputJsonNlp = gson.fromJson(new InputStreamReader(inputStreamNlp), JsonArray.class);
        for(JsonElement elem : inputJsonNlp) {
            String text = elem.getAsJsonObject().get("text").getAsString();
            resultList.add(text);
        }
        return resultList;
    }

    private WikiParsedPageRelations getFileRelationContant(String fileName) {
        InputStream resultStream = TestRelationsBuilderAndPageParser.class.getClassLoader().getResourceAsStream(fileName);
        WikiParsedPageRelations jsonResult = gson.fromJson(new InputStreamReader(resultStream), WikiParsedPageRelations.class);
        return jsonResult;
    }

    private void writeResultToFile(String fileLocation, WikiParsedPageRelations relations) throws IOException {
        FileUtils.write(new File(fileLocation), gson.toJson(relations), "UTF-8");
    }

}
