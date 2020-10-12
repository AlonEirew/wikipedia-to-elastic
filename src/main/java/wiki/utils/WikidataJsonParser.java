package wiki.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import wiki.data.WikiDataParsedPage;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WikidataJsonParser {
    private final static Gson GSON = new Gson();

    private final static String PART_OF = "P361";
    private final static String HAS_PART = "P527";
    private final static String HAS_EFFECT = "P1542";
    private final static String HAS_CAUSE = "P828";
    private final static String HAS_IMMEDIATE_CAUSE = "P1536";

    public Map<String, WikiDataParsedPage> parse(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        Map<String, WikiDataParsedPage> messages = new HashMap<>();
        reader.beginArray();
        int count = 0;
        while (reader.hasNext()) {
            count++;
            JsonObject message = GSON.fromJson(reader, JsonObject.class);
            JsonObject labels = message.get("labels").getAsJsonObject();
            JsonObject aliasesJson = message.get("aliases").getAsJsonObject();
            JsonObject claims = message.get("claims").getAsJsonObject();
            String pageId = message.get("id").getAsString();
            String pageTitle = labels.getAsJsonObject("en").get("value").getAsString();
            List<String> aliases = getAliasesFromJsonArray(aliasesJson.getAsJsonArray("en"));
            List<String> partOf = getClaimsFromJsonArray(claims.getAsJsonArray(PART_OF));
            List<String> hasPart = getClaimsFromJsonArray(claims.getAsJsonArray(HAS_PART));
            List<String> hasEffect = getClaimsFromJsonArray(claims.getAsJsonArray(HAS_EFFECT));
            List<String> hasCause = getClaimsFromJsonArray(claims.getAsJsonArray(HAS_CAUSE));
            List<String> hasImmCause = getClaimsFromJsonArray(claims.getAsJsonArray(HAS_IMMEDIATE_CAUSE));

            messages.put(pageTitle, new WikiDataParsedPage(pageId, pageTitle, aliases,
                    partOf, hasPart, hasEffect, hasCause, hasImmCause));
        }

        reader.endArray();
        reader.close();
        return messages;
    }

    private List<String> getClaimsFromJsonArray(JsonArray array) {
        List<String> retList = new ArrayList<>();
        if(array != null) {
            for (int i = 0; i < array.size(); i++) {
                String id = array.get(i).getAsJsonObject()
                        .getAsJsonObject("mainsnak").getAsJsonObject("datavalue")
                        .getAsJsonObject("value").get("id").getAsString();
                retList.add(id);
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
