package wiki.persistency;

import wiki.data.WikipediaParsedPage;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

public interface IAPI<R> extends Closeable {
    R deleteIndex() throws IOException;
    R createIndex() throws IOException;
    boolean isIndexExists();
    boolean isDocExists(String docId);
    void addBulkAsnc(List<WikipediaParsedPage> pages);
    void addDoc(WikipediaParsedPage page);
    int getTotalIdsProcessed();
    int getTotalIdsSuccessfullyCommitted();
}
