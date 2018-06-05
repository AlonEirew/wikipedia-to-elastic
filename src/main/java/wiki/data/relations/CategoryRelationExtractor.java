package wiki.data.relations;

import wiki.utils.WikiPageParser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CategoryRelationExtractor implements IRelationsExtractor<Set<String>> {

    private static final String DISAMBIGUATION_TITLE = "(disambiguation)";

    private static final String CAT_REGEX = "\\[\\[Category:(.*)\\]\\]";
    private static final Pattern CAT_PATTERN = Pattern.compile(CAT_REGEX);

    private static final String DIS_REGEX = "^\\{\\{(disambig.*|Disambig.*)\\}\\}$";
    private static final Pattern DIS_PATTERN = Pattern.compile(DIS_REGEX);

    @Override
    public Set<String> extract(String line) {
        Set<String> categories = new HashSet<>();
        if (line != null && !line.isEmpty()) {
            Matcher catMatch = CAT_PATTERN.matcher(line);
            while (catMatch.find()) {
                String cat = catMatch.group(1);
                cat = trimDisambig(cat);
                cat = WikiPageParser.getTrimTextOnly(cat);
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

    private String trimDisambig(String cat) {
        if (cat.toLowerCase().contains(DISAMBIGUATION_TITLE)) {
            cat = cat.replace(DISAMBIGUATION_TITLE, "");
        }
        return cat;
    }
}
