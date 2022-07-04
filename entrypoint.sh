#!/bin/bash
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

. "$SCRIPT_DIR"/kafka-cluster/kafka.topics

# WARNING: Script kafka-related properties must be the same as set in ./docker-compose/docker-compose.yml config file.

# Kafka-brokers properties
BROKER_1="broker1"
BROKER_2="broker2"
BROKER_3="broker3"
BOOTSTRAP_SERVER_1="127.0.0.1:19092"
BOOTSTRAP_SERVER_2="127.0.0.1:19093"
BOOTSTRAP_SERVER_3="127.0.0.1:19094"

# Kafka-connect properties
KAFKA_CONNECT_CONTAINER_NAME="kafka-connect"
KAFKA_CONNECT_VOLUME_ROOT_DIR="/data"
KAFKA_CONNECT_INPUT_FILE_PATTERN='.*\\.json'    # pattern must be in '' quotes
KAFKA_CONNECT_HOST="localhost"
KAFKA_CONNECT_PORT="8083"

# # KSQL_DB properties
ADDITIONAL_KSQL_DB_SERVERS=1
KSQL_DB_HOST="localhost"
KSQL_DB_PORT="8088"

# Script properties
SERVICES_LOAD_WAIT_TIME=30

# Start Kafka infrastructure
docker-compose -f ./docker-compose/docker-compose.yml up --scale additional-ksqldb-server=$ADDITIONAL_KSQL_DB_SERVERS -d

# Wait untill all services will be ready to use
echo "INFO: Waiting for all services to be ready to use..."
sleep $SERVICES_LOAD_WAIT_TIME

running="$(docker-compose -f ./docker-compose/docker-compose.yml ps --services --filter "status=running")"
services="$(docker-compose -f ./docker-compose/docker-compose.yml ps --services)"
if [ "$running" != "$services" ]; then
    echo "ERROR: Following services failed to start:" 
    comm -13 <(sort <<<"$running") <(sort <<<"$services")
    # stop running containers
    docker-compose -f ./docker-compose/docker-compose.yml down
    exit 1
else
    echo "INFO: All required services are running"
fi

# Create required topics
bash "./kafka-cluster/kafka-create-topics.sh" $BROKER_1 $BOOTSTRAP_SERVER_1

# Create KSQL pipelines
bash "./kafka-sql-metrics/kafka-sql.sh" "ksqldb-cli" $KSQL_DB_HOST $KSQL_DB_PORT

# Create Github accounts source connector
bash "./kafka-connect/spooldir-json-file-source-connector.sh" \
    $KAFKA_CONNECT_CONTAINER_NAME \
    $KAFKA_CONNECT_VOLUME_ROOT_DIR \
    $KAFKA_CONNECT_INPUT_FILE_PATTERN \
    $KAFKA_CONNECT_HOST \
    $KAFKA_CONNECT_PORT \
    $GITHUB_ACCOUNTS

# Create Kafka sink connectors for github-metrics

# Top 5 contributors by number of commits
bash "./kafka-connect/filestream-sink-connector.sh" \
    $KAFKA_CONNECT_CONTAINER_NAME \
    $KAFKA_CONNECT_VOLUME_ROOT_DIR \
    $KAFKA_CONNECT_HOST \
    $KAFKA_CONNECT_PORT \
    "githubMetricsTop5ContrByCommits.txt" \
    $GITHUB_METRICS_TOP5_CONTR_BY_COMMITS

# Total number of commits
bash "./kafka-connect/filestream-sink-connector.sh" \
    $KAFKA_CONNECT_CONTAINER_NAME \
    $KAFKA_CONNECT_VOLUME_ROOT_DIR \
    $KAFKA_CONNECT_HOST \
    $KAFKA_CONNECT_PORT \
    "githubMetricsTotalNumberOfCommits.txt" \
    $GITHUB_METRICS_TOTAL_NUMBER_OF_COMMITS

# Total number of commiters
bash "./kafka-connect/filestream-sink-connector.sh" \
    $KAFKA_CONNECT_CONTAINER_NAME \
    $KAFKA_CONNECT_VOLUME_ROOT_DIR \
    $KAFKA_CONNECT_HOST \
    $KAFKA_CONNECT_PORT \
    "githubMetricsTotalNumberOfCommiters.txt" \
    $GITHUB_METRICS_TOTAL_NUMBER_OF_COMMITERS

# Total number of commits for each programming language
bash "./kafka-connect/filestream-sink-connector.sh" \
    $KAFKA_CONNECT_CONTAINER_NAME \
    $KAFKA_CONNECT_VOLUME_ROOT_DIR \
    $KAFKA_CONNECT_HOST \
    $KAFKA_CONNECT_PORT \
    "githubMetricsTotalLanguage.txt" \
    $GITHUB_METRICS_TOTAL_LANGUAGE

# Run producers and consumers apps
# java -jar github-accounts-app/target/github-accounts-app-0.11-SNAPSHOT-jar-with-dependencies.jar &
# java -jar kafka-stream-metrics/target/kafka-stream-metrics-0.11-SNAPSHOT-jar-with-dependencies.jar &

sleep 15 # wait for producers and consumers to be up and running

# Start pipeline
cp ./github-accounts.json ./docker-compose/containers-data/kafka-connect/data/githubAccounts-unprocessed-files

exit 0