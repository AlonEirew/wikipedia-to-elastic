package wiki.data;

import java.util.Objects;
import java.util.Set;

public class WikiParsedPageRelations {
    private boolean isPartName;
    private boolean isDisambiguation;
    private Set<String> disambiguationLinks;
    private Set<String> categories;
    private Set<String> aliases;
    private Set<String> titleParenthesis;
    private Set<String> beCompRelations;

    private Set<String> disambiguationLinksNorm;
    private Set<String> categoriesNorm;
    private Set<String> aliasesNorm;
    private Set<String> titleParenthesisNorm;
    private Set<String> beCompRelationsNorm;

    public WikiParsedPageRelations(boolean isPartName, boolean isDisambiguation,
                                   Set<String> disambiguationLinks,
                                   Set<String> categories, Set<String> aliases, Set<String> titleParenthesis,
                                   Set<String> beCompRelations, Set<String> disambiguationLinksNorm,
                                   Set<String> categoriesNorm, Set<String> aliasesNorm,
                                   Set<String> titleParenthesisNorm, Set<String> beCompRelationsNorm) {
        this.isPartName = isPartName;
        this.isDisambiguation = isDisambiguation;
        this.disambiguationLinks = disambiguationLinks;
        this.categories = categories;
        this.aliases = aliases;
        this.titleParenthesis = titleParenthesis;
        this.beCompRelations = beCompRelations;
        this.disambiguationLinksNorm = disambiguationLinksNorm;
        this.categoriesNorm = categoriesNorm;
        this.aliasesNorm = aliasesNorm;
        this.titleParenthesisNorm = titleParenthesisNorm;
        this.beCompRelationsNorm = beCompRelationsNorm;
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

    public Set<String> getDisambiguationLinksNorm() {
        return disambiguationLinksNorm;
    }

    public Set<String> getCategoriesNorm() {
        return categoriesNorm;
    }

    public Set<String> getAliasesNorm() {
        return aliasesNorm;
    }

    public Set<String> getTitleParenthesis() {
        return titleParenthesis;
    }

    public Set<String> getTitleParenthesisNorm() {
        return titleParenthesisNorm;
    }

    public Set<String> getBeCompRelations() {
        return beCompRelations;
    }

    public Set<String> getBeCompRelationsNorm() {
        return beCompRelationsNorm;
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
                Objects.equals(beCompRelations, that.beCompRelations) &&
                Objects.equals(disambiguationLinksNorm, that.disambiguationLinksNorm) &&
                Objects.equals(categoriesNorm, that.categoriesNorm) &&
                Objects.equals(aliasesNorm, that.aliasesNorm) &&
                Objects.equals(titleParenthesisNorm, that.titleParenthesisNorm) &&
                Objects.equals(beCompRelationsNorm, that.beCompRelationsNorm);
    }

    @Override
    public int hashCode() {

        return Objects.hash(isPartName, isDisambiguation, disambiguationLinks, categories, aliases, titleParenthesis, beCompRelations, disambiguationLinksNorm, categoriesNorm, aliasesNorm, titleParenthesisNorm, beCompRelationsNorm);
    }
}
