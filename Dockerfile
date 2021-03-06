FROM docker.elastic.co/elasticsearch/elasticsearch-oss:6.4.0
COPY ./target/releases/search-guard-6-6.4.0-23.1.zip /usr/share/
RUN bin/elasticsearch-plugin install -b file:///usr/share/search-guard-6-6.4.0-23.1.zip
RUN chmod +x plugins/search-guard-6/tools/install_demo_configuration.sh
RUN printf 'y\ny\nn\n' | plugins/search-guard-6/tools/install_demo_configuration.sh

USER elasticsearch
#RUN mkdir -p snapshot-restore
#RUN chmod 0777 snapshot-restore
COPY ./es-config/elasticsearch.yml ./config/elasticsearch.yml

USER root
RUN chown elasticsearch:elasticsearch config/elasticsearch.yml
RUN mkdir -p snapshot-restore
RUN chmod 0777 snapshot-restore

USER elasticsearch
EXPOSE 9200 9300
