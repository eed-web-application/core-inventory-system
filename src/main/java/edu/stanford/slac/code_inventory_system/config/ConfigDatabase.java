package edu.stanford.slac.code_inventory_system.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "edu.stanford.slac.code_inventory_system.repository")
public class ConfigDatabase {

}
