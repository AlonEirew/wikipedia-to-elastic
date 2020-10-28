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
import java.util.*;
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


    private final static Set<String> uniqueTitles = Collections.synchronizedSet(new HashSet<>());

    private final AtomicInteger counter = new AtomicInteger(1);
    private final Map<String, WikiDataParsedPage> messages = new Hashtable<>();

    public Map<String, WikiDataParsedPage> parse(File inputDump, final String lang) throws IOException {
        ExecutorService executorService = SimpleExecutorService.initExecutorService();

        try(final JsonReader reader = new JsonReader(new InputStreamReader(WikiToElasticUtils.openCompressedFileInputStream(
                        inputDump.getPath()), StandardCharsets.UTF_8))) {
            reader.beginArray();
            while (reader.hasNext()) {
                JsonObject message = GSON.fromJson(reader, JsonObject.class);
                executorService.submit(() -> {
                    JsonObject labels = message.get("labels").getAsJsonObject();
                    JsonObject pageLang = labels.getAsJsonObject(lang);
                    if(pageLang != null) {
                        String pageTitle = pageLang.get("value").getAsString();
                        if(!uniqueTitles.contains(pageTitle) && isValidPage(pageTitle)) {
                            JsonObject aliasesJson = message.get("aliases").getAsJsonObject();
                            JsonObject claims = message.get("claims").getAsJsonObject();
                            String wikidatePageId = message.get("id").getAsString();

                            List<String> aliases = getAliasesFromJsonArray(aliasesJson.getAsJsonArray("en"));
                            List<String> partOf = getClaimsFromJsonArray(claims.getAsJsonArray(PART_OF));
                            List<String> hasPart = getClaimsFromJsonArray(claims.getAsJsonArray(HAS_PART));
                            List<String> hasEffect = getClaimsFromJsonArray(claims.getAsJsonArray(HAS_EFFECT));
                            List<String> hasCause = getClaimsFromJsonArray(claims.getAsJsonArray(HAS_CAUSE));
                            List<String> hasImmCause = getClaimsFromJsonArray(claims.getAsJsonArray(HAS_IMMEDIATE_CAUSE));
                            List<String> immCauseOf = getClaimsFromJsonArray(claims.getAsJsonArray(IMMEDIATE_CAUSE_OF));

                            if (!(aliases.isEmpty() && partOf.isEmpty() && hasPart.isEmpty() && hasEffect.isEmpty() &&
                                    hasCause.isEmpty() && hasImmCause.isEmpty() && immCauseOf.isEmpty())) {
                                messages.put(pageTitle, new WikiDataParsedPage(wikidatePageId, pageTitle, aliases,
                                        partOf, hasPart, hasEffect, hasCause, hasImmCause, immCauseOf));
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
        return this.messages;
    }

    private boolean isValidPage(String title) {
        return !(title.startsWith("Wikipedia:") || title.startsWith("Template:") || title.startsWith("Category:") ||
                title.startsWith("Help:") || StringUtils.isNumericSpace(title) || title.length() == 1);
    }

    private List<String> getClaimsFromJsonArray(JsonArray array) {
        List<String> retList = new ArrayList<>();
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

        return retList;
    }

    private List<String> getAliasesFromJsonArray(JsonArray array) {
        List<String> retList = new ArrayList<>();
        if(array != null) {
            for (int i = 0; i < array.size(); i++) {
                JsonObject elem = array.get(i).getAsJsonObject();
                if (elem.get("language").getAsString().equals("en")) {
                    String aliasValue = elem.get("value").getAsString();
                    retList.add(aliasValue);
                }
            }
        }

        return retList;
    }
}
