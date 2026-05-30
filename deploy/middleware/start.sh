#!/bin/bash
cd "$(dirname "$0")"

dirs=(
  postgres/data postgres/logs
  redis/data redis/logs
  kafka/data
  elasticsearch/data elasticsearch/logs
  seaweedfs/master/data seaweedfs/volume/data seaweedfs/filer/data
  infini-console/config infini-console/data infini-console/logs
)
for d in "${dirs[@]}"; do mkdir -p "$d"; done

chown -R 1000:1000 postgres/logs
chown -R  999:999  redis/logs
chown -R 1000:1000 kafka/data elasticsearch/data elasticsearch/logs

docker-compose up -d --force-recreate "$@"
