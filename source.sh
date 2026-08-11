#!/bin/bash

JAVA_DIR="src/main/java"
RESOURCE_DIR="src/main/resources"
DATEBASE_DIR="database"
OUTPUT_FILE="source.txt"
DEVCONTAINER_DIR=".devcontainer"

> "$OUTPUT_FILE"

echo "===== EXPORT COMPLET SPRING =====" >> "$OUTPUT_FILE"
echo "Date : $(date)" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

# Tous les fichiers Java
find "$JAVA_DIR" -type f -name "*.java" | sort | while read file
do
    echo "" >> "$OUTPUT_FILE"
    echo "======================================" >> "$OUTPUT_FILE"
    echo "FILE : $file" >> "$OUTPUT_FILE"
    echo "======================================" >> "$OUTPUT_FILE"

    cat "$file" >> "$OUTPUT_FILE"
    echo "" >> "$OUTPUT_FILE"
done

find "$DEVCONTAINER_DIR" -type f -name "*.*" | sort | while read file
do
    echo "" >> "$OUTPUT_FILE"
    echo "======================================" >> "$OUTPUT_FILE"
    echo "FILE : $file" >> "$OUTPUT_FILE"
    echo "======================================" >> "$OUTPUT_FILE"

    cat "$file" >> "$OUTPUT_FILE"
    echo "" >> "$OUTPUT_FILE"
done

find "$DATEBASE_DIR" -type f -name "*.sql" | sort | while read file
do
    echo "" >> "$OUTPUT_FILE"
    echo "======================================" >> "$OUTPUT_FILE"
    echo "FILE : $file" >> "$OUTPUT_FILE"
    echo "======================================" >> "$OUTPUT_FILE"

    cat "$file" >> "$OUTPUT_FILE"
    echo "" >> "$OUTPUT_FILE"
done

# Resources texte
find "$RESOURCE_DIR" -type f \
    ! -name "*.svg" \
    ! -name "*.png" \
    ! -name "*.jpg" \
    ! -name "*.jpeg" \
    ! -name "*.gif" \
    ! -name "*.ico" \
    ! -name "*.woff" \
    ! -name "*.woff2" \
    ! -name "*.js" \
    ! -name "*.css" \
    ! -name "*.DS_Store" \
    | sort | while read file
do
    echo "" >> "$OUTPUT_FILE"
    echo "======================================" >> "$OUTPUT_FILE"
    echo "RESOURCE : $file" >> "$OUTPUT_FILE"
    echo "======================================" >> "$OUTPUT_FILE"

    cat "$file" >> "$OUTPUT_FILE"
    echo "" >> "$OUTPUT_FILE"
done

echo " Export terminé : $OUTPUT_FILE"