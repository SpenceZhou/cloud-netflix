package me.spencez.cloud.netflix.server.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author spence
 */
@RestController
public class ConsumerController {

    @Autowired
    private Consumer consumer;

    @GetMapping(value = "/consume")
    public String consume(@RequestParam String name) {
        String content = consumer.consume(name);
        return "echo { " + content + " }";
    }
}
