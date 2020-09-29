/**
 * @author  Alon Eirew
 */

package wiki.handlers;

import wiki.data.WikipediaParsedPage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ArrayPageHandler implements IPageHandler {

    private final List<WikipediaParsedPage> pages = new ArrayList<>();

    @Override
    public boolean isPageExists(String pageId) {
        for(WikipediaParsedPage page : this.pages) {
            if(String.valueOf(page.getId()).equals(pageId)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void addPage(WikipediaParsedPage page) {
        if(page != null) {
            pages.add(page);
        }
    }

    @Override
    public void flush() {

    }

    public List<WikipediaParsedPage> getPages() {
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
