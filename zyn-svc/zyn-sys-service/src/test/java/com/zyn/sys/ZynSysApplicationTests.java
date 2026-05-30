package com.zyn.sys;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
})
class ZynSysApplicationTests {

    @Test
    void contextLoads() {
    }
}
