package wiki.data.relations;

import wiki.utils.LangConfiguration;
import wiki.utils.WikiPageParser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CategoryRelationExtractor implements IRelationsExtractor<Set<String>> {

    private static String disambiguationTitle;

    private static Pattern catPattern;
    private static Pattern disPattern;

    public static void initResources(LangConfiguration lang) {
        disambiguationTitle = "(" + lang.getDisambiguation() + ")";

        String catRegex = "\\[\\[" + lang.getCategory() + ":(.*)\\]\\]";
        catPattern = Pattern.compile(catRegex);

        String disRegex = "^\\{\\{(" + lang.getDisambiguation().toLowerCase() + "|" + lang.getDisambiguation() + ").*\\}\\}$";
        disPattern = Pattern.compile(disRegex);
    }

    @Override
    public Set<String> extract(String line) {
        Set<String> categories = new HashSet<>();
        if (line != null && !line.isEmpty()) {
            Matcher catMatch = catPattern.matcher(line);
            while (catMatch.find()) {
                String cat = catMatch.group(1);
                cat = trimDisambig(cat);
                cat = WikiPageParser.getTrimTextOnly(cat);
                categories.add(cat);
            }

            Matcher disMatch = disPattern.matcher(line);
            while (disMatch.find()) {
                String cat = disMatch.group(1);
                String[] cats = cat.split("\\|");
                categories.addAll(Arrays.asList(cats));
            }
        }
        return categories;
    }

    private String trimDisambig(String cat) {
        if (cat.toLowerCase().contains(disambiguationTitle)) {
            cat = cat.replace(disambiguationTitle, "");
        }
        return cat;
    }
}
