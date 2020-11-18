package wiki.wikidata;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wiki.data.WikiDataParsedPage;
import wiki.utils.parsers.MyJsonWikidataParser;
import wiki.utils.parsers.WikidataJsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ReadParsedWikidataJson {
    private final static Logger LOGGER = LogManager.getLogger(ReadParsedWikidataJson.class);
    private final static Gson GSON = new Gson();

    public static void main(String[] args) throws Exception {
        WikidataJsonParser parser = new WikidataJsonParser();
        File file = new File("dumps/pased_wikidata/wikidata_parsed_out_orig.json");
        MyJsonWikidataParser myparser = new MyJsonWikidataParser();
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
        List<WikiDataParsedPage> pageList = parser.parse(myparser, inputStreamReader);

        LOGGER.info("Found relevant pages-" + pageList.size());
        printDist(pageList);
    }

    private static void printDist(List<WikiDataParsedPage> pageList) {
        String Effect = "Effect";
        String Cause = "Cause";
        String HasPart = "HasPart";
        String PartOf = "PartOf";
        String ImmCauseOf = "ImmCauseOf";
        String HasImmCause = "HasImmCause";
        Map<String, AtomicInteger> distMap = new HashMap<>();
        distMap.put(Effect, new AtomicInteger());
        distMap.put(Cause, new AtomicInteger());
        distMap.put(HasPart, new AtomicInteger());
        distMap.put(PartOf, new AtomicInteger());
        distMap.put(ImmCauseOf, new AtomicInteger());
        distMap.put(HasImmCause, new AtomicInteger());
        for (WikiDataParsedPage page : pageList) {
            if(page.getPartOf() != null && !page.getPartOf().isEmpty()) {
                distMap.get(PartOf).incrementAndGet();
            }

            if(page.getHasPart() != null && !page.getHasPart().isEmpty()) {
                distMap.get(HasPart).incrementAndGet();
            }

            if(page.getHasCause() != null && !page.getHasCause().isEmpty()) {
                distMap.get(Cause).incrementAndGet();
            }

            if(page.getHasEffect() != null && !page.getHasEffect().isEmpty()) {
                distMap.get(Effect).incrementAndGet();
            }

            if(page.getImmediateCauseOf() != null && !page.getImmediateCauseOf().isEmpty()) {
                distMap.get(ImmCauseOf).incrementAndGet();
            }

            if(page.getHasImmediateCause() != null && !page.getHasImmediateCause().isEmpty()) {
                distMap.get(HasImmCause).incrementAndGet();
            }
        }

        LOGGER.info(GSON.toJson(distMap));
    }
}
