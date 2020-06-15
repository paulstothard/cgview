#!/bin/bash
mvn package
mvn -Dexec.mainClass="ca.ualberta.stothard.cgview.CgviewTest0" -Dexec.classpathScope="test" test-compile exec:java
