package wiki.data.relations;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wiki.data.obj.LinkParenthesisPair;
import wiki.utils.LangConfiguration;
import wiki.utils.WikiPageParser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkAndParenthesisRelationExtractor implements IRelationsExtractor<List<LinkParenthesisPair>> {
    private final static Logger LOGGER = LogManager.getLogger(LinkAndParenthesisRelationExtractor.class);

    private static final String PARENTHESIS_REGEX_1 = "\\[\\[((?>\\P{M}\\p{M}*)+)\\]\\]";
    private static final Pattern PARENTHESIS_PATTERN_1 = Pattern.compile(PARENTHESIS_REGEX_1);

    private static final String PARENTHESIS_REGEX_2 = "((?>\\P{M}\\p{M}*)+)\\s?\\(((?>\\P{M}\\p{M}*)+)\\)";
    private static final Pattern PARENTHESIS_PATTERN_2 = Pattern.compile(PARENTHESIS_REGEX_2);

    private static String disambiguationTitle;

    private final List<LinkParenthesisPair> titleParenthesis = new ArrayList<>();

    public static void initResources(LangConfiguration lang) {
        LOGGER.info("Initiating LinkAndParenthesisRelationExtractor");
        disambiguationTitle = "(" + lang.getDisambiguation() + ")";
        LOGGER.info("LinkAndParenthesisRelationExtractor initialized");
    }

    @Override
    public IRelationsExtractor<List<LinkParenthesisPair>> extract(String line) throws Exception {
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

        this.titleParenthesis.add(new LinkParenthesisPair(links, parenthesis));
        return this;
    }

    @Override
    public List<LinkParenthesisPair> getResult() {
        return this.titleParenthesis;
    }

    public Set<String> getLinks() {
        Set<String> result = new HashSet<>();
        for(LinkParenthesisPair pair : this.titleParenthesis) {
            result.addAll(pair.getLinks());
        }

        return result;
    }

    public Set<String> getTitleParenthesis() {
        Set<String> result = new HashSet<>();
        for(LinkParenthesisPair pair : this.titleParenthesis) {
            result.addAll(pair.getParenthesis());
        }

        return result;
    }
}
