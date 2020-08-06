package wiki.data;

import wiki.data.obj.BeCompRelationResult;
import wiki.data.obj.LinkParenthesisPair;
import wiki.data.relations.*;
import wiki.utils.WikiPageParser;

import java.util.HashSet;
import java.util.Set;

public class WikiParsedPageRelationsBuilder {
    private boolean isPartName = false;
    private boolean isDisambiguation = false;
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

    public WikiParsedPageRelations buildFromWikipediaPageText(String pageText) throws Exception {
        return buildFromText(pageText, true);
    }

    public WikiParsedPageRelations buildFromWikipediaPageTextNoNormalization(String pageText) throws Exception {
        return buildFromText(pageText, false);
    }

    public WikiParsedPageRelations build() {
        return new WikiParsedPageRelations(
                this.isPartName,
                this.isDisambiguation,
                this.disambiguationLinks,
                this.categories,
                this.aliases,
                this.titleParenthesis,
                this.beCompRelations,
                this.disambiguationLinksNorm,
                this.categoriesNorm,
                this.aliasesNorm,
                this.titleParenthesisNorm,
                this.beCompRelationsNorm);
    }

    private WikiParsedPageRelations buildFromText(String pageText, boolean normalize) throws Exception {
        this.categories = new HashSet<>();
        this.categoriesNorm = new HashSet<>();
        this.titleParenthesis = new HashSet<>();
        this.titleParenthesisNorm = new HashSet<>();

        IRelationsExtractor<Set<String>> categoryExtractor = new CategoryRelationExtractor();
        IRelationsExtractor<Boolean> partNameExtractor = new PartNameRelationExtractor();
        IRelationsExtractor<LinkParenthesisPair> pairExtractor = new LinkAndParenthesisRelationExtractor();
        IRelationsExtractor<BeCompRelationResult> beCompExtractor = new BeCompRelationExtractor();

        String[] textLines = pageText.split("\n");
        Set<String> extLinks = new HashSet<>();
        Set<String> extParenthesis = new HashSet<>();
        for (String line : textLines) {
            Set<String> lineCategories = categoryExtractor.extract(line);
            if(!this.isPartName) {
                this.isPartName = partNameExtractor.extract(line);
                if (!this.isPartName) {
                    this.isPartName = WikiPageParser.isPartNameInCategories(lineCategories);
                }
            }

            LinkParenthesisPair linkParenthesisPair = pairExtractor.extract(line);

            this.categories.addAll(lineCategories);
            if(normalize) {
                this.categoriesNorm.addAll(WikiPageParser.normalizeStringSet(lineCategories));
            }
            extLinks.addAll(linkParenthesisPair.getLinks());
            extParenthesis.addAll(linkParenthesisPair.getParenthesis());
        }

        if (WikiPageParser.isDisambiguation(this.categories)) { // need to replace with utils method to check in categories if disambig
            this.isDisambiguation = true;
            this.disambiguationLinks = extLinks;
            this.titleParenthesis = extParenthesis;
            if(normalize) {
                this.disambiguationLinksNorm = WikiPageParser.normalizeStringSet(extLinks);
                this.titleParenthesisNorm = WikiPageParser.normalizeStringSet(extParenthesis);
            }
        } else {
            String firstParagraph = WikiPageParser.extractFirstPageParagraph(pageText);
            final BeCompRelationResult beCompRelations = beCompExtractor.extract(firstParagraph);
            this.beCompRelations = beCompRelations.getBeCompRelations();
            if(normalize) {
                this.beCompRelationsNorm = beCompRelations.getBeCompRelationsNorm();
            }
        }
        return this.build();
    }
}
