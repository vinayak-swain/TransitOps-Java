package com.transitops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * TransitOps Backend — Spring Boot Application Entry Point
 * Equivalent to Node.js src/index.js
 */
@SpringBootApplication
public class TransitOpsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransitOpsApplication.class, args);
    }
}
