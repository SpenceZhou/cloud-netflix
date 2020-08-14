package me.spencez.cloud.netflix.server.consumer;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author spence
 */
@FeignClient(value = "service-producer", fallback = FailConsumer.class)
public interface Consumer {

    @RequestMapping(value = "/produce",method = RequestMethod.GET)
    String consume(@RequestParam(value = "name") String name);

}
