/**
 * @author  Alon Eirew
 */

package wiki.handlers;

import wiki.data.WikiParsedPage;

import java.io.Closeable;

public interface IPageHandler extends Closeable {
    boolean isPageExists(String pageId);
    void addPage(WikiParsedPage page);
    void flush();
}
