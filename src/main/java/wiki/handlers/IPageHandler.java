/**
 * @author  Alon Eirew
 */

package wiki.handlers;

import wiki.data.WikipediaParsedPage;

import java.io.Closeable;

public interface IPageHandler extends Closeable {
    boolean isPageExists(String pageId);
    void addPage(WikipediaParsedPage page);
    void flush();
}
