#!/bin/bash
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

. "$SCRIPT_DIR"/../kafka-cluster/kafka.topics

# Script properties
#REQUIRED_INPUT_VALUES=2
#
#if (($# < $REQUIRED_INPUT_VALUES))
#then
#    echo "ERROR: missing required arguments"
#    echo "INFO: The number of arguments should be $REQUIRED_INPUT_VALUES"
#    echo "INFO: Try again with command 'bash $0 <KSQL_CLI_NAME> <KSQL_BOOTSTRAP_SERVER>'"
#    exit 1
#fi

# docker exec -it "$1" ksql "$2"
docker exec -it ksqldb-cli ksql http://ksqldb-server:8088

# $GITHUB_COMMITS
CREATE OR REPLACE STREAM
githubCommitsStream (
    sha VARCHAR,
    authorName VARCHAR,
    authorLogin VARCHAR,
    createdTime VARCHAR,
    language VARCHAR,
    message VARCHAR,
    commitRepository VARCHAR)
WITH (KAFKA_TOPIC='github-commits', VALUE_FORMAT='json');

CREATE OR REPLACE TABLE "github-metrics-top5-contributors-by-commits" AS
    SELECT
        authorLogin AS contributor_login,
        count(*) AS total_commits
    FROM githubCommitsStream
    GROUP BY authorLogin
EMIT CHANGES;

## odtad nie pokazuje wynikow, jest pusta tabela
#CREATE OR REPLACE STREAM
#contributionByCommitsStream (
#    contributor_login VARCHAR KEY,
#    total_commits BIGINT)
#WITH (KAFKA_TOPIC='github-metrics-top5-contributors-by-commits', VALUE_FORMAT='json');
#
#CREATE OR REPLACE TABLE test1 AS
#    SELECT
#        contributor_login,
#        topk(total_commits, 5)
#    FROM contributionByCommitsStream
#    GROUP BY contributor_login;




CREATE OR REPLACE TABLE TEST10 AS
    SELECT
        authorLogin AS contributor_login,
        count(1) AS total_commits
    FROM githubCommitsStream
    GROUP BY authorLogin
EMIT CHANGES;

CREATE OR REPLACE STREAM
contributionByCommitsStreamTEST (
    contributor_login VARCHAR KEY,
    total_commits BIGINT)
WITH (KAFKA_TOPIC='TEST10', VALUE_FORMAT='json');

# najbardziej legit wersja
CREATE OR REPLACE TABLE TEST11 AS
    SELECT
        contributor_login,
        topk(total_commits, 5)
    FROM contributionByCommitsStreamTEST
    GROUP BY contributor_login;