package uk.ac.man.cs.eventlite.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("default")
public class Container implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    private final static Logger log = LoggerFactory.getLogger(Container.class);
    private final static String PORT_ENV = "EVENTLITE_PORT";
    private final static int DEFAULT_PORT = 8080;
    
    private final EnvProvider envProvider;
    
    @Autowired
    public Container(EnvProvider envProvider) {
        this.envProvider = envProvider;
    }

    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
        int port = DEFAULT_PORT;

        try {
            String portValue = envProvider.getEnv(PORT_ENV);
            if (portValue != null && !portValue.isEmpty()) {
                port = Integer.parseInt(portValue);
                log.info("Using port number from " + PORT_ENV + ": " + port);
            } else {
                log.info("Using default port number: " + DEFAULT_PORT);
            }
        } catch (NumberFormatException nfe) {
            log.info("Using default port number: " + DEFAULT_PORT);
        }

        factory.setPort(port);
    }
}