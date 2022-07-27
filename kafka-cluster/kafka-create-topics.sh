#!/bin/bash
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

. $SCRIPT_DIR/kafka.topics

# Script properties
REQUIRED_INPUT_VALUES=2

if (($# < $REQUIRED_INPUT_VALUES))
then
    echo "ERROR: missing required arguments"
    echo "INFO: The number of arguments should be $REQUIRED_INPUT_VALUES"
    echo "INFO: Try again with command 'bash $0 <BROKER_NAME> <BOOTSTRAP_SERVER>'"
    exit 1
fi

# Kafka connection
BROKER=$1
BOOTSTRAP_SERVER=$2

# Github accounts topic
docker exec -it $BROKER kafka-topics \
    --create \
    --bootstrap-server $BOOTSTRAP_SERVER \
    --topic $GITHUB_ACCOUNTS \
    --partitions 3 \
    --replication-factor 3

# Github commits topic
docker exec -it $BROKER kafka-topics \
    --create \
    --bootstrap-server $BOOTSTRAP_SERVER \
    --topic $GITHUB_COMMITS \
    --partitions 3 \
    --replication-factor 3

# Github metrics with top 5 contributors by number of commits topic
docker exec -it $BROKER kafka-topics \
    --create \
    --bootstrap-server $BOOTSTRAP_SERVER \
    --topic $GITHUB_METRICS_TOP5_CONTR_BY_COMMITS \
    --partitions 3 \
    --replication-factor 3

# Github metrics with total number of commits for each programming language topic
docker exec -it $BROKER kafka-topics \
    --create \
    --bootstrap-server $BOOTSTRAP_SERVER \
    --topic $GITHUB_METRICS_TOTAL_LANGUAGE \
    --partitions 3 \
    --replication-factor 3

exit 0