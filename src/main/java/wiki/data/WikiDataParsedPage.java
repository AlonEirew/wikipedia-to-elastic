package wiki.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WikiDataParsedPage {
    private final static HashMap<String, String> wikidataIdsToTitles = new HashMap<>();

    private final String pageId;
    private final String pageTitle;
    private final List<String> aliases;
    private List<String> partOf;
    private List<String> hasPart;
    private List<String> hasEffect;
    private List<String> hasCause;
    private List<String> hasImmidiateCause;

    public WikiDataParsedPage(String pageId, String pageTitle,
                              List<String> aliases, List<String> partOf, List<String> hasPart,
                              List<String> hasEffect, List<String> hasCause, List<String> hasImmidiateCause) {
        this.pageId = pageId;
        this.pageTitle = pageTitle;
        this.aliases = aliases;
        this.partOf = partOf;
        this.hasPart = hasPart;
        this.hasEffect = hasEffect;
        this.hasCause = hasCause;
        this.hasImmidiateCause = hasImmidiateCause;

        if(!wikidataIdsToTitles.containsKey(pageId)) {
            wikidataIdsToTitles.put(pageId, pageTitle);
        }
    }

    public String getPageId() {
        return pageId;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public List<String> getPartOf() {
        return partOf;
    }

    public List<String> getHasPart() {
        return hasPart;
    }

    public List<String> getHasEffect() {
        return hasEffect;
    }

    public List<String> getHasCause() {
        return hasCause;
    }

    public List<String> getHasImmidiateCause() {
        return hasImmidiateCause;
    }

    public void setPartOf(List<String> partOf) {
        this.partOf = partOf;
    }

    public void setHasPart(List<String> hasPart) {
        this.hasPart = hasPart;
    }

    public void setHasEffect(List<String> hasEffect) {
        this.hasEffect = hasEffect;
    }

    public void setHasCause(List<String> hasCause) {
        this.hasCause = hasCause;
    }

    public void setHasImmidiateCause(List<String> hasImmidiateCause) {
        this.hasImmidiateCause = hasImmidiateCause;
    }

    public static void replaceIdsWithTitles(Map<String, WikiDataParsedPage> wikiDataParsedPages) {
        for (WikiDataParsedPage page : wikiDataParsedPages.values()) {
            page.setHasCause(convertToTitles(page.hasCause));
            page.setHasEffect(convertToTitles(page.hasEffect));
            page.setHasImmidiateCause(convertToTitles(page.hasImmidiateCause));
            page.setHasPart(convertToTitles(page.hasPart));
            page.setPartOf(convertToTitles(page.partOf));
        }
    }

    private static List<String> convertToTitles(List<String> idsList) {
        List<String> titels = new ArrayList<>();
        if(idsList != null) {
            for(String id : idsList) {
                if (wikidataIdsToTitles.containsKey(id)) {
                    titels.add(wikidataIdsToTitles.get(id));
                }
            }
        }

        return titels;
    }
}
