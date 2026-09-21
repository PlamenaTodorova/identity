package com.identity.api;

import com.identity.core.config.CoreConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(CoreConfig.class)
public class IdentityApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityApplication.class, args);
    }
}
