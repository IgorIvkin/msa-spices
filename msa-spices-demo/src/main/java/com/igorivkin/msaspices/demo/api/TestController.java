package com.igorivkin.msaspices.demo.api;

import com.igorivkin.msaspices.demo.model.HelloResponse;
import com.igorivkin.msaspices.lib.log.LogMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @LogMethod
    @GetMapping("/hello/{user}")
    public HelloResponse helloUser(@PathVariable String user) {
        return HelloResponse.builder()
                .name(user)
                .build();
    }
}
