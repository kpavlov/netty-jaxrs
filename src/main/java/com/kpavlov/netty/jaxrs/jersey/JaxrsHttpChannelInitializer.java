package com.kpavlov.netty.jaxrs.jersey;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import java.util.function.Consumer;
import javax.ws.rs.core.Application;

public class JaxrsHttpChannelInitializer extends ChannelInitializer<SocketChannel> {

    public static final String JERSEY_HANDLER = JerseyHttpHandler.class.getName();

    private final Application application;
    private final Consumer<ChannelPipeline> pipelineConfigurer;

    public JaxrsHttpChannelInitializer(Application resourceConfig, Consumer<ChannelPipeline> pipelineConfigurer) {
        this.application = resourceConfig;
        this.pipelineConfigurer = pipelineConfigurer;
    }

    public JaxrsHttpChannelInitializer(Application resourceConfig) {
        this(resourceConfig, null);
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast("codec", new HttpServerCodec());
        pipeline.addLast("httpObjectAggregator", new HttpObjectAggregator(1048576));
        pipeline.addLast(JERSEY_HANDLER, new JerseyHttpHandler(application, false));

        if (pipelineConfigurer != null) {
            pipelineConfigurer.accept(pipeline);
        }
    }

    protected void configure(ChannelPipeline pipeline) {

    }
}
