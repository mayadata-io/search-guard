FROM centos:7

WORKDIR /usr/share/curator

RUN yum -y update

RUN yum --enablerepo=extras -y install epel-release

RUN yum -y install python-pip

RUN pip install -U elasticsearch-curator==5.8.1
