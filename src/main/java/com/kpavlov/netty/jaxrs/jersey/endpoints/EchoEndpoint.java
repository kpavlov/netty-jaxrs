package com.kpavlov.netty.jaxrs.jersey.endpoints;

import jersey.repackaged.com.google.common.base.MoreObjects;
import org.slf4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;

import static org.slf4j.LoggerFactory.getLogger;

@Path("/")
public class EchoEndpoint {

    private static final Logger LOGGER = getLogger(EchoEndpoint.class);

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response echo(@Context UriInfo uriInfo, @Context Request request, @Context HttpHeaders headers) {

        String requestInfo = createRequestInfo(uriInfo, request, headers);

        LOGGER.debug(requestInfo);

        return Response.ok()
                .entity(requestInfo)
                .build();
    }

    private String createRequestInfo(UriInfo uriInfo, Request request, HttpHeaders headers) {

        return MoreObjects.toStringHelper("request")
                .add("UriInfo", uriInfo)
                .add("Request", request)
                .add("Headers", headers.getRequestHeaders())
                .toString();
    }
}
