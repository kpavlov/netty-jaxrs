package com.kpavlov.netty.jaxrs.jersey;

import io.netty.channel.ChannelHandlerContext;
import org.glassfish.jersey.server.ContainerRequest;

@SuppressWarnings("WeakerAccess")
public class ContainerRequestHelper {

    static final String CHANNEL_HANDLER_CONTEXT_PROPERTY = ChannelHandlerContext.class.getName();

    private ContainerRequestHelper() {
        // to prevent instantiation
    }

    public static ChannelHandlerContext getChannelHandlerContext(ContainerRequest containerRequest) {
        return (ChannelHandlerContext) containerRequest.getProperty(CHANNEL_HANDLER_CONTEXT_PROPERTY);
    }
}
