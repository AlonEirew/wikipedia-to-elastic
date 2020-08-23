/**
 * @author  Alon Eirew
 */

package wiki.utils;

import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import wiki.data.relations.RelationType;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public class WikiToElasticConfiguration {

    public static final Type CONFIGURATION_TYPE = new TypeToken<WikiToElasticConfiguration>() {}.getType();

    private String indexName;
    private String docType;
    private String mapping;
    private String setting;
    private String host;
    private String scheme;
    private String wikiDump;
    private int port;
    private int shards;
    private int replicas;
    private int insertBulkSize;
    private boolean extractRelationFields;
    private String lang;
    private boolean includeRawText;
    private List<RelationType> relationTypes;

    private transient String mappingFileContent = null;
    private transient String settingFileContent = null;

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getMapping() {
        return mapping;
    }

    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getShards() {
        return shards;
    }

    public void setShards(int shards) {
        this.shards = shards;
    }

    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }

    public String getWikiDump() {
        return wikiDump;
    }

    public void setWikiDump(String wikiDump) {
        this.wikiDump = wikiDump;
    }

    public String getSetting() {
        return setting;
    }

    public void setSetting(String setting) {
        this.setting = setting;
    }

    public int getInsertBulkSize() {
        return insertBulkSize;
    }

    public void setInsertBulkSize(int insertBulkSize) {
        this.insertBulkSize = insertBulkSize;
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

    public String getMappingFileContent() throws IOException {
        if(this.mappingFileContent == null && this.mapping != null) {
            this.mappingFileContent = IOUtils.toString(Objects.requireNonNull(
                    WikiToElasticUtils.class.getClassLoader().getResourceAsStream(this.mapping)), StandardCharsets.UTF_8);
        }
        return this.mappingFileContent;
    }

    public String getSettingFileContent() throws IOException {
        if(this.settingFileContent == null && this.setting != null) {
            this.settingFileContent = IOUtils.toString(Objects.requireNonNull(
                            WikiToElasticUtils.class.getClassLoader().getResourceAsStream(this.setting)), StandardCharsets.UTF_8);
        }
        return this.settingFileContent;
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
}
