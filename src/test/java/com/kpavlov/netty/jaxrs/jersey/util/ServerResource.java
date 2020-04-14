package com.kpavlov.netty.jaxrs.jersey.util;

import com.kpavlov.netty.jaxrs.jersey.JaxrsNettyServer;
import com.kpavlov.netty.jaxrs.jersey.TestJerseyConfig;
import org.junit.rules.ExternalResource;

public class ServerResource extends ExternalResource {

    private final JaxrsNettyServer myServer;

    public ServerResource(String host, int port) {
        myServer = new JaxrsNettyServer(
                host, port,
                new TestJerseyConfig());
    }

    @Override
    protected void before() {
        myServer.start();
    }

    @Override
    protected void after() {
        myServer.stop();
    }
}
