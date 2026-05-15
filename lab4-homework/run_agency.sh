#!/bin/bash
javac -cp "lib/amqp-client.jar:lib/slf4j-api.jar:lib/slf4j-simple.jar" Agency.java
java -cp ".:lib/amqp-client.jar:lib/slf4j-api.jar:lib/slf4j-simple.jar" Agency
