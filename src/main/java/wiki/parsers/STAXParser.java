/**
 * @author  Alon Eirew
 */

package wiki.parsers;

import wiki.data.IPageHandler;
import wiki.data.WikiParsedPage;
import wiki.data.WikiParsedPageBuilder;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;

public class STAXParser implements IWikiParser {

    private final IPageHandler handler;

    public STAXParser(IPageHandler handler) {
        this.handler = handler;
    }

    @Override
    public void parse(InputStream inputStream) {
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLEventReader reader = factory.createXMLEventReader(inputStream);
            while (reader.hasNext()) {
                final XMLEvent event = reader.nextEvent();
                if (event.isStartElement() && event.asStartElement().getName()
                        .getLocalPart().equals(PAGE_ELEMENT)) {
                    parsePage(reader);
                }
            }

        } catch(XMLStreamException e) {
            e.printStackTrace();
        }
    }

    private void parsePage(final XMLEventReader reader) throws XMLStreamException {
        String title = null;
        long id = -1;
        String text = null;
        String redirect = null;
        while (reader.hasNext()) {
            final XMLEvent event = reader.nextEvent();
            if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(PAGE_ELEMENT)) {
                break;
            }
            if (event.isStartElement()) {
                final StartElement element = event.asStartElement();
                final String elementName = element.getName().getLocalPart();
                switch (elementName) {
                    case TITLE_ELEMENT:
                        title = reader.getElementText();
                        break;
                    case REDIRECT_ELEMENT:
                        redirect = element.getAttributeByName(new QName(TITLE_ELEMENT)).getValue();
                        break;
                    case ID_ELEMENT:
                        if(id == -1) {
                            id = Long.valueOf(reader.getElementText());
                        }
                        break;
                    case TEXT_ELEMENT:
                        text = reader.getElementText();
                        if(text.startsWith(REDIRECT_TEXT_PREFIX)) {
                            text = REDIRECT_TEXT_PREFIX;
                        }
                        break;
                }
            }
        }

        final WikiParsedPage page = new WikiParsedPageBuilder()
                .setId(id)
                .setText(text)
                .setTitle(title)
                .setRedirectTitle(redirect)
                .build();

        handler.addPage(page);
    }
}
