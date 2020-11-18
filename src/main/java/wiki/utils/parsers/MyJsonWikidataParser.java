package wiki.utils.parsers;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wiki.data.WikiDataParsedPage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyJsonWikidataParser implements IWikidataJsonParser<List<WikiDataParsedPage>> {
    private final static Logger LOGGER = LogManager.getLogger(MyJsonWikidataParser.class);
    private final static Gson GSON = new Gson();

    @Override
    public List<WikiDataParsedPage> read(JsonReader reader) throws IOException {
        reader.setLenient(true);
        final List<WikiDataParsedPage> allParsedWikidataPages = new ArrayList<>();
        try {
            int counter = 0;
            reader.beginArray();
            while (reader.hasNext()) {
                counter++;
                if(counter % 1000 == 0) {
                    LOGGER.info("Pages parsed-" + counter);
                }

                WikiDataParsedPage wikidataPage = GSON.fromJson(reader, WikiDataParsedPage.class);
                if(isEmpty(wikidataPage)) {
                    continue;
                }

                allParsedWikidataPages.add(wikidataPage);
            }

            reader.endArray();
        } catch (IOException e) {
            LOGGER.error("Failed to run JSON parser instance", e);
            throw e;
        }

        return allParsedWikidataPages;
    }

    private boolean isEmpty(WikiDataParsedPage wikidataPage) {
        return ((wikidataPage.getPartOf()) == null || wikidataPage.getPartOf().isEmpty()) &&
                (wikidataPage.getHasPart() == null || wikidataPage.getHasPart().isEmpty()) &&
                (wikidataPage.getHasEffect() == null || wikidataPage.getHasEffect().isEmpty()) &&
                (wikidataPage.getHasCause() == null || wikidataPage.getHasCause().isEmpty()) &&
                (wikidataPage.getImmediateCauseOf() == null || wikidataPage.getImmediateCauseOf().isEmpty()) &&
                (wikidataPage.getHasImmediateCause() == null || wikidataPage.getHasImmediateCause().isEmpty());
    }
}
