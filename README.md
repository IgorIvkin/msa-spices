# MSA Spices

MSA Spices (of MicroService Architecture Spices) is a set of useful libraries
that can help to automate some typical tasks when developing a microservice on 
**Spring Boot**.

## Logging requests and responses

A common task is to log the content of requests and responses. Doesn't matter
what is it exactly: REST controllers, database interaction methods, SOAP handlers etc.
The case is similar all the time: some parameters are incoming and some result is 
outgoing.

Library provides a simple way to implement logging: annotation `@LogMethod`. For **Spring WebFlux**
use its sister ship annotation `@LogReactiveMethod`. 

Reactive implementation has some limitations currently: it will not log `Flux` request parts but will do that for responses. 
Currently, I recognize WebFlux implementation to be in experimental status, it is not tested well but seems to be working.

### Configuration

You put it over some method, and it will apply a logging of parameters of this
method and response of this method. By default, all the objects using there will be
converted with `toString()`.

```java
@LogMethod
@GetMapping("/hello/{user}")
public HelloResponse helloUser(@PathVariable String user) {
    return HelloResponse.builder()
            .name(user)
            .build();
}
```


The annotation has following configurable parameters:

| Name        | Type     | Description                                                                                          |
|-------------|----------|------------------------------------------------------------------------------------------------------|
| parameters  | String[] | If empty all the params will be logged.<br/>Contains list of params that are suitable to log.        |
| logResponse | boolean  | By default, true. If false then response will not be logged. Instead it will be written `<ignored>`. |

It can be useful to skip some params that are not suitable for logging like `HttpRequest` or binary data. The same it is
possible to say about response logging. Sometimes your answers are not applicable to logging because can form kind of 
binary data, stream or similar data.

The logging use log level `DEBUG`. So it will not visible by default. If you want to activate it you can change the log level
for a package of library. It is also possible to change log level dynamically while application is running with **Spring Boot Actuator**
helper methods.

```yaml
logging:
  level:
    com:
      igorivkin:
        msaspices:
          lib: DEBUG
```

A typical request and response logging will be looking like:

    >>> Request 515060d3-7c4d-4749-a7de-52162615fa74, method helloUser
    user="123"
    <<< Response 515060d3-7c4d-4749-a7de-52162615fa74, method: helloUser
    {"name":"123"}

Logger based on **Slf4j** so it can work with log4j2 or Logback by your choice. Just put your preferred
dependency, and it should start to work with appropriate logging backend.