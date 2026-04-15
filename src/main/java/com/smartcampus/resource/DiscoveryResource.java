package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response discover(@Context UriInfo uriInfo) {
        // Build base cleanly from the request URI (strips trailing slash safely)
        String requestUri = uriInfo.getRequestUri().toString();
        // Normalise: ensure no double slash
        String base = requestUri.endsWith("/") ? requestUri.substring(0, requestUri.length() - 1) : requestUri;

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("name", "Smart Campus Sensor & Room Management API");
        response.put("version", "1.0.0");
        response.put("description", "RESTful API for managing campus rooms and IoT sensors.");

        Map<String, String> contact = new LinkedHashMap<>();
        contact.put("name", "Campus Facilities Management");
        contact.put("email", "facilities@smartcampus.ac.uk");
        contact.put("department", "School of Computer Science and Engineering");
        response.put("contact", contact);

        // Primary resource collection links (HATEOAS navigation map)
        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("rooms",   base + "/rooms");
        resources.put("sensors", base + "/sensors");
        response.put("resources", resources);

        Map<String, String> links = new LinkedHashMap<>();
        links.put("self",    base);
        links.put("rooms",   base + "/rooms");
        links.put("sensors", base + "/sensors");
        response.put("_links", links);

        return Response.ok(response).build();
    }
}
