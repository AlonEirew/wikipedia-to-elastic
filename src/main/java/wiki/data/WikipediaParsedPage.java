/**
 * @author  Alon Eirew
 */

package wiki.data;

import java.util.Objects;

public class WikipediaParsedPage {
    private final String title;
    private final transient long id;
    private final String text;
    private final String redirectTitle;
    private final WikipediaParsedPageRelations relations;

    public WikipediaParsedPage(String title, long id, String text, String redirectTitle, WikipediaParsedPageRelations relations) {
        this.title = title;
        this.id = id;
        this.text = text;
        this.redirectTitle = redirectTitle;
        this.relations = relations;
    }

    public WikipediaParsedPage(WikipediaParsedPage page) {
        this.title = page.title;
        this.id = page.id;
        this.text = page.text;
        this.redirectTitle = page.redirectTitle;
        this.relations = page.relations;
    }

    public String getTitle() {
        return title;
    }

    public long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getRedirectTitle() {
        return redirectTitle;
    }

    public WikipediaParsedPageRelations getRelations() {
        return relations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WikipediaParsedPage that = (WikipediaParsedPage) o;
        return id == that.id &&
                Objects.equals(title, that.title) &&
                Objects.equals(text, that.text) &&
                Objects.equals(redirectTitle, that.redirectTitle) &&
                Objects.equals(relations, that.relations);
    }

    @Override
    public int hashCode() {

        return Objects.hash(title, id, text, redirectTitle, relations);
    }

    @Override
    public String toString() {
        return "WikipediaParsedPage{" +
                "title='" + title + '\'' +
                ", id='" + id + '\'' +
                ", redirectTitle='" + redirectTitle + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
