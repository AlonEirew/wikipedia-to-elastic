package wiki.data.relations;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wiki.utils.LangConfiguration;

import java.util.List;

public class PartNameRelationExtractor implements IRelationsExtractor<Boolean> {
    private final static Logger LOGGER = LogManager.getLogger(PartNameRelationExtractor.class);

    private static List<String> partNames;
    private static List<String> category;

    public static void initResources(LangConfiguration langConfig) {
        LOGGER.info("Initiating PartNameRelationExtractor");
        partNames = langConfig.getPartNames();
        category = langConfig.getCategory();
        LOGGER.info("PartNameRelationExtractor initialized");
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
