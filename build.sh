#!/bin/bash
mvn package
mvn -Dexec.mainClass="ca.ualberta.stothard.cgview.CgviewTest0" -Dexec.classpathScope="test" test-compile exec:java
mvn -Dexec.mainClass="ca.ualberta.stothard.cgview.CgviewTest1" -Dexec.classpathScope="test" test-compile exec:java
mvn -Dexec.mainClass="ca.ualberta.stothard.cgview.CgviewTest2" -Dexec.classpathScope="test" test-compile exec:java
mvn -Dexec.mainClass="ca.ualberta.stothard.cgview.CgviewTest3" -Dexec.classpathScope="test" test-compile exec:java

CGVIEW_JAR=$(find ./target -name "*jar-with-dependencies.jar" -print -quit)

XML_INPUT=($(find ./sample_input -name "*.xml" -type f))

for file in "${XML_INPUT[@]}"; do
  f=$(basename "$file" .xml)
  echo "Processing file '$file'."

  java -jar $CGVIEW_JAR -i "$file" -f png -o ./test_maps/"${f}".png
  java -jar $CGVIEW_JAR -i "$file" -f svg -o ./test_maps/"${f}".svg
done

TAB_INPUT=($(find ./sample_input -name "*.tab" -type f))

for file in "${TAB_INPUT[@]}"; do
  f=$(basename "$file" .tab)
  echo "Processing file '$file'."

  java -jar $CGVIEW_JAR -i "$file" -f png -o ./test_maps/"${f}".png
  java -jar $CGVIEW_JAR -i "$file" -f svg -o ./test_maps/"${f}".svg
done

#test image series
java -jar $CGVIEW_JAR -i ./sample_input/xml/featureRange_element.xml -s ./test_maps/featureRange_element_series -x 1,6
