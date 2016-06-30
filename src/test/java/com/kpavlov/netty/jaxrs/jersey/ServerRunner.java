package com.kpavlov.netty.jaxrs.jersey;

import com.kpavlov.netty.jaxrs.jersey.JaxrsNettyServer;
import com.kpavlov.netty.jaxrs.jersey.TestJerseyConfig;

public class ServerRunner {

    public static void main(String... args) {
        final JaxrsNettyServer server = new JaxrsNettyServer("localhost", 8080, new TestJerseyConfig());
        server.start();
    }
}
