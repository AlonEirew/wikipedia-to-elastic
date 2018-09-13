# Wikipedia XML dump to ElasticSearch

Project goal - Use 3 different types of Wikipedia pages (Redirect/Disambiguation/Title) in order to extract 5 different 
semantic features for the task of semantic relations between entities:<br/>
* Redirect Links
* Disambiguation Links 
* Category Links 
* Link Title Parenthesis
* 'Is A' (extracted from page first paragraph)

***

##Basic Info and Statistics
* Process will only export the following Wikipedia Dump fields: PageTitle, PageId, RedirectTitle and PageText.<br/>
All meta data such as contributor/revision/comment/format/etc. are not exported.
* In case page has a redirect page, text field will not be exported (in order to remove redundancy) most accurate page text will be available in the redirect page. 
* Each 'relation' features will be extracted and saved in Elastic in two fields: one raw, and one that is  normalized and lemmatized.
* Processing Wiki latest full dump (15GB .bz2 AND 66GB unpacked as .xml) will take about **5 days** (tested on MacBook pro, using stanford parser to extract relations, normalize and lemmatize the data)
* The generated ElasticSearch index size will be 29GB and will contain 18,289,785 searchable entities and relations.

##Requisites
* Java 1.8
* ElasticSearch 6.2 (<a href=https://www.elastic.co/downloads/elasticsearch>Installation Guide</a>)
* Wikipedia xml.bz2 dump file (<a href=https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2>download enwiki latest dump xml</a>)

##Project Configuration Files
* `.../src/main/resources/conf.json` - basic configuration
* `.../src/main/resources/mapping.json` - Elastic wiki index mapping
* `.../src/main/resources/es_map_settings.json` - Elastic index settings

##Building And Running From Source
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

##Generated Elastic Page Example
**Disambiguation Page Example:**
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
  }
}
 ```

| json field  | Value | comment |
| ------------- | ------------- | ------------- |
| _id | Text | Wikipedia page id |
| _source.title | Text | Wikipedia page title |
| _source.text | Text | Wikipedia page text |
| _source.redirectTitle | Text (optional) | Wikipedia page redirect title |
| _source.relations.beCompRelations | List (optional) | Be-Comp (or 'Is A') relation list |
| _source.relations.beCompRelationsNorm | List (optional) | Be-Comp (or 'Is A') relation list norm |
| _source.relations.categories | List (optional) | Categories relation list |
| _source.relations.categoriesNorm | List (optional) | Categories relation list norm |
| _source.relations.isDisambiguation | Bool (optional) | is Wikipedia disambiguation page |
| _source.relations.isPartName | List (optional) | is Wikipedia page name description |
| _source.relations.titleParenthesis | List (optional) | List of disambiguation secondary links  |
| _source.relations.titleParenthesisNorm | List (optional) | List of disambiguation secondary links norm |

