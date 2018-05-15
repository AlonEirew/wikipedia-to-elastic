package wiki.data;

import java.util.Objects;
import java.util.Set;

public class WikiParsedPageRelations {
    private String phrase;
    private boolean isPartName = false;
    private boolean isDisambiguation = false;
    private Set<String> disambiguationLinks;
    private Set<String> pageLinks;
    private Set<String> categories;
    private Set<String> aliases;

    private Set<String> disambiguationLinksNorm;
    private Set<String> pageLinksNorm;
    private Set<String> categoriesNorm;
    private Set<String> aliasesNorm;

    public WikiParsedPageRelations(String phrase, boolean isPartName, boolean isDisambiguation,
                                   Set<String> disambiguationLinks, Set<String> pageLinks, Set<String> categories,
                                   Set<String> aliases, Set<String> disambiguationLinksNorm, Set<String> pageLinksNorm,
                                   Set<String> categoriesNorm, Set<String> aliasesNorm) {
        this.phrase = phrase;
        this.isPartName = isPartName;
        this.isDisambiguation = isDisambiguation;
        this.disambiguationLinks = disambiguationLinks;
        this.pageLinks = pageLinks;
        this.categories = categories;
        this.aliases = aliases;
        this.disambiguationLinksNorm = disambiguationLinksNorm;
        this.pageLinksNorm = pageLinksNorm;
        this.categoriesNorm = categoriesNorm;
        this.aliasesNorm = aliasesNorm;
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

    public Set<String> getPageLinks() {
        return pageLinks;
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

    public Set<String> getPageLinksNorm() {
        return pageLinksNorm;
    }

    public Set<String> getCategoriesNorm() {
        return categoriesNorm;
    }

    public Set<String> getAliasesNorm() {
        return aliasesNorm;
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
                Objects.equals(pageLinks, that.pageLinks) &&
                Objects.equals(categories, that.categories) &&
                Objects.equals(aliases, that.aliases) &&
                Objects.equals(disambiguationLinksNorm, that.disambiguationLinksNorm) &&
                Objects.equals(pageLinksNorm, that.pageLinksNorm) &&
                Objects.equals(categoriesNorm, that.categoriesNorm) &&
                Objects.equals(aliasesNorm, that.aliasesNorm);
    }

    @Override
    public int hashCode() {

        return Objects.hash(phrase, isPartName, isDisambiguation, disambiguationLinks, pageLinks, categories, aliases, disambiguationLinksNorm, pageLinksNorm, categoriesNorm, aliasesNorm);
    }
}
