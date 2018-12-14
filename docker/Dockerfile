FROM ubuntu:latest
RUN apt-get update && apt-get install -y \
    wget \
    gnupg \
    curl \
    p7zip-full \
    default-jre

RUN wget -qO - https://artifacts.elastic.co/GPG-KEY-elasticsearch | apt-key add -
RUN apt-get install -y apt-transport-https
RUN echo "deb https://artifacts.elastic.co/packages/6.x/apt stable main" | tee -a /etc/apt/sources.list.d/elastic-6.x.list
RUN curl -sL https://deb.nodesource.com/setup_10.x | bash -

RUN apt-get update && apt-get install -y \
    default-jre \
    elasticsearch \
    nodejs

RUN npm install elasticdump -g

COPY elastic-dump.7z /tmp/elastic-dump.7z
COPY build.sh /tmp/build.sh
COPY elasticsearch.yml /etc/elasticsearch/

RUN chmod +r /etc/elasticsearch/elasticsearch.yml
RUN chmod +x /tmp/build.sh

EXPOSE 9200 9300

CMD service elasticsearch start && tail -f var/log/elasticsearch/docker-cluster.log
