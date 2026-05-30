package com.zyn.infra.kafka;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Component;

/**
 * Kafka 客户端封装。
 * <p>
 * 持有 {@link KafkaOperations} 引用，通过 {@link #getOperations()} 获取底层操作对象。
 */
@Component
@Getter
@RequiredArgsConstructor
@ConditionalOnBean(KafkaOperations.class)
@ConditionalOnSingleCandidate(KafkaOperations.class)
public class KafkaClient {

    private final KafkaOperations<Object, Object> operations;
}
