## refactoring-option-discoverer

This module implements the discovery of new nodes and resources by using semantic matchmaking.

## Prerequisites
This module depends on the SODALITE sub-project “semantic-reasoner”. Thus, first built it

The information about building semantic reasoner can be found at
 ` https://github.com/SODALITE-EU/semantic-reasoner `

## Build Process 

Use `maven` to build this project.
```
mvn clean install 
```
This requires maven 3.x

## Deployment

The built artifact is a jar file that can be used by other components (e.g., ml and rule-based refactoring)
