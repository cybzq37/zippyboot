package com.zippyboot.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchClientAutoConfiguration"
})
class ZippyBootApplicationTests {

    @Test
    void contextLoads() {
    }
}
