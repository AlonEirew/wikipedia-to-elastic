package wiki.utils.parsers;

import info.bliki.wiki.model.WikiModel;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikiPageParser {

    public static List<String> extractAllPageParagraphs(String text, int wordlengthThresh) {
        List<String> allPageParagraphs = new ArrayList<>();
        String htmlText = WikiModel.toHtml(text);
        String cleanHtml = cleanTextField(htmlText);
        Document doc = Jsoup.parse(cleanHtml);
        Elements pis = doc.getElementsByTag("p");
        for(Element elem : pis) {
            String ptext = elem.text().trim();
            if(!ptext.isEmpty() && ptext.split(" ").length >= wordlengthThresh) {
                allPageParagraphs.add(ptext);
            }
        }

        if (allPageParagraphs.isEmpty()) {
            allPageParagraphs = null;
        }
        return allPageParagraphs;
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
        cleanHtml = cleanHtml.replaceAll("\\[\\d+\\]", "");
        return cleanHtml;
    }

    public static String getTrimTextOnly(String text) {
        return text.trim(); //text.replaceAll("[^a-zA-Z0-9]", " ").trim();
    }
}
