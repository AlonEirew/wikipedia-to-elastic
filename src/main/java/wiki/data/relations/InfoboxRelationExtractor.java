package wiki.data.relations;

import wiki.utils.LangConfiguration;

public class InfoboxRelationExtractor implements IRelationsExtractor<String> {

    private static String infoboxText;

    public static void initResources(LangConfiguration lang) {
        infoboxText = lang.getInfoboxText();
    }

    @Override
    public String extract(String pageText) throws Exception {
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

        return infoBoxFinal.toString();
    }
}
