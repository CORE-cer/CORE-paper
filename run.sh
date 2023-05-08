#!/bin/bash

java -Djava.security.manager -Djava.security.policy=files/java.policy -jar CORE-FAT-1.0-SNAPSHOT.jar -q ./example_queries/example9/query.data -s ./example_queries/example9/streams.data -f -v -o