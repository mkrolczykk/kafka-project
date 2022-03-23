#!/bin/bash

# Script properties
REQUIRED_INPUT_VALUES=6

if (($# < $REQUIRED_INPUT_VALUES))
then
    echo "ERROR: missing required arguments"
    echo "INFO: The number of arguments should be $REQUIRED_INPUT_VALUES"
    echo "INFO: Try again with command 'bash $0 <CONTAINER_NAME> <CONTAINER_VOLUME_ROOT_DIR> <INPUT_FILE_PATTERN> <HOSTNAME> <PORT> <TOPIC>'"
    exit 1
fi

# kafka-connect container properties
CONTAINER_NAME=$1
VOLUME_ROOT_DIR=$2

# Github accounts source connector properties
TOPIC=$6

INPUT_FOLDER="$TOPIC-unprocessed-files"
ERROR_FOLDER="$TOPIC-error-files"
FINISHED_FOLDER="$TOPIC-processed-files"

CONNECTOR_NAME="$TOPIC-source-connector"
INPUT_PATH="${VOLUME_ROOT_DIR}/${INPUT_FOLDER}"
ERROR_PATH="${VOLUME_ROOT_DIR}/${ERROR_FOLDER}"
FINISHED_PATH="${VOLUME_ROOT_DIR}/${FINISHED_FOLDER}"
TASKS_MAX=1
INPUT_FILE_PATTERN=$3
HALT_ON_ERROR=true     # Sets whether the task halts when it encounters an error or continues to the next file

# API properties
HOSTNAME=$4
PORT=$5
API_SERVER="http://${HOSTNAME}:${PORT}"

# curl request properties
CURL_REQUEST_WAIT_TIME=10
CURL_REQUEST_RETRY_LIMIT=6

# Check if given container exists
if [ ! "$(docker ps -a | grep ${CONTAINER_NAME})" ]
then
    echo "ERROR: '${CONTAINER_NAME}' container doesn't exists!"
    exit 2
fi

# Be sure kafka-connect container and kafka-connect-rest are up
until [ "`docker inspect -f {{.State.Running}} ${CONTAINER_NAME}`"=="true" ]; do
    echo "INFO: Checking '$CONTAINER_NAME' container status..."
    sleep 1;
done;

echo "INFO: '$CONTAINER_NAME' container is running and ready to process requests"

# Create required directories inside container
docker exec -it $CONTAINER_NAME mkdir -p $VOLUME_ROOT_DIR/$INPUT_FOLDER $VOLUME_ROOT_DIR/$ERROR_FOLDER $VOLUME_ROOT_DIR/$FINISHED_FOLDER

number_of_tries=0

# Create a kafka-connect-spooldir connector reading github accounts from local file to kafka topic
until curl -i -f -X PUT -H "Accept:application/json" \
-H "Content-Type: application/json" "$API_SERVER/connectors/$CONNECTOR_NAME/config" \
-d '{
    "connector.class": "com.github.jcustenborder.kafka.connect.spooldir.SpoolDirSchemaLessJsonSourceConnector",
    "input.path": "'"$INPUT_PATH"'",
    "error.path": "'"$ERROR_PATH"'",
    "finished.path": "'"$FINISHED_PATH"'",
    "tasks.max": "'"$TASKS_MAX"'",
    "input.file.pattern": "'"$INPUT_FILE_PATTERN"'",
    "halt.on.error": "'"$HALT_ON_ERROR"'",
    "topic": "'"$TOPIC"'",
    "value.converter": "org.apache.kafka.connect.storage.StringConverter"
    }'
do
    number_of_tries=$((number_of_tries+1))
    if [ $number_of_tries -eq $CURL_REQUEST_RETRY_LIMIT ]
    then
        echo "ERROR: Failed to make PUT request to $API_SERVER"
        exit 3
    fi
    echo "WARN: No response from $API_SERVER, request will be retried in $CURL_REQUEST_WAIT_TIME seconds..."
    sleep $CURL_REQUEST_WAIT_TIME
done

echo -e "\nINFO: Succesfully finished PUT request to $API_SERVER"
echo "INFO: $0 finished with success"
exit 0