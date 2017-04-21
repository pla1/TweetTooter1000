#!/bin/bash
#
# Run Tweet Tooter 1000
#
while true
do
  rm -rf build/classes/main
  mkdir -p build/classes/main
  javac -cp lib/sikulixapi.jar -d build/classes/main src/main/java/net/pla1/tweettooter/*.java
  java -cp build/classes/main:.:lib/* net.pla1.tweettooter.Bot
done
