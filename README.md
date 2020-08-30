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

## Docker Image Building and Usage
```
sudo docker build -t sodalite/refactoring-option-discoverer .
sudo docker run -p 8080:8080 -d --name=refactoring-option-discoverer sodalite/refactoring-option-discoverer
sudo docker start refactoring-option-discoverer
sudo docker logs refactoring-option-discoverer
sudo docker stop refactoring-option-discoverer
sudo docker rm  refactoring-option-discoverer
sudo docker rmi sodalite/refactoring-option-discoverer
```
## Refactoring Option Discovery REST APIs 

To find compute nodes or software nodes
```
http://{serverIP}:8080/refactoring-option-discoverer-api/v0.1/refactoringoptions/computenodes
http://{ serverIP}:8080/refactoring-option-discoverer-api/v0.1/refactoringoptions/softwarenodes
```
An Example Request 
```
{
	 "vars" : [
        "flavor",
        "image"
    ],
	"expr" : "( ?flavor = \"m1.small\" ) && ( ?image = \"centos7\" )"
}
```
