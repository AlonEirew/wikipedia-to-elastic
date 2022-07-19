package wiki.persistency;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import wiki.data.WikipediaParsedPage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

public class JsonFileAPI implements IAPI<Boolean> {
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    private final Path indexFolder;
    private final AtomicInteger indexFile = new AtomicInteger(0);

    private final AtomicInteger totalIdsProcessed = new AtomicInteger(0);
    private final AtomicInteger totalIdsSuccessfullyCommitted = new AtomicInteger(0);

    private final ConcurrentLinkedQueue<WikipediaParsedPage> docsToWrite = new ConcurrentLinkedQueue<>();
    private final CopyOnWriteArraySet<String> allDocIds = new CopyOnWriteArraySet<>();

    public JsonFileAPI(String indexFolder) {
        this.indexFolder = Paths.get(indexFolder);
    }

    @Override
    public Boolean deleteIndex() throws IOException {
        if(Files.exists(this.indexFolder)) {
            Files.walk(this.indexFolder).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
        return true;
    }

    @Override
    public Boolean createIndex() throws IOException {
        if(!Files.exists(this.indexFolder)) {
            Files.createDirectory(this.indexFolder);
        }

        return true;
    }

    @Override
    public boolean isIndexExists() {
        return Files.exists(this.indexFolder);
    }

    @Override
    public boolean isDocExists(String docId) {
        return allDocIds.contains(docId);
    }

    @Override
    public void addDoc(WikipediaParsedPage page) {
        if (page != null) {
            this.docsToWrite.add(page);
            this.totalIdsProcessed.incrementAndGet();
        }
    }

    @Override
    public int getTotalIdsProcessed() {
        return this.totalIdsProcessed.get();
    }

    @Override
    public int getTotalIdsSuccessfullyCommitted() {
        return this.totalIdsSuccessfullyCommitted.get();
    }

    @Override
    public void addBulkAsnc(List<WikipediaParsedPage> list) {
        if (list != null && !list.isEmpty()) {
            String filePath = this.indexFolder + File.separator + "WikiIndexFile_" + this.indexFile.incrementAndGet() + ".json";
            try(FileWriter writer = new FileWriter(filePath)) {
                gson.toJson(list, writer);
                this.totalIdsSuccessfullyCommitted.addAndGet(list.size());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() throws IOException {
        List<WikipediaParsedPage> allRemaining = new ArrayList<>(this.docsToWrite);
        if(!this.docsToWrite.isEmpty()) {
            this.addBulkAsnc(allRemaining);
            this.docsToWrite.clear();
        }
    }
}
