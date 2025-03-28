package uk.ac.man.cs.eventlite.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SystemEnvProviderTest {
    
    private SystemEnvProvider envProvider;
    
    @BeforeEach
    public void setUp() {
        envProvider = new SystemEnvProvider();
    }
    
    @Test
    public void testGetExistingEnvVariable() {
        // PATH or HOME should exist on most systems
        String path = envProvider.getEnv("PATH");
        // We don't know the exact value, but it shouldn't be null on most systems
        assertNotNull(path);
        
        // Alternatively, test with HOME on Unix/Mac or USERPROFILE on Windows
        String home = envProvider.getEnv(System.getProperty("os.name").toLowerCase().contains("win") ? 
                "USERPROFILE" : "HOME");
        assertNotNull(home);
    }
    
    @Test
    public void testGetNonExistentEnvVariable() {
        // This name is unlikely to exist as an environment variable
        String nonExistent = envProvider.getEnv("THIS_ENV_VAR_SHOULD_NOT_EXIST_12345");
        assertNull(nonExistent);
    }
    
    @Test
    public void testGetEnvWithNullKey() {
        // System.getenv(null) throws NullPointerException
        assertThrows(NullPointerException.class, () -> {
            envProvider.getEnv(null);
        });
    }
    
    @Test
    public void testGetEnvWithEmptyKey() {
        // Empty string is a valid key, but should return null as it likely doesn't exist
        String result = envProvider.getEnv("");
        assertNull(result);
    }
}