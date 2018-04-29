/**
 * @author  Alon Eirew
 */

package wiki.parsers;

import java.io.InputStream;

public interface IWikiParser {

    String PAGE_ELEMENT = "page";
    String TITLE_ELEMENT = "title";
    String ID_ELEMENT = "id";
    String TEXT_ELEMENT = "text";
    String REDIRECT_ELEMENT = "redirect";
    String REDIRECT_TEXT_PREFIX = "#REDIRECT";

    void parse(InputStream inputStream);
}
