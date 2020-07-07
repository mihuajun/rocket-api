package com.github.alenfive.rocketapi;

import com.github.alenfive.rocketapi.config.QLRequestMappingFactory;
import com.github.alenfive.rocketapi.extend.DefaultApiInfoInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RocketAPIApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(RocketAPIApplication.class, args);
    }

    @Autowired
    private QLRequestMappingFactory mappingFactory;

    @Override
    public void run(String... args) throws Exception {
        mappingFactory.addInterceptor(new DefaultApiInfoInterceptor(mappingFactory));
    }
}
