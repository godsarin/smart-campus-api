package com.smartcampus;

import com.smartcampus.exception.*;
import com.smartcampus.filter.LoggingFilter;
import com.smartcampus.resource.DiscoveryResource;
import com.smartcampus.resource.RoomResource;
import com.smartcampus.resource.SensorResource;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api/v1")
public class SmartCampusApplication extends ResourceConfig {

    public SmartCampusApplication() {
        // Register resource classes
        register(DiscoveryResource.class);
        register(RoomResource.class);
        register(SensorResource.class);

        // Register exception mappers
        register(RoomNotEmptyExceptionMapper.class);
        register(LinkedResourceNotFoundExceptionMapper.class);
        register(SensorUnavailableExceptionMapper.class);
        register(GlobalExceptionMapper.class);

        // Register filters
        register(LoggingFilter.class);

        // Register Jackson JSON feature + custom ObjectMapper configuration
        register(JacksonFeature.class);
        register(JacksonConfig.class);
    }
}
