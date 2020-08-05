package wiki.data.relations;

import wiki.utils.LangConfiguration;

import java.util.List;

public class PartNameRelationExtractor implements IRelationsExtractor<Boolean> {

    private static List<String> partNames;
    private static List<String> category;

    public static void initResources(LangConfiguration langConfig) {
        partNames = langConfig.getPartNames();
        category = langConfig.getCategory();
    }

    @Override
    public Boolean extract(String line) throws Exception {
        if (line != null && !line.isEmpty()) {
            line = line.toLowerCase();
            for(String name : partNames) {
                if (line.contains("===" + name + "===")) {
                    return true;
                }

                for(String cat : category) {
                    if (line.contains(cat + ":" + name)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
