package wiki.data.obj;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import wiki.utils.WikiPageParser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BeCompRelationResult {
    private final Set<String> beCompRelations;
    private final Set<String> beCompRelationsNorm;

    public BeCompRelationResult() {
        this.beCompRelations = new HashSet<>();
        this.beCompRelationsNorm = new HashSet<>();
    }

    public BeCompRelationResult(Set<String> beCompRelations, Set<String> beCompRelationsNorm) {
        this.beCompRelations = beCompRelations;
        this.beCompRelationsNorm = beCompRelationsNorm;
    }

    public Set<String> getBeCompRelations() {
        return beCompRelations;
    }

    public void addBeCompRelation(String beCompRelations) {
        if(beCompRelations != null && !beCompRelations.isEmpty()) {
            this.beCompRelations.add(beCompRelations);
        }
    }

    public Set<String> getBeCompRelationsNorm() {
        return beCompRelationsNorm;
    }

    public void addBeCompRelationsNorm(String beCompRelationsNorm) {
        if(beCompRelationsNorm != null && !beCompRelationsNorm.isEmpty()) {
            this.beCompRelationsNorm.add(beCompRelationsNorm);
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

    public void addBeCompRelationListNorm(List<IndexedWord> list) {
        StringBuilder converted = new StringBuilder();
        if(list != null && !list.isEmpty()) {
            for(IndexedWord word : list) {
                String value = word.get(CoreAnnotations.LemmaAnnotation.class);
                if(value == null || value.isEmpty()) {
                    value = word.get(CoreAnnotations.ValueAnnotation.class);
                }
                String cleanVal = WikiPageParser.cleanValue(value);

                if(cleanVal != null) {
                    converted.append(cleanVal).append(" ");
                }
            }
        }

        if(converted.length() != 0) {
            this.beCompRelationsNorm.add(converted.toString().toLowerCase().trim());
        }
    }
}
