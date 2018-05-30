/**
 * @author  Alon Eirew
 */

package wiki.parsers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wiki.handlers.IPageHandler;
import wiki.handlers.WikiParsedPageCreateAndCommit;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

public class STAXParser implements IWikiParser {

    private final static Logger LOGGER = LogManager.getLogger(STAXParser.class);

    private RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
    private BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(100);
    private final IPageHandler handler;
    private ExecutorService executorService;
    private Set<Long> totalIds = new HashSet<>();

    public STAXParser(IPageHandler handler) {
        int cores = Runtime.getRuntime().availableProcessors();
        this.executorService = new ThreadPoolExecutor(cores, cores,
                0L, TimeUnit.MILLISECONDS, this.blockingQueue, this.rejectedExecutionHandler);
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
            LOGGER.error(e);
        }
    }

    public void shutDownPool() {
        this.executorService.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!this.executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                this.executorService.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!this.executorService.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            this.executorService.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
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
                            totalIds.add(id);
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

        WikiParsedPageCreateAndCommit wikiParsedPageCreateAndCommit = new WikiParsedPageCreateAndCommit(id, title, redirect, text, this.handler);

        this.executorService.submit(wikiParsedPageCreateAndCommit);
    }

    public Set<Long> getTotalIds() {
        return totalIds;
    }
}
