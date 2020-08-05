package wiki.data.relations;

import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Test;
import wiki.WikiToElasticMain;
import wiki.utils.LangConfiguration;

import java.io.FileReader;
import java.util.Objects;
import java.util.Set;

public class TestCategoryRelationExtractor {

    public static final Gson GSON = new Gson();

    @Test
    public void testChinessCategory() throws Exception {
        String langConfigFile = Objects.requireNonNull(WikiToElasticMain.class.getClassLoader().getResource("lang/zh.json")).getFile();
        LangConfiguration langConfiguration = GSON.fromJson(new FileReader(langConfigFile), LangConfiguration.class);
        CategoryRelationExtractor.initResources(langConfiguration);
        CategoryRelationExtractor categ = new CategoryRelationExtractor();
        Set<String> extract1 = categ.extract("[[Category:爵床科|L]]");
        Set<String> extract2 = categ.extract("[[Category:爵床科]]");
        Assert.assertNotEquals(0, extract1.size());
        Assert.assertNotEquals(0, extract2.size());
        Assert.assertEquals(extract1, extract2);
    }
}
