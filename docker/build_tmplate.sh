#!/usr/bin/env bash

7z x /tmp/elastic-dump.7z -o/tmp

elasticdump --input=/tmp/elastic-dump/enwiki_analyzer.json --output=http://localhost:9200/enwiki_v3 --type=analyzer
elasticdump --input=/tmp/elastic-dump/enwiki_mapping.json --output=http://localhost:9200/enwiki_v3 --type=mapping
elasticdump --input=/tmp/elastic-dump/enwiki_data.json --output=http://localhost:9200/enwiki_v3 --type=data

rm /tmp/elastic-dump.7z
rm -rf /tmp/elastic-dump
