package wiki.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wiki.data.WikiParsedPage;
import wiki.data.WikiParsedPageBuilder;
import wiki.data.WikiParsedPageRelations;
import wiki.data.WikiParsedPageRelationsBuilder;

public class WikiParsedPageCreateAndCommit implements Runnable {

    private final static Logger LOGGER = LogManager.getLogger(WikiParsedPageCreateAndCommit.class);

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

        if(!this.handler.isPageExists(String.valueOf(this.id))) {
            LOGGER.info("prepare to commit page with id-" + this.id + ", title-" + this.pageTitle);
            WikiParsedPageRelations relations;
            if (this.redirect == null || this.redirect.isEmpty()) {
                relations = new WikiParsedPageRelationsBuilder().buildFromWikipediaPageText(this.pageText);
            } else {
                relations = new WikiParsedPageRelationsBuilder().build();
            }

            final WikiParsedPage page = new WikiParsedPageBuilder()
                    .setId(this.id)
                    .setTitle(this.pageTitle)
                    .setRedirectTitle(redirect)
                    .setText(this.pageText)
                    .setRelations(relations)
                    .build();

            this.handler.addPage(page);
        } else {
            LOGGER.info("Page with id-" + this.id + ", title-" + this.pageTitle + ", already exist, moving on...");
        }
    }
}
