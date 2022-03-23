#!/bin/bash

# Script properties
REQUIRED_INPUT_VALUES=6

if (($# < $REQUIRED_INPUT_VALUES))
then
    echo "ERROR: missing required arguments"
    echo "INFO: The number of arguments should be at least $REQUIRED_INPUT_VALUES"
    echo "INFO: Try again with command 'bash $0 <CONTAINER_NAME> <CONTAINER_VOLUME_ROOT_DIR> <HOSTNAME> <PORT> <SINK_FILE_NAME> [<TOPIC> | <TOPICS>]'"
    exit 1
fi

# kafka-connect container properties
CONTAINER_NAME=$1
VOLUME_ROOT_DIR=$2

# API properties
HOSTNAME=$3
PORT=$4
API_SERVER="http://${HOSTNAME}:${PORT}"

# Sink connector properties
SINK_FILE_NAME=$5
CONNECTOR_NAME="$SINK_FILE_NAME-sink-file-connector"
TASKS_MAX=1
FILE_PATH="$VOLUME_ROOT_DIR/$SINK_FILE_NAME"

shift 5
TOPIC=""
for topic in "$@"
do
    TOPIC+="${topic},"
done

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

number_of_tries=0

# Create a kafka-filestream sink connector reading from kafka topic/topics to specified file
until curl -i -f -X PUT -H "Accept:application/json" \
-H "Content-Type: application/json" "$API_SERVER/connectors/$CONNECTOR_NAME/config" \
-d '{
    "connector.class": "FileStreamSink",
    "topics": "'"$TOPIC"'",
    "file": "'"$FILE_PATH"'",
    "tasks.max": "'"$TASKS_MAX"'"
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
echo "INFO: $0 finished with success, '$CONNECTOR_NAME' has been created."
exit 0