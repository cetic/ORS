#!/bin/sh

javac -d ./generated_pojos/  ./generated_pojos/*.java
java -jar ORS-0.2-SNAPSHOT-jar-with-dependencies.jar
