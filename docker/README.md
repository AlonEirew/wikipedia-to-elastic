# ElasticSearch Docker Configuration
This is a step-by-step instructions on how to pull and run elasticsearch docker image with all needed plugins for the project

### Pull docker image
This will pull the docker elasticsearch 7.17.4 image with "icu" plugins
```
#>docker pull aeirew/elasticsearch
```

### Set disk image size and `vm.max_map_count`
1) Before starting this one-time process, make sure docker disk image size (*in your Docker Engine Preferences*) is not limited under 150GB.
   Once below one-time process done, you can decrease image size
2) If working with a Linux host, you might need to increase host vm.max_map_count,
   in order to verify you have the right value (i.e 262144), run:
```
#>sudo sysctl vm.max_map_count
You should see: vm.max_map_count = 262144
If you see instead: vm.max_map_count = 65530, 
Fix by running:
#>sudo sysctl -w vm.max_map_count=262144
```

### Run the docker image
```
#>docker run -p 127.0.0.1:9200:9200 -p 127.0.0.1:9300:9300 -e "discovery.type=single-node" aeirew/elasticsearch
```

### All Done :sunglasses:
