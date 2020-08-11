/**
 * @author  Alon Eirew
 */

package wiki.data;

public class WikiParsedPageBuilder {
    private String title;
    private long id;
    private String text = "";
    private String redirectTitle;
    private WikiParsedPageRelations relations;

    public WikiParsedPageBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public WikiParsedPageBuilder setId(long id) {
        this.id = id;
        return this;
    }

    public WikiParsedPageBuilder setText(String text) {
        this.text = text;
        return this;
    }

    public WikiParsedPageBuilder setRedirectTitle(String redirectTitle) {
        this.redirectTitle = redirectTitle;
        return this;
    }

    public WikiParsedPageBuilder setRelations(WikiParsedPageRelations relations) {
        this.relations = relations;
        return this;
    }

    public WikiParsedPage build() {
        return new WikiParsedPage(this.title, this.id, this.text, this.redirectTitle, this.relations);
    }
}
