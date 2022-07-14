<p align="center">
    <a href="https://doi.org/10.5281/zenodo.3239509">
        <img src="https://zenodo.org/badge/DOI/10.5281/zenodo.3239509.svg" alt="DOI">
    </a>
</p>

# Wikipedia to ElasticSearch

This project generates a knowledge resource based on wikipedia. <br/>
It also includes a *multilingual* parsing mechanism that enables parsing of Wikipedia, Wikinews, Wikidata and other Wikimedia .bz2 dumps into an ElasticSearch index.

* Integrated with Intel <a href="https://github.com/NervanaSystems/nlp-architect">NLP Architect</a>
* Used in publication: <a href="https://www.aclweb.org/anthology/2021.naacl-main.198/">WEC: Wikipedia Event Coreference</a>

Supported languages: *{English, French, Spanish, German, Chinese}* <br/> 
*Note Relations integrity tested only for English. Other languages might require some adjustments. <br/>

## Table Of Contents

- [Introduction](https://github.com/AlonEirew/wikipedia-to-elastic#Introduction)
    - [Exploited Wiki Resources](https://github.com/AlonEirew/wikipedia-to-elastic#Exploited-Wiki-Resources)
    - [Extracted Relations Types](https://github.com/AlonEirew/wikipedia-to-elastic#Extracted-Relations-Types)
- [Prerequisites](https://github.com/AlonEirew/wikipedia-to-elastic#Prerequisites)
- [Configuration](https://github.com/AlonEirew/wikipedia-to-elastic#Configuration)
- [Build Run and Test](https://github.com/AlonEirew/wikipedia-to-elastic#Build-Run-and-Test)
- [Integrating Wikidata Attributes](https://github.com/AlonEirew/wikipedia-to-elastic#Integrating-Wikidata-Attributes)
- [Usage](https://github.com/AlonEirew/wikipedia-to-elastic#Usage)

***

## Introduction

### Exploited Wiki Resources
3 different types of Wikipedia pages are used: {Redirect/Disambiguation/Title} in order to extract 6 different 
semantic features for tasks such as Identifying Semantic Relations, Entity Linking, Cross Document Co-Reference, Knowledge Graphs, Summarization and other.

* Redirect Links - See details at <a href="https://en.wikipedia.org/wiki/Wikipedia:Redirect">Wikipedia Redirect</a>
* Disambiguation Links - See details at <a href="https://en.wikipedia.org/wiki/Category:Disambiguation_pages">Wikipedia Disambiguation</a>
* Category Links - See details at <a href="https://en.wikipedia.org/wiki/Help:Category">Wikipedia Category</a>
* Link Title Parenthesis - See details at paper <a href="http://u.cs.biu.ac.il/~dagan/publications/ACL09%20camera%20ready.pdf">"Extracting Lexical Reference Rules from Wikipedia"</a>
* Infobox - See details at <a href="https://en.wikipedia.org/wiki/Help:Infobox">Wikipedia Infobox</a>
* Term Frequency (TBD/WIP) - Hold a map of term frequency for computing TFIDF on Wikipadia

### Extracted Relations Types
List of Wikidata properties which can extend above Wikipedia relations (by running Wikidata postprocess described below).

Links for further details on those properties:
* <a href="https://https://www.wikidata.org/wiki/Help:Aliases">Aliases</a>
* <a href="https://www.wikidata.org/wiki/Property:P828">Has Cause</a>
* <a href="https://www.wikidata.org/wiki/Property:P1542">Has Effect</a>
* <a href="https://www.wikidata.org/wiki/Property:P527">Has Part</a>
* <a href="https://www.wikidata.org/wiki/Property:P361">Part Of</a>
* <a href="https://www.wikidata.org/wiki/Property:P1536">Immediate Cause Of</a>
* <a href="https://www.wikidata.org/wiki/Property:P1478">Has Immediate Cause</a>


***

## Prerequisites
* Java 11
* ElasticSearch 7.17.4 
    * Recommended: Set Elastic using docker (<a href=https://github.com/AlonEirew/wikipedia-to-elastic/blob/master/docker/README.md>docker/README.md</a>)
    * Alternative:
        * Install Elastic from the official <a href=https://www.elastic.co/downloads/past-releases/#elasticsearch>Elasticsearch site</a>
        * Install plugins: analysis-icu, analysis-smartcn (<a href=https://www.elastic.co/guide/en/elasticsearch/plugins/7.17/analysis-icu.html>guide</a>)
* Wikipedia xml.bz2 dump file in required language (For example <a href=https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2>latest en XML dump</a>)
* Optional: Wikidata json.bz2 dump file (<a href=https://dumps.wikimedia.org/wikidatawiki/entities/latest-all.json.bz2>latest JSON dump</a>)

***

## Configuration
* `conf.json` - Main project configuration

```json
    "indexName": "enwiki_v3" (Set your desired Elastic Search index name)  
    "docType": "wikipage" (Set your desired Elastic Search documnent type)
    "extractRelationFields": true (Weather to extract relations fields while processing the data, support only english wikipedia)
    "insertBulkSize": 100 (Number of pages to bulk insert to elastic search every iteration (found this number to give best preformence))
    "mapping": "mapping.json" (Elastic Mapping file, should point to src/main/resources/mapping.json)
    "setting": "en_map_settings.json" (Elastic Setting file, current support {en, fr, es, de, zh})
    "host": "localhost" (Elastic host, were Elastic instance is installed and running)
    "port": 9200 (Elastic port, host port were Elastic is installed and running, elastic defualt is set to 9200)
    "wikipediaDump": "dumps/enwiki-latest-pages-articles.xml.bz2" (Wikipedia .bz2 downloaded dump file location)
    "scheme": "http" (Elastic host schema, should probebly stay unchanged)
    "shards": 1 (Number of Elastic shards to use)
    "replicas": 0 (Number of Elastic replicas to use)
    "lang": "en" (current support {en, fr, es, de, zh})
    "includeRawText": true (will include wikipedia page text, parsed and clean as possible)
    "relationTypes": ["Category", "Infobox", "Parenthesis", "PartName"] (Which relations to extract, full list at /src/main/java/wiki/data/relations/RelationType.java)
```
* `src/main/resources/mapping.json` - Elastic wiki index mapping (Should probably stay unchanged)
* `src/main/resources/{en,es,fr,de,zh}_map_settings.json` - Elastic index settings (Should probably stay unchanged)
* `src/main/resources/lang/{en,es,fr,de,zh}.json` - language specific configuration files
* `src/main/resources/stop_words/{en,es,fr,de,zh}.txt` - language specific stop-words list

***

## Build Run and Test

- Make sure Elastic process is running and active on your host (if running Elastic locally your IP is <a href="http://localhost:9200/">http://localhost:9200/</a>)
- Checkout/Clone the repository
- Put wiki xml.bz2 dump file (no need to extract the bz2 file!) in: `dumps` folder under root checkout repository.<br/> 
<i><b>Recommendation:</b> Start with a small wiki dump, make sure you like what you get (or modify configurations to meet your needs) before moving to a full blown 15GB dump export..</i>
- Make sure `conf.json` configuration for Elastic are set as expected (default localhost:9200)
- From command line navigate to project root directory and run:<br/>
`./gradlew clean build -x test` <br/>
*Should get a message saying: `BUILD SUCCESSFUL in 7s`*
- Extract the build zip file created at this location `build/distributions/WikipediaToElastic-1.0.zip`
- Run the process from command line:<br/>
`java -Xmx6000m -DentityExpansionLimit=2147480000 -DtotalEntitySizeLimit=2147480000 -Djdk.xml.totalEntitySizeLimit=2147480000 -jar build/distributions/WikipediaToElastic-1.0/WikipediaToElastic-1.0.jar`

- To test/query, you can run from terminal:<br/>
`curl -XGET 'http://localhost:9200/enwiki_v3/_search?pretty=true' -H 'Content-Type: application/json' -d '{"size": 5, "query": {"match_phrase": { "title.near_match": "Alan Turing"}}}'`
- Should return a wikipedia page on Alan Turing

***

## Integrating Wikidata Attributes
Running this process require a Wikipedia index (generated by the above process) 

### Wikidata Configuration Files
* `wikidata_conf.json` - basic process configuration
```json
    "indexName" : "enwiki_v3" (Set your Elastic Search index to be modidied)  
    "docType" : "wikipage" (Set your desired Elastic Search documnent type)
    "insertBulkSize": 100 (Number of pages to bulk insert to elastic search every iteration (found this number to give best preformence))
    "host" : "localhost" (Elastic host, were Elastic instance is installed and running)
    "port" : 9200 (Elastic port, host port were Elastic is installed and running, elastic defualt is set to 9200)
    "wikidataDump" : "dumps/enwiki-latest-pages-articles.xml.bz2" (Wikidata .bz2 downloaded dump file location)
    "scheme" : "http" (Elastic host schema, should probebly stay unchanged)
    "lang": "en" (should corrolate with the wikipedia index)
```

### Wikidata Running and Testing
- Make sure Elastic process is running and active on your host (if running Elastic locally your IP is <a href="http://localhost:9200/">http://localhost:9200/</a>)
- Make sure `wikidata_conf.json` configuration are set as expected
- Run the process from command line:<br/>
`java -cp WikipediaToElastic-1.0.jar wiki.wikidata.WikiDataFeatToFile` <br/>
Process will read the full wikidata dump, parse, extract the relations and merge them relative wikipedia data in search index. Process might take a while to finish.

- To test/query, you can run from terminal:<br/>
`curl -XGET 'http://localhost:9200/enwiki_v3/_search?pretty=true' -H 'Content-Type: application/json' -d '{"size": 5, "query": {"match_phrase": { "title.near_match": "Alan Turing"}}}'`

This should return a wikipedia page on Alan Turing including the new Wikidata relations.

***

## Usage

### Elastic Page Query

Once process is complete, two main query options are available (for more details and title query options, see `mapping.json`):<br/>
* title.plain - fuzzy search (sorted)
* title.keyword - exact match

### Generated Elastic Page Example

Pages that have been created with the following structures (also see "Created Fields Attributes" for more details):   

**Page Example (Extracted from Wikipedia disambiguation page):**
```json
{
  "_index": "enwiki_v3",
  "_type": "wikipage",
  "_id": "40573",
  "_version": 1,
  "_score": 20.925367,
  "_source": {
    "title": "NLP",
    "text": "{{wiktionary|NLP}}\n\n'''NLP''' may refer to:\n\n; .....",
    "relations": {
      "isPartName": false,
      "isDisambiguation": true,
      "disambiguationLinks": [
        "Natural language programming",
        "New Labour",
        "National Library of the Philippines",
        "Neuro linguistic programming",
        "Natural language processing",
        "National Liberal Party",
        "Natural Law Party",
        "National Labour Party",
        "Normal link pulses",
        "New Labour Party"
      ],
      "categories": [
        "disambiguation"
      ],
      "infobox": "",
      "titleParenthesis": [],
      "partOf": [],
      "aliases": [
        "LmxM36.1060"
      ],
      "hasPart": [],
      "hasEffect": [],
      "hasCause": [],
      "hasImmediateCause": []
    }
  }
}
 ```
**Page Example (Extracted from Wikipedia redirect page):**
```json
{
  "_index": "enwiki_v3",
  "_type": "wikipage",
  "_id": "2577248",
  "_version": 1,
  "_score": 20.925367,
  "_source": {
    "title": "Nlp",
    "text": "#REDIRECT",
    "redirectTitle": "NLP",
    "relations": {
      "isPartName": false,
      "isDisambiguation": false
    }
  }
}
 ```

### Fields & Attributes 

| json field  | Value | comment |
| ------------- | ------------- | ------------- |
| _id | Text | Wikipedia page id |
| _source.title | Text | Wikipedia page title |
| _source.text | Text | Wikipedia page text |
| _source.redirectTitle | Text (optional) | Wikipedia page redirect title |
| _source.relations.infobox | Text (optional) | The article infobox element |
| _source.relations.categories | List (optional) | Categories relation list |
| _source.relations.isDisambiguation | Bool (optional) | is Wikipedia disambiguation page |
| _source.relations.isPartName | List (optional) | is Wikipedia page name description |
| _source.relations.titleParenthesis | List (optional) | List of disambiguation secondary links  |
| _source.relations.aliases | List (optional) | Wikidata Rel |
| _source.relations.partOf | List (optional) | Wikidata Rel |
| _source.relations.hasPart | List (optional) | Wikidata Rel |
| _source.relations.hasEffect | List (optional) | Wikidata Rel |
| _source.relations.hasCause | List (optional) | Wikidata Rel |
| _source.relations.hasImmediateCause | List (optional) | Wikidata Rel |
