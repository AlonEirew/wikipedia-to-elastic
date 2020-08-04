package wiki.utils;

import java.util.List;

public class LangConfiguration {
    private String disambiguation;
    private String category;
    private String redirect;
    private List<String> name;
    private List<String> titlesPref;
    private String coreNlpLang;

    public String getDisambiguation() {
        return disambiguation;
    }

    public void setDisambiguation(String disambiguation) {
        this.disambiguation = disambiguation;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public List<String> getName() {
        return name;
    }

    public void setName(List<String> name) {
        this.name = name;
    }

    public List<String> getTitlesPref() {
        return titlesPref;
    }

    public void setTitlesPref(List<String> titlesPref) {
        this.titlesPref = titlesPref;
    }

    public String getCoreNlpLang() {
        return coreNlpLang;
    }

    public void setCoreNlpLang(String coreNlpLang) {
        this.coreNlpLang = coreNlpLang;
    }
}
