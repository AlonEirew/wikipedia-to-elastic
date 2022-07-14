FROM docker.elastic.co/elasticsearch/elasticsearch:7.17.4

RUN /usr/share/elasticsearch/bin/elasticsearch-plugin install analysis-icu
RUN /usr/share/elasticsearch/bin/elasticsearch-plugin install analysis-smartcn

COPY elasticsearch.yml /usr/share/elasticsearch/config/
RUN chmod +r /usr/share/elasticsearch/config/elasticsearch.yml