package wiki.data;

import wiki.utils.WikiPageParser;

import java.util.HashSet;
import java.util.Set;

public class WikiParsedPageRelationsBuilder {
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

    public WikiParsedPageRelationsBuilder setPhrase(String phrase) {
        this.phrase = phrase;
        return this;
    }

    public WikiParsedPageRelationsBuilder setPartName(boolean partName) {
        this.isPartName = partName;
        return this;
    }

    public WikiParsedPageRelationsBuilder setDisambiguation(boolean disambiguation) {
        this.isDisambiguation = disambiguation;
        return this;
    }

    public WikiParsedPageRelationsBuilder setDisambiguationLinks(Set<String> disambiguationLinks) {
        this.disambiguationLinks = disambiguationLinks;
        return this;
    }

    public WikiParsedPageRelationsBuilder setPageLinks(Set<String> pageLinks) {
        this.pageLinks = pageLinks;
        return this;
    }

    public WikiParsedPageRelationsBuilder setCategories(Set<String> categories) {
        this.categories = categories;
        return this;
    }

    public WikiParsedPageRelationsBuilder setAliases(Set<String> aliases) {
        this.aliases = aliases;
        return this;
    }

    public WikiParsedPageRelations buildFromWikipediaPageText(String pageText) {
        this.categories = new HashSet<>();
        this.categoriesNorm = new HashSet<>();

        String[] textLines = pageText.split("\n");
        Set<String> extLinks = new HashSet<>();
        Set<String> extLinksNorm = new HashSet<>();
        for (String line : textLines) {
            Set<String> lineCategories = WikiPageParser.extractCategories(line);
            if(!this.isPartName) {
                this.isPartName = WikiPageParser.isPartName(line);
                if (!this.isPartName) {
                    this.isPartName = WikiPageParser.isPartNameInCategories(lineCategories);
                }
            }

            Set<String> lineLinks = WikiPageParser.extractLinks(line);

            this.categories.addAll(lineCategories);
            this.categoriesNorm.addAll(WikiPageParser.normalizeStringSet(lineCategories));
            extLinks.addAll(lineLinks);
            extLinksNorm.addAll(WikiPageParser.normalizeStringSet(lineLinks));
        }

        if (WikiPageParser.isDisambiguation(this.categories)) { // need to replace with utils method to check in categories if disambig
            this.isDisambiguation = true;
            this.disambiguationLinks = extLinks;
            this.disambiguationLinksNorm = extLinksNorm;
        } else {
            this.pageLinks = extLinks;
            this.pageLinksNorm = extLinksNorm;
        }

        return this.build();
    }

    public WikiParsedPageRelations build() {
        return new WikiParsedPageRelations(
                this.phrase,
                this.isPartName,
                this.isDisambiguation,
                this.disambiguationLinks,
                this.pageLinks,
                this.categories,
                this.aliases,
                this.disambiguationLinksNorm,
                this.pageLinksNorm,
                this.categoriesNorm,
                this.aliasesNorm);
    }
}
