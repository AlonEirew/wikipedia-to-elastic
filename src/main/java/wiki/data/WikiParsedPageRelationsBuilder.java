package wiki.data;

import wiki.data.obj.BeCompRelationResult;
import wiki.data.relations.*;
import wiki.utils.WikiPageParser;

public class WikiParsedPageRelationsBuilder {
    private boolean isDisambiguation = false;
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
                this.isDisambiguation,
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

        if (WikiPageParser.isDisambiguation(this.categoryExtractor.getResult())) { // need to replace with utils method to check in categories if disambig
            this.isDisambiguation = true;
        } else {
            String firstParagraph = WikiPageParser.extractFirstPageParagraph(pageText);
            beCompExtractor.extract(firstParagraph);
            this.beCompExtractor.extract(firstParagraph);
        }
        return this.build();
    }
}
