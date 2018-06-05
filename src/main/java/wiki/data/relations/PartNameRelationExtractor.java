package wiki.data.relations;

import org.apache.xpath.operations.Bool;

import java.util.Set;

public class PartNameRelationExtractor implements IRelationsExtractor<Boolean> {
    @Override
    public Boolean extract(String line) {
        if (line != null && !line.isEmpty()) {
            line = line.toLowerCase();
            if (line.contains("===as surname==="))
                return true;
            else if (line.contains("===as given name==="))
                return true;
            else if (line.contains("===given names==="))
                return true;
            else if (line.contains("==as a surname=="))
                return true;
            else if (line.contains("==people with the surname=="))
                return true;
            else if (line.contains("==family name and surname=="))
                return true;
            else if (line.contains("category:given names"))
                return true;
        }
        return false;
    }
}
