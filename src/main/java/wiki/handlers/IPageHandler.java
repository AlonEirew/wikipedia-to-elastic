/**
 * @author  Alon Eirew
 */

package wiki.handlers;

import wiki.data.WikiParsedPage;

public interface IPageHandler {
    boolean isPageExists(String pageId);
    void addPage(WikiParsedPage page);
    void flush();
    void flushRemains();
}
