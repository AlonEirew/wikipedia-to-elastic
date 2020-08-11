package wiki.data;

import wiki.data.relations.ExtractorsManager;
import wiki.utils.WikiPageParser;

public class WikiParsedPageRelationsBuilder {
    private boolean isPartName = false;

    private final ExtractorsManager extractorsManager = new ExtractorsManager();

    public WikiParsedPageRelations build() {
        return new WikiParsedPageRelations(
                this.extractorsManager.getInfoboxExtrator().getResult(),
                this.isPartName,
                this.extractorsManager.getCategoryExtractor().isDisambiguation(),
                this.extractorsManager.getPairExtractor().getLinks(),
                this.extractorsManager.getCategoryExtractor().getResult(),
                this.extractorsManager.getPairExtractor().getTitleParenthesis(),
                this.extractorsManager.getBeCompExtractor().getResult().getBeCompRelations());
    }

    public WikiParsedPageRelations buildFromText(String pageText) throws Exception {
        this.extractorsManager.runExtractFromPageText(pageText);

        String[] textLines = pageText.split("\n");
        this.extractorsManager.runExtractFromPageLines(textLines);

        if (!this.extractorsManager.getPartNameExtractor().getResult()) {
            this.isPartName = this.extractorsManager.getCategoryExtractor().isPartNameInCategories();
        }

        if (!this.extractorsManager.getCategoryExtractor().isDisambiguation()) { // need to replace with utils method to check in categories if disambig
            String firstParagraph = WikiPageParser.extractFirstPageParagraph(pageText);
            this.extractorsManager.runExtractorFromParagraph(firstParagraph);
        }
        return this.build();
    }
}
