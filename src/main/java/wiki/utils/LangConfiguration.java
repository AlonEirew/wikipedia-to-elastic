package wiki.utils;

import java.util.List;

public class LangConfiguration {
    private String infoboxText;
    private List<String> disambiguation;
    private List<String> category;
    private String redirect;
    private List<String> partNames;
    private List<String> titlesPref;
    private List<String> beComp;
    private String coreNlpLang;

    public String getInfoboxText() {
        return infoboxText;
    }

    public void setInfoboxText(String infoboxText) {
        this.infoboxText = infoboxText;
    }

    public List<String> getDisambiguation() {
        return disambiguation;
    }

    public void setDisambiguation(List<String> disambiguation) {
        this.disambiguation = disambiguation;
    }

    public List<String> getCategory() {
        return category;
    }

    public void setCategory(List<String> category) {
        this.category = category;
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public List<String> getPartNames() {
        return partNames;
    }

    public void setPartNames(List<String> partNames) {
        this.partNames = partNames;
    }

    public List<String> getTitlesPref() {
        return titlesPref;
    }

    public void setTitlesPref(List<String> titlesPref) {
        this.titlesPref = titlesPref;
    }

    public List<String> getBeComp() {
        return beComp;
    }

    public void setBeComp(List<String> beComp) {
        this.beComp = beComp;
    }

    public String getCoreNlpLang() {
        return coreNlpLang;
    }

    public void setCoreNlpLang(String coreNlpLang) {
        this.coreNlpLang = coreNlpLang;
    }
}
