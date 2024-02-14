#!/usr/bin/env bash

# Function to check if Maven is installed
check_maven() {
    if [ ! command -v mvn &> /dev/null ]; then
        echo "Maven is required but not installed. Please install Maven and re-run this script."
        exit 1
    fi
}

check_maven

# Check if an argument is provided
if [ "$#" -ne 1 ]; then
    echo "Please specify the path to a .tar.gz file to process."
    exit 1
fi

TAR_FILE=$1

# Unpack the tar.gz file
tar -xvf $TAR_FILE

# Find all directories containing a pom.xml file and run the command in them
find . -type f -name "pom.xml" | while read pom; do
    DIR=$(dirname "$pom")
    pushd "$DIR" > /dev/null
    echo "Gathering dependencies within $DIR"
    mvn dependency:tree | grep -E '(org.springframework|io.micrometer)' > spring-dependencies.txt
    popd > /dev/null
done

echo "Processing complete."
