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

# Github commits stream
bash "$SCRIPT_DIR/processors/commits_stream.sh" $KSQL_BOOTSTRAP_SERVER
# Total number of commits
bash "$SCRIPT_DIR/processors/total_number_of_commits.sh" $KSQL_BOOTSTRAP_SERVER
# Total number of committers
bash "$SCRIPT_DIR/processors/total_number_of_committers.sh" $KSQL_BOOTSTRAP_SERVER
# Total number of commits for each programming language
bash "$SCRIPT_DIR/processors/total_commits_language.sh" $KSQL_BOOTSTRAP_SERVER

# TODO -> (Optionally) Suggest four new metrics and implement them, be ready to explain their value

exit 0

