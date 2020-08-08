package wiki;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import wiki.data.TestRelationsBuilderAndPageParser;
import wiki.data.WikiParsedPageRelations;
import wiki.utils.LangConfiguration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TestUtils {
    public static final Gson GSON = new Gson();

    public static String getFileJsonContent(String fileName) {
        InputStream inputStreamNlp = TestRelationsBuilderAndPageParser.class.getClassLoader().getResourceAsStream(fileName);
        assert inputStreamNlp != null;
        return GSON.fromJson(new InputStreamReader(inputStreamNlp), JsonObject.class).get("text").getAsString();
    }

    public static JsonArray getFileJsonArray(String fileName) {
        InputStream inputStreamNlp = TestRelationsBuilderAndPageParser.class.getClassLoader().getResourceAsStream(fileName);
        assert inputStreamNlp != null;
        return GSON.fromJson(new InputStreamReader(inputStreamNlp), JsonArray.class);
    }

    public static List<String> getFileJsonListContent(String fileName) {
        List<String> resultList = new ArrayList<>();
        InputStream inputStreamNlp = TestRelationsBuilderAndPageParser.class.getClassLoader().getResourceAsStream(fileName);
        assert inputStreamNlp != null;
        JsonArray inputJsonNlp = GSON.fromJson(new InputStreamReader(inputStreamNlp), JsonArray.class);
        for(JsonElement elem : inputJsonNlp) {
            String text = elem.getAsJsonObject().get("text").getAsString();
            resultList.add(text);
        }
        return resultList;
    }

    public static WikiParsedPageRelations getFileRelationContent(String fileName) {
        InputStream resultStream = TestRelationsBuilderAndPageParser.class.getClassLoader().getResourceAsStream(fileName);
        assert resultStream != null;
        return GSON.fromJson(new InputStreamReader(resultStream), WikiParsedPageRelations.class);
    }

    public static LangConfiguration getLangConfig(String lang) throws FileNotFoundException {
        String langConfigFile = Objects.requireNonNull(WikiToElasticMain.class.getClassLoader().getResource("lang/" + lang + ".json")).getFile();
        return GSON.fromJson(new FileReader(langConfigFile), LangConfiguration.class);
    }

    private void writeResultToFile(String fileLocation, WikiParsedPageRelations relations) throws IOException {
        FileUtils.write(new File(fileLocation), GSON.toJson(relations), "UTF-8");
    }
}
