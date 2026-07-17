#!/bin/bash
# Simple script to compile and run the DPMS backend

echo "=> Building backend (skipping tests)..."
JAVA_HOME=/opt/idea-IU-261.25134.95/jbr mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo "=> Build successful! Starting backend server..."
    JAVA_HOME=/opt/idea-IU-261.25134.95/jbr java -jar target/backend-0.0.1-SNAPSHOT.jar
else
    echo "=> Build failed. Please check the errors above."
fi
