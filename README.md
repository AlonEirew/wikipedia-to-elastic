# Wikipedia XML dump to ElasticSearch

Project goal - Use 3 different types of Wikipedia pages (Redirect/Disambiguation/Title) in order to extract 5 different 
semantic features for the task of semantic relations between entities:<br/>
* Redirect Links - See details at <a href="https://en.wikipedia.org/wiki/Wikipedia:Redirect">Wikipedia Redirect</a>
* Disambiguation Links - See details at <a href="https://en.wikipedia.org/wiki/Category:Disambiguation_pages">Wikipedia Disambiguation</a>
* Category Links - See details at <a href="https://en.wikipedia.org/wiki/Help:Category">Wikipedia Category</a>
* Link Title Parenthesis - See details at paper <a href="http://u.cs.biu.ac.il/~dagan/publications/ACL09%20camera%20ready.pdf">"Extracting Lexical Reference Rules from Wikipedia"</a>
* 'Is A' (extracted from page first paragraph) - See details at paper <a href="http://u.cs.biu.ac.il/~dagan/publications/ACL09%20camera%20ready.pdf">"Extracting Lexical Reference Rules from Wikipedia"</a>

***

### Basic Info and Statistics
* Process will only export the following Wikipedia XML Dump fields attributes: *PageTitle, PageId, RedirectTitle and PageText*.<br/>
All meta data such as contributor/revision/comment/format/etc. are not exported into elastic.
* In case page has a redirect page, text field will not be exported (in order to remove redundancy) most accurate page text will be available in the redirect page. 
* Each 'relation' from above relations (Redirect, Disambiguation, Category, ...) will be extracted and saved in the Elastic index into two separated fields: one containing the raw text as extracted from xml wiki dump text field, 
and the other one will be saved after normalizing and lemmatizing the text (the second field is optional).
* The generated ElasticSearch index size will be 29GB and will contain 18,289,785 searchable entities and relations.
* Query time on created Wikipedia Elastic index will take roughly about 1-2 milliseconds

### Requisites
* Java 1.8
* ElasticSearch 6.2 (<a href=https://www.elastic.co/downloads/elasticsearch>Installation Guide</a>)
* Wikipedia xml.bz2 dump file (<a href=https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2>download enwiki latest dump xml</a>)

### Project Configuration Files
* `src/main/resources/conf.json` - basic process configuration
```
    "indexName" : "enwiki_v3" #Set your desired Elastic Search index name  
    "docType" : "wikipage" #Set your desired Elastic Search documnent type
    "normalizeFields" : "true" #Weather to run normalization of fields while processing the data
    "insertBulkSize": 100 #Number of pages to bulk insert to elastic search every iteration (found this number to give best preformence)
    "mapping" : "mapping.json" #Elastic Mapping file (should point to src/main/resources/mapping.json)
    "setting" : "es_map_settings.json" #Elastic Setting file (should point to src/main/resources/es_map_settings.json)
    "host" : "localhost" #Elastic host (were Elastic instance is installed and running)
    "port" : 9200 #Elastic port (host port were Elastic is installed and running, elastic defualt is 9200)
    "wikiDump" : "dumps/enwiki-latest-pages-articles.xml.bz2" #Wikipedia bz2 downloaded dump file location
    "scheme" : "http" #Elastic host schema (should probebly stay unchanged)
    "shards" : 1 #Number of Elastic shards to use
    "replicas" : 0 #Number of Elastic replicas to use
```
* `src/main/resources/mapping.json` - Elastic wiki index mapping (Should probably stay unchanged)
* `src/main/resources/es_map_settings.json` - Elastic index settings (Should probably stay unchanged)

### Getting with Docker
This is the preferred and fastest way to get the elastic index locally.<br/>
Before starting this one-time process, make sure docker disk image size is not limited under 150GB.
Once below one-time process done, you can decrease image size
 
1) Pull the image and run it (pulled image is 11GB)

    `#>docker pull aeirew/elastic-wiki`
    
    `#>docker run -d -p 9200:9200 -p 9300:9300 elastic-wiki`

2) The Elastic container index is still empty, all data is in a compressed file within the image,
in order to create and export the wiki data into the Elastic index (which takes a while) run the following command

    `#>docker exec <REPLACE_WITH_RUNNING_CONTAINER_ID> /tmp/build.sh`
    
3) Save to new image (you can delete the original elastic-wiki one)

    `#>docker commit <CONTAINER_ID> <IMAGE_NEW_NAME>`

4) That's it! from now on run your image using the above `run` command, replace elastic-wiki with <IMAGE_NEW_NAME>


### Building And Running From Source
**Disclimer** Processing Wiki latest full dump (15GB .bz2 AND 66GB unpacked as .xml) including the normalization and lemmatization of text, will take about **5 days** (tested on MacBook pro, using stanford parser to extract relations, normalize and lemmatize the data).<br/>
In case of using this data in order to identify semantic relations between phrases at run time, It is recommended to normalize the fields for better results, in case not needed or for a much faster data export into elastic **(5 hours)**, set normalize to false in `conf.json`, as shown in "Project Configuration Files".<br />

You might want to consider using the Docker Image to save that time

* Make sure Elastic process is running and active on your host (if running Elastic locally your IP is <a href="http://localhost:9200/">http://localhost:9200/</a>)
* Checkout/Clone the repository
* Put wiki xml.bz2 dump file (no need to extract the bz2 file!) in: `dumps` folder under root checkout repository.<br/> 
<i><b>Recommendation:</b> Start with a small wiki dump, make sure you like what you get (or modify configurations to meet your needs) before moving to a full blown 15GB dump export..</i>
* Make sure `conf.json` configuration for Elastic are set as expected (default localhost:9200)
* From command line navigate to project root directory and run:<br/>
`./gradlew clean build` <br/>
*Should get a message saying: `BUILD SUCCESSFUL in 7s`*
* Extract the build zip file created at this location `build/distributions/WikipediaToElastic-1.0.zip`
* Run the process from command line:<br/>
`java -Xmx6000m -DentityExpansionLimit=2147480000 -DtotalEntitySizeLimit=2147480000 -Djdk.xml.totalEntitySizeLimit=2147480000 -jar build/distributions/WikipediaToElastic-1.0/WikipediaToElastic-1.0.jar`

### Generated Elastic Page Example

Once process has finished, you should see in your ElasticSearch selected index, pages that have been created with the following structures (also see "Created Fields Attributes" for more details):   

**Page Example (Extracted from Wikipedia disambiguation page):**
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
**Page Example (Extracted from Wikipedia redirect page):**
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

### Created Fields Attributes 

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

