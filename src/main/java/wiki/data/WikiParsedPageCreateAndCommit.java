package wiki.data;

public class WikiParsedPageCreateAndCommit implements Runnable {

    private String pageTitle;
    private String pageText;
    private WikiParsedPageBuilder pageBuilder;
    private final IPageHandler handler;

    public WikiParsedPageCreateAndCommit(WikiParsedPageBuilder pageBuilder, String title, String pageText, IPageHandler handler) {
        this.pageTitle = title;
        this.pageText = pageText;
        this.pageBuilder = pageBuilder;
        this.handler = handler;
    }

    @Override
    public void run() {

        final WikiParsedPageRelations relations = new WikiParsedPageRelationsBuilder().buildFromWikipediaPageText(this.pageTitle, this.pageText);

        final WikiParsedPage page = this.pageBuilder
                .setText(this.pageText)
                .setRelations(relations)
                .build();

        this.handler.addPage(page);
    }
}
