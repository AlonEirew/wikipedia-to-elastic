/**
 * @author  Alon Eirew
 */

package wiki.data;

import java.util.ArrayList;
import java.util.List;

public class ArrayPageHandler implements IPageHandler {

    private final List<WikiParsedPage> pages = new ArrayList<>();

    @Override
    public void addPage(WikiParsedPage page) {
        if(page != null) {
            pages.add(page);
        }
    }

    @Override
    public void flush() {

    }

    @Override
    public void flushRemains() {

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
}
