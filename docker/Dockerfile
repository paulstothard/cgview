FROM ubuntu:18.04
LABEL maintainer="stothard@ualberta.ca"
LABEL description="CGView is a Java package for generating high-quality, zoomable maps of circular genomes."

RUN apt-get update && apt-get install -y \
  apt-utils \
  build-essential \
  cpanminus \
  default-jre \
  expat \
  libexpat-dev \
  perl \
  wget

RUN cpanm --force XML::DOM::XPath

RUN cpanm CPAN::Meta \
 Bio::SeqIO \
 Bio::SeqUtils \ 
 Tie::IxHash

WORKDIR /usr/bin

RUN wget https://paulstothard.github.io/cgview/downloads/cgview.jar

RUN wget https://paulstothard.github.io/cgview/downloads/cgview_xml_builder.pl