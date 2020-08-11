package wiki.data.relations;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import wiki.TestUtils;
import wiki.data.obj.BeCompRelationResult;
import wiki.utils.LangConfiguration;
import wiki.utils.WikiPageParser;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestBeCompRelationExtraction {

//    @BeforeAll
    public static void initResources() throws FileNotFoundException {
        LangConfiguration langConfig = TestUtils.getLangConfig("en");
        BeCompRelationExtractor.initResources(langConfig);
    }

//    @Test
    public void testExtractBeComp() throws Exception {
        IRelationsExtractor<BeCompRelationResult> beCompExtractor = new BeCompRelationExtractor();

        final List<String> textsLists = TestUtils.getFileJsonListContent("is_a_wiki_pages_test.json");

        String[] jimIsA = {"Carrey", "Eugene Carrey", "James Eugene Carrey", "James Carrey", "Canadian-American actor",
                "actor", "comedian", "impressionist", "producer", "screenwriter"};
        List<String> jimList = new ArrayList<>(Arrays.asList(jimIsA));
        final String firstPageParagraph0 = WikiPageParser.extractFirstPageParagraph(textsLists.get(0));
        final BeCompRelationResult jimResultSet = beCompExtractor.extract(firstPageParagraph0).getResult();
        assertEquals(new TreeSet<>(jimList), new TreeSet<>(jimResultSet.getBeCompRelations()));

        String[] ellenIsA = {"Ellen Lee DeGeneres", "DeGeneres", "Lee DeGeneres", "Ellen DeGeneres", "American comedian", "activist",
                "actress", "comedian", "host", "LGBT activist", "producer", "television host", "writer"};
        List<String> ellenList = new ArrayList<>(Arrays.asList(ellenIsA));
        final String firstPageParagraph1 = WikiPageParser.extractFirstPageParagraph(textsLists.get(1));
        final BeCompRelationResult elenResultSet = beCompExtractor.extract(firstPageParagraph1).getResult();
        assertTrue(elenResultSet.getBeCompRelations().containsAll(ellenList));
        assertTrue(ellenList.containsAll(elenResultSet.getBeCompRelations()));

        String[] nlpIsA = {"area", "computer science", "artificial intelligence", "intelligence", "Natural-language processing", "processing"};
        List<String> nlpList = new ArrayList<>(Arrays.asList(nlpIsA));
        final String firstPageParagraph2 = WikiPageParser.extractFirstPageParagraph(textsLists.get(2));
        final BeCompRelationResult nlpResultSet = beCompExtractor.extract(firstPageParagraph2).getResult();
        assertTrue(nlpResultSet.getBeCompRelations().containsAll(nlpList));
        assertTrue(nlpList.containsAll(nlpResultSet.getBeCompRelations()));

        String[] ibmIsA = {"multinational American technology company", "company", "technology company", "IBM"};
        List<String> ibmList = new ArrayList<>(Arrays.asList(ibmIsA));
        final String firstPageParagraph3 = WikiPageParser.extractFirstPageParagraph(textsLists.get(3));
        final BeCompRelationResult imbResultSet = beCompExtractor.extract(firstPageParagraph3).getResult();
        assertTrue(imbResultSet.getBeCompRelations().containsAll(ibmList));
        assertTrue(ibmList.containsAll(imbResultSet.getBeCompRelations()));

        String[] nyIsA = {"New York", "City"};
        List<String> nyList = new ArrayList<>(Arrays.asList(nyIsA));
        final String firstPageParagraph4 = WikiPageParser.extractFirstPageParagraph(textsLists.get(4));
        final BeCompRelationResult nyResultSet = beCompExtractor.extract(firstPageParagraph4).getResult();
        assertTrue(nyResultSet.getBeCompRelations().containsAll(nyList));
        assertTrue(nyList.containsAll(nyResultSet.getBeCompRelations()));

        String[] abbaIsA = {"pop group", "Swedish pop group", "group", "ABBA"};
        List<String> abbaList = new ArrayList<>(Arrays.asList(abbaIsA));
        final String firstPageParagraph5 = WikiPageParser.extractFirstPageParagraph(textsLists.get(5));
        final BeCompRelationResult abbaResultSet = beCompExtractor.extract(firstPageParagraph5).getResult();
        assertTrue(abbaResultSet.getBeCompRelations().containsAll(abbaList));
        assertTrue(abbaList.containsAll(abbaResultSet.getBeCompRelations()));
    }
}
