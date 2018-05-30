package wiki.data.relations;

import java.util.Set;

public interface IRelationsExtractor {
    Set<String> extract(String lineToExtractFrom);
}
