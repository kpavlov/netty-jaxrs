package com.kpavlov.netty.jaxrs.jersey;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.AsciiString;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Map;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;
import org.jetbrains.annotations.NotNull;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.rtsp.RtspResponseStatuses.CONTINUE;

@ChannelHandler.Sharable
public class JerseyHttpHandler extends ChannelInboundHandlerAdapter implements Container {

    private static final AsciiString CONTENT_TYPE = new AsciiString("Content-Type");
    private static final AsciiString CONTENT_LENGTH = new AsciiString("Content-Length");
    private static final AsciiString CONNECTION = new AsciiString("Connection");
    private static final AsciiString KEEP_ALIVE = new AsciiString("keep-alive");

    private final ApplicationHandler appHandler;

    private static final SecurityContext dummySecurityContext = new SecurityContext() {

        @Override
        public boolean isUserInRole(final String role) {
            return false;
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public Principal getUserPrincipal() {
            return null;
        }

        @Override
        public String getAuthenticationScheme() {
            return null;
        }
    };

    public JerseyHttpHandler(Application application) {
        appHandler = new ApplicationHandler(application);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;

            if (HttpUtil.is100ContinueExpected(req)) {
                ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
            }
            boolean keepAlive = HttpUtil.isKeepAlive(req);

            FullHttpResponse response = consumeRequest(req);

            response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

            if (!keepAlive) {
                ctx.write(response).addListener(ChannelFutureListener.CLOSE);
            } else {
                response.headers().set(CONNECTION, KEEP_ALIVE);
                ctx.write(response);
            }
        }
    }

    @NotNull
    private FullHttpResponse consumeRequest(HttpRequest req) {

        FullHttpResponse response;
        try {
            final ContainerRequest requestContext = createContainerRequest(req);

            final ByteBuf buffer = Unpooled.buffer();
            final ContainerResponse containerResponse = getApplicationHandler()
                    .apply(requestContext, new ByteBufOutputStream(buffer))
                    .get();

            response = createNettyResponse(containerResponse, buffer);

        } catch (Exception e) {
            response = new DefaultFullHttpResponse(req.protocolVersion(), INTERNAL_SERVER_ERROR);
            response.headers().set(CONTENT_TYPE, "text/plain");
        }

        return response;
    }

    @NotNull
    private ContainerRequest createContainerRequest(HttpRequest req) throws URISyntaxException {
        final HttpHeaders headers = req.headers();
        final URI baseUri = new URI("http://" + headers.get(HttpHeaderNames.HOST) + "/");
        final URI requestUri = UriBuilder.fromUri(baseUri).path(req.uri()).build();
        final String httpMethod = req.method().name();

        final ContainerRequest requestContext = new ContainerRequest(
                baseUri,
                requestUri,
                httpMethod,
                dummySecurityContext,
                new MapPropertiesDelegate());

        if (req instanceof FullHttpRequest) {
            consumeEntity((FullHttpRequest) req, requestContext);
        }

        processHeaders(headers, requestContext);
        return requestContext;
    }

    private static void processHeaders(HttpHeaders headers, ContainerRequest requestContext) {
        for (final Map.Entry<String, String> header : headers) {
            String value = header.getValue();
            final String headerName = header.getKey();
            if (HttpHeaderNames.CONTENT_TYPE.contentEqualsIgnoreCase(headerName) && value.indexOf(';') > 0) {
                value = value.substring(0, value.indexOf(';'));
            }
            requestContext.headers(headerName, value);
        }
    }

    private void consumeEntity(FullHttpRequest req, ContainerRequest requestContext) {
        final ByteBuf content = req.content();
        if (content != null) {
            requestContext.setEntityStream(new ByteBufInputStream(content));
        }
    }

    private FullHttpResponse createNettyResponse(ContainerResponse containerResponse, ByteBuf buffer) {
        final HttpResponseStatus status = HttpResponseStatus.valueOf(containerResponse.getStatus());

        final DefaultFullHttpResponse result = new DefaultFullHttpResponse(HTTP_1_1, status, buffer);

        final MediaType mediaType = containerResponse.getMediaType();

        result.headers().set(CONTENT_TYPE, mediaType.toString());
        result.headers().setInt(CONTENT_LENGTH, buffer.readableBytes());

        return result;
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public ApplicationHandler getApplicationHandler() {
        return appHandler;
    }

    @Override
    public ResourceConfig getConfiguration() {
        return appHandler.getConfiguration();
    }

    @Override
    public void reload() {
        reload(getConfiguration());
    }

    @Override
    public void reload(final ResourceConfig configuration) {
        throw new UnsupportedOperationException();
    }

}
