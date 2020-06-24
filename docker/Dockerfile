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
  perl

RUN cpanm --force XML::DOM::XPath

RUN cpanm CPAN::Meta \
 Bio::SeqIO \
 Bio::SeqUtils \ 
 Tie::IxHash

WORKDIR /usr/bin

COPY cgview.jar .

COPY cgview_xml_builder.pl .