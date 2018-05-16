package wiki.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import wiki.utils.WikiPageParser;

import java.io.*;
import java.util.Set;

public class TestWikiParsedPageRelationsBuilder {

    private static Gson gson = new Gson();

    @Test
    public void testPageAndRelationExtractPageLinks() throws IOException {
        String text = getFileJsonContant("nlp_wiki_test_text.json");
        WikiParsedPageRelations jsonResult = getFileRelationContant("nlp_relation_result.json");
        WikiParsedPageRelationsBuilder builder = new WikiParsedPageRelationsBuilder();
        WikiParsedPageRelations wikiParsedPageRelations = builder.buildFromWikipediaPageText("Natural language processing", text);
//        writeResultToFile("/Users/aeirew/workspace/WikipediaToElastic/src/test/resources/nlp_relation_result.json", wikiParsedPageRelations);
        Assert.assertEquals(jsonResult, wikiParsedPageRelations);
    }

    @Test
    public void testPageAndRelationExtractDisambiguation() throws IOException {
        String text = getFileJsonContant("nlp_disambig_wiki_test_text.json");
        WikiParsedPageRelations jsonResult = getFileRelationContant("nlp_disambig_relation_result.json");
        WikiParsedPageRelationsBuilder builder = new WikiParsedPageRelationsBuilder();
        WikiParsedPageRelations wikiParsedPageRelations = builder.buildFromWikipediaPageText("NLP", text);
//        writeResultToFile("/Users/aeirew/workspace/WikipediaToElastic/src/test/resources/nlp_disambig_relation_result.json", wikiParsedPageRelations);
        Assert.assertEquals(jsonResult, wikiParsedPageRelations);
    }

    @Test
    public void textExtractCategories() {
        Set<String> categories = WikiPageParser.extractCategories("NP", "[[Category:Artificial intelligence]]");
        Assert.assertTrue(categories.contains("Artificial intelligence"));
    }

    @Test
    public void textExtractDisCategory() {
        Set<String> categories = WikiPageParser.extractCategories("NP","{{disambiguation}}");
        Assert.assertTrue(categories.contains("disambiguation"));
    }

    @Test
    public void textExtractLinksAndParenthesis() {
        LinkParenthesisPair linkParenthesisPair1 = WikiPageParser.extractLinksAndParenthesis("[[Artificial intelligence|Machine Learning]]");
        Assert.assertTrue(linkParenthesisPair1.getLinks().contains("Artificial intelligence"));
        Assert.assertTrue(linkParenthesisPair1.getLinks().contains("Machine Learning"));

        LinkParenthesisPair linkParenthesisPair2 = WikiPageParser.extractLinksAndParenthesis("[[Artificial intelligence (Machine Learning)]]");
        Assert.assertTrue(linkParenthesisPair2.getLinks().contains("Artificial intelligence"));
        Assert.assertFalse(linkParenthesisPair2.getLinks().contains("Machine Learning"));
        Assert.assertTrue(linkParenthesisPair2.getParenthesis().contains("Machine Learning"));

        LinkParenthesisPair linkParenthesisPair3 = WikiPageParser.extractLinksAndParenthesis("[[Artificial intelligence(Machine Learning)]]");
        Assert.assertTrue(linkParenthesisPair3.getLinks().contains("Artificial intelligence"));
        Assert.assertFalse(linkParenthesisPair3.getLinks().contains("Machine Learning"));
        Assert.assertTrue(linkParenthesisPair3.getParenthesis().contains("Machine Learning"));
    }

    public static String getFileJsonContant(String fileName) {
        InputStream inputStreamNlp = TestWikiParsedPageRelationsBuilder.class.getClassLoader().getResourceAsStream(fileName);
        JsonObject inputJsonNlp = gson.fromJson(new InputStreamReader(inputStreamNlp), JsonObject.class);
        String text = inputJsonNlp.get("text").getAsString();
        return text;
    }

    private WikiParsedPageRelations getFileRelationContant(String fileName) {
        InputStream resultStream = TestWikiParsedPageRelationsBuilder.class.getClassLoader().getResourceAsStream(fileName);
        WikiParsedPageRelations jsonResult = gson.fromJson(new InputStreamReader(resultStream), WikiParsedPageRelations.class);
        return jsonResult;
    }

    private void writeResultToFile(String fileLocation, WikiParsedPageRelations relations) throws IOException {
        FileUtils.write(new File(fileLocation), gson.toJson(relations), "UTF-8");
    }
}
