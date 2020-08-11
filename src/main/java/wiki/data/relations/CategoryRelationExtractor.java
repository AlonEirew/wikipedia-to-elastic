package wiki.data.relations;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wiki.utils.LangConfiguration;
import wiki.utils.WikiPageParser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CategoryRelationExtractor implements IRelationsExtractor<Set<String>> {
    private final static Logger LOGGER = LogManager.getLogger(CategoryRelationExtractor.class);
    private static List<String> disambiguationTitles = new ArrayList<>();
    private static List<String> partNameCategories = new ArrayList<>();

    private static Pattern catPattern;
    private static Pattern disPattern;

    private static List<String> disambiguationCategories = new ArrayList<>();

    private final Set<String> categories = new HashSet<>();

    public static void initResources(LangConfiguration lang) {
        LOGGER.info("Initiating CategoryRelationExtractor");
        disambiguationTitles = lang.getDisambiguation();
        partNameCategories = lang.getPartNames();
        disambiguationCategories = lang.getDisambiguation();

        String catRegex = "\\[\\[(" + String.join("|", lang.getCategory()) + "):((?>\\P{M}\\p{M}*)+)\\]\\]";
        catPattern = Pattern.compile(catRegex);

        String disRegex = "^\\{\\{((" + String.join("|", lang.getDisambiguation()) + ")(?>\\P{M}\\p{M}*)*)\\}\\}$";
        disPattern = Pattern.compile(disRegex);
        LOGGER.info("CategoryRelationExtractor initialized");
    }

    @Override
    public IRelationsExtractor<Set<String>> extract(String line) throws Exception {
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

        this.categories.addAll(categories);
        return this;
    }

    @Override
    public Set<String> getResult() {
        return this.categories;
    }

    public Set<String> getCategories() {
        return categories;
    }

    public boolean isPartNameInCategories() {
        for (String cat : categories) {
            for (String partName : partNameCategories) {
                if (cat.equalsIgnoreCase(partName)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isDisambiguation() {
        return !Collections.disjoint(this.categories, disambiguationCategories);
    }
}
