package me.spencez.cloud.netflix.server.consumer;

import org.springframework.stereotype.Component;

/**
 * @author spence
 */
@Component
public class FailConsumer implements Consumer {
    @Override
    public String consume(String name) {
        return "sorry, " + name;
    }
}
