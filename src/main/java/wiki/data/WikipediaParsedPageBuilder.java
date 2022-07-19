/**
 * @author  Alon Eirew
 */

package wiki.data;

import java.util.List;

public class WikipediaParsedPageBuilder {
    private String title;
    private long id;
    private String text = "";
    private List<String> parsedParagraphs;
    private String redirectTitle;
    private WikipediaParsedPageRelations relations;

    public WikipediaParsedPageBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public WikipediaParsedPageBuilder setId(long id) {
        this.id = id;
        return this;
    }

    public WikipediaParsedPageBuilder setText(String text) {
        this.text = text;
        return this;
    }

    public WikipediaParsedPageBuilder setRedirectTitle(String redirectTitle) {
        this.redirectTitle = redirectTitle;
        return this;
    }

    public WikipediaParsedPageBuilder setRelations(WikipediaParsedPageRelations relations) {
        this.relations = relations;
        return this;
    }

    public WikipediaParsedPageBuilder setParsedParagraphs(List<String> parsedParagraphs) {
        this.parsedParagraphs = parsedParagraphs;
        return this;
    }

    public WikipediaParsedPage build() {
        return new WikipediaParsedPage(this.title, this.id, this.text, this.parsedParagraphs, this.redirectTitle, this.relations);
    }
}
