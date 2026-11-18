package com.teumobile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class TeuMobileApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeuMobileApplication.class, args);
    }
}
