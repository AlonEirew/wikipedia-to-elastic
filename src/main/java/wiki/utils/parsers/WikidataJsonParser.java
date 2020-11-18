package wiki.utils.parsers;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wiki.data.WikiDataParsedPage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

public class WikidataJsonParser {
    private final static Logger LOGGER = LogManager.getLogger(WikidataJsonParser.class);
    private final static Gson GSON = new Gson();

    public <T> T parse(IWikidataJsonParser<T> parser, InputStreamReader inputStreamReader) throws Exception {
        T allParsedWikidataPages;
        try(JsonReader reader = GSON.newJsonReader(inputStreamReader)) {
            allParsedWikidataPages = parser.read(reader);
        } catch (Exception ex) {
            LOGGER.error("Failed to parse JSON file", ex);
            throw ex;
        }

        LOGGER.info("Done Loading Json!");
        return allParsedWikidataPages;
    }

    public void write(File outFile, List<WikiDataParsedPage> wikidataPages) throws IOException {
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(new FileOutputStream(outFile), StandardCharsets.UTF_8));
        writer.setIndent("    ");
        writePages(writer, wikidataPages);
        writer.close();
    }

    private void writePages(JsonWriter writer, List<WikiDataParsedPage> wikidataPages) throws IOException {
        int totalPage = wikidataPages.size();
        LOGGER.info("Start writing " + totalPage + " wikidataPages");
        writer.beginArray();
        for(int i = 0 ; i < wikidataPages.size() ; i++) {
            WikiDataParsedPage page = wikidataPages.get(i);
            writePage(writer, page);
            totalPage--;

            if (i % 1000 == 0) {
                LOGGER.info(totalPage + "- Pages remaining...");
            }
        }
        writer.endArray();
    }

    private void writePage(JsonWriter writer, WikiDataParsedPage page) throws IOException {
        writer.beginObject();
        writer.name("wikidataPageId").value(page.getWikidataPageId());
        writer.name("wikipediaLangPageTitle").value(page.getWikipediaLangPageTitle());
        writer.name("elasticPageId").value(page.getElasticPageId());

        writer.name("aliases");
        writeSet(writer, page.getAliases());

        writer.name("hasPart");
        writeSet(writer, page.getHasPart());

        writer.name("partOf");
        writeSet(writer, page.getPartOf());

        writer.name("hasCause");
        writeSet(writer, page.getHasCause());

        writer.name("hasEffect");
        writeSet(writer, page.getHasEffect());

        writer.name("hasImmediateCause");
        writeSet(writer, page.getHasImmediateCause());

        writer.name("immediateCauseOf");
        writeSet(writer, page.getImmediateCauseOf());

        writer.endObject();
    }

    private void writeSet(JsonWriter writer, Set<String> setToWriter) throws IOException {
        writer.beginArray();
        for (String value : setToWriter) {
            writer.value(value);
        }
        writer.endArray();
    }
}
