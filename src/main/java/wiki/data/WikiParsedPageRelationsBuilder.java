package wiki.data;

import wiki.data.obj.BeCompRelationResult;
import wiki.data.obj.LinkParenthesisPair;
import wiki.data.relations.*;
import wiki.utils.WikiPageParser;

import java.util.HashSet;
import java.util.Set;

public class WikiParsedPageRelationsBuilder {
    private String infobox;
    private boolean isPartName = false;
    private boolean isDisambiguation = false;
    private Set<String> disambiguationLinks;
    private Set<String> categories;
    private Set<String> titleParenthesis;
    private Set<String> beCompRelations;

    public WikiParsedPageRelations build() {
        return new WikiParsedPageRelations(
                this.infobox,
                this.isPartName,
                this.isDisambiguation,
                this.disambiguationLinks,
                this.categories,
                this.titleParenthesis,
                this.beCompRelations);
    }

    public WikiParsedPageRelations buildFromText(String pageText) throws Exception {
        this.categories = new HashSet<>();
        this.titleParenthesis = new HashSet<>();

        IRelationsExtractor<Set<String>> categoryExtractor = new CategoryRelationExtractor();
        IRelationsExtractor<Boolean> partNameExtractor = new PartNameRelationExtractor();
        IRelationsExtractor<LinkParenthesisPair> pairExtractor = new LinkAndParenthesisRelationExtractor();
        IRelationsExtractor<BeCompRelationResult> beCompExtractor = new BeCompRelationExtractor();
        IRelationsExtractor<String> infoboxExtrator = new InfoboxRelationExtractor();

        this.infobox = infoboxExtrator.extract(pageText);
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
            extLinks.addAll(linkParenthesisPair.getLinks());
            extParenthesis.addAll(linkParenthesisPair.getParenthesis());
        }

        if (WikiPageParser.isDisambiguation(this.categories)) { // need to replace with utils method to check in categories if disambig
            this.isDisambiguation = true;
            this.disambiguationLinks = extLinks;
            this.titleParenthesis = extParenthesis;
        } else {
            String firstParagraph = WikiPageParser.extractFirstPageParagraph(pageText);
            final BeCompRelationResult beCompRelations = beCompExtractor.extract(firstParagraph);
            this.beCompRelations = beCompRelations.getBeCompRelations();
        }
        return this.build();
    }
}
