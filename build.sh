#!/bin/bash -e

#compile and package
mvn package

#run test classes
mvn -Dexec.mainClass="ca.ualberta.stothard.cgview.CgviewTest0" -Dexec.classpathScope="test" exec:java
mvn -Dexec.mainClass="ca.ualberta.stothard.cgview.CgviewTest1" -Dexec.classpathScope="test" exec:java
mvn -Dexec.mainClass="ca.ualberta.stothard.cgview.CgviewTest2" -Dexec.classpathScope="test" exec:java
mvn -Dexec.mainClass="ca.ualberta.stothard.cgview.CgviewTest3" -Dexec.classpathScope="test" exec:java

#jar with dependencies created by mvn
CGVIEW_JAR=$(find ./target -name "*jar-with-dependencies.jar" -print -quit)

#test command-line processing of xml input
echo "Testing command-line processing of xml files in 'sample_input'."
XML_INPUT=($(find ./sample_input -name "*.xml" -type f))

for file in "${XML_INPUT[@]}"; do
  f=$(basename "$file" .xml)
  echo "Processing file '$file'."

  java -jar $CGVIEW_JAR -i "$file" -f png -o ./test_maps/"${f}".png
  java -jar $CGVIEW_JAR -i "$file" -f svg -o ./test_maps/"${f}".svg
done
echo "Maps created in 'test_maps'."

#test command-line processing of tab-delimited input
echo "Testing command-line processing of tab files in 'sample_input'."
TAB_INPUT=($(find ./sample_input -name "*.tab" -type f))

for file in "${TAB_INPUT[@]}"; do
  f=$(basename "$file" .tab)
  echo "Processing file '$file'."

  java -jar $CGVIEW_JAR -i "$file" -f png -o ./test_maps/"${f}".png
  java -jar $CGVIEW_JAR -i "$file" -f svg -o ./test_maps/"${f}".svg
done
echo "Maps created in 'test_maps'."

#test command-line creation of linked image series
echo "Testing command-line creation of linked image series."
java -jar $CGVIEW_JAR -i ./sample_input/xml/navigable.xml -s ./test_maps/navigable -x 1,6
echo "Maps created in 'test_maps/featureRange_element_series'."

#create cgview.jar
cp $CGVIEW_JAR ./target/cgview.jar

#add cgview.jar to ./bin
cp ./target/cgview.jar ./bin/

#add cgview.jar to ./docs/downloads
cp ./target/cgview.jar ./docs/downloads/

#add cgview_xml_builder to ./docs/downloads
tar --exclude=".*" -C ./scripts -cvzf cgview_xml_builder.tar.gz cgview_xml_builder
mv cgview_xml_builder.tar.gz ./docs/downloads/

#copy files to ./docker for image
cp ./bin/cgview.jar ./docker/
cp ./scripts/cgview_xml_builder/cgview_xml_builder.pl ./docker/