#!/bin/bash

URL="http://localhost:8080/api/v1/create-url"
TOTAL_REQUESTS=12

PAYLOAD='{"url":"https://example.com"}'

for i in $(seq 1 $TOTAL_REQUESTS); do
    echo -n "Request $i: "

    # Sends a POST request with the JSON payload
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
      -X POST \
      -H "Content-Type: application/json" \
      -d "$PAYLOAD" \
      "$URL")

    echo "$HTTP_CODE"
done