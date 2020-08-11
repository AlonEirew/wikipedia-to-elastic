/**
 * @author  Alon Eirew
 */

package wiki.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wiki.data.WikiParsedPageBuilder;
import wiki.data.WikiParsedPageRelations;
import wiki.data.WikiParsedPageRelationsBuilder;
import wiki.handlers.IPageHandler;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

public class STAXParser {

    public enum DeleteUpdateMode {DELETE, UPDATE, NA}

    private static final String PAGE_ELEMENT = "page";
    private static final String TITLE_ELEMENT = "title";
    private static final String ID_ELEMENT = "id";
    private static final String TEXT_ELEMENT = "text";
    private static final String REDIRECT_ELEMENT = "redirect";

    private final String redirectTextPrefix;
    private final String[] filterTitles;

    private final static Logger LOGGER = LogManager.getLogger(STAXParser.class);

    private final boolean includeRawText;
    private final RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
    private final BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<>(100);
    private final IPageHandler handler;
    private final ExecutorService executorService;
    private final Set<Long> totalIds = new HashSet<>();
    private boolean extractFields;
    private DeleteUpdateMode deleteUpdateMode;


    public STAXParser(IPageHandler handler, LangConfiguration langConfig, boolean includeRawText) {
        this.executorService = initExecutorService();
        this.handler = handler;
        this.extractFields = true;
        this.deleteUpdateMode = DeleteUpdateMode.NA;
        this.filterTitles = langConfig.getTitlesPref().toArray(new String[0]);
        this.redirectTextPrefix = "#" + langConfig.getRedirect();
        this.includeRawText = includeRawText;
    }

    public STAXParser(IPageHandler handler, WikiToElasticConfiguration config, LangConfiguration langConfig, DeleteUpdateMode mode) {
        this(handler, langConfig, config.isIncludeRawText());
        this.deleteUpdateMode = mode;
        this.extractFields = config.isExtractRelationFields();
    }

    private ExecutorService initExecutorService() {
        int cores = Runtime.getRuntime().availableProcessors();
        return new ThreadPoolExecutor(cores, cores,
                0L, TimeUnit.MILLISECONDS, this.blockingQueue, this.rejectedExecutionHandler);
    }


    /**
     * Starting point of the parsing process of the wikipedia xml dump
     * @param inputStream to the wikipedia dump file
     */
    public void parse(InputStream inputStream) {
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLEventReader reader = factory.createXMLEventReader(inputStream);
            // Go over the xml element and search for <page> element
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
        LOGGER.info("Shutting down thread pool and preparing to close process...");
        this.executorService.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!this.executorService.awaitTermination(5, TimeUnit.MINUTES)) {
                this.executorService.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!this.executorService.awaitTermination(60, TimeUnit.SECONDS))
                    LOGGER.error("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            this.executorService.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Parse xml "page" element (represent a full wikipedia page with attributes)
     * Extract the main attributes of {title, redirect, id, text}
     * @param reader
     * @throws XMLStreamException
     */
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
                            id = Long.parseLong(reader.getElementText());
                            totalIds.add(id);
                        }
                        break;
                    case TEXT_ELEMENT:
                        text = reader.getElementText();
                        if(text.startsWith(redirectTextPrefix)) {
                            text = redirectTextPrefix;
                        }
                        break;
                }
            }
        }

        if(title != null) {
            if (this.deleteUpdateMode == DeleteUpdateMode.UPDATE) {
                if (this.handler.isPageExists(String.valueOf(id))) {
                    LOGGER.info("Page with id-" + id + ", title-" + title + ", already exist, moving on...");
                } else {
                    handlePage(title, id, text, redirect);
                }
            } else {
                handlePage(title, id, text, redirect);
            }
        }
    }

    /**
     * Processing the page in a separate thread (extracting relations and adding to persistence queue)
     */
    private void handlePage(String title, long id, String text, String redirect) {
        String titleLow = title.toLowerCase();
        // If page is a meta data page, don't process it
        if(Arrays.stream(filterTitles).parallel().anyMatch(titleLow::contains)) {
            LOGGER.info("Skipping page processing of- " + title);
        } else {
            this.executorService.submit(() -> {
                try {
                    LOGGER.info("prepare to commit page with id-" + id + ", title-" + title);
                    WikiParsedPageRelations relations;
                    // Redirect pages text are not processed (can be found in the underline redirected page)
                    if (this.extractFields && (redirect == null || redirect.isEmpty())) {
                        relations = new WikiParsedPageRelationsBuilder().buildFromText(text);
                    } else {
                        // Empty relations for redirect pages (Relations can be found in the underline redirected page)
                        relations = new WikiParsedPageRelationsBuilder().build();
                    }

                    final WikiParsedPageBuilder pageBuilder = new WikiParsedPageBuilder()
                            .setId(id)
                            .setTitle(title)
                            .setRedirectTitle(redirect)
                            .setRelations(relations);

                    if(includeRawText) {
                        pageBuilder.setText(text);
                    }

                    // Adding the page to the elastic search queue handler
                    handler.addPage(pageBuilder.build());
                } catch (Exception ex) {
                    LOGGER.error("Got Exception in thread", ex);
                }
            });
        }
    }

    public Set<Long> getTotalIds() {
        return totalIds;
    }
}
