package com.kpavlov.netty.jaxrs.jersey;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.Application;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class JerseyHttpHandlerTest {

    JerseyHttpHandler handler;

    @Mock
    Application applicaiton;
    @Mock
    ChannelHandlerContext ctx;
    @Mock
    private ByteBuf byteBuf;

    @Before
    public void setUp() throws Exception {
        handler = new JerseyHttpHandler(applicaiton, false);
    }

    @Test
    public void shouldFlushOnReadComplete() {
        handler.channelReadComplete(ctx);

        verify(ctx).flush();
    }

    @Test
    public void shouldHandleFullHttpRequest() {
        final DefaultFullHttpRequest defaultFullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/test", Unpooled.buffer(),
                new DefaultHttpHeaders(), new DefaultHttpHeaders());

        handler.channelRead(ctx, defaultFullHttpRequest);
    }
}