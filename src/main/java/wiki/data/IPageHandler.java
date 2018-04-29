/**
 * @author  Alon Eirew
 */

package wiki.data;

public interface IPageHandler {
    void addPage(WikiParsedPage page);
    void flush();
}
