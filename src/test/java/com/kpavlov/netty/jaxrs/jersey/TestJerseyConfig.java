package com.kpavlov.netty.jaxrs.jersey;

import com.kpavlov.netty.jaxrs.jersey.endpoints.EchoEndpoint;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

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
