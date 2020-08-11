package wiki.data.relations;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import wiki.TestUtils;
import wiki.utils.LangConfiguration;

import java.io.FileNotFoundException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TestCategoryRelationExtractor {

    public static final Gson GSON = new Gson();

    @BeforeAll
    static void initTest() throws FileNotFoundException {
        LangConfiguration langConfig = TestUtils.getLangConfig("en");
        CategoryRelationExtractor.initResources(langConfig);
    }

    @Test
    public void testChinessCategory() throws Exception {
        CategoryRelationExtractor categ = new CategoryRelationExtractor();
        Set<String> extract1 = categ.extract("[[Category:爵床科|L]]").getResult();
        Set<String> extract2 = categ.extract("[[Category:爵床科]]").getResult();
        assertNotEquals(0, extract1.size());
        assertNotEquals(0, extract2.size());
        assertEquals(extract1, extract2);
    }

    @Test
    public void textExtractCategories() throws Exception {
        IRelationsExtractor<Set<String>> categoryExtract = new CategoryRelationExtractor();
        Set<String> categories = categoryExtract.extract("[[Category:Artificial intelligence]]").getResult();
        assertTrue(categories.contains("Artificial intelligence"));
    }

    @Test
    public void textExtractDisCategory() throws Exception {
        IRelationsExtractor<Set<String>> categoryExtract = new CategoryRelationExtractor();
        Set<String> categories1 = categoryExtract.extract("{{disambiguation}}").getResult();
        assertTrue(categories1.contains("disambiguation"));

        Set<String> categories2 = categoryExtract.extract("{{disambig}}").getResult();
        assertTrue(categories2.contains("disambig"));

        Set<String> categories3 = categoryExtract.extract("{{Disambiguation}}").getResult();
        assertTrue(categories3.contains("disambiguation"));

        Set<String> categories4 = categoryExtract.extract("{{Disambig}}").getResult();
        assertTrue(categories4.contains("disambig"));
    }
}
