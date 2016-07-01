# JAX-RS Handler for Netty 4.1 

[![Build Status](https://travis-ci.org/kpavlov/netty-jaxrs.png?branch=master)](https://travis-ci.org/kpavlov/netty-jaxrs)

JAX-RS server handler for [Netty 4.1][netty] using [Jersey][jersey].
 
## Usage
 
Create resource endpoint:

```java
@Path("/")
public class EchoResource {

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
```java
public class JerseyConfig extends ResourceConfig {
   
   public TestJerseyConfig() {
           setApplicationName("test");
           register(JacksonFeature.class);
           register(LoggingFeature.class);
           register(MultiPartFeature.class);
           // endpoints
           register(EchoResource.class);
   }
}
```

Start netty server:
```java
JaxrsNettyServer server = new JaxrsNettyServer(
        "localhost", 8080, 
        new JerseyConfig()
    );
server.start();
```

See integration tests for more examples.

[netty]: http://netty.io
[jersey]: https://jersey.java.net
