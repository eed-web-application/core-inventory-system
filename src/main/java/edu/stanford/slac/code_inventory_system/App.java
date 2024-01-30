package edu.stanford.slac.code_inventory_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"edu.stanford.slac"})
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
