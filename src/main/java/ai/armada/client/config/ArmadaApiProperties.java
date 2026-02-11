package ai.armada.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "armada.api")
@Data
public class ArmadaApiProperties {
    
    private String baseUrl;
    private Endpoints endpoints;
    private Credentials credentials;
    private Token tokenConfig;
    
    @Data
    public static class Endpoints {
        private Auth auth;
        private Organizations organizations;
        
        @Data
        public static class Auth {
            private String token;
        }
        
        @Data
        public static class Organizations {
            private String list;
            private String dataPools;
            private String dataPoolById;
            private String dataPoolUsage;
            private String dataPoolSettings;
            private String serviceLines;
            private String serviceLineById;
            private String serviceLineUsage;
            private String serviceLineSettings;
            private String allServiceLinesUsage;
            private String allServiceLinesSettings;
        }
    }
    
    @Data
    public static class Credentials {
        private String apiKeyId;
        private String apiKey;
    }
    
    @Data
    public static class Token {
        private long refreshRateMs;
        private int expiryThresholdPercent;
    }
}