package wiki.data.obj;

import java.util.Set;

public class LinkParenthesisPair {
    private Set<String> links;
    private Set<String> parenthesis;

    public LinkParenthesisPair(Set<String> links, Set<String> parenthesis) {
        this.links = links;
        this.parenthesis = parenthesis;
    }

    public Set<String> getLinks() {
        return links;
    }

    public void setLinks(Set<String> links) {
        this.links = links;
    }

    public Set<String> getParenthesis() {
        return parenthesis;
    }

    public void setParenthesis(Set<String> parenthesis) {
        this.parenthesis = parenthesis;
    }
}

