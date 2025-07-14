#!/bin/bash

# Intelligent context-aware preservation checker
# This script ensures that content updates maintain semantic value and historical context
# while allowing for meaningful improvements

set -e

# Function to analyze content importance
analyze_importance() {
    local content=$1
    # Check for key indicators of importance
    local has_technical_details=$(echo "$content" | grep -q "technical|implementation|architecture|security" && echo "true" || echo "false")
    local has_historical_context=$(echo "$content" | grep -q "history|background|context|reasoning" && echo "true" || echo "false")
    local has_decision_points=$(echo "$content" | grep -q "decision|rationale|consideration" && echo "true" || echo "false")
    
    # Score the importance
    local score=0
    [ "$has_technical_details" = "true" ] && score=$((score + 3))
    [ "$has_historical_context" = "true" ] && score=$((score + 2))
    [ "$has_decision_points" = "true" ] && score=$((score + 2))
    
    echo $score
}

# Function to check semantic preservation
check_semantic_preservation() {
    local original=$1
    local current=$2
    
    # Extract key concepts and their context
    local original_concepts=$(echo "$original" | grep -o -E "technical|implementation|architecture|security|history|background|context|reasoning|decision|rationale|consideration" | sort | uniq)
    local current_concepts=$(echo "$current" | grep -o -E "technical|implementation|architecture|security|history|background|context|reasoning|decision|rationale|consideration" | sort | uniq)
    
    # Check if key concepts are preserved
    local missing_concepts=$(comm -23 <(echo "$original_concepts") <(echo "$current_concepts"))
    if [ -n "$missing_concepts" ]; then
        echo "WARNING: Some key concepts may be missing: $missing_concepts"
        return 1
    fi
    return 0
}

# Function to check if content was meaningfully preserved
check_preservation() {
    local file=$1
    local original_content=$(git show HEAD:$file 2>/dev/null || echo "")
    local current_content=$(cat $file 2>/dev/null || echo "")
    
    if [ -n "$original_content" ] && [ -n "$current_content" ]; then
        # Analyze importance of original content
        local importance_score=$(analyze_importance "$original_content")
        
        # Check semantic preservation
        if ! check_semantic_preservation "$original_content" "$current_content"; then
            echo "WARNING: Semantic preservation check failed for $file"
            echo "Some key concepts or context may be missing"
            if [ $importance_score -ge 5 ]; then
                echo "ERROR: High-importance content may have been lost"
                exit 1
            fi
        fi
        
        # Check for improvements
        local has_improvements=$(echo "$current_content" | grep -q "improved|enhanced|updated|clarified" && echo "true" || echo "false")
        if [ "$has_improvements" = "true" ]; then
            echo "INFO: Content improvements detected in $file"
        fi
    fi
}

# Check documentation files
find docs -type f -name "*.md" | while read file; do
    check_preservation "$file"
done

# Check source files
find src -type f \( -name "*.java" -o -name "*.xml" -o -name "*.json" \) | while read file; do
    check_preservation "$file"
done

# Check configuration files
find . -maxdepth 1 -type f \( -name "*.gradle" -o -name "*.properties" -o -name "pom.xml" \) | while read file; do
    check_preservation "$file"
done

echo "Context-aware preservation check completed successfully" 