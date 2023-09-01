#!/bin/bash

# The output file to which all contents will be concatenated
OUTPUT_FILE="all_contents.txt"

# Check if the output file already exists, if so, remove it
if [ -f "$OUTPUT_FILE" ]; then
    rm "$OUTPUT_FILE"
fi

# Use the 'find' command to get all files (not directories) from current directory and subdirectories
# Exclude files with .sh and .txt suffixes
# Then, loop through each file and append its content to the output file
find . -type f -not -name "*.sh" -not -name "*.txt" | while read file; do
    # Check if the file is not the output file to avoid infinite loop
    if [ "$file" != "./$OUTPUT_FILE" ]; then
        cat "$file" >> "$OUTPUT_FILE"
    fi
done

echo "All files (excluding .sh and .txt files) have been concatenated into $OUTPUT_FILE"
