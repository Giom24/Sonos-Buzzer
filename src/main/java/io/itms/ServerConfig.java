
package io.itms;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

public class ServerConfig extends ResourceConfig {

    public static final URI BASE_URI = URI.create("http://0.0.0.0:8080");
    public static final Path CLIP_DIRECTORY = Paths.get("clips");
    public static final String PACKAGE_HANDLER = "io.itms.handler";

    public ServerConfig() {
        super();
        this.packages(PACKAGE_HANDLER)
                .register(new LoggingFeature(Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME), Level.INFO,
                        LoggingFeature.Verbosity.PAYLOAD_TEXT, 10000))
                .property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true)
                .property(ServerProperties.WADL_FEATURE_DISABLE, true)
                .property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, true);
    }
}