## Table of contents
- [Project modules](#project-modules)
- [Requirements](#requirements)
- [Usage](#usage)
- [Status](#status)
- [Contact](#contact)

## Project modules
```
.
├── docker-compose          # docker files and kafka cluster containers data
├── github-accounts-app     # github accounts analyzer app
├── kafka-cluster           # files, CLI commands and scripts related with kafka cluster 
├── kafka-connect           # files, CLI commands and scripts related with kafka connect
├── kafka-sql-metrics       # files, CLI commands and scripts related with KSQL
├── kafka-stream-metrics    # kafka streams metrics app
├── modules-tests           # e2e tests for modules of the project
.
```

## Requirements
* Unix-like operating system
* Git
* Docker
* JDK with Java 8 or higher
* Apache Maven

## Usage

To run project, follow these steps: <br />

1. Open terminal and clone the project from github repository <br /> <br />
   
2. Go to project root directory
```
$ cd <project_folder>
```
where <project_folder> is a path to project root directory <br />

3. Download and install all required project dependencies (use Maven) <br /> <br />
   
4. Build .jar files for producer and consumer apps (github-accounts-app and kafka-stream-metrics)
```
$ mvn package
```
or
```
$ mvn install
```

5. Run 'entrypoint.sh' script and wait until it finish (it may take some time)
```
$ bash entrypoint.sh
```

6. Check if producers and consumers apps were started in 'entrypoint.sh' script, if not, run them manually
```
$ java -jar github-accounts-app/target/<created_jar_name> &
$ java -jar kafka-stream-metrics/target/<created_jar_name> &
```
Example: <br />
```
$ java -jar github-accounts-app/target/github-accounts-app-0.11-SNAPSHOT-jar-with-dependencies.jar &
$ java -jar kafka-stream-metrics/target/kafka-stream-metrics-0.11-SNAPSHOT-jar-with-dependencies.jar &
```

7. Check if 'entrypoint.sh' script has started pipeline, if not, run following command in root project directory:
```
$ cp ./github-accounts.json ./docker-compose/containers-data/kafka-connect/data/githubAccounts-unprocessed-files
```
Kafka connect source connector should detect 'github-accounts.json' file, read data from it, and run the pipeline. <br /> <br />

All metrics will be written under '<root_project_directory>/docker-compose/containers-data/kafka-connect/data/' folder (as JSON files). <br />

### Ending application

1. Stop all producers and consumers apps <br /> <br />
   
2. Go to 'docker-compose' directory
```
$ cd <root_project_directory>/docker-compose/
```

3. Run command:
```
$ docker-compose down
```

4. Remove 'containers-data' folder from 'docker-compose' module.

## Status

_completed_

## Contact

Created by @mkrolczyk - feel free to contact me!