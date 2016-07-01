package com.kpavlov.netty.jaxrs.jersey;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import javax.ws.rs.core.Application;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class JaxrsNettyServer {

    private final Logger logger = getLogger(JaxrsNettyServer.class);

    private final String host;
    private final int port;
    private volatile Channel serverChannel;
    private final ServerBootstrap serverBootstrap;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    public JaxrsNettyServer(String host, int port, Application application) {
        this.host = host;
        this.port = port;
        // Configure the server.
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);

        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new JaxrsHttpChannelInitializer(application));
    }

    public void start() {
        try {
            serverChannel = serverBootstrap.bind(host, port).sync().channel();
            logger.info("Server started. Open your web browser and navigate to http://{}:{}/", host, port);
        } catch (InterruptedException e) {
            close();
        }
    }

    public void stop() {
        try {
            serverChannel.disconnect();
            serverChannel.closeFuture().sync();
            logger.info("Server stopped");
        } catch (InterruptedException e) {
            logger.error("Error while stopping server", e);
        } finally {
            close();
        }
    }

    public void close() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
