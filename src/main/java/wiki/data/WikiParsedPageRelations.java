package wiki.data;

import java.util.Objects;
import java.util.Set;

public class WikiParsedPageRelations {
    private String phrase;
    private boolean isPartName = false;
    private boolean isDisambiguation = false;
    private Set<String> disambiguationLinks;
    private Set<String> categories;
    private Set<String> aliases;
    private Set<String> titleParenthesis;

    private Set<String> disambiguationLinksNorm;
    private Set<String> categoriesNorm;
    private Set<String> aliasesNorm;
    private Set<String> titleParenthesisNorm;

    public WikiParsedPageRelations(String phrase, boolean isPartName, boolean isDisambiguation,
                                   Set<String> disambiguationLinks,
                                   Set<String> categories, Set<String> aliases, Set<String> titleParenthesis,
                                   Set<String> disambiguationLinksNorm,
                                   Set<String> categoriesNorm, Set<String> aliasesNorm,
                                   Set<String> titleParenthesisNorm) {
        this.phrase = phrase;
        this.isPartName = isPartName;
        this.isDisambiguation = isDisambiguation;
        this.disambiguationLinks = disambiguationLinks;
        this.categories = categories;
        this.aliases = aliases;
        this.titleParenthesis = titleParenthesis;
        this.disambiguationLinksNorm = disambiguationLinksNorm;
        this.categoriesNorm = categoriesNorm;
        this.aliasesNorm = aliasesNorm;
        this.titleParenthesisNorm = titleParenthesisNorm;
    }

    public String getPhrase() {
        return phrase;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WikiParsedPageRelations that = (WikiParsedPageRelations) o;
        return isPartName == that.isPartName &&
                isDisambiguation == that.isDisambiguation &&
                Objects.equals(phrase, that.phrase) &&
                Objects.equals(disambiguationLinks, that.disambiguationLinks) &&
                Objects.equals(categories, that.categories) &&
                Objects.equals(aliases, that.aliases) &&
                Objects.equals(titleParenthesis, that.titleParenthesis) &&
                Objects.equals(disambiguationLinksNorm, that.disambiguationLinksNorm) &&
                Objects.equals(categoriesNorm, that.categoriesNorm) &&
                Objects.equals(aliasesNorm, that.aliasesNorm) &&
                Objects.equals(titleParenthesisNorm, that.titleParenthesisNorm);
    }

    @Override
    public int hashCode() {

        return Objects.hash(phrase, isPartName, isDisambiguation, disambiguationLinks, categories, aliases,
                titleParenthesis, disambiguationLinksNorm, categoriesNorm, aliasesNorm, titleParenthesisNorm);
    }
}
