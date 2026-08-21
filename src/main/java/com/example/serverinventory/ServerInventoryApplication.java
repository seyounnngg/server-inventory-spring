package com.example.serverinventory;

import com.example.serverinventory.service.storage.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class ServerInventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerInventoryApplication.class, args);
    }
}
