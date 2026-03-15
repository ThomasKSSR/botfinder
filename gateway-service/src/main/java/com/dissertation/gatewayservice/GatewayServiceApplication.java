package com.dissertation.gatewayservice;

import com.dissertation.messaging.config.RabbitCommonConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(RabbitCommonConfig.class)
@SpringBootApplication(scanBasePackages = "com.dissertation")
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }

}
