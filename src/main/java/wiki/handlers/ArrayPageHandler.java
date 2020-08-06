/**
 * @author  Alon Eirew
 */

package wiki.handlers;

import wiki.data.WikiParsedPage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ArrayPageHandler implements IPageHandler {

    private final List<WikiParsedPage> pages = new ArrayList<>();

    @Override
    public boolean isPageExists(String pageId) {
        for(WikiParsedPage page : this.pages) {
            if(String.valueOf(page.getId()).equals(pageId)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void addPage(WikiParsedPage page) {
        if(page != null) {
            pages.add(page);
        }
    }

    @Override
    public void flush() {

    }

    public List<WikiParsedPage> getPages() {
        return this.pages;
    }

    @Override
    public String toString() {
        return "ArrayPageHandler{" +
                "pages=" + pages +
                '}';
    }

    @Override
    public void close() throws IOException {
    }
}
