package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    // Base URI includes the /api/v1 prefix so all @Path annotations resolve correctly
    public static final String BASE_URI = "http://0.0.0.0:8080/api/v1/";

    public static void main(String[] args) throws IOException {
        final ResourceConfig config = new SmartCampusApplication();
        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);

        LOGGER.info("====================================================");
        LOGGER.info("  Smart Campus API started successfully!");
        LOGGER.info("  API Root : http://localhost:8080/api/v1/");
        LOGGER.info("  Rooms    : http://localhost:8080/api/v1/rooms");
        LOGGER.info("  Sensors  : http://localhost:8080/api/v1/sensors");
        LOGGER.info("  Press ENTER to stop the server...");
        LOGGER.info("====================================================");

        try {
            System.in.read();
        } finally {
            server.shutdownNow();
            LOGGER.info("Server stopped.");
        }
    }
}
