package wiki.utils;

import edu.stanford.nlp.simple.Sentence;
import info.bliki.wiki.model.WikiModel;
import joptsimple.internal.Strings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikiPageParser {
    private final static Logger LOGGER = LogManager.getLogger(WikiPageParser.class);

    private static List<String> disambiguationCategories;

    public static List<String> STOP_WORDS;

    public static void initResources(String lang, LangConfiguration langConfig) {
        disambiguationCategories = langConfig.getDisambiguation();

        if(STOP_WORDS == null) {
            InputStream stopWordsFile = Objects.requireNonNull(WikiPageParser.class.getClassLoader().getResourceAsStream("stop_words/" + lang + ".txt"));
            try {
                STOP_WORDS = IOUtils.readLines(stopWordsFile, StandardCharsets.UTF_8);
            } catch (IOException e) {
                LOGGER.error("failed to load STOP_WORDS", e);
            }
        }
    }

    public static String normalizeString(String toNorm) {
        List<String> wordsList = new ArrayList<>();
        String cleanText = getTrimTextOnly(toNorm).toLowerCase();
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
        return !Collections.disjoint(categories, disambiguationCategories);
    }

    public static String extractFirstPageParagraph(String text) {
        String htmlText = WikiModel.toHtml(text);
        String cleanHtml = cleanTextField(htmlText);
        Document doc = Jsoup.parse(cleanHtml);
        Elements pis = doc.getElementsByTag("p");
        for(Element elem : pis) {
            String ptext = elem.text().trim();
            if(!ptext.isEmpty()) {
                return ptext;
            }
        }

        return null;
    }

    private static String cleanTextField(String html) {
        String cleanHtml = html;
        Pattern pat1 = Pattern.compile("(?s)\\{\\{[^{]*?\\}\\}");
        Matcher match1 = pat1.matcher(cleanHtml);
        while (match1.find()) {
            cleanHtml = match1.replaceAll("");
            match1 = pat1.matcher(cleanHtml);
        }

        return cleanHtml;
    }

    public static String cleanValue(String value) {
        String cleanText = null;
        if(value != null && !value.isEmpty() && !STOP_WORDS.contains(value.toLowerCase())) {
            cleanText = getTrimTextOnly(value).toLowerCase();
        }

        return cleanText;
    }

    public static String getTrimTextOnly(String text) {
        return text.trim(); //text.replaceAll("[^a-zA-Z0-9]", " ").trim();
    }
}
