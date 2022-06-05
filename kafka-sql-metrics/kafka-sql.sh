#!/bin/bash
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

. "$SCRIPT_DIR"/../kafka-cluster/kafka.topics

# Script properties
REQUIRED_INPUT_VALUES=3

if (($# < $REQUIRED_INPUT_VALUES))
then
    echo "ERROR: missing required arguments"
    echo "INFO: The number of arguments should be $REQUIRED_INPUT_VALUES"
    echo "INFO: Try again with command 'bash $0 <KSQL_CLI_NAME> <HOSTNAME> <PORT>'"
    exit 1
fi

# ksqldb container properties
CONTAINER_NAME=$1

# API properties
HOSTNAME=$2
PORT=$3
KSQL_BOOTSTRAP_SERVER="http://${HOSTNAME}:${PORT}"

# When curl fail function
curl_fail_handler() {
  REQUEST_TYPE=$1
  NUMBER_OF_TRIES=$2
  SERVER=$3
  # curl requests properties
  CURL_REQUEST_WAIT_TIME=10
  CURL_REQUEST_RETRY_LIMIT=6

  if [ $NUMBER_OF_TRIES -eq $CURL_REQUEST_RETRY_LIMIT ]
  then
      echo "ERROR: Failed to make $REQUEST_TYPE request to $SERVER"
      exit 3
  fi
  echo "WARN: No response from $SERVER, request will be retried in $CURL_REQUEST_WAIT_TIME seconds..."
  sleep $CURL_REQUEST_WAIT_TIME
}

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

# Create github commits stream
commits_stream_retries=0
until curl -i -X "POST" "$KSQL_BOOTSTRAP_SERVER/ksql" \
     -H "Accept: application/vnd.ksql.v1+json" \
     -H "Content-Type: application/json" \
     -d $'{
        "streamsProperties": {},
        "ksql": "CREATE STREAM githubCommitsStream (sha VARCHAR KEY,authorName VARCHAR,authorLogin VARCHAR,createdTime VARCHAR,language VARCHAR,message VARCHAR,commitRepository VARCHAR) WITH (kafka_topic='\'$GITHUB_COMMITS\'', VALUE_FORMAT='\''json'\'');"
     }'
do
  commits_stream_retries=$((commits_stream_retries+1))
  curl_fail_handler "POST" $commits_stream_retries $KSQL_BOOTSTRAP_SERVER
done

# Total number of commits
total_commits_retries=0
until curl -i -X "POST" "$KSQL_BOOTSTRAP_SERVER/ksql" \
     -H "Accept: application/vnd.ksql.v1+json" \
     -H "Content-Type: application/json" \
     -d $'{
        "streamsProperties": {},
        "ksql": "CREATE TABLE '"$GITHUB_METRICS_TOTAL_NUMBER_OF_COMMITS"' AS SELECT 1 AS KEY, COUNT(*) AS TOTAL_COMMITS FROM GITHUBCOMMITSSTREAM GROUP BY 1;"
     }'
do
  total_commits_retries=$((total_commits_retries+1))
  curl_fail_handler "POST" $total_commits_retries $KSQL_BOOTSTRAP_SERVER
done

# TODO -> Total number of committers
# TODO -> Total number of commits for each programming language
# TODO -> Suggest four new metrics and implement them, be ready to explain their value (Optionally)

exit 0

