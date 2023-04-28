package br.com.ada.jogosapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class JogosApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(JogosApiApplication.class, args);
        System.out.println("Ola, Mundo!");
    }

}
