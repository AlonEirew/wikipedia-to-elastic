package wiki.data;

import java.util.Objects;
import java.util.Set;

public class WikiParsedPageRelations {
    private final boolean isPartName;
    private final boolean isDisambiguation;
    private final Set<String> disambiguationLinks;
    private final Set<String> categories;
    private final Set<String> aliases;
    private final Set<String> titleParenthesis;
    private final Set<String> beCompRelations;

    public WikiParsedPageRelations(boolean isPartName, boolean isDisambiguation,
                                   Set<String> disambiguationLinks,
                                   Set<String> categories, Set<String> aliases, Set<String> titleParenthesis,
                                   Set<String> beCompRelations) {
        this.isPartName = isPartName;
        this.isDisambiguation = isDisambiguation;
        this.disambiguationLinks = disambiguationLinks;
        this.categories = categories;
        this.aliases = aliases;
        this.titleParenthesis = titleParenthesis;
        this.beCompRelations = beCompRelations;
    }

    public boolean isPartName() {
        return isPartName;
    }

    public boolean isDisambiguation() {
        return isDisambiguation;
    }

    public Set<String> getDisambiguationLinks() {
        return disambiguationLinks;
    }

    public Set<String> getCategories() {
        return categories;
    }

    public Set<String> getAliases() {
        return aliases;
    }

    public Set<String> getTitleParenthesis() {
        return titleParenthesis;
    }

    public Set<String> getBeCompRelations() {
        return beCompRelations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WikiParsedPageRelations that = (WikiParsedPageRelations) o;
        return isPartName == that.isPartName &&
                isDisambiguation == that.isDisambiguation &&
                Objects.equals(disambiguationLinks, that.disambiguationLinks) &&
                Objects.equals(categories, that.categories) &&
                Objects.equals(aliases, that.aliases) &&
                Objects.equals(titleParenthesis, that.titleParenthesis) &&
                Objects.equals(beCompRelations, that.beCompRelations);
    }

    @Override
    public int hashCode() {

        return Objects.hash(isPartName, isDisambiguation, disambiguationLinks, categories, aliases, titleParenthesis, beCompRelations);
    }
}
