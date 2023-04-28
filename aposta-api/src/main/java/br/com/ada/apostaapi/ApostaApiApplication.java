package br.com.ada.apostaapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient

public class ApostaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApostaApiApplication.class, args);
        System.out.println("Ola mundo!");
    }

}
