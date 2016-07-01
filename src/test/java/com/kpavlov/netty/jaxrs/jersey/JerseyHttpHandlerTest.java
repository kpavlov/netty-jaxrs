package com.kpavlov.netty.jaxrs.jersey;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class JerseyHttpHandlerTest {

    private JerseyHttpHandler handler;

    private TestResource testResource;

    @Mock
    ChannelHandlerContext ctx;
    @Mock
    ByteBuf byteBuf;
    @Mock
    ContainerResponse containerResponse;
    @Mock
    Response jaxrsResponse;
    @Captor
    ArgumentCaptor<ContainerRequest> containerRequestCaptor;
    @Captor
    ArgumentCaptor<OutputStream> outputStreamCaptor;
    @Captor
    ArgumentCaptor<FullHttpResponse> httpResponseCaptor;

    private String headerName;

    @Before
    public void setUp() throws Exception {

        final ResourceConfig resourceConfig = new ResourceConfig();
        testResource = new TestResource();
        resourceConfig.register(testResource);

        handler = new JerseyHttpHandler(resourceConfig, false);

        headerName = randomAlphanumeric(10);
    }

    @Test
    public void shouldFlushOnReadComplete() {
        handler.channelReadComplete(ctx);

        verify(ctx).flush();
    }

    @Test
    public void shouldHandleGetRequest() throws Exception {
        // given
        final DefaultFullHttpRequest defaultFullHttpRequest = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/test?query=a&offset=b#hash=c", Unpooled.buffer(),
                new DefaultHttpHeaders(), new DefaultHttpHeaders());
        defaultFullHttpRequest.headers().add(HttpHeaderNames.HOST, "localhost:8080");
        defaultFullHttpRequest.headers().add(headerName, randomAlphanumeric(4));
        defaultFullHttpRequest.headers().add(headerName, randomAlphanumeric(5));
        defaultFullHttpRequest.headers().add(headerName, randomAlphanumeric(6));
        final NewCookie newCookie = new NewCookie(randomAlphanumeric(4), randomAlphanumeric(5));
        testResource.setResponse(Response.ok()
                .cookie(newCookie)
                .build());

        // when
        handler.channelRead(ctx, defaultFullHttpRequest);

        // then
        verify(ctx).write(httpResponseCaptor.capture());

        final FullHttpResponse fullHttpResponse = httpResponseCaptor.getValue();
        assertThat(fullHttpResponse.status(), is(HttpResponseStatus.OK));
        assertThat(fullHttpResponse.headers().get("Set-Cookie"), equalTo(newCookie.toString()));

        testResource.await();

        final ContainerRequest containerRequest = testResource.getCapturedRequest();
        assertThat(containerRequest, notNullValue());
        final URI requestUri = containerRequest.getRequestUri();
        assertThat(requestUri.getPath(), equalTo("/test"));
        assertThat(requestUri.getQuery(), equalTo("query=a&offset=b"));
        assertThat(requestUri.getFragment(), equalTo("hash=c"));

        assertHeaders(testResource.getHttpHeaders(), defaultFullHttpRequest);
    }

    private void assertHeaders(HttpHeaders actualHeaders, DefaultFullHttpRequest expectedHeaders) {
        final List<String> resultHeaderValues = actualHeaders.getRequestHeader(headerName);
        final List<String> sourceHeaderValues = expectedHeaders.headers().getAll(headerName);
        for (int i = 0; i < resultHeaderValues.size(); i++) {
            assertThat(resultHeaderValues.get(i), equalTo(sourceHeaderValues.get(i)));
        }
    }

}