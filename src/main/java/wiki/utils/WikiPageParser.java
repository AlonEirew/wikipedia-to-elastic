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

    private static final String[] PART_NAME_CATEGORIES = {"name", "given name", "surname"};
    private static final String[] DISAMBIGUATION_CATEGORIES = {"disambig", "disambiguation"};

    private static final String URL_PATTERN = "(https?://[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";

    public static HashSet<String> STOP_WORDS;

    public static void initResources() {
        if(STOP_WORDS == null) {
            try (InputStream resource = WikiPageParser.class.getClassLoader().getResourceAsStream("stop_words_en.json")) {
                if(resource != null) {
                    Type type = new TypeToken<HashSet<String>>() {
                    }.getType();
                    Gson gson = new Gson();
                    STOP_WORDS = gson.fromJson(new InputStreamReader(resource), type);
                }
            } catch (IOException e) {
                LOGGER.error("failed to load STOP_WORDS", e);
            }
        }
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
                for (String dis : DISAMBIGUATION_CATEGORIES) {
                    if (cat.equalsIgnoreCase(dis)) {
                        return true;
                    }
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

        if(sentence.contains("is a")) {
            result = sentence.indexOf("is a");
        } else if(sentence.contains("are a")) {
            result = sentence.indexOf("are a");
        } else if(sentence.contains("was a")) {
            result = sentence.indexOf("was a");
        } else if(sentence.contains("were a")) {
            result = sentence.indexOf("were a");
        } else if(sentence.contains("be a")) {
            result = sentence.indexOf("be a");
        } else if(sentence.contains("is the")) {
            result = sentence.indexOf("is the");
        } else if(sentence.contains("are the")) {
            result = sentence.indexOf("are the");
        } else if(sentence.contains("was the")) {
            result = sentence.indexOf("was the");
        } else if(sentence.contains("were the")) {
            result = sentence.indexOf("were the");
        } else if(sentence.contains("be the")) {
            result = sentence.indexOf("be the");
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
        return text.replaceAll("[^a-zA-Z0-9]", " ").trim();
    }
}
