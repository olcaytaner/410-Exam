# Changelog

## [1.2.5] - 2025-11-04

### Added
- DFA validation in execute() method to check for complete transition function before execution
- Validation error detection in TestRunner to prevent grading of invalid automata

### Fixed
- Invalid DFAs no longer receive partial credit during batch grading
- GraphViz visualization error handling for DFAs with missing transitions

### Improved
- Clear error messages when DFA is incomplete or improperly configured
- Better separation between validation errors and execution errors

## [1.2.4] - 2025-10-24

### Changed
- Simplified release workflow to build single universal JAR instead of multiple Java-specific versions
- Extended Java testing support to Java 24 and 25 (GA releases)
- Added Java 26 (Early Access) testing support
- Restructured test workflow to verify Java 8-built JAR compatibility across all supported Java versions
- Release workflow now includes JAR verification step on all supported Java versions before publishing

### Technical Details
- Test workflow now builds JAR once with Java 8, then tests that specific JAR on Java 8, 11, 17, 21, 24, and 25
- Release workflow includes new `verify-jar` job that validates the release artifact on all supported Java versions
- Ensures released JAR actually works on all claimed supported versions (8+)

## [1.2.3] - 2025-10-23

### Added
- Syntax Help dialog and menu item in Help menu
- DFA test files for exercises
- Default template for SyntaxTree class
- Epsilon support for regex with revised regex exercises

### Changed
- GraalVM plugin configuration refactored to use arguments instead of system properties
- GraalVM dependency version updated with enhanced graph compilation checks

### Fixed
- Regex README documentation
- Issue #38 (compilation fix)
- Merge pull request #40 for compilation improvements

## [1.2.2] - 2025-10-16

### Changed
- Improved false positive handling in scoring algorithm
- Adjusted log10 calculation for penalty scoring (changed denominator from FN+FP+2 to FN+FP+10)
- Refined conditional logic for zero false positive cases in point calculation

### Improved
- More accurate grading with better handling of edge cases
- Optimized import statements in TestRunner

## [1.2.1] - 2025-10-16

### Added
- Resizable divider between text editor and graph visualization panels
- Custom divider styling with grip lines for better visibility
- One-touch expandable arrows for quick panel collapse/expand
- Resize cursor on hover to indicate draggable divider

### Improved
- Enhanced UI usability with more obvious resizing controls
- Proportional panel resizing when window is resized (30/70 split)

## [1.2.0] - 2025-10-16

### Changed
- Refactored calculatePoints and getPoints methods to return double values for more precise point calculations
- Updated documentation for grading methods to reflect double-precision scoring

## [1.1.6] - 2025-10-12

### Changed
- Enhanced compileWithFigure method to skip visualization for CFG and REGEX types

### Added
- Regex test files and week 5 regex exercises
- README updates for regex documentation

## [1.1.5] - 2025-10-12

### Changed
- Refactored scoring logic to remove minimum point constraint in TestRunner

## [1.1.4] - 2025-10-12

### Added
- Grading functionality for exams with automatic points calculation
- Grading configuration fields (minimum points, per-test points, penalty points) in exam interface
- Student-friendly test results reporting with detailed grading summary
- Enhanced test results display showing points earned vs total points

### Fixed
- NFA epsilon transition bug causing incorrect acceptance/rejection
- Test case generation producing wrong solutions

### Improved
- Additional NFA execution tests for better coverage
- Test result validation and error reporting

## [1.1.3] - 2025-10-09

### Fixed
- Regex replacement in state and transition validation to use replaceFirst instead of replace

### Changed
- Added exclusions for xml-apis and xalan in batik-all dependency to avoid conflicts

## [1.1.2] - 2025-10-09

### Fixed
- CFG bug caused by epsilon productions

### Changed
- Updated default TM template

## [1.1.1] - 2025-10-05

### Added
- NFA test case generation for exercises
- Warning when NFA state name is too long

### Changed
- NFA now accepts alphanumerical state names (not just numerical)

### Fixed
- JAR artifact download path in release workflow

## [1.1.0] - 2025-10-05

### Changed
- Improved SVG rendering with JSVGCanvas replacing previous image handling
- Removed custom resize logic as JSVGCanvas handles dynamic resizing internally
- Added zoom and pan interactions for figure visualization

### Fixed
- Pixelated figure rendering issues (#24)
- Incomplete DFA compilation bug
- Dialog options handling in save confirmation
- File closing issues (#25)
- Exception handling improvements
- GraphViz rendering tests now validate SVG output instead of image icons

### Improved
- NFA tests enabled and fixed

## [1.0.2] - 2025-09-30

### Added
- Undo/redo functionality in AbstractAutomatonPanel
- Automaton validation bug report template

### Fixed
- Multiple transition with the same symbol from the same state bug (#16)

### Changed
- Update version retrieval method in MainFrame and SplashScreen to read from Maven properties
- Refactor MockAutomatonPanel methods for clarity and consistency

## [1.0.1] - Previous Release