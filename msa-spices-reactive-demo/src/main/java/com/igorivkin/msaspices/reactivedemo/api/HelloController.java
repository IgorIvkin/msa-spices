package com.igorivkin.msaspices.reactivedemo.api;

import com.igorivkin.msaspices.lib.log.LogReactiveMethod;
import com.igorivkin.msaspices.reactivedemo.model.HelloRequest;
import com.igorivkin.msaspices.reactivedemo.model.HelloResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class HelloController {

    @LogReactiveMethod
    @PostMapping("/hello")
    public Mono<HelloResponse> hello(@RequestBody Mono<HelloRequest> request) {
        return request.map(r ->
                HelloResponse.builder()
                        .name(r.getUserName())
                        .build());
    }

    @LogReactiveMethod
    @PostMapping("/hello/{userId}")
    public Mono<HelloResponse> helloUser(@PathVariable String userId, @RequestBody Mono<HelloRequest> request) {
        return request.map(r ->
                HelloResponse.builder()
                        .userId(userId)
                        .name(r.getUserName())
                        .build());
    }

    @LogReactiveMethod
    @PostMapping("/hello-flux")
    public Flux<HelloResponse> helloFlux(@RequestBody Mono<HelloRequest> request) {
        return Flux.just(
                HelloResponse.builder()
                        .name("Name 1")
                        .build(),
                HelloResponse.builder()
                        .name("Name 2")
                        .build()
        );
    }
}
