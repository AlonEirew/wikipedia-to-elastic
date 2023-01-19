<p align="center">
    <a href="https://doi.org/10.5281/zenodo.3239509">
        <img src="https://zenodo.org/badge/DOI/10.5281/zenodo.3239509.svg" alt="DOI">
    </a>
</p>

# Wikipedia to ElasticSearch

This project generates an ElasticSearch, or file index from Wikipedia (xml dumps). The process will analyze, extract and store Wikipedia article text and several distinct Wikipedia attributes and relations (detailed below).<br/>

## Project Features:
* Export Wikipedia in different languages *{English, French, Spanish, German, Chinese}*
* Export other Wikimedia resources: *{Wikipedia, Wikinews, Wikidata}*
* Support storing to either an Elastic index or file system (json files)
* Support the extraction of Wikipedia article text clean of markdown and html tags
* Integrated with Intel <a href="https://github.com/NervanaSystems/nlp-architect">NLP Architect</a>
* Used in scientific research: 
    * <a href="https://www.aclweb.org/anthology/2021.naacl-main.198/">WEC: Wikipedia Event Coreference</a>
    * <a href="https://arxiv.org/abs/2210.12654">Cross-document Event Coreference Search: Task, Dataset and Modeling</a>

*Relations integrity tested only for English. Other languages might require some adjustments. <br/>

## Table Of Contents

- [Introduction](https://github.com/AlonEirew/wikipedia-to-elastic#Introduction)
    - [Special Wikipedia Resources and Attributes](https://github.com/AlonEirew/wikipedia-to-elastic#Exploited-Wiki-Resources)
    - [Supported Relations Types](https://github.com/AlonEirew/wikipedia-to-elastic#Extracted-Relations-Types)
- [Prerequisites](https://github.com/AlonEirew/wikipedia-to-elastic#Prerequisites)
- [Configuration](https://github.com/AlonEirew/wikipedia-to-elastic#Configuration)
  - [Main Configuration File](https://github.com/AlonEirew/wikipedia-to-elastic#Main-Configuration-File)
  - [Json Export Configuration File](https://github.com/AlonEirew/wikipedia-to-elastic#Json-Export-Configuration-File)
  - [Elastic Configuration Files](https://github.com/AlonEirew/wikipedia-to-elastic#Elastic-Configuration-Files)
- [Build Run and Test](https://github.com/AlonEirew/wikipedia-to-elastic#Build-Run-and-Test)
- [Integrating Wikidata Attributes](https://github.com/AlonEirew/wikipedia-to-elastic#Integrating-Wikidata-Attributes)
- [Usage](https://github.com/AlonEirew/wikipedia-to-elastic#Usage)

***

## Introduction

### Special Wikipedia Resources and Attributes
3 different types of Wikipedia pages are used: {Redirect/Disambiguation/Title} in order to extract 6 different 
semantic features for tasks such as Identifying Semantic Relations, Entity Linking, Cross Document Co-Reference, Knowledge Graphs, Summarization and other.

* Redirect Links - See details at <a href="https://en.wikipedia.org/wiki/Wikipedia:Redirect">Wikipedia Redirect</a>
* Disambiguation Links - See details at <a href="https://en.wikipedia.org/wiki/Category:Disambiguation_pages">Wikipedia Disambiguation</a>
* Category Links - See details at <a href="https://en.wikipedia.org/wiki/Help:Category">Wikipedia Category</a>
* Link Title Parenthesis - See details at paper <a href="http://u.cs.biu.ac.il/~dagan/publications/ACL09%20camera%20ready.pdf">"Extracting Lexical Reference Rules from Wikipedia"</a>
* Infobox - See details at <a href="https://en.wikipedia.org/wiki/Help:Infobox">Wikipedia Infobox</a>
* Term Frequency (TBD/WIP) - Hold a map of term frequency for computing TFIDF on Wikipadia

### Supported Relations Types
Listed below the Wikidata properties which can extend above attributes by running the Wikidata postprocess described below.

Click relation for further details:
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
* Wikipedia xml.bz2 dump file in required language (For example <a href=https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2>latest en XML dump</a>)
* Optional: ElasticSearch 7.17.4 (needed when exporting to an elastic index)
    * Recommended: Set Elastic using docker (<a href=https://github.com/AlonEirew/wikipedia-to-elastic/blob/master/docker/README.md>docker/README.md</a>)
    * Alternative:
        * Install Elastic from the official <a href=https://www.elastic.co/downloads/past-releases/#elasticsearch>Elasticsearch site</a>
        * Install plugins: analysis-icu, analysis-smartcn (<a href=https://www.elastic.co/guide/en/elasticsearch/plugins/7.17/analysis-icu.html>guide</a>)
* Optional: Wikidata json.bz2 dump file (<a href=https://dumps.wikimedia.org/wikidatawiki/entities/latest-all.json.bz2>latest JSON dump</a>)

***

## Configuration
### Main Configuration File 
`conf.json` is the main process configuration file: 
* `exportMethod` - Whether to export to Elastic Index (set to `elastic`) or json files (then set to `json_files`)
* `extractRelationFields` - When set to `true` will extract the relations fields (listed in `relationTypes`) while processing the data (support only with english Wikipedia)
* `wikipediaDump` - Wikipedia .bz2 downloaded dump file location
* `lang` - Support {`en` (English), `fr` (French), `es` (Spanish), `de` (German), `zh` (Chinese)}
* `includeRawText` - When set to `true`, will include original wikipedia page text (including html and markdown), parsed and clean as possible
* `includeParsedParagraphs` - When set to `true`, will include a list of parsed wikipedia article paragraphs, clean of any markdown or html tags
* `relationTypes` - ["Category", "Infobox", "Parenthesis", "PartName"]. To export those relations, the `extractRelationFields` configuration need to be set to `true` (the full list of available relations is in `/src/main/java/wiki/data/relations/RelationType.java`)

### Json Export Configuration File
`config/json_file_conf.json` is the configuration needed only when the `exportMethod` is set to `json_files`
* `outIndexDirectory` - The folder location where to save the exported files
* `pagesPerFile` - How many pages to save per file (100,000 pages ~ 0.5 GB)

### Elastic Configuration Files
#### Main Elastic Configuration File
`config/elastic_conf.json` - Those configurations are needed only when the `exportMethod` is set to `elastic` 
* `indexName` - Set your desired Elastic Search index name
* `docType` - Set your desired Elastic Search documnent type
* `insertBulkSize` - Number of pages to bulk insert to elastic search every iteration (found `1000` to give best preformence)
* `mapping` - Elastic Mapping file, should point to src/main/resources/mapping.json
* `setting` - Elastic Setting file, current support {en, fr, es, de, zh}
* `host` - Elastic host
* `port` - Elastic port
* `scheme` - Elastic host schema (default: `http`)
* `shards` - Number of Elastic shards
* `replicas` - Number of Elastic replicas

#### Elastic Mapping File
`src/main/resources/mapping.json` - Elastic wiki index mapping (Should probably stay unchanged)

#### Elastic Index Files
* `src/main/resources/{en,es,fr,de,zh}_map_settings.json` - Elastic index settings (Should probably stay unchanged)
* `src/main/resources/lang/{en,es,fr,de,zh}.json` - language specific configuration for relation key word translations
* `src/main/resources/stop_words/{en,es,fr,de,zh}.txt` - language specific stop-words list

***

## Build Run and Test

- Make sure Elastic process is running and active on your host (if running Elastic locally your IP is <a href="http://localhost:9200/">http://localhost:9200/</a>)
- Checkout/Clone the repository
- From command line navigate to project root directory and run:<br/>
`./gradlew clean build -x test` <br/>
*Should get a message saying: `BUILD SUCCESSFUL in 7s`*
- Extract the build zip file created at this location `build/distributions/WikipediaToElastic-1.0.zip`
- Put wiki xml.bz2 dump file (no need to extract the bz2 file!) in: `dumps` folder<br/> 
<i><b>Recommendation:</b> Start with a small wiki dump, make sure you like what you get (or modify configurations to meet your needs) before moving to a full blown 15GB dump export.</i>
- Make sure `conf.json` configurations are set as expected
- Make sure `config` folder configurations are set as expected
- Run the process from command line:<br/>
`java -Xmx6000m -DentityExpansionLimit=2147480000 -DtotalEntitySizeLimit=2147480000 -Djdk.xml.totalEntitySizeLimit=2147480000 -jar build/distributions/WikipediaToElastic-1.0/WikipediaToElastic-1.0.jar`

- To test/query, you can run from terminal:<br/>
`curl -XGET 'http://localhost:9200/enwiki_v3/_search?pretty=true' -H 'Content-Type: application/json' -d '{"size": 5, "query": {"match_phrase": { "title.near_match": "Alan Turing"}}}'`
- Should return a wikipedia page on Alan Turing

***

## Integrating Wikidata Attributes
Running this process require a Wikipedia index (generated by the above process) 

### Wikidata Main Configuration File (`config/wikidata_conf.json`)
Main configuration file for Wikidata export process, currently only support if Wikipedia was export to an Elasticsearch index.

* indexName - Elasticsearch index to enhance with wikidata attributes  
* docType - Set your desired documnent type
* insertBulkSize - Number of pages to bulk insert to elastic search every iteration
* host - Elastic host
* port - Elastic port
* wikidataDump - Wikidata .bz2 downloaded dump file location
* scheme - Elastic host schema
* lang - should correlate with the wikipedia index language

## Wikidata Running and Testing
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
| _source.text | Text (optional) | Wikipedia page text |
| _source.parsedParagraphs | List (optional) | Clean of html/markdown Wikipedia article text split to passages |
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
