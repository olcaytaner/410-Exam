# Changelog

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