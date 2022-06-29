#!/bin/bash
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

. "$SCRIPT_DIR"/../../kafka-cluster/kafka.topics
source "$SCRIPT_DIR"/../functions/curl_fail_handler.sh

# Script properties
REQUIRED_INPUT_VALUES=1
KSQL_BOOTSTRAP_SERVER=$1

if (($# < $REQUIRED_INPUT_VALUES))
then
    echo "ERROR: missing required arguments"
    echo "INFO: The number of arguments should be $REQUIRED_INPUT_VALUES"
    echo "INFO: Try again with command 'bash $0 <KSQL_BOOTSTRAP_SERVER>'"
    exit 1
fi

total_commits_language_retries=0
until curl -i -X "POST" "$KSQL_BOOTSTRAP_SERVER/ksql" \
     -H "Accept: application/vnd.ksql.v1+json" \
     -H "Content-Type: application/json" \
     -d $'{
        "streamsProperties": {},
        "ksql": "CREATE TABLE '"$GITHUB_METRICS_TOTAL_LANGUAGE"' AS SELECT language, count(sha) AS TOTAL FROM GITHUBCOMMITSSTREAM GROUP BY language;"
     }'
do
  total_commits_language_retries=$((total_commits_language_retries+1))
  curl_fail_handler "POST" total_commits_language_retries $KSQL_BOOTSTRAP_SERVER
done

exit 0