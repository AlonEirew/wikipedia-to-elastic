/**
 * @author  Alon Eirew
 */

package wiki.data;

import java.util.List;
import java.util.Objects;

public class WikipediaParsedPage {
    private final String title;
    private final transient long id;
    private final String text;
    private final List<String> parsedParagraphs;
    private final String redirectTitle;
    private final WikipediaParsedPageRelations relations;

    public WikipediaParsedPage(String title, long id, String text, List<String> parsedParagraphs, String redirectTitle, WikipediaParsedPageRelations relations) {
        this.title = title;
        this.id = id;
        this.text = text;
        this.parsedParagraphs = parsedParagraphs;
        this.redirectTitle = redirectTitle;
        this.relations = relations;
    }

    public WikipediaParsedPage(WikipediaParsedPage page) {
        this.title = page.title;
        this.id = page.id;
        this.text = page.text;
        this.parsedParagraphs = page.parsedParagraphs;
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

    public List<String> getParsedParagraphs() {
        return parsedParagraphs;
    }

    public WikipediaParsedPageRelations getRelations() {
        return relations;
    }

    public boolean isValid() {
        return this.text != null || this.parsedParagraphs != null || this.relations != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WikipediaParsedPage that = (WikipediaParsedPage) o;
        return id == that.id && Objects.equals(title, that.title) && Objects.equals(text, that.text) && Objects.equals(parsedParagraphs, that.parsedParagraphs) && Objects.equals(redirectTitle, that.redirectTitle) && Objects.equals(relations, that.relations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, id, text, parsedParagraphs, redirectTitle, relations);
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
