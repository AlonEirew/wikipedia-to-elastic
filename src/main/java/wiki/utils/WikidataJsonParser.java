package wiki.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wiki.data.WikiDataParsedPage;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class WikidataJsonParser {
    private final static Logger LOGGER = LogManager.getLogger(WikidataJsonParser.class);

    private final static Gson GSON = new Gson();

    private final static String PART_OF = "P361";
    private final static String HAS_PART = "P527";
    private final static String HAS_EFFECT = "P1542";
    private final static String HAS_CAUSE = "P828";
    private final static String IMMEDIATE_CAUSE_OF = "P1536";
    private final static String HAS_IMMEDIATE_CAUSE = "P1478";

    private final AtomicInteger counter = new AtomicInteger(1);

    public Map<String, WikiDataParsedPage> parse(File inputDump, final String lang) throws IOException {
        ExecutorService executorService = SimpleExecutorService.initExecutorService();
        final Map<String, WikiDataParsedPage> allParsedWikidataPages = new ConcurrentHashMap<>();

        try(final JsonReader reader = new JsonReader(new InputStreamReader(WikiToElasticUtils.openCompressedFileInputStream(
                        inputDump.getPath()), StandardCharsets.UTF_8))) {
            reader.beginArray();
            while (reader.hasNext()) {
                JsonObject message = GSON.fromJson(reader, JsonObject.class);
                executorService.submit(() -> {
                    JsonObject labels = message.get("labels").getAsJsonObject();
                    JsonObject pageLang = labels.getAsJsonObject(lang);
                    if(pageLang != null) {
                        String wikidataPageId = message.get("id").getAsString();
                        String wikipediaPageLangTitle = pageLang.get("value").getAsString();
                        if(isValidPage(wikipediaPageLangTitle)) {
                            WikiDataParsedPage wikidataParsedPage = new WikiDataParsedPage(wikidataPageId, wikipediaPageLangTitle);

                            JsonObject aliasesJson = message.get("aliases").getAsJsonObject();
                            JsonObject claims = message.get("claims").getAsJsonObject();

                            wikidataParsedPage.setAliases(getAliasesFromJsonArray(aliasesJson.getAsJsonArray(lang)));
                            wikidataParsedPage.setPartOf(getClaimsFromJsonArray(claims.getAsJsonArray(PART_OF)));
                            wikidataParsedPage.setHasPart(getClaimsFromJsonArray(claims.getAsJsonArray(HAS_PART)));
                            wikidataParsedPage.setHasEffect(getClaimsFromJsonArray(claims.getAsJsonArray(HAS_EFFECT)));
                            wikidataParsedPage.setHasCause(getClaimsFromJsonArray(claims.getAsJsonArray(HAS_CAUSE)));
                            wikidataParsedPage.setHasImmediateCause(getClaimsFromJsonArray(claims.getAsJsonArray(HAS_IMMEDIATE_CAUSE)));
                            wikidataParsedPage.setImmediateCauseOf(getClaimsFromJsonArray(claims.getAsJsonArray(IMMEDIATE_CAUSE_OF)));

                            if(!wikidataParsedPage.isEmpty()) {
                                allParsedWikidataPages.put(wikidataPageId, wikidataParsedPage);
                            }
                        }
                    }

                    int currentCount = counter.incrementAndGet();

                    if (currentCount % 1000 == 0) {
                        LOGGER.debug("Wikidata " + currentCount + " pages processed");
                    }
                });
            }

            reader.endArray();
        }

        SimpleExecutorService.shutDownPool(executorService);
        LOGGER.info("Done Loading Json!");
        return allParsedWikidataPages;
    }

    private boolean isValidPage(String title) {
        return !(title.startsWith("Wikipedia:") || title.startsWith("Template:") || title.startsWith("Category:") ||
                title.startsWith("Help:") || StringUtils.isNumericSpace(title) || title.length() == 1);
    }

    private Set<String> getClaimsFromJsonArray(JsonArray array) {
        Set<String> retList = new HashSet<>();
        if(array != null) {
            for (int i = 0; i < array.size(); i++) {
                JsonObject datavalue = array.get(i).getAsJsonObject().getAsJsonObject("mainsnak").getAsJsonObject("datavalue");
                if (datavalue != null) {
                    JsonObject value = datavalue.getAsJsonObject("value");
                    if(value != null) {
                        String id = value.get("id").getAsString();
                        retList.add(id);
                    }
                }
            }
        }

        if(retList.isEmpty()) {
            return null;
        }

        return retList;
    }

    private Set<String> getAliasesFromJsonArray(JsonArray array) {
        Set<String> retList = new HashSet<>();
        if(array != null) {
            for (int i = 0; i < array.size(); i++) {
                JsonObject elem = array.get(i).getAsJsonObject();
                if (elem.get("language").getAsString().equals("en")) {
                    String aliasValue = elem.get("value").getAsString();
                    retList.add(aliasValue);
                }
            }
        }

        if(retList.isEmpty()) {
            return null;
        }

        return retList;
    }
}
