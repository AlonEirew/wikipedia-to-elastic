# Wikipedia XML dump to ElasticSearch

#### Export Wikipedia XML dump to ElasticSearch database

***

**Requisites:**
* Java 1.8
* ElasticSearch 6.2 (<a href=https://www.elastic.co/downloads/elasticsearch>Installation Guide</a>)
* Wikipedia xml.bz2 dump file (<a href=https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2>download enwiki latest dump xml</a>)

**Basic Info:**
* Process will only export the following fields: PageTitle, PageId, RedirectTitle and PageText.<br/>
All meta data such as contributor/revision/comment/format/etc. are not exported.
* In case page has a redirect page, text field will not be saved (in order to remove redundancy) most accurate page text will be available in redirect page. 
* Processing Wiki latest full dump (15GB .bz2 AND 66GB unpacked as .xml) will take about 6 Hours (tested on MacBook pro)
* The generated ElasticSearch index size will be 29GB.

**Configuration:**
* `.../src/main/resources/conf.json` - basic configuration
* `.../src/main/resources/mapping.json` - Elastic wiki index mapping

**Building And Running**:<br/>
* Checkout/Clone the repository
* Put wiki xml.bz2 dump file (no need to extract the bz2 file!) in: `/.../wikitoelasticjava/dumps/` folder.<br/> 
<i><b>Recommendation:</b> Start with a small wiki dump, make sure you like what you get (or modify configurations to meet your needs) before moving to a full blown 15GB dump export..</i>
* Make sure `conf.json` configuration for Elastic are set as expected (default localhost:9200)
* From command line navigate to project root directory and run:<br/>
`./gradlew clean build` <br/>
*Need to do only once or on any source/resource change*<br/>
*Should get a message saying: `BUILD SUCCESSFUL in 7s`*
* Extract build zip file build/distributions/WikipediaToElastic-1.0.zip
* Then run:<br/>
`java -DentityExpansionLimit=2147480000 -DtotalEntitySizeLimit=2147480000 -Djdk.xml.totalEntitySizeLimit=2147480000 -jar build/distributions/WikipediaToElastic-1.0/WikipediaToElastic-1.0.jar`

**Generated Elastic Page Example:**<br/>
**Full Main Page Example:**
```json
{
   "_index": "enwiki",
   "_type": "wikipage",
   "_id": "40573",
   "_version": 1,
   "_score": 17.92254,
   "_source": {
     "title": "NLP",
     "text": "{{wiktionary|NLP}}\n\n'''NLP''' may refer to:\n\n; Computer science\n*[[Natural language processing]], the field of computer science concerned with human speech as it is spoken.\n*[[Natural language programming]], an ontology-assisted way of programming in terms of natural language sentences.\n\n; Networking\n*[[Normal link pulses]], a signalling mechanism used in Ethernet\n\n; Political parties\n*[[National Labour Party (disambiguation)|National Labour Party]]\n*[[National Liberal Party (disambiguation)|National Liberal Party]]\n*[[New Labour (disambiguation)|New Labour Party]]\n*[[Natural Law Party]]\n\n; Libraries\n*[[National Library of the Philippines]]\n\n; Personal development\n*[[Neuro-linguistic programming]]\n\n{{disambiguation}}"
   },
   "highlight": {
     "title": [
       "@kibana-highlighted-field@NLP@/kibana-highlighted-field@"
     ]
   }
 }
 ```
**Redirect Page Example:**
```json
{
   "_index": "enwiki",
   "_type": "wikipage",
   "_id": "2577248",
   "_version": 1,
   "_score": 18.315203,
   "_source": {
     "title": "Nlp",
     "text": "#REDIRECT",
     "redirectTitle": "NLP"
   },
   "highlight": {
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

*In case of a bug, please contact/open issue/fix and PR :)*
