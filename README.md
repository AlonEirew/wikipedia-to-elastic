# Wikipedia XML dump to ElasticSearch

#### Export Wikipedia XML dump to ElasticSearch database

***

**Requisites:**
* Java 1.8
* ElasticSearch 6.2 (<a href=https://www.elastic.co/downloads/elasticsearch>Installation Guide</a>)
* Wikipedia xml.bz2 dump file (<a href=https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2>download enwiki latest dump xml</a>)

**Basic Info and Statistics:**
* Process will only export the following fields: PageTitle, PageId, RedirectTitle and PageText.<br/>
All meta data such as contributor/revision/comment/format/etc. are not exported.
* In case page has a redirect page, text field will not be exported (in order to remove redundancy) most accurate page text will be available in the redirect page. 
* Processing Wiki latest full dump (15GB .bz2 AND 66GB unpacked as .xml) will take about **5 days** (tested on MacBook pro, using stanford parser to extract relations from wikipage text)
* The generated ElasticSearch index size will be 29GB.
* Index count should be around 18,289,785 Wikipedia pages

**Project Configuration Files:**
* `.../src/main/resources/conf.json` - basic configuration
* `.../src/main/resources/mapping.json` - Elastic wiki index mapping
* `.../src/main/resources/es_map_settings.json` - Elastic index settings

**Building And Running From Source**:<br/>
* Make sure Elastic process is running and active (<a href="http://localhost:9200/">http://localhost:9200/</a>)
* Checkout/Clone the repository
* Put wiki xml.bz2 dump file (no need to extract the bz2 file!) in: `/.../wikiToElasticjava/dumps/` folder.<br/> 
<i><b>Recommendation:</b> Start with a small wiki dump, make sure you like what you get (or modify configurations to meet your needs) before moving to a full blown 15GB dump export..</i>
* Make sure `conf.json` configuration for Elastic are set as expected (default localhost:9200)
* From command line navigate to project root directory and run:<br/>
`./gradlew clean build` <br/>
*Should get a message saying: `BUILD SUCCESSFUL in 7s`*
* Extract build zip file build/distributions/WikipediaToElastic-1.0.zip
* Then run:<br/>
`java -Xmx6000m -DentityExpansionLimit=2147480000 -DtotalEntitySizeLimit=2147480000 -Djdk.xml.totalEntitySizeLimit=2147480000 -jar build/distributions/WikipediaToElastic-1.0/WikipediaToElastic-1.0.jar`

**Generated Elastic Page Example:**<br/>
**Full Main Page Example:**
```json
{
  "_index": "enwiki_v2",
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
      "titleParenthesis": [],
      "disambiguationLinksNorm": [
        "natural language processing",
        "natural law party",
        "national labour party",
        "natural language programming",
        "national library philippine",
        "neuro linguistic programming",
        "new labour party",
        "national liberal party",
        "new labour",
        "normal link pulse"
      ],
      "categoriesNorm": [
        "disambiguation"
      ],
      "titleParenthesisNorm": []
    }
  },
  "highlight": {
    "title.keyword": [
      "@kibana-highlighted-field@NLP@/kibana-highlighted-field@"
    ],
    "title.prefix_asciifolding": [
      "@kibana-highlighted-field@NLP@/kibana-highlighted-field@"
    ],
    "title.prefix": [
      "@kibana-highlighted-field@NLP@/kibana-highlighted-field@"
    ],
    "title.near_match_asciifolding": [
      "@kibana-highlighted-field@NLP@/kibana-highlighted-field@"
    ],
    "title.plain": [
      "@kibana-highlighted-field@NLP@/kibana-highlighted-field@"
    ],
    "title.near_match": [
      "@kibana-highlighted-field@NLP@/kibana-highlighted-field@"
    ],
    "title": [
      "@kibana-highlighted-field@NLP@/kibana-highlighted-field@"
    ]
  }
}
 ```
**Redirect Page Example:**
```json
{
  "_index": "enwiki_v2",
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
  },
  "highlight": {
    "title.prefix_asciifolding": [
      "@kibana-highlighted-field@Nlp@/kibana-highlighted-field@"
    ],
    "title.prefix": [
      "@kibana-highlighted-field@Nlp@/kibana-highlighted-field@"
    ],
    "title.near_match_asciifolding": [
      "@kibana-highlighted-field@Nlp@/kibana-highlighted-field@"
    ],
    "title.plain": [
      "@kibana-highlighted-field@Nlp@/kibana-highlighted-field@"
    ],
    "title.near_match": [
      "@kibana-highlighted-field@Nlp@/kibana-highlighted-field@"
    ],
    "title": [
      "@kibana-highlighted-field@Nlp@/kibana-highlighted-field@"
    ],
    "redirectTitle": [
      "@kibana-highlighted-field@NLP@/kibana-highlighted-field@"
    ]
  }
}
 ```

| json field  | Value | comment |
| ------------- | ------------- | ------------- |
| _id | Text | Wikipedia page id |
| _source.title | Text | Wikipedia page title |
| _source.text | Text | Wikipedia page text |
| _source.redirectTitle | Text (optional) | Wikipedia page redirect title |
| _source.relations.beCompRelations | List (optional) | Be Comp relation list |
| _source.relations.beCompRelationsNorm | List (optional) | Be Comp relation list norm |
| _source.relations.categories | List (optional) | Categories relation list |
| _source.relations.categoriesNorm | List (optional) | Categories relation list norm |
| _source.relations.isDisambiguation | Bool (optional) | is Wikipedia disambiguation page |
| _source.relations.isPartName | List (optional) | is Wikipedia page name description |
| _source.relations.titleParenthesis | List (optional) | List of disambiguation secondary links  |
| _source.relations.titleParenthesisNorm | List (optional) | List of disambiguation secondary links norm |

*In case of a bug, please contact/open issue/fix and PR :)*
