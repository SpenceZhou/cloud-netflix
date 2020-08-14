package me.spencez.cloud.netflix.server.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author spence
 */
@SpringBootApplication
@EnableEurekaClient
@RestController
public class ServerProducerApplication {

    @Value("${server.port}")
    private String port;

    @RequestMapping("/produce")
    public String produce(@RequestParam(value = "name", defaultValue = "anonymous") String name) {
        return "Hello " + name + " ,this is a message from port:" + port;
    }

    public static void main(String[] args) {
        SpringApplication.run(ServerProducerApplication.class, args);
    }
}
