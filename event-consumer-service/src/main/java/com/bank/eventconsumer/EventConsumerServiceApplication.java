// Application principale du event-consumer-service (examen ADRIA/ENSET, P1)
package com.bank.eventconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EventConsumerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventConsumerServiceApplication.class, args);
    }
}
