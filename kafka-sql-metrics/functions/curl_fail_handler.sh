#!/bin/bash

# function for handling curl request fail
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

export -f curl_fail_handler