package wiki.data.relations;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wiki.utils.LangConfiguration;

import java.util.ArrayList;
import java.util.List;

public class PartNameRelationExtractor implements IRelationsExtractor<Boolean> {
    private final static Logger LOGGER = LogManager.getLogger(PartNameRelationExtractor.class);

    private static List<String> partNames = new ArrayList<>();
    private static List<String> category = new ArrayList<>();

    private boolean isPartName = false;

    public static void initResources(LangConfiguration langConfig) {
        LOGGER.info("Initiating PartNameRelationExtractor");
        partNames = langConfig.getPartNames();
        category = langConfig.getCategory();
        LOGGER.info("PartNameRelationExtractor initialized");
    }

    @Override
    public IRelationsExtractor<Boolean> extract(String line) throws Exception {
        if(!this.isPartName) {
            if (line != null && !line.isEmpty()) {
                line = line.toLowerCase();
                for (String name : partNames) {
                    if (line.contains("===" + name + "===")) {
                        this.isPartName = true;
                        return this;
                    }

                    for (String cat : category) {
                        if (line.contains(cat + ":" + name)) {
                            this.isPartName = true;
                            return this;
                        }
                    }
                }
            }
        }

        return this;
    }

    @Override
    public Boolean getResult() {
        return this.isPartName;
    }
}
