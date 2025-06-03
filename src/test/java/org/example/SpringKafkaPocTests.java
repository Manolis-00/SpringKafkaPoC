package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Basic Application context tests.
 * Verifies that the Spring Context loads successfully.
 */
@SpringBootTest
@ActiveProfiles("test")
public class SpringKafkaPocTests {

    @Test
    void contextLoads() {

    }
}
