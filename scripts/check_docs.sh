#!/bin/bash

# Documentation Check Script
# This script verifies that documentation is up to date with code changes
# and enforces content preservation rules

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "Checking documentation status..."

# Function to check content preservation
check_content_preservation() {
    local old_file="$1"
    local new_file="$2"
    
    # Check if old content exists in new file
    while IFS= read -r line; do
        if [[ ! "$line" =~ ^[[:space:]]*$ ]] && [[ ! "$line" =~ ^[[:space:]]*# ]]; then
            if ! grep -q "$line" "$new_file"; then
                echo -e "${RED}Content preservation violation: Line from $old_file not found in $new_file${NC}"
                echo "Missing line: $line"
                return 1
            fi
        fi
    done < "$old_file"
    return 0
}

# Check if documentation exists for new files
echo -e "\n${YELLOW}Checking for undocumented files...${NC}"
find src -type f -name "*.java" | while read -r file; do
    doc_file="docs/implementation/phase1/$(basename "$file" .java).md"
    if [ ! -f "$doc_file" ]; then
        echo -e "${RED}Missing documentation for: $file${NC}"
        exit 1
    fi
done

# Check if documentation is up to date with code
echo -e "\n${YELLOW}Checking documentation timestamps...${NC}"
find docs -type f -name "*.md" | while read -r doc; do
    related_files=$(grep -r "$(basename "$doc" .md)" src/ 2>/dev/null)
    if [ -n "$related_files" ]; then
        doc_time=$(stat -c %Y "$doc")
        code_time=$(stat -c %Y $(echo "$related_files" | cut -d: -f1))
        if [ "$doc_time" -lt "$code_time" ]; then
            echo -e "${RED}Documentation out of date: $doc${NC}"
            exit 1
        fi
    fi
done

# Check for broken links in documentation
echo -e "\n${YELLOW}Checking for broken links...${NC}"
find docs -type f -name "*.md" | while read -r doc; do
    grep -o "\[.*\](.*)" "$doc" | while read -r link; do
        if [[ $link =~ \[(.*)\]\((.*)\) ]]; then
            target="${BASH_REMATCH[2]}"
            if [ ! -f "$target" ] && [ ! -f "docs/$target" ]; then
                echo -e "${RED}Broken link in $doc: $target${NC}"
                exit 1
            fi
        fi
    done
done

# Check content preservation for modified files
echo -e "\n${YELLOW}Checking content preservation...${NC}"
git diff --cached --name-only | grep "\.md$" | while read -r doc; do
    if [ -f "$doc" ]; then
        # Create temporary file with staged changes
        git show "HEAD:$doc" > "/tmp/old_$doc" 2>/dev/null
        if [ $? -eq 0 ]; then
            if ! check_content_preservation "/tmp/old_$doc" "$doc"; then
                echo -e "${RED}Content preservation check failed for $doc${NC}"
                exit 1
            fi
        fi
    fi
done

# Check for change history
echo -e "\n${YELLOW}Checking change history...${NC}"
find docs -type f -name "*.md" | while read -r doc; do
    if ! grep -q "## Change History" "$doc"; then
        echo -e "${RED}Missing change history in $doc${NC}"
        exit 1
    fi
done

echo -e "\n${GREEN}Documentation check completed successfully!${NC}"
exit 0 