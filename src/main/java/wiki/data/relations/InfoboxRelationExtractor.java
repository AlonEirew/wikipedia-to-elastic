package wiki.data.relations;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wiki.utils.LangConfiguration;

public class InfoboxRelationExtractor implements IRelationsExtractor<String> {
    private final static Logger LOGGER = LogManager.getLogger(InfoboxRelationExtractor.class);

    private static String infoboxText;
    private String infobox;

    public static void initResources(LangConfiguration lang) {
        LOGGER.info("Initiating InfoboxRelationExtractor");
        infoboxText = lang.getInfoboxText();
        LOGGER.info("InfoboxRelationExtractor initialized");
    }

    @Override
    public IRelationsExtractor<String> extract(String pageText) throws Exception {
        StringBuilder infoBoxFinal = new StringBuilder();

        final int beginIndex = pageText.indexOf("{{" + infoboxText);
        if (beginIndex != -1) {
            final String infoboxSubstring = pageText.substring(beginIndex);
            int infoBarCount = 0;
            for (int i = 0; i < infoboxSubstring.length(); i++) {
                final char c = infoboxSubstring.charAt(i);
                if (c == '}') {
                    infoBarCount--;
                    if (infoBarCount == 0) {
                        infoBoxFinal.append(c);
                        break;
                    }
                } else if (c == '{') {
                    infoBarCount++;
                }

                infoBoxFinal.append(c);
            }
        }

        this.infobox = infoBoxFinal.toString();
        return this;
    }

    @Override
    public String getResult() {
        return this.infobox;
    }
}
