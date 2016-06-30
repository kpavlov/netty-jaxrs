# JAX-RS Handler for Netty 4.1 

[![Build Status](https://travis-ci.org/kpavlov/netty-jaxrs.png?branch=master)](https://travis-ci.org/kpavlov/netty-jaxrs)

JAX-RS server handler for [Netty 4.1][netty] using [Jersey][jersey].
 
## Usage
 
Create resource endpoint:

```EchoEndpoint.java
@Path("/")
public class EchoEndpoint {

     @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response echo() {
        return Response.ok()
                .entity("Hello, World!")
                .build();
    }
}
```

Setup Jersey Application:
```TestJerseyConfig.java

public class TestJerseyConfig extends ResourceConfig {
   
   public TestJerseyConfig() {
           setApplicationName("test");
           register(JacksonFeature.class);
           register(LoggingFeature.class);
           register(MultiPartFeature.class);
           // endpoints
           register(EchoEndpoint.class);
   }
}
```

Start netty server:
```
JaxrsNettyServer server = new JaxrsNettyServer(
        "localhost", 8080, 
        new TestJerseyConfig()
    );
server.start();
```

See integration tests for more examples.

[netty]: http://netty.io
[jersey]: https://jersey.java.net
