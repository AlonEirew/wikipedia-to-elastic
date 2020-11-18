package wiki.utils.parsers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wiki.data.WikiDataParsedPage;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class WikidataParseThread implements IWikidataJsonParser<Map<String, WikiDataParsedPage>> {
    private final static Logger LOGGER = LogManager.getLogger(WikidataParseThread.class);
    private final static Gson GSON = new Gson();
    private final static AtomicInteger counter = new AtomicInteger(1);

    private final static String PART_OF = "P361";
    private final static String HAS_PART = "P527";
    private final static String HAS_EFFECT = "P1542";
    private final static String HAS_CAUSE = "P828";
    private final static String IMMEDIATE_CAUSE_OF = "P1536";
    private final static String HAS_IMMEDIATE_CAUSE = "P1478";

    private final String lang;

    public WikidataParseThread(String lang) {
        this.lang = lang;
    }

    public Map<String, WikiDataParsedPage> read(JsonReader reader) {
        reader.setLenient(true);
        final Map<String, WikiDataParsedPage> allParsedWikidataPages = new HashMap<>();
        try {
            reader.beginArray();
            while (reader.hasNext()) {
                runInterval(reader, allParsedWikidataPages);
            }

            reader.endArray();
        } catch (IOException e) {
            LOGGER.error("Failed to run JSON parser instance", e);
        }

        return allParsedWikidataPages;
    }

    private void runInterval(JsonReader reader, Map<String, WikiDataParsedPage> allParsedWikidataPages) throws IOException {
        while (reader.hasNext()) {
            try {
                WikiDataParsedPage wikiDataParsedPage = readWikidataPage(GSON.fromJson(reader, JsonObject.class));
                if (wikiDataParsedPage.isValid()) {
                    allParsedWikidataPages.put(wikiDataParsedPage.getWikidataPageId(), wikiDataParsedPage);
                }

                int currentCount = counter.incrementAndGet();
                if (currentCount % 1000 == 0) {
                    LOGGER.debug("Wikidata " + currentCount + " pages processed");
                }

            } catch (Exception ex) {
                LOGGER.error("Failed to parse Json object", ex);
            }
        }
    }

    private WikiDataParsedPage readWikidataPage(JsonObject message) {
        WikiDataParsedPage wikidataParsedPage = new WikiDataParsedPage();
        JsonObject labels = message.get("labels").getAsJsonObject();
        JsonObject pageLang = labels.getAsJsonObject(this.lang);
        if(pageLang != null) {
            String wikidataPageId = message.get("id").getAsString();
//            LOGGER.debug(wikidataPageId);
            String wikipediaPageLangTitle = pageLang.get("value").getAsString();

            wikidataParsedPage.setWikipediaLangPageTitle(wikipediaPageLangTitle);
            wikidataParsedPage.setWikidataPageId(wikidataPageId);

            JsonElement aliasesElement = message.get("aliases");
            if(aliasesElement != null) {
                wikidataParsedPage.setAliases(getAliasesFromJsonArray(aliasesElement.getAsJsonObject().getAsJsonArray(lang)));
            }

            JsonElement claimsElement = message.get("claims");
            if(claimsElement != null) {
                extractClaims(claimsElement.getAsJsonObject(), wikidataParsedPage);
            }
        }

        return wikidataParsedPage;
    }

    private void extractClaims(JsonObject claims, WikiDataParsedPage wikidataParsedPage) {
        JsonArray partOf = claims.getAsJsonArray(PART_OF);
        if(partOf != null) {
            wikidataParsedPage.setPartOf(getClaimsFromJsonArray(partOf));
        }

        JsonArray hasPart = claims.getAsJsonArray(HAS_PART);
        if(hasPart != null) {
            wikidataParsedPage.setHasPart(getClaimsFromJsonArray(hasPart));
        }

        JsonArray hasEffect = claims.getAsJsonArray(HAS_EFFECT);
        if(hasEffect != null) {
            wikidataParsedPage.setHasEffect(getClaimsFromJsonArray(hasEffect));
        }

        JsonArray hasCause = claims.getAsJsonArray(HAS_CAUSE);
        if(hasCause != null) {
            wikidataParsedPage.setHasCause(getClaimsFromJsonArray(hasCause));
        }

        JsonArray hasImmCause = claims.getAsJsonArray(HAS_IMMEDIATE_CAUSE);
        if(hasImmCause != null) {
            wikidataParsedPage.setHasImmediateCause(getClaimsFromJsonArray(hasImmCause));
        }

        JsonArray immCauseOf = claims.getAsJsonArray(IMMEDIATE_CAUSE_OF);
        if(immCauseOf != null) {
            wikidataParsedPage.setImmediateCauseOf(getClaimsFromJsonArray(immCauseOf));
        }
    }

    private Set<String> getClaimsFromJsonArray(JsonArray array) {
        Set<String> retList = new HashSet<>();
        if(array != null) {
            for (JsonElement elem : array) {
                JsonObject mainsnak = elem.getAsJsonObject().getAsJsonObject("mainsnak");
                if(mainsnak != null) {
                    JsonObject datavalue = mainsnak.getAsJsonObject("datavalue");
                    if (datavalue != null) {
                        JsonObject value = datavalue.getAsJsonObject("value");
                        if (value != null) {
                            retList.add(value.get("id").getAsString());
                        }
                    }
                }
            }
        }

        return retList;
    }

    private Set<String> getAliasesFromJsonArray(JsonArray array) {
        Set<String> retList = new HashSet<>();
        if(array != null) {
            for (JsonElement element : array) {
                JsonObject alias = element.getAsJsonObject();
                retList.add(alias.get("value").getAsString());
            }
        }

        return retList;
    }
}
