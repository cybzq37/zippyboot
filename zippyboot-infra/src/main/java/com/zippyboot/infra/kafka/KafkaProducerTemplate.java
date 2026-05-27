package com.zippyboot.infra.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.PartitionInfo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@ConditionalOnBean(KafkaOperations.class)
@ConditionalOnSingleCandidate(KafkaOperations.class)
public class KafkaProducerTemplate {

    private final KafkaOperations<Object, Object> kafkaOperations;

    public CompletableFuture<SendResult<Object, Object>> sendDefault(Object value) {
        return kafkaOperations.sendDefault(value);
    }

    public CompletableFuture<SendResult<Object, Object>> sendDefault(Object key, Object value) {
        return kafkaOperations.sendDefault(key, value);
    }

    public CompletableFuture<SendResult<Object, Object>> send(String topic, Object value) {
        return kafkaOperations.send(topic, value);
    }

    public CompletableFuture<SendResult<Object, Object>> send(String topic, Object key, Object value) {
        return kafkaOperations.send(topic, key, value);
    }

    public CompletableFuture<SendResult<Object, Object>> send(String topic, Integer partition, Object key, Object value) {
        return kafkaOperations.send(topic, partition, key, value);
    }

    public CompletableFuture<SendResult<Object, Object>> send(String topic, Integer partition, Long timestamp, Object key, Object value) {
        return kafkaOperations.send(topic, partition, timestamp, key, value);
    }

    public CompletableFuture<SendResult<Object, Object>> send(ProducerRecord<Object, Object> record) {
        return kafkaOperations.send(record);
    }

    public CompletableFuture<SendResult<Object, Object>> send(Message<?> message) {
        return kafkaOperations.send(message);
    }

    public List<PartitionInfo> partitionsFor(String topic) {
        return kafkaOperations.partitionsFor(topic);
    }

    public Map<MetricName, ? extends Metric> metrics() {
        return kafkaOperations.metrics();
    }

    public <T> T execute(KafkaOperations.ProducerCallback<Object, Object, T> callback) {
        return kafkaOperations.execute(callback);
    }

    public <T> T executeInTransaction(KafkaOperations.OperationsCallback<Object, Object, T> callback) {
        return kafkaOperations.executeInTransaction(callback);
    }

    public boolean isTransactional() {
        return kafkaOperations.isTransactional();
    }

    public boolean inTransaction() {
        return kafkaOperations.inTransaction();
    }

    public void flush() {
        kafkaOperations.flush();
    }
}
