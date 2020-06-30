package com.github.alenfive.dataway2;

import com.github.alenfive.dataway2.config.QLRequestMappingFactory;
import com.github.alenfive.dataway2.extend.DefaultApiInfoInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Dataway2Application implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(Dataway2Application.class, args);
    }

    @Autowired
    private QLRequestMappingFactory mappingFactory;

    @Override
    public void run(String... args) throws Exception {
        mappingFactory.addInterceptor(new DefaultApiInfoInterceptor(mappingFactory));
    }
}
