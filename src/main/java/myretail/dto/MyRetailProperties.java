package myretail.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration to retrive the properties from application.properties
 *
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
public class MyRetailProperties {

    private String redskyClientEndpoint;

    public String getRedskyClientEndpoint() {
        return redskyClientEndpoint;
    }

    public void setRedskyClientEndpoint(String redskyClientEndpoint) {
        this.redskyClientEndpoint = redskyClientEndpoint;
    }
}
