#!/bin/bash
# 中间件启动脚本：自动创建目录 + 修复权限 + 启动服务

cd "$(dirname "$0")"

# 创建数据目录
dirs=(
  postgres/data postgres/logs
  redis/data redis/logs
  kafka/data
  elasticsearch/data elasticsearch/logs
  seaweedfs/master/data seaweedfs/volume/data
  infini-console/config infini-console/data infini-console/logs
)

for d in "${dirs[@]}"; do
  mkdir -p "$d"
done

# 修复权限（容器内各服务的 UID）
chown -R 999:999   postgres/logs redis/logs      # postgres/redis
chown -R 1000:1000 kafka/data                     # kafka (appuser)
chown -R 1000:1000 elasticsearch/data elasticsearch/logs  # elasticsearch

# 启动
docker-compose up -d "$@"
