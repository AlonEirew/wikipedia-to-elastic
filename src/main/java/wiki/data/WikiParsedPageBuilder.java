/**
 * @author  Alon Eirew
 */

package wiki.data;

import java.util.Set;

public class WikiParsedPageBuilder {
    private String title;
    private long id;
    private String text;
    private String redirectTitle;
    private Set<String> aliases;
    private Set<String> description;

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

    public WikiParsedPageBuilder setAliases(Set<String> aliases) {
        this.aliases = aliases;
        return this;
    }

    public WikiParsedPageBuilder setDescription(Set<String> description) {
        this.description = description;
        return this;
    }

    public WikiParsedPage build() {
        return new WikiParsedPage(this.title, this.id, this.text, this.redirectTitle, this.aliases, this.description);
    }
}
