package com.kpavlov.netty.jaxrs.jersey;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import javax.ws.rs.core.Application;

public class JaxrsHttpChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final Application application;

    public JaxrsHttpChannelInitializer(Application resourceConfig) {
        this.application = resourceConfig;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();

        p.addLast("codec", new HttpServerCodec());
//        p.addLast("encoder", new HttpResponseEncoder());
//        p.addLast("decoder", new HttpRequestDecoder());
        p.addLast("aggregator", new HttpObjectAggregator(1048576));
        p.addLast(new JerseyHttpHandler(application, false));
    }
}
