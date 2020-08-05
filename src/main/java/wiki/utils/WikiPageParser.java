package wiki.utils;

import edu.stanford.nlp.simple.Sentence;
import joptsimple.internal.Strings;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikiPageParser {
    private final static Logger LOGGER = LogManager.getLogger(WikiPageParser.class);

    private static List<String> partNameCategories;
    private static String disambiguationCategories;
    private static List<String> beComps;

    private static final String URL_PATTERN = "(https?://[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";

    public static List<String> STOP_WORDS;

    public static void initResources(LangConfiguration langConfig, String lang) {
        partNameCategories = langConfig.getPartNames();
        disambiguationCategories = langConfig.getDisambiguation();
        beComps = langConfig.getBeComp();

        if(STOP_WORDS == null) {
            String stopWordsFile = Objects.requireNonNull(WikiPageParser.class.getClassLoader().getResource("stop_words/" + lang + ".txt")).getFile();
            try {
                if(stopWordsFile != null) {
                    STOP_WORDS = FileUtils.readLines(new File(stopWordsFile), "UTF-8");
                }
            } catch (IOException e) {
                LOGGER.error("failed to load STOP_WORDS", e);
            }
        }
    }

    public static boolean isPartNameInCategories(Collection<String> categories) {
        if (categories != null) {
            for (String cat : categories) {
                for (String partName : partNameCategories) {
                    if (cat.equalsIgnoreCase(partName)) {
                        return true;
                    }
                }
            }
        }
        return false;
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
        if (categories != null) {
            for (String cat : categories) {
                if (cat.equalsIgnoreCase(disambiguationCategories)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String extractFirstPageParagraph(String text) {
        String firstParagraph = "";
        int firstSentenceStartIndex = text.indexOf("'''");
        if(firstSentenceStartIndex >= 0) {
            int lastTempIndex = text.indexOf("\n", firstSentenceStartIndex);
            if (lastTempIndex == -1) {
                lastTempIndex = text.length();
            }

            firstParagraph = text.substring(firstSentenceStartIndex, lastTempIndex);
            if(extractBeAIndex(firstParagraph) == -1) {
                firstParagraph = extractFirstPageParagraph(text.substring(lastTempIndex));
            }
        }

        String firstParagraphRemJson = removeJsonTags(firstParagraph);
        String firstParagraphCleanLinks = normTextLinks(firstParagraphRemJson);
        return cleanUrlPattern(firstParagraphCleanLinks);
    }

    private static String cleanUrlPattern(String textToClean) {
        Pattern p = Pattern.compile(URL_PATTERN,Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(textToClean);
        while (m.find()) {
            try {
                textToClean = textToClean.replaceAll(m.group(0), "").trim();
            } catch (Exception ignored) { }
        }
        return textToClean;
    }

    private static int extractBeAIndex(String sentence) {
        int result = -1;

        for(String be : beComps) {
            if (sentence.contains(be)) {
                return sentence.indexOf(be);
            }
        }

        return result;
    }

    // Remove Json, html and parenthesis blocks {{.*}}/<.*>/(.*) mostly hyper links
    private static String removeJsonTags(String text) {
        text = removeParenthesis("{{", "}}", text);
        text = removeParenthesis("<", ">", text);
        text = removeParenthesis("(", ")", text);
        text = text.replaceAll("'", "");
        text = text.replaceAll("&nbsp;", " ");

        return text;
    }

    private static String removeParenthesis(String parenthesisTypeBegin, String parenthesisTypeEnd, String text) {
        while(text.contains(parenthesisTypeBegin) || text.contains(parenthesisTypeEnd)) {
            StringBuffer buff = new StringBuffer(text);
            int end = buff.indexOf(parenthesisTypeEnd);
            int start;
            if(end != -1) {
                start = findInnerStartIndex(text.substring(0, end), -1, parenthesisTypeBegin);
                if(start == -1) {
                    return text;
                }
            } else {
                start = buff.indexOf(parenthesisTypeBegin);
                if(start != -1) {
                    end = buff.length() - parenthesisTypeEnd.length();
                }
            }

            if(start == -1 || end == -1) {
                return text;
            }

            buff.delete(start, end + parenthesisTypeEnd.length());
            text = buff.toString();
        }

        return text;
    }

    private static int findInnerStartIndex(String text, int startIndex, String parenthesisTypeBegin) {
        int sIndex = text.indexOf(parenthesisTypeBegin);
        if(sIndex == -1) {
            return startIndex;
        }

        return findInnerStartIndex(text.substring(sIndex + parenthesisTypeBegin.length()), sIndex, parenthesisTypeBegin);
    }

    // Every thing in between [[.*]]
    private static String normTextLinks(String text) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            if(text.charAt(i) == '[') {
                StringBuilder resultIn = new StringBuilder();
                while (i + 1 < text.length() && text.charAt(++i) != ']') {
                    if (text.charAt(i) == '[') {
                        continue;
                    }
                    else if (text.charAt(i) == '|') {
                        resultIn = new StringBuilder(); // clean (multi separated link)
                        continue;
                    }
                    resultIn.append(text.charAt(i));
                }
                result.append(resultIn);
            } else {
                if (text.charAt(i) != ']') {
                    result.append(text.charAt(i));
                }
            }
        }

        return result.toString();
    }

    public static String cleanValue(String value) {
        String cleanText = null;
        if(value != null && !value.isEmpty() && !STOP_WORDS.contains(value.toLowerCase())) {
            cleanText = getTrimTextOnly(value).toLowerCase();
        }

        return cleanText;
    }

    public static String getTrimTextOnly(String text) {
        return text; //text.replaceAll("[^a-zA-Z0-9]", " ").trim();
    }
}
