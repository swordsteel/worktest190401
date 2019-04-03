# Work Sample

This work sample to simulate file pulling at time intervals to processing folder and if it succeed will be archived or if the file fails moved to a folder for invalid files. The data will be stored any bad record will be discarded…   

## Requirements

For building and running the application you need:

- [JDK 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [Maven 3](https://maven.apache.org)

## Usage: 
Grab you copy of the code from:
```shell
git clone https://github.com/swordsteel/worktest190401.git
```
Create folder structure for management files with the appropriate rights:
```shell
mkdir \tmp\archive
mkdir \tmp\invalid
mkdir \tmp\process
mkdir \tmp\watch
```

Change for folder and pull time parameters in:
```shell
worktest190401\src\main\resources\transaction.properties
```
Change database paramte in:
```shell
worktest190401\src\main\resources\application.properties
```
Run the application locally:
```shell
mvn spring-boot:run
```
