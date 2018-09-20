#!/bin/bash

# Do some cleanup
rm -f distribution.zip

# Create distribution file
zip -r distribution.zip . -i@distribution.txt
zip -uj distribution.zip src/main/resources/app.properties

