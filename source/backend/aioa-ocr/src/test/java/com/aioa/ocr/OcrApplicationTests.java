package com.aioa.ocr;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * OCR Module Application Test
 */
@SpringBootTest(classes = OcrApplicationTestConfig.class)
@ActiveProfiles("test")
class OcrApplicationTests {

    @Test
    void contextLoads() {
        // Verify that the application context loads successfully
    }
}
