package wiki.data.relations;

import wiki.data.obj.LinkParenthesisPair;
import wiki.utils.LangConfiguration;
import wiki.utils.WikiPageParser;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkAndParenthesisRelationExtractor implements IRelationsExtractor<LinkParenthesisPair> {

    private static final String PARENTHESIS_REGEX_1 = "\\[\\[((?>\\P{M}\\p{M}*)+)\\]\\]";
    private static final Pattern PARENTHESIS_PATTERN_1 = Pattern.compile(PARENTHESIS_REGEX_1);

    private static final String PARENTHESIS_REGEX_2 = "((?>\\P{M}\\p{M}*)+)\\s?\\(((?>\\P{M}\\p{M}*)+)\\)";
    private static final Pattern PARENTHESIS_PATTERN_2 = Pattern.compile(PARENTHESIS_REGEX_2);

    private static String disambiguationTitle;

    public static void initResources(LangConfiguration lang) {
        disambiguationTitle = "(" + lang.getDisambiguation() + ")";
    }

    @Override
    public LinkParenthesisPair extract(String line) throws Exception {
        Set<String> parenthesis = new HashSet<>();
        Set<String> links = new HashSet<>();

        Matcher parenthesisMatcher1 = PARENTHESIS_PATTERN_1.matcher(line);
        while (parenthesisMatcher1.find()) {
            String inPar = parenthesisMatcher1.group(1);
            String[] splittedLine = inPar.split("\\|");
            for (String splLine : splittedLine) {
                Matcher parenthesisMatcher2 = PARENTHESIS_PATTERN_2.matcher(splLine);
                if(parenthesisMatcher2.matches()) {
                    String link = parenthesisMatcher2.group(1);
                    link = WikiPageParser.getTrimTextOnly(link);
                    links.add(link);
                    if (parenthesisMatcher2.group(2) != null) {
                        String parenth = parenthesisMatcher2.group(2);
                        if(!parenth.equalsIgnoreCase(disambiguationTitle)) {
                            parenth = WikiPageParser.getTrimTextOnly(parenth);
                            parenthesis.add(parenth);
                        }
                    }
                } else {
                    splLine = WikiPageParser.getTrimTextOnly(splLine);
                    links.add(splLine);
                }
            }
        }
        return new LinkParenthesisPair(links, parenthesis);
    }
}
