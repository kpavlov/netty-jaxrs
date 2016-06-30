package com.kpavlov.netty.jaxrs.jersey;

import com.kpavlov.netty.jaxrs.jersey.util.ServerResource;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

public class ServerIT {
    private static final Logger LOGGER = getLogger(ServerIT.class);
    private static Client client;

    private String host = "localhost";
    private int port = 8080;
    private WebTarget target;
    @Rule
    public ServerResource httpServer = new ServerResource(host, port);

    @BeforeClass
    public static void beforeClass() throws Exception {
        Thread.sleep(1000);
        client = ClientBuilder.newClient();
    }

    @Before
    public void beforeMethod() {
        target = client.target("http://" + host + ":" + port);
    }

    @After
    public void afterMethod() {
        client.close();
    }

    @Test
    public void echoGetTextPlain() {
        final Response response = target.path("/").request().header("foo", "bar").get();

        LOGGER.info("Response: {}", response);

        assertThat(response.getStatus(), is(200));
        assertThat(response.hasEntity(), is(true));
        final String responseAsString = response.readEntity(String.class);
        assertThat(responseAsString, CoreMatchers.containsString("bar"));
    }
}
