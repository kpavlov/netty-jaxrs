package com.kpavlov.netty.jaxrs.jersey;

import io.netty.channel.ChannelHandlerContext;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.server.ContainerRequest;

@Path("/test")
public class TestResource {

    private volatile javax.ws.rs.core.HttpHeaders httpHeaders;

    private volatile ContainerRequest capturedRequest;
    private volatile ChannelHandlerContext capturedChannelHandlerContext;
    private volatile Response response;
    private final CountDownLatch latch = new CountDownLatch(1);

    @GET
    public Response get(@Context ContainerRequest containerRequest,
                        @Context javax.ws.rs.core.HttpHeaders headers) {
        this.capturedRequest = containerRequest;
        this.httpHeaders = headers;
        capturedChannelHandlerContext = ContainerRequestHelper.getChannelHandlerContext(containerRequest);
        latch.countDown();
        return response;
    }

    void await() throws InterruptedException {
        latch.await(100, TimeUnit.MILLISECONDS);
    }

    ContainerRequest getCapturedRequest() {
        return capturedRequest;
    }

    public HttpHeaders getHttpHeaders() {
        return httpHeaders;
    }


    public void setResponse(Response response) {
        this.response = response;
    }

    public ChannelHandlerContext getCapturedChannelHandlerContext() {
        return capturedChannelHandlerContext;
    }
}
