package com.aioa.attendance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * AIOA Attendance Application
 */
@SpringBootApplication
@EnableScheduling
public class AioaAttendanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AioaAttendanceApplication.class, args);
    }
}