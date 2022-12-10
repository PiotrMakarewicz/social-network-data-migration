package pl.edu.agh.socialnetworkdatamigration.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.loader.SQLMappingLoader;

@Configuration
public class ExternalBeansConfiguration {
    @Bean
    public SQLMappingLoader provideSQLMappingLoader() {
        return new SQLMappingLoader();
    }
}
