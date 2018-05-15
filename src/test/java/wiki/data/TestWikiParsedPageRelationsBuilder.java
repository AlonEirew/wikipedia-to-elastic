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
        WikiParsedPageRelations wikiParsedPageRelations = builder.buildFromWikipediaPageText(text);
        Assert.assertEquals(jsonResult, wikiParsedPageRelations);
    }

    @Test
    public void testPageAndRelationExtractDisambiguation() throws IOException {
        String text = getFileJsonContant("nlp_disambig_wiki_test_text.json");
        WikiParsedPageRelations jsonResult = getFileRelationContant("nlp_disambig_relation_result.json");
        WikiParsedPageRelationsBuilder builder = new WikiParsedPageRelationsBuilder();
        WikiParsedPageRelations wikiParsedPageRelations = builder.buildFromWikipediaPageText(text);
        Assert.assertEquals(jsonResult, wikiParsedPageRelations);
    }

    @Test
    public void textExtractCategories() {
        Set<String> categories = WikiPageParser.extractCategories("[[Category:Artificial intelligence]]");
        Assert.assertTrue(categories.contains("Artificial intelligence"));
    }

    @Test
    public void textExtractDisCategory() {
        Set<String> categories = WikiPageParser.extractCategories("{{disambiguation}}");
        Assert.assertTrue(categories.contains("disambiguation"));
    }

    @Test
    public void textExtractLinks() {
        Set<String> categories = WikiPageParser.extractLinks("[[Artificial intelligence|Machine Learning]]");
        Assert.assertTrue(categories.contains("Artificial intelligence"));
        Assert.assertTrue(categories.contains("Machine Learning"));
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

    private void writeResultToFile(WikiParsedPageRelations relations) throws IOException {
        FileUtils.write(new File("/Users/aeirew/workspace/WikipediaToElastic/src/test/resources/nlp_disambig_relation_result.json"), gson.toJson(relations), "UTF-8");
    }
}
