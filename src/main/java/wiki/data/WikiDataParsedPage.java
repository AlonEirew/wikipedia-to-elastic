package wiki.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WikiDataParsedPage {
    private final static Map<String, String> wikidataIdsToTitles = new HashMap<>();

    private transient String elasticPageId;
    private transient String wikidataPageId;
    private transient final String pageTitle;
    private final List<String> aliases;
    private List<String> partOf;
    private List<String> hasPart;
    private List<String> hasEffect;
    private List<String> hasCause;
    private List<String> hasImmediateCause;

    public WikiDataParsedPage(String wikidataPageId, String pageTitle,
                              List<String> aliases, List<String> partOf, List<String> hasPart,
                              List<String> hasEffect, List<String> hasCause, List<String> hasImmediateCause) {
        this.pageTitle = pageTitle;
        this.aliases = aliases;
        this.partOf = partOf;
        this.hasPart = hasPart;
        this.hasEffect = hasEffect;
        this.hasCause = hasCause;
        this.hasImmediateCause = hasImmediateCause;
        this.wikidataPageId = wikidataPageId;

        if(!wikidataIdsToTitles.containsKey(wikidataPageId)) {
            wikidataIdsToTitles.put(wikidataPageId, pageTitle);
        }
    }

    public String getElasticPageId() {
        return elasticPageId;
    }

    public void setElasticPageId(String elasticPageId) {
        this.elasticPageId = elasticPageId;
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

    public List<String> getHasImmediateCause() {
        return hasImmediateCause;
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

    public void setHasImmediateCause(List<String> hasImmediateCause) {
        this.hasImmediateCause = hasImmediateCause;
    }

    public static void replaceIdsWithTitles(Map<String, WikiDataParsedPage> wikiDataParsedPages) {
        for (WikiDataParsedPage page : wikiDataParsedPages.values()) {
            page.setHasCause(convertToTitles(page.hasCause));
            page.setHasEffect(convertToTitles(page.hasEffect));
            page.setHasImmediateCause(convertToTitles(page.hasImmediateCause));
            page.setHasPart(convertToTitles(page.hasPart));
            page.setPartOf(convertToTitles(page.partOf));
        }
    }

    public static List<WikiDataParsedPage> updateElasticIds(Map<String, WikiDataParsedPage> wikiDataParsedPages,
                                                                   Map<String, String> elasticIds) {
        List<WikiDataParsedPage> updatedWikiDataPages = new ArrayList<>();
        for (WikiDataParsedPage page : wikiDataParsedPages.values()) {
            if(elasticIds.containsKey(page.getPageTitle())) {
                page.setElasticPageId(elasticIds.get(page.getPageTitle()));
                updatedWikiDataPages.add(page);
            }
        }

        return updatedWikiDataPages;
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
