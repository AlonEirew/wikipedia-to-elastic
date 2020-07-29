#!/usr/bin/env bash

WORKING_DIR=tmp

if [ "$#" -ne 2 ];
  then
    echo "Illegal number of parameters, expected 2: elasticIndex, dockerImage"
    exit 1
fi

echo supplied parameters are: elasticIndex="$1", dockerImage="$2"

mkdir $WORKING_DIR

Dockerfile=false
ymlFile=false
for i in *
do
  if [ $i = "Dockerfile" ]
  then
    Dockerfile=true
    cp "$i" $WORKING_DIR
  fi
  if [ $i = "elasticsearch.yml" ]
  then
    ymlFile=true
    cp "$i" $WORKING_DIR
  fi
done

if [ "$Dockerfile" = false ] || [ "$ymlFile" = false ] ; then
    echo 'could not find mandatory files {Dockerfile, elasticsearch.yml} in local directory!'
    exit 1
fi

echo "#!/usr/bin/env bash" >> $WORKING_DIR/build.sh
echo "7z x /tmp/elastic-dump.7z -o/tmp" >> $WORKING_DIR/build.sh
echo "elasticdump --input=/tmp/elastic-dump/$1_settings.json --output=http://localhost:9200/$1 --type=settings" >> $WORKING_DIR/build.sh
echo "elasticdump --input=/tmp/elastic-dump/$1_mapping.json --output=http://localhost:9200/$1 --type=mapping" >> $WORKING_DIR/build.sh
echo "elasticdump --input=/tmp/elastic-dump/$1_data.json --output=http://localhost:9200/$1 --type=data" >> $WORKING_DIR/build.sh
echo "rm /tmp/elastic-dump.7z" >> $WORKING_DIR/build.sh
echo "rm -rf /tmp/elastic-dump" >> $WORKING_DIR/build.sh

echo "Extracting elasticdump for $1"

elasticdump --input=http://localhost:9200/"$1" --output="$1"_mapping.json --type=mapping
elasticdump --input=http://localhost:9200/"$1" --output="$1"_settings.json --type=settings
elasticdump --input=http://localhost:9200/"$1" --output="$1"_data.json --type=data

mkdir $WORKING_DIR/elastic-dump
mv "$1"_mapping.json "$1"_settings.json "$1"_data.json $WORKING_DIR/elastic-dump
(cd $WORKING_DIR && 7z a elastic-dump.7z elastic-dump)
rm -rf $WORKING_DIR/elastic-dump

(cd $WORKING_DIR && docker build -t "$2" .)

echo "Process complete!"

