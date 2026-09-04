#!/bin/bash

docker exec -it urlshortener-postgres \
  psql -U jesson -d urlshortener_db
