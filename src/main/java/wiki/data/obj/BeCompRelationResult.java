package wiki.data.obj;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import wiki.utils.WikiPageParser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BeCompRelationResult {
    private final Set<String> beCompRelations;

    public BeCompRelationResult() {
        this.beCompRelations = new HashSet<>();
    }

    public Set<String> getBeCompRelations() {
        return beCompRelations;
    }

    public void addBeCompRelation(String beCompRelations) {
        if(beCompRelations != null && !beCompRelations.isEmpty()) {
            this.beCompRelations.add(beCompRelations);
        }
    }

    public void addBeCompRelationList(List<IndexedWord> list) {
        StringBuilder converted = new StringBuilder();
        if(list != null && !list.isEmpty()) {
            for(IndexedWord word : list) {
                converted.append(word.get(CoreAnnotations.ValueAnnotation.class)).append(" ");
            }
        }

        if(converted.length() != 0) {
            this.beCompRelations.add(converted.toString().trim());
        }
    }
}
