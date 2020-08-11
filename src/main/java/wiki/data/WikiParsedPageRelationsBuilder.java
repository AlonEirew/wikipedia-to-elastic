package wiki.data;

import wiki.data.obj.BeCompRelationResult;
import wiki.data.relations.*;
import wiki.utils.WikiPageParser;

public class WikiParsedPageRelationsBuilder {
    private boolean isPartName = false;

    private final CategoryRelationExtractor categoryExtractor = new CategoryRelationExtractor();
    private final IRelationsExtractor<Boolean> partNameExtractor = new PartNameRelationExtractor();
    private final LinkAndParenthesisRelationExtractor pairExtractor = new LinkAndParenthesisRelationExtractor();
    private final IRelationsExtractor<BeCompRelationResult> beCompExtractor = new BeCompRelationExtractor();
    private final IRelationsExtractor<String> infoboxExtrator = new InfoboxRelationExtractor();

    public WikiParsedPageRelations build() {
        return new WikiParsedPageRelations(
                this.infoboxExtrator.getResult(),
                this.isPartName,
                this.categoryExtractor.isDisambiguation(),
                this.pairExtractor.getLinks(),
                this.categoryExtractor.getResult(),
                this.pairExtractor.getTitleParenthesis(),
                this.beCompExtractor.getResult().getBeCompRelations());
    }

    public WikiParsedPageRelations buildFromText(String pageText) throws Exception {
        infoboxExtrator.extract(pageText);
        String[] textLines = pageText.split("\n");

        for (String line : textLines) {
            categoryExtractor.extract(line);
            partNameExtractor.extract(line);
            pairExtractor.extract(line);
        }

        if (!this.partNameExtractor.getResult()) {
            this.isPartName = this.categoryExtractor.isPartNameInCategories();
        }

        if (!this.categoryExtractor.isDisambiguation()) { // need to replace with utils method to check in categories if disambig
            String firstParagraph = WikiPageParser.extractFirstPageParagraph(pageText);
            this.beCompExtractor.extract(firstParagraph);
        }
        return this.build();
    }
}
