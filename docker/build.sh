#!/usr/bin/env bash

7z x /tmp/elastic-dump.7z -o/tmp
rm /tmp/elastic-dump.7z

curl -XPUT http://localhost:9200/enwiki_v2
curl -XPOST 'localhost:9200/enwiki_v2/_close'
curl -H "Content-Type: application/json" -d @tmp/elastic-dump/enwiki_settings.json -X PUT http://localhost:9200/enwiki_v2/_settings
curl -XPOST 'localhost:9200/enwiki_v2/_open'
elasticdump --input=/tmp/elastic-dump/enwiki_mapping.json --output=http://localhost:9200/enwiki_v2 --type=mapping
elasticdump --input=/tmp/elastic-dump/enwiki_data.json --output=http://localhost:9200/enwiki_v2 --type=data

rm -rf /tmp/elastic-dump
