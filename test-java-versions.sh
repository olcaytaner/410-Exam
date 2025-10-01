#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}GraphViz Multi-Java Version Test Suite${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Maven command - use Homebrew installation
MVN_CMD="/opt/homebrew/bin/mvn"

if [ ! -f "$MVN_CMD" ]; then
    echo -e "${RED}Error: Maven not found at $MVN_CMD${NC}"
    echo "Please install Maven: brew install maven"
    exit 1
fi

# Array of Java versions to test
JAVA_VERSIONS=(1.8 11 17 21 24)

# Track results
PASSED=0
FAILED=0
SKIPPED=0

# Create results directory
RESULTS_DIR="test-results-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$RESULTS_DIR"

for version in "${JAVA_VERSIONS[@]}"; do
    echo ""
    echo -e "${YELLOW}========================================${NC}"
    echo -e "${YELLOW}Testing with Java $version${NC}"
    echo -e "${YELLOW}========================================${NC}"

    # Find Java home for this version
    JAVA_PATH=$(/usr/libexec/java_home -v $version 2>/dev/null)

    if [ $? -eq 0 ]; then
        echo -e "${BLUE}Java Home: $JAVA_PATH${NC}"

        # Show Java version
        "$JAVA_PATH/bin/java" -version 2>&1 | head -1
        echo ""

        # Run tests with this Java version
        JAVA_HOME="$JAVA_PATH" "$MVN_CMD" test -Dtest=GraphvizEngineTest \
            > "$RESULTS_DIR/java-$version.log" 2>&1

        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✅ PASSED${NC}"
            ((PASSED++))
            # Show summary from log
            grep "Tests run:" "$RESULTS_DIR/java-$version.log" | tail -1
        else
            echo -e "${RED}❌ FAILED${NC}"
            ((FAILED++))
            # Show error summary
            echo "Last 10 lines of error:"
            tail -10 "$RESULTS_DIR/java-$version.log"
        fi
    else
        echo -e "${RED}Java $version not found, skipping...${NC}"
        ((SKIPPED++))
    fi
done

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Test Summary${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}Passed: $PASSED${NC}"
echo -e "${RED}Failed: $FAILED${NC}"
echo -e "${YELLOW}Skipped: $SKIPPED${NC}"
echo ""
echo "Detailed logs saved to: $RESULTS_DIR/"
echo ""

# Exit with error if any tests failed
if [ $FAILED -gt 0 ]; then
    exit 1
fi
