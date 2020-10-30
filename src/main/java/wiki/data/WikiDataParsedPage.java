package wiki.data;

import java.util.*;

public class WikiDataParsedPage {
    private final transient String wikidataPageId;
    private final transient String wikipediaLangPageTitle;
    private transient String elasticPageId;
    private final Set<String> aliases = new HashSet<>();
    private Set<String> partOf = new HashSet<>();
    private Set<String> hasPart = new HashSet<>();
    private Set<String> hasEffect = new HashSet<>();
    private Set<String> hasCause = new HashSet<>();
    private Set<String> hasImmediateCause = new HashSet<>();
    private Set<String> immediateCauseOf = new HashSet<>();

    public WikiDataParsedPage(String wikidataPageId, String wikipediaLangPageTitle) {
        this.wikidataPageId = wikidataPageId;
        this.wikipediaLangPageTitle = wikipediaLangPageTitle;
    }

    public String getWikidataPageId() {
        return wikidataPageId;
    }

    public String getElasticPageId() {
        return elasticPageId;
    }

    public void setElasticPageId(String elasticPageId) {
        this.elasticPageId = elasticPageId;
    }

    public String getWikipediaLangPageTitle() {
        return wikipediaLangPageTitle;
    }

    public Set<String> getAliases() {
        return aliases;
    }

    public Set<String> getPartOf() {
        return partOf;
    }

    public Set<String> getHasPart() {
        return hasPart;
    }

    public Set<String> getHasEffect() {
        return hasEffect;
    }

    public Set<String> getHasCause() {
        return hasCause;
    }

    public Set<String> getHasImmediateCause() {
        return hasImmediateCause;
    }

    public void setPartOf(Set<String> partOf) {
        this.partOf = partOf;
    }

    public void setHasPart(Set<String> hasPart) {
        this.hasPart = hasPart;
    }

    public void setHasEffect(Set<String> hasEffect) {
        this.hasEffect = hasEffect;
    }

    public void setHasCause(Set<String> hasCause) {
        this.hasCause = hasCause;
    }

    public void setHasImmediateCause(Set<String> hasImmediateCause) {
        this.hasImmediateCause = hasImmediateCause;
    }

    public Set<String> getImmediateCauseOf() {
        return immediateCauseOf;
    }

    public void setImmediateCauseOf(Set<String> immediateCauseOf) {
        this.immediateCauseOf = immediateCauseOf;
    }

    public static List<WikiDataParsedPage> prepareWikipediaWikidataMergeList(Map<String, WikiDataParsedPage> wikiDataParsedPages,
                                                                             Map<String, WikipediaParsedPage> wikipediaTitleToId) {
        replaceRelIdsWithTitles(wikiDataParsedPages, wikipediaTitleToId);
        return updateElasticIds(wikiDataParsedPages, wikipediaTitleToId);
    }

    private static void replaceRelIdsWithTitles(Map<String, WikiDataParsedPage> wikiDataParsedPages,
                                            Map<String, WikipediaParsedPage> wikipediaTitleToId) {
        for (WikiDataParsedPage page : wikiDataParsedPages.values()) {
            page.setHasCause(convertWikidataIdToWikipediaTitles(wikiDataParsedPages, wikipediaTitleToId, page.hasCause));
            page.setHasEffect(convertWikidataIdToWikipediaTitles(wikiDataParsedPages, wikipediaTitleToId, page.hasEffect));
            page.setHasImmediateCause(convertWikidataIdToWikipediaTitles(wikiDataParsedPages, wikipediaTitleToId, page.hasImmediateCause));
            page.setImmediateCauseOf(convertWikidataIdToWikipediaTitles(wikiDataParsedPages, wikipediaTitleToId, page.immediateCauseOf));
            page.setHasPart(convertWikidataIdToWikipediaTitles(wikiDataParsedPages, wikipediaTitleToId, page.hasPart));
            page.setPartOf(convertWikidataIdToWikipediaTitles(wikiDataParsedPages, wikipediaTitleToId, page.partOf));
        }
    }

    private static List<WikiDataParsedPage> updateElasticIds(Map<String, WikiDataParsedPage> wikiDataParsedPages,
                                                                   Map<String, WikipediaParsedPage> elasticTitleToIds) {
        List<WikiDataParsedPage> updatedWikiDataPages = new ArrayList<>();
        for (WikiDataParsedPage page : wikiDataParsedPages.values()) {
            if(elasticTitleToIds.containsKey(page.getWikipediaLangPageTitle())) {
                WikipediaParsedPage wikipediaParsedPage = elasticTitleToIds.get(page.getWikipediaLangPageTitle());
                if(wikipediaParsedPage.getRedirectTitle() != null && !wikipediaParsedPage.getRedirectTitle().isEmpty()) {
                    if(elasticTitleToIds.containsKey(wikipediaParsedPage.getRedirectTitle())) {
                        WikipediaParsedPage redirectPage = elasticTitleToIds.get(wikipediaParsedPage.getRedirectTitle());
                        page.setElasticPageId(String.valueOf(redirectPage.getId()));
                        updatedWikiDataPages.add(page);
                    }
                } else {
                    page.setElasticPageId(String.valueOf(wikipediaParsedPage.getId()));
                    updatedWikiDataPages.add(page);
                }
            }
        }

        return updatedWikiDataPages;
    }

    private static Set<String> convertWikidataIdToWikipediaTitles(Map<String, WikiDataParsedPage> wikiDataParsedPages,
                                                                  Map<String, WikipediaParsedPage> wikipediaTitleToId,
                                                                  Set<String> idsList) {
        Set<String> replacedTitels = new HashSet<>();
        if(idsList != null) {
            for(String id : idsList) {
                if (wikiDataParsedPages.containsKey(id)) {
                    String wikipediaLangPageTitle = wikiDataParsedPages.get(id).getWikipediaLangPageTitle();
                    String redirectTitle = wikipediaTitleToId.get(wikipediaLangPageTitle).getRedirectTitle();
                    if(redirectTitle != null && !redirectTitle.isEmpty()) {
                        replacedTitels.add(redirectTitle);
                    } else {
                        replacedTitels.add(wikipediaLangPageTitle);
                    }
                }
            }
        }

        return replacedTitels;
    }
}
