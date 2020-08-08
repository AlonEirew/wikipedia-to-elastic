package wiki.data.relations;

import wiki.utils.LangConfiguration;
import wiki.utils.WikiPageParser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CategoryRelationExtractor implements IRelationsExtractor<Set<String>> {

    private static List<String> disambiguationTitles;

    private static Pattern catPattern;
    private static Pattern disPattern;

    public static void initResources(LangConfiguration lang) {
        disambiguationTitles = lang.getDisambiguation();

        String catRegex = "\\[\\[(" + String.join("|", lang.getCategory()) + "):((?>\\P{M}\\p{M}*)+)\\]\\]";
        catPattern = Pattern.compile(catRegex);

        String disRegex = "^\\{\\{((" + String.join("|", lang.getDisambiguation()) + ")(?>\\P{M}\\p{M}*)*)\\}\\}$";
        disPattern = Pattern.compile(disRegex);
    }

    @Override
    public Set<String> extract(String line) throws Exception {
        Set<String> categories = new HashSet<>();
        if (line != null && !line.isEmpty()) {
            Matcher catMatch = catPattern.matcher(line);
            while (catMatch.find()) {
                String cat = catMatch.group(2).trim();
                if(cat.contains("|")) {
                    cat = cat.split("\\|")[0];
                }

                cat = WikiPageParser.getTrimTextOnly(cat);
                categories.add(cat);
            }

            Matcher disMatch = disPattern.matcher(line.toLowerCase());
            while (disMatch.find()) {
                String cat = disMatch.group(2);
                String[] cats = cat.split("\\|");
                categories.addAll(Arrays.asList(cats));
                categories.addAll(disambiguationTitles);
            }
        }
        return categories;
    }
}
