/**
 * @author  Alon Eirew
 */

package wiki.config;

import wiki.data.relations.RelationType;

import java.util.List;

public class MainConfiguration {

    private String exportMethod;
    private String wikipediaDump;
    private String wikidataDump;
    private String wikidataJsonOutput;
    private boolean extractRelationFields;
    private String lang;
    private boolean includeRawText;
    private List<RelationType> relationTypes;

    public String getExportMethod() {
        return exportMethod;
    }

    public void setExportMethod(String exportMethod) {
        this.exportMethod = exportMethod;
    }

    public String getWikipediaDump() {
        return wikipediaDump;
    }

    public void setWikipediaDump(String wikipediaDump) {
        this.wikipediaDump = wikipediaDump;
    }

    public boolean isExtractRelationFields() {
        return extractRelationFields;
    }

    public void setExtractRelationFields(boolean extractRelationFields) {
        this.extractRelationFields = extractRelationFields;
    }

    public List<RelationType> getRelationTypes() {
        return relationTypes;
    }

    public void setRelationTypes(List<RelationType> relationTypes) {
        this.relationTypes = relationTypes;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public boolean isIncludeRawText() {
        return includeRawText;
    }

    public void setIncludeRawText(boolean includeRawText) {
        this.includeRawText = includeRawText;
    }

    public String getWikidataDump() {
        return wikidataDump;
    }

    public void setWikidataDump(String wikidataDump) {
        this.wikidataDump = wikidataDump;
    }

    public String getWikidataJsonOutput() {
        return wikidataJsonOutput;
    }

    public void setWikidataJsonOutput(String wikidataJsonOutput) {
        this.wikidataJsonOutput = wikidataJsonOutput;
    }
}
