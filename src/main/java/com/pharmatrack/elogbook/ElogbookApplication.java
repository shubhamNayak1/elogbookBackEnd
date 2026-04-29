package com.pharmatrack.elogbook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = { UserDetailsServiceAutoConfiguration.class })
public class ElogbookApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElogbookApplication.class, args);
    }
}
