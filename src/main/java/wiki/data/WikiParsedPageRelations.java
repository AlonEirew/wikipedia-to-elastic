package wiki.data;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class WikiParsedPageRelations {
    private final String infobox;
    private final boolean isPartName;
    private final boolean isDisambiguation;
    private final Set<String> disambiguationLinks = new HashSet<>();
    private final Set<String> categories;
    private final Set<String> titleParenthesis;
    private final Set<String> beCompRelations;

    public WikiParsedPageRelations(String infobox, boolean isPartName, boolean isDisambiguation,
                                   Set<String> disambiguationLinks,
                                   Set<String> categories, Set<String> titleParenthesis,
                                   Set<String> beCompRelations) {
        this.infobox = infobox;
        this.isPartName = isPartName;
        this.isDisambiguation = isDisambiguation;

        if(isDisambiguation) {
            this.disambiguationLinks.addAll(disambiguationLinks);
        }

        this.categories = categories;
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

    public Set<String> getTitleParenthesis() {
        return titleParenthesis;
    }

    public Set<String> getBeCompRelations() {
        return beCompRelations;
    }

    public String getInfobox() {
        return infobox;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WikiParsedPageRelations that = (WikiParsedPageRelations) o;
        return isPartName == that.isPartName &&
                isDisambiguation == that.isDisambiguation &&
                Objects.equals(infobox, that.infobox) &&
                Objects.equals(disambiguationLinks, that.disambiguationLinks) &&
                Objects.equals(categories, that.categories) &&
                Objects.equals(titleParenthesis, that.titleParenthesis) &&
                Objects.equals(beCompRelations, that.beCompRelations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(infobox, isPartName, isDisambiguation, disambiguationLinks, categories, titleParenthesis, beCompRelations);
    }
}
