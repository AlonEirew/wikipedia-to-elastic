package wiki.handlers;

import wiki.data.WikiParsedPage;
import wiki.data.WikiParsedPageBuilder;
import wiki.data.WikiParsedPageRelations;
import wiki.data.WikiParsedPageRelationsBuilder;
import wiki.handlers.IPageHandler;

public class WikiParsedPageCreateAndCommit implements Runnable {

    private String pageTitle;
    private String pageText;
    private long id;
    private String redirect;
    private final IPageHandler handler;

    public WikiParsedPageCreateAndCommit(long id, String title, String redirect, String pageText, IPageHandler handler) {
        this.id = id;
        this.pageTitle = title;
        this.redirect = redirect;
        this.pageText = pageText;
        this.handler = handler;
    }

    @Override
    public void run() {

        WikiParsedPageRelations relations = new WikiParsedPageRelationsBuilder().build();
        if(this.redirect == null || this.redirect.isEmpty()) {
            relations = new WikiParsedPageRelationsBuilder().buildFromWikipediaPageText(this.pageTitle, this.pageText);
        }

        final WikiParsedPage page = new WikiParsedPageBuilder()
                .setId(this.id)
                .setTitle(this.pageTitle)
                .setRedirectTitle(redirect)
                .setText(this.pageText)
                .setRelations(relations)
                .build();;

        this.handler.addPage(page);
    }
}
