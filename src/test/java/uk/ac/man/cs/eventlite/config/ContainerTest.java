package uk.ac.man.cs.eventlite.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContainerTest {

    @Mock
    private ConfigurableServletWebServerFactory factory;
    
    @Mock
    private EnvProvider envProvider;

    private Container container;
    
    @BeforeEach
    public void setUp() {
        container = new Container(envProvider);
    }

    @Test
    public void testDefaultPort() {
        // Test when EVENTLITE_PORT doesn't exist
        when(envProvider.getEnv("EVENTLITE_PORT")).thenReturn(null);
        
        container.customize(factory);
        
        verify(factory).setPort(8080);
    }
    
    @Test
    public void testCustomPortFromEnv() {
        // Test when EVENTLITE_PORT is set to a valid integer
        when(envProvider.getEnv("EVENTLITE_PORT")).thenReturn("9090");
        
        container.customize(factory);
        
        verify(factory).setPort(9090);
    }
    
    @Test
    public void testInvalidPortFromEnv() {
        // Test when EVENTLITE_PORT is set to an invalid value
        when(envProvider.getEnv("EVENTLITE_PORT")).thenReturn("not-a-number");
        
        container.customize(factory);
        
        verify(factory).setPort(8080);
    }
    
    @Test
    public void testEmptyPortFromEnv() {
        // Test when EVENTLITE_PORT is an empty string
        when(envProvider.getEnv("EVENTLITE_PORT")).thenReturn("");
        
        container.customize(factory);
        
        verify(factory).setPort(8080);
    }
}