/**
 * @author  Alon Eirew
 */

package wiki.data;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class WikiParsedPage {
    private final String title;
    private final transient long id;
    private final String text;
    private final String redirectTitle;
    private final WikiParsedPageRelations relations;

    public WikiParsedPage(String title, long id, String text, String redirectTitle, WikiParsedPageRelations relations) {
        this.title = title;
        this.id = id;
        this.text = text;
        this.redirectTitle = redirectTitle;
        this.relations = relations;
    }

    public WikiParsedPage(WikiParsedPage page) {
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

    public WikiParsedPageRelations getRelations() {
        return relations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WikiParsedPage that = (WikiParsedPage) o;
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
        return "WikiParsedPage{" +
                "title='" + title + '\'' +
                ", id='" + id + '\'' +
                ", redirectTitle='" + redirectTitle + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
