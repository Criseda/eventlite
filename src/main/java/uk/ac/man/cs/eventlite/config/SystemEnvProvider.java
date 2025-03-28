package uk.ac.man.cs.eventlite.config;

import org.springframework.stereotype.Component;

@Component
public class SystemEnvProvider implements EnvProvider {
    @Override
    public String getEnv(String name) {
        return System.getenv(name);
    }
}