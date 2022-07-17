package wiki.config;

import org.apache.commons.io.IOUtils;
import wiki.utils.WikiToElasticUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ElasticConfiguration {
    private String indexName;
    private String docType;
    private String mapping;
    private String setting;
    private String host;
    private String scheme;
    private int port;
    private int shards;
    private int replicas;
    private int insertBulkSize;

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
}
