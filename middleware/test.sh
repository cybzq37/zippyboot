#!/bin/bash
# ----------------------------------------------------------
# 中间件健康检查脚本
# 用法: ./test.sh
# ----------------------------------------------------------
set -e
cd "$(dirname "$0")"

PASS=0
FAIL=0
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

pass() { ((PASS++)); echo -e "  ${GREEN}✓${NC} $1"; }
fail() { ((FAIL++)); echo -e "  ${RED}✗${NC} $1"; }
info() { echo -e "${YELLOW}[$1]${NC}"; }

# ----------------------------------------------------------
# PostgreSQL
# ----------------------------------------------------------
info "PostgreSQL"
result=$(docker exec zyn-postgres-1 psql -U postgres -d zyn_base -t -c "SELECT 1;" 2>/dev/null | tr -d ' ')
[ "$result" = "1" ] && pass "连接查询: SELECT 1" || fail "连接查询失败"

result=$(docker exec zyn-postgres-1 psql -U postgres -d zyn_base -t -c "SELECT PostGIS_Version();" 2>/dev/null | tr -d ' ')
[ -n "$result" ] && pass "PostGIS 版本: $result" || fail "PostGIS 未安装"

# ----------------------------------------------------------
# Redis
# ----------------------------------------------------------
info "Redis"
docker exec zyn-redis-1 redis-cli -a 'Zyn@Secure#99' SET zyn:test "hello" > /dev/null 2>&1
result=$(docker exec zyn-redis-1 redis-cli -a 'Zyn@Secure#99' GET zyn:test 2>/dev/null)
[ "$result" = "hello" ] && pass "读写测试: SET/GET zyn:test" || fail "读写测试失败"
docker exec zyn-redis-1 redis-cli -a 'Zyn@Secure#99' DEL zyn:test > /dev/null 2>&1

result=$(docker exec zyn-redis-1 redis-cli -a 'Zyn@Secure#99' PING 2>/dev/null)
[ "$result" = "PONG" ] && pass "PING: PONG" || fail "PING 失败"

# ----------------------------------------------------------
# Kafka
# ----------------------------------------------------------
info "Kafka"
echo "zyn-test-message" | docker exec -i zyn-kafka-1 /opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server localhost:9092 --topic zyn-test-topic > /dev/null 2>&1
pass "生产消息: zyn-test-topic"

result=$(docker exec zyn-kafka-1 /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 --topic zyn-test-topic --from-beginning --timeout-ms 3000 2>/dev/null | head -1)
[ "$result" = "zyn-test-message" ] && pass "消费消息: $result" || fail "消费消息失败"

docker exec zyn-kafka-1 /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 --delete --topic zyn-test-topic > /dev/null 2>&1

# ----------------------------------------------------------
# Elasticsearch
# ----------------------------------------------------------
info "Elasticsearch"
result=$(curl -sf http://localhost:9200/_cluster/health 2>/dev/null | python3 -c "import sys,json;print(json.load(sys.stdin)['status'])" 2>/dev/null)
[ "$result" = "green" ] || [ "$result" = "yellow" ] && pass "集群状态: $result" || fail "集群状态异常: $result"

result=$(curl -sf http://localhost:9200/_cat/plugins 2>/dev/null)
echo "$result" | grep -q "analysis-ik"      && pass "插件: IK 中文分词"       || fail "插件缺失: IK"
echo "$result" | grep -q "analysis-pinyin"   && pass "插件: Pinyin 拼音"      || fail "插件缺失: Pinyin"
echo "$result" | grep -q "analysis-stconvert" && pass "插件: STConvert 简繁"  || fail "插件缺失: STConvert"

# IK 分词测试
result=$(curl -sf -X POST "http://localhost:9200/_analyze" \
  -H "Content-Type: application/json" \
  -d '{"analyzer": "ik_smart", "text": "中华人民共和国"}' 2>/dev/null \
  | python3 -c "import sys,json;tokens=json.load(sys.stdin)['tokens'];print(','.join(t['token'] for t in tokens))" 2>/dev/null)
[ -n "$result" ] && pass "IK 分词: $result" || fail "IK 分词失败"

# Pinyin 测试
result=$(curl -sf -X POST "http://localhost:9200/_analyze" \
  -H "Content-Type: application/json" \
  -d '{"analyzer": "pinyin", "text": "中国"}' 2>/dev/null \
  | python3 -c "import sys,json;tokens=json.load(sys.stdin)['tokens'];print(','.join(t['token'] for t in tokens))" 2>/dev/null)
[ -n "$result" ] && pass "Pinyin: $result" || fail "Pinyin 分词失败"

# ----------------------------------------------------------
# SeaweedFS (S3)
# ----------------------------------------------------------
info "SeaweedFS (S3)"
result=$(aws --endpoint-url http://localhost:8333 s3 ls 2>/dev/null)
echo "$result" | grep -q "zyn" && pass "Bucket: zyn 已存在" || fail "Bucket zyn 不存在"

echo "zyn-s3-test" > /tmp/zyn-s3-test.txt
aws --endpoint-url http://localhost:8333 s3 cp /tmp/zyn-s3-test.txt s3://zyn/test/zyn-s3-test.txt > /dev/null 2>&1
pass "上传文件: s3://zyn/test/zyn-s3-test.txt"

result=$(aws --endpoint-url http://localhost:8333 s3 cp s3://zyn/test/zyn-s3-test.txt - 2>/dev/null)
[ "$result" = "zyn-s3-test" ] && pass "下载文件: $result" || fail "下载文件失败"

aws --endpoint-url http://localhost:8333 s3 rm s3://zyn/test/zyn-s3-test.txt > /dev/null 2>&1
rm -f /tmp/zyn-s3-test.txt

# ----------------------------------------------------------
# INFINI Console
# ----------------------------------------------------------
info "INFINI Console"
code=$(curl -sf -o /dev/null -w "%{http_code}" http://localhost:9000 2>/dev/null)
[ "$code" = "200" ] && pass "HTTP 访问: $code" || fail "HTTP 访问失败: $code"

# ----------------------------------------------------------
# 汇总
# ----------------------------------------------------------
echo ""
echo "=============================="
echo -e "  ${GREEN}通过: ${PASS}${NC}  ${RED}失败: ${FAIL}${NC}"
echo "=============================="

[ $FAIL -eq 0 ] && echo -e "  ${GREEN}所有组件正常${NC}" || echo -e "  ${RED}存在异常，请检查${NC}"
exit $FAIL
