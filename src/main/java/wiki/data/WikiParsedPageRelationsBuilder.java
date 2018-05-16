package wiki.data;

import wiki.utils.WikiPageParser;

import java.util.HashSet;
import java.util.Set;

public class WikiParsedPageRelationsBuilder {
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

    public WikiParsedPageRelations buildFromWikipediaPageText(String title, String pageText) {
        this.categories = new HashSet<>();
        this.categoriesNorm = new HashSet<>();
        this.titleParenthesis = new HashSet<>();
        this.titleParenthesisNorm = new HashSet<>();

        String[] textLines = pageText.split("\n");
        Set<String> extLinks = new HashSet<>();
        Set<String> extParenthesis = new HashSet<>();
        for (String line : textLines) {
            Set<String> lineCategories = WikiPageParser.extractCategories(title, line);
            if(!this.isPartName) {
                this.isPartName = WikiPageParser.isPartName(line);
                if (!this.isPartName) {
                    this.isPartName = WikiPageParser.isPartNameInCategories(lineCategories);
                }
            }

            LinkParenthesisPair linkParenthesisPair = WikiPageParser.extractLinksAndParenthesis(line);

            this.categories.addAll(lineCategories);
            this.categoriesNorm.addAll(WikiPageParser.normalizeStringSet(lineCategories));
            extLinks.addAll(linkParenthesisPair.getLinks());
            extParenthesis.addAll(linkParenthesisPair.getParenthesis());
        }

        if (WikiPageParser.isDisambiguation(this.categories)) { // need to replace with utils method to check in categories if disambig
            this.isDisambiguation = true;
            this.disambiguationLinks = extLinks;
            this.disambiguationLinksNorm = WikiPageParser.normalizeStringSet(extLinks);
            this.titleParenthesis = extParenthesis;
            this.titleParenthesisNorm = WikiPageParser.normalizeStringSet(extParenthesis);
        }

        return this.build();
    }

    public WikiParsedPageRelations build() {
        return new WikiParsedPageRelations(this.phrase,
                this.isPartName,
                this.isDisambiguation,
                this.disambiguationLinks,
                this.categories,
                this.aliases,
                this.titleParenthesis,
                this.disambiguationLinksNorm,
                this.categoriesNorm,
                this.aliasesNorm,
                this.titleParenthesisNorm);
    }
}
