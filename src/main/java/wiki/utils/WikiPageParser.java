package wiki.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.stanford.nlp.simple.Sentence;
import joptsimple.internal.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikiPageParser {
    private final static Logger LOGGER = LogManager.getLogger(WikiPageParser.class);

    private static final String DISAMBIGUATION_TITLE = "(disambiguation)";

    private static final String CAT_REGEX = "\\[\\[Category:([\\w\\s\\-\\(\\)\\|]+)\\]\\]";
    private static final Pattern CAT_PATTERN = Pattern.compile(CAT_REGEX);

    private static final String DIS_REGEX = "^\\{\\{(disambig.*|Disambig.*)\\}\\}$";
    private static final Pattern DIS_PATTERN = Pattern.compile(DIS_REGEX);

    private static final String LINK_REGEX = "\\[\\[([\\w\\s\\-\\(\\)\\|]+)\\]\\]";
    private static final Pattern LINK_PATTERN = Pattern.compile(LINK_REGEX);

    private static final String[] PART_NAME_CATEGORIES = {"name", "given name", "surname"};
    private static final String[] DISAMBIGUATION_CATEGORIES = {"disambig", "disambiguation"};

    public static HashSet<String> STOP_WORDS;

    static {
        try (InputStream resource = WikiPageParser.class.getClassLoader().getResourceAsStream("stop_words_en.json")) {
            Type type = new TypeToken<HashSet<String>>() {
            }.getType();
            Gson gson = new Gson();
            STOP_WORDS = gson.fromJson(new InputStreamReader(resource), type);
        } catch (IOException e) {
            LOGGER.error("failed to load STOP_WORDS", e);
        }
    }

    public static Set<String> extractCategories(String line) {

        Set<String> categories = new HashSet<>();
        if (line != null && !line.isEmpty()) {
            Matcher catMatch = CAT_PATTERN.matcher(line);
            while (catMatch.find()) {
                String cat = catMatch.group(1);
                cat = trimDisambig(cat);
                categories.add(cat);
            }

            Matcher disMatch = DIS_PATTERN.matcher(line);
            while (disMatch.find()) {
                String cat = disMatch.group(1);
                String[] cats = cat.split("\\|");
                categories.addAll(Arrays.asList(cats));
            }
        }
        return categories;
    }

    public static boolean isPartName(String line) {
        if (line != null && !line.isEmpty()) {
            line = line.toLowerCase();
            if (line.contains("===as surname==="))
                return true;
            else if (line.contains("===as given name==="))
                return true;
            else if (line.contains("===given names==="))
                return true;
            else if (line.contains("==as a surname=="))
                return true;
            else if (line.contains("==people with the surname=="))
                return true;
            else if (line.contains("==family name and surname=="))
                return true;
            else if (line.contains("category:given names"))
                return true;
        }
        return false;
    }

    public static boolean isPartNameInCategories(Collection<String> categories) {
        if (categories != null) {
            for (String cat : categories) {
                for (String partName : PART_NAME_CATEGORIES) {
                    if (cat.equalsIgnoreCase(partName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static final Set<String> extractLinks(String line) {
        Set<String> links = new HashSet<>();
        Matcher linkMatcher = LINK_PATTERN.matcher(line);
        while (linkMatcher.find()) {
            String lineLinks = linkMatcher.group(1);
            String[] linkSplit = lineLinks.split("\\|");
            for (String link : linkSplit) {
                link = trimDisambig(link);
                links.add(link);

            }
        }
        return links;
    }

    public static String normalizeString(String toNorm) {
        List<String> wordsList = new ArrayList<>();
        String cleanText = toNorm.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase().trim();
        String[] cleanTextSplit = cleanText.split(" ");
        for (String token : cleanTextSplit) {
            if (!token.isEmpty() && !STOP_WORDS.contains(token)) {
                String tokenLemma = new Sentence(token).lemma(0);
                wordsList.add(tokenLemma);
            }
        }

        return Strings.join(wordsList, " ");
    }

    public static Set<String> normalizeStringSet(Set<String> toNormList) {
        Set<String> retNormList = new HashSet<>();
        if (toNormList != null)
            for (String str : toNormList) {
                if (!str.isEmpty()) {
                    retNormList.add(normalizeString(str));
                }
            }

        return retNormList;
    }

    public static boolean isDisambiguation(Collection<String> categories) {
        if (categories != null) {
            for (String cat : categories) {
                for (String dis : DISAMBIGUATION_CATEGORIES) {
                    if (cat.equalsIgnoreCase(dis)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static String trimDisambig(String cat) {
        if (cat.toLowerCase().contains(DISAMBIGUATION_TITLE)) {
            cat = cat.replace(DISAMBIGUATION_TITLE, "");
        }
        return cat;
    }
}
