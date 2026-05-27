package com.zippyboot.app.kafka;

import com.zippyboot.infra.kafka.KafkaConsumerSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaDemoConsumer {

    @SuppressWarnings("java:S1075")
    private static final String DEMO_KAFKA_TOPIC = "zippyboot.demo.topic";

    private final ObjectProvider<KafkaConsumerSupport> kafkaConsumerSupportProvider;

    @KafkaListener(topics = DEMO_KAFKA_TOPIC)
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        KafkaConsumerSupport kafkaConsumerSupport = kafkaConsumerSupportProvider.getIfAvailable(KafkaConsumerSupport::new);
        if ("fail".equalsIgnoreCase(record.value())) {
            throw new IllegalStateException("Kafka demo consumer received a fail marker.");
        }

        log.info(
                "Kafka demo consume topic={}, partition={}, offset={}, key={}, traceId={}, value={}",
                kafkaConsumerSupport.topic(record).orElse("-"),
                kafkaConsumerSupport.partition(record).map(String::valueOf).orElse("-"),
                kafkaConsumerSupport.offset(record).map(String::valueOf).orElse("-"),
                kafkaConsumerSupport.keyAsString(record).orElse("-"),
                kafkaConsumerSupport.header(record, "traceId").orElse("-"),
                record.value()
        );

        kafkaConsumerSupport.acknowledge(acknowledgment);
    }
}
