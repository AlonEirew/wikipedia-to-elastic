package wiki.data;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class WikiDataParsedPage {
    private transient String wikidataPageId;
    private transient String wikipediaLangPageTitle;
    private transient String elasticPageId;
    private Set<String> aliases;
    private Set<String> partOf;
    private Set<String> hasPart;
    private Set<String> hasEffect;
    private Set<String> hasCause;
    private Set<String> hasImmediateCause;
    private Set<String> immediateCauseOf;

    public void setWikidataPageId(String wikidataPageId) {
        this.wikidataPageId = wikidataPageId;
    }

    public void setWikipediaLangPageTitle(String wikipediaLangPageTitle) {
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

    public void setAliases(Set<String> aliases) {
        this.aliases = aliases;
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

    private boolean isEmpty() {
        return (this.aliases == null || this.aliases.isEmpty()) && (this.partOf == null || this.partOf.isEmpty()) &&
                (this.hasPart == null || this.hasPart.isEmpty()) && (this.hasEffect == null || this.hasEffect.isEmpty()) &&
                (this.hasCause == null || this.hasCause.isEmpty()) &&
                (this.immediateCauseOf == null || this.immediateCauseOf.isEmpty()) &&
                (this.hasImmediateCause == null || this.hasImmediateCause.isEmpty());
    }

    public boolean isValid() {
        boolean retVal = false;
        if(!isEmpty()) {
            String title = this.wikipediaLangPageTitle;
            retVal = title != null && !(title.startsWith("Wikipedia:") || title.startsWith("Template:") || title.startsWith("Category:") ||
                    title.startsWith("Help:") || StringUtils.isNumericSpace(title) || title.length() == 1);
        }

        return retVal;
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
                                                                  Set<String> wikiDataIdsList) {
        Set<String> replacedTitles = new HashSet<>();
        if(wikiDataIdsList != null) {
            for(String id : wikiDataIdsList) {
                if (wikiDataParsedPages.containsKey(id)) {
                    String wikipediaLangPageTitle = wikiDataParsedPages.get(id).getWikipediaLangPageTitle();
                    if(wikipediaTitleToId.containsKey(wikipediaLangPageTitle)) {
                        String redirectTitle = wikipediaTitleToId.get(wikipediaLangPageTitle).getRedirectTitle();
                        if (redirectTitle != null && !redirectTitle.isEmpty()) {
                            replacedTitles.add(redirectTitle);
                        } else {
                            replacedTitles.add(wikipediaLangPageTitle);
                        }
                    }
                }
            }
        }

        return replacedTitles;
    }
}
